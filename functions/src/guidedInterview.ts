/**
 * Guided interview: one turn of an adaptive Q&A that ends in a strategy recommendation.
 *
 * The client sends the case seed, the Q&A history so far, and the running fact sheet.
 * The model returns EITHER the next question (2–4 options) OR a final recommendation,
 * plus an updated fact sheet. Output is constrained with a JSON response schema and
 * then validated here, so the app never has to parse free-form prose.
 *
 * The system prompt lives server-side on purpose: the app cannot weaken the rules,
 * and the prompt can be improved without shipping a new APK.
 */
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { GoogleGenerativeAI, SchemaType, type ResponseSchema } from "@google/generative-ai";
import { geminiApiKey } from "./secrets";

const MODEL_NAME = "gemini-3.5-flash-lite";

/** Hard cap on questions. At this many answers the server forces a recommendation. */
export const MAX_TURNS = 8;
const MIN_OPTIONS = 2;
const MAX_OPTIONS = 4;
const MAX_FIELD_CHARS = 2000;
const MAX_FACTS = 30;

// ---------- Wire types (mirror the Kotlin DTOs) ----------

export interface InterviewSeed {
  caseType: string;
  posture: string;
  summary: string;
  goal?: string;
}

export interface InterviewTurn {
  question: string;
  answer: string;
  wasCustom: boolean;
}

export interface CaseFact {
  label: string;
  value: string;
}

export interface InterviewTurnRequest {
  seed: InterviewSeed;
  history: InterviewTurn[];
  facts: CaseFact[];
  forceRecommendation?: boolean;
}

export interface QuestionPayload {
  text: string;
  why: string;
  options: string[];
}

export interface Approach {
  title: string;
  rationale: string;
  risks: string;
}

export interface RecommendationPayload {
  summary: string;
  approaches: Approach[];
  missingFacts: string[];
  verifyBeforeRelying: string[];
}

export interface InterviewTurnResponse {
  kind: "QUESTION" | "RECOMMENDATION";
  question: QuestionPayload | null;
  recommendation: RecommendationPayload | null;
  facts: CaseFact[];
  turnNumber: number;
  maxTurns: number;
  modelName: string;
}

// ---------- Prompt ----------

const SYSTEM_INSTRUCTION = `
You are a litigation-strategy interviewer inside a lawyer's mobile app. The user is a licensed
attorney working with SANITIZED or FICTIONAL facts. Your job is to ask the fewest, most
decision-relevant questions, then recommend an approach.

How to ask:
- Ask exactly ONE question per turn. Pick the unknown fact that would most change the strategy.
- Give ${MIN_OPTIONS}-${MAX_OPTIONS} answer options. Options must be short (under 12 words),
  mutually exclusive, and cover the realistic possibilities. When the attorney may not know yet,
  make the last option "Unknown / not yet determined".
- Never ask something already answered in the history or fact sheet.
- "why" is one sentence telling the attorney why this fact matters to strategy.
- The attorney may answer in their own words; treat custom answers as authoritative.

Fact sheet:
- Return the COMPLETE updated fact sheet every turn: short labels ("Entry date", "Prior removals")
  and concise values. Only record facts the attorney actually gave. Never invent facts.

When to recommend instead of asking:
- When you have enough to distinguish between the plausible strategies, or when told to finish.
- Recommendation: a 2-3 sentence summary, then 1-3 approaches ranked best-first, each with a
  rationale tied to the recorded facts and its main risk. List missing facts that would change
  the answer.
- Do NOT cite case names or statute sections from memory as settled law. Name the doctrine or
  standard instead, and list anything the attorney must verify in "verifyBeforeRelying".
- This is internal strategy brainstorming for a lawyer, not advice to a client or the public.
`.trim();

function buildTurnPrompt(req: InterviewTurnRequest, mustRecommend: boolean): string {
  const history = req.history.length === 0
    ? "(no questions asked yet)"
    : req.history
        .map((t, i) => `Q${i + 1}: ${t.question}\nA${i + 1}${t.wasCustom ? " (attorney's own words)" : ""}: ${t.answer}`)
        .join("\n\n");

  const facts = req.facts.length === 0
    ? "(empty)"
    : req.facts.map((f) => `- ${f.label}: ${f.value}`).join("\n");

  return `
CASE SEED
Case type: ${req.seed.caseType}
Procedural posture: ${req.seed.posture}
Summary: ${req.seed.summary}
Attorney's goal: ${req.seed.goal?.trim() || "(not stated)"}

FACT SHEET SO FAR
${facts}

INTERVIEW SO FAR (${req.history.length} of max ${MAX_TURNS} questions)
${history}

INSTRUCTION
${mustRecommend
    ? 'Stop asking. Return kind "RECOMMENDATION" now, using only the facts gathered.'
    : 'Return kind "QUESTION" with the next most decision-relevant question, or kind "RECOMMENDATION" if you already have enough.'}
`.trim();
}

// ---------- Response schema (Gemini structured output) ----------

const stringArray: ResponseSchema = { type: SchemaType.ARRAY, items: { type: SchemaType.STRING } };

const responseSchema: ResponseSchema = {
  type: SchemaType.OBJECT,
  properties: {
    kind: { type: SchemaType.STRING, description: 'Either "QUESTION" or "RECOMMENDATION".' },
    question: {
      type: SchemaType.OBJECT,
      nullable: true,
      properties: {
        text: { type: SchemaType.STRING },
        why: { type: SchemaType.STRING },
        options: { ...stringArray, description: `${MIN_OPTIONS} to ${MAX_OPTIONS} short options.` },
      },
      required: ["text", "why", "options"],
    },
    recommendation: {
      type: SchemaType.OBJECT,
      nullable: true,
      properties: {
        summary: { type: SchemaType.STRING },
        approaches: {
          type: SchemaType.ARRAY,
          items: {
            type: SchemaType.OBJECT,
            properties: {
              title: { type: SchemaType.STRING },
              rationale: { type: SchemaType.STRING },
              risks: { type: SchemaType.STRING },
            },
            required: ["title", "rationale", "risks"],
          },
        },
        missingFacts: stringArray,
        verifyBeforeRelying: stringArray,
      },
      required: ["summary", "approaches", "missingFacts", "verifyBeforeRelying"],
    },
    facts: {
      type: SchemaType.ARRAY,
      items: {
        type: SchemaType.OBJECT,
        properties: {
          label: { type: SchemaType.STRING },
          value: { type: SchemaType.STRING },
        },
        required: ["label", "value"],
      },
    },
  },
  required: ["kind", "facts"],
};

// ---------- Validation ----------

const clip = (s: unknown, max = MAX_FIELD_CHARS): string =>
  typeof s === "string" ? s.trim().slice(0, max) : "";

export function parseRequest(data: unknown): InterviewTurnRequest {
  const d = (data ?? {}) as Partial<InterviewTurnRequest>;
  const seed = (d.seed ?? {}) as Partial<InterviewSeed>;
  if (!clip(seed.caseType) || !clip(seed.summary)) {
    throw new HttpsError("invalid-argument", "seed.caseType and seed.summary are required.");
  }
  const history = Array.isArray(d.history) ? d.history : [];
  const facts = Array.isArray(d.facts) ? d.facts : [];
  return {
    seed: {
      caseType: clip(seed.caseType, 200),
      posture: clip(seed.posture, 500),
      summary: clip(seed.summary),
      goal: clip(seed.goal, 500),
    },
    history: history.slice(0, MAX_TURNS).map((t) => ({
      question: clip(t?.question, 500),
      answer: clip(t?.answer, 1000),
      wasCustom: t?.wasCustom === true,
    })),
    facts: facts.slice(0, MAX_FACTS).map((f) => ({ label: clip(f?.label, 80), value: clip(f?.value, 300) }))
      .filter((f) => f.label && f.value),
    forceRecommendation: d.forceRecommendation === true,
  };
}

/** Normalizes model output so the app can trust the shape. Exported for unit tests. */
export function normalizeModelOutput(
  raw: any,
  mustRecommend: boolean,
  previousFacts: CaseFact[],
  turnNumber: number,
): InterviewTurnResponse {
  const facts: CaseFact[] = Array.isArray(raw?.facts)
    ? raw.facts
        .map((f: any) => ({ label: clip(f?.label, 80), value: clip(f?.value, 300) }))
        .filter((f: CaseFact) => f.label && f.value)
        .slice(0, MAX_FACTS)
    : previousFacts;

  const base = { facts, turnNumber, maxTurns: MAX_TURNS, modelName: MODEL_NAME };

  const options: string[] = Array.isArray(raw?.question?.options)
    ? [...new Set<string>(raw.question.options.map((o: unknown) => clip(o, 120)).filter(Boolean))].slice(0, MAX_OPTIONS)
    : [];

  const questionIsUsable =
    !mustRecommend &&
    raw?.kind === "QUESTION" &&
    clip(raw?.question?.text).length > 0 &&
    options.length >= MIN_OPTIONS;

  if (questionIsUsable) {
    return {
      ...base,
      kind: "QUESTION",
      question: { text: clip(raw.question.text, 500), why: clip(raw.question.why, 500), options },
      recommendation: null,
    };
  }

  const rec = raw?.recommendation;
  if (rec && clip(rec.summary)) {
    return {
      ...base,
      kind: "RECOMMENDATION",
      question: null,
      recommendation: {
        summary: clip(rec.summary),
        approaches: (Array.isArray(rec.approaches) ? rec.approaches : [])
          .slice(0, 3)
          .map((a: any) => ({ title: clip(a?.title, 200), rationale: clip(a?.rationale), risks: clip(a?.risks) }))
          .filter((a: Approach) => a.title),
        missingFacts: (Array.isArray(rec.missingFacts) ? rec.missingFacts : []).map((s: unknown) => clip(s, 300)).filter(Boolean),
        verifyBeforeRelying: (Array.isArray(rec.verifyBeforeRelying) ? rec.verifyBeforeRelying : []).map((s: unknown) => clip(s, 300)).filter(Boolean),
      },
    };
  }

  throw new HttpsError("internal", "Model returned an unusable turn. Please retry.");
}

// ---------- Function ----------

export const guidedInterviewTurn = onCall(
  { secrets: [geminiApiKey], cors: true, timeoutSeconds: 60 },
  async (request): Promise<InterviewTurnResponse> => {
    const req = parseRequest(request.data);
    const mustRecommend = req.forceRecommendation === true || req.history.length >= MAX_TURNS;
    const turnNumber = req.history.length + 1;

    const model = new GoogleGenerativeAI(geminiApiKey.value()).getGenerativeModel({
      model: MODEL_NAME,
      systemInstruction: SYSTEM_INSTRUCTION,
      generationConfig: {
        temperature: 0.3,
        responseMimeType: "application/json",
        responseSchema,
      },
    });

    let raw: unknown;
    try {
      const result = await model.generateContent(buildTurnPrompt(req, mustRecommend));
      raw = JSON.parse(result.response.text());
    } catch (error: any) {
      console.error("guidedInterviewTurn Gemini error:", error);
      throw new HttpsError("internal", "Failed to get the next interview step.");
    }

    return normalizeModelOutput(raw, mustRecommend, req.facts, turnNumber);
  }
);
