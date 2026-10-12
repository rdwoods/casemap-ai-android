import { onCall, HttpsError } from "firebase-functions/v2/https";
import { GoogleGenerativeAI } from "@google/generative-ai";
import * as admin from "firebase-admin";
import { geminiApiKey } from "./secrets";

admin.initializeApp();

export { guidedInterviewTurn } from "./guidedInterview";


export const analyzeWithGemini = onCall(
  {
    secrets: [geminiApiKey],
    cors: true,
  },
  async (request) => {
    const prompt = request.data.prompt;

    if (!prompt || typeof prompt !== "string") {
      throw new HttpsError(
        "invalid-argument",
        "The function must be called with a 'prompt' string argument."
      );
    }

    try {
      const apiKey = geminiApiKey.value();
      const genAI = new GoogleGenerativeAI(apiKey);
      const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

      const result = await model.generateContent(prompt);
      const response = await result.response;
      const text = response.text();

      return {
        body: text,
        provider: "GEMINI",
        modelName: "gemini-2.5-flash",
      };
    } catch (error: any) {
      console.error("Gemini API Error:", error);
      throw new HttpsError(
        "internal",
        error.message || "Failed to generate content from Gemini API."
      );
    }
  }
);
