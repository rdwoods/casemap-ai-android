package com.casemapai.data.sample

import com.casemapai.domain.model.interview.InterviewSeed

/**
 * FICTIONAL test scenarios. No real people, A-numbers, dates, or proceedings.
 * They're written to resemble the shape of common matters so the interview
 * can be exercised end to end without ever touching real case data.
 */
data class SampleScenario(val label: String, val seed: InterviewSeed)

object SampleScenarios {

    val all: List<SampleScenario> = listOf(
        SampleScenario(
            label = "Asylum — late filing",
            seed = InterviewSeed(
                caseType = "Asylum / withholding / CAT",
                posture = "Individual merits hearing scheduled; pre-hearing statements due",
                summary = "Fictional respondent from a fictional country claims persecution by a local militia " +
                    "after refusing recruitment. Application filed more than one year after arrival. " +
                    "Respondent cites a period of illness as the reason for delay.",
                goal = "Decide which issues to contest and where to focus cross-examination"
            )
        ),
        SampleScenario(
            label = "Cancellation — hardship",
            seed = InterviewSeed(
                caseType = "Non-LPR cancellation of removal",
                posture = "Master calendar; merits not yet set",
                summary = "Fictional respondent claims 12 years of presence, a qualifying U.S. citizen child " +
                    "with a chronic condition, and steady employment. One prior misdemeanor arrest, disposition unclear.",
                goal = "Identify eligibility bars and the strongest position on hardship"
            )
        ),
        SampleScenario(
            label = "Motion to reopen — in absentia",
            seed = InterviewSeed(
                caseType = "Motion to reopen (in absentia order)",
                posture = "Respondent's motion filed; response due",
                summary = "Fictional respondent was ordered removed in absentia and now claims the hearing " +
                    "notice went to an old address. Motion filed about 14 months after the order.",
                goal = "Decide whether to oppose and on what grounds"
            )
        ),
        SampleScenario(
            label = "Civil — contract dispute",
            seed = InterviewSeed(
                caseType = "Breach of contract (state court)",
                posture = "Discovery closes in 60 days",
                summary = "Fictional small business claims a vendor delivered defective equipment; vendor " +
                    "says the buyer missed the contract's inspection window.",
                goal = "Choose between summary judgment, settlement, or trial prep"
            )
        )
    )
}
