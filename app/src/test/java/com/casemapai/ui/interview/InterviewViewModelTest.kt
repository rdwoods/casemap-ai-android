package com.casemapai.ui.interview

import androidx.lifecycle.SavedStateHandle
import com.casemapai.data.repository.InMemoryCaseFileRepository
import com.casemapai.data.sample.SampleScenarios
import com.casemapai.domain.model.casefile.FactSource
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewQuestion
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.model.interview.InterviewTurn
import com.casemapai.domain.model.interview.StrategyRecommendation
import com.casemapai.domain.repository.InterviewRepository
import com.casemapai.domain.usecase.GetCaseFileUseCase
import com.casemapai.domain.usecase.NextInterviewStepUseCase
import com.casemapai.domain.usecase.SaveInterviewResultUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InterviewViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Records every request and answers with "Qn" questions until [questionsBeforeDone], then recommends. */
    private class RecordingRepository(var questionsBeforeDone: Int = 3) : InterviewRepository {
        val calls = mutableListOf<Pair<List<InterviewTurn>, Boolean>>()
        var failNext = false

        override suspend fun nextStep(
            seed: InterviewSeed,
            history: List<InterviewTurn>,
            facts: List<CaseFact>,
            forceRecommendation: Boolean
        ): InterviewStep {
            calls += history to forceRecommendation
            if (failNext) { failNext = false; error("Network down") }
            // Like the real model: start from the fact sheet it was given, then record each answer.
            val sheet = LinkedHashMap<String, String>()
            facts.forEach { sheet[it.label] = it.value }
            history.forEach { sheet[it.question] = it.answer }
            val facts = sheet.map { (label, value) -> CaseFact(label, value) }
            return if (forceRecommendation || history.size >= questionsBeforeDone) {
                InterviewStep.Recommendation(
                    StrategyRecommendation("Do X", emptyList(), emptyList(), emptyList()),
                    facts, history.size + 1, 8
                )
            } else {
                val n = history.size + 1
                InterviewStep.Question(
                    InterviewQuestion("Q$n", "why", listOf("A$n", "B$n", "C$n")),
                    facts, n, 8
                )
            }
        }
    }

    private lateinit var repo: RecordingRepository
    private lateinit var caseFiles: InMemoryCaseFileRepository
    private lateinit var vm: InterviewViewModel

    private fun newViewModel(args: Map<String, Any?> = emptyMap()) = InterviewViewModel(
        SavedStateHandle(args),
        NextInterviewStepUseCase(repo),
        GetCaseFileUseCase(caseFiles),
        SaveInterviewResultUseCase(caseFiles)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = RecordingRepository()
        caseFiles = InMemoryCaseFileRepository()
        vm = newViewModel()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun startWithSample() {
        vm.onIntent(InterviewIntent.LoadSample(SampleScenarios.all.first()))
        vm.onIntent(InterviewIntent.Start)
    }

    @Test
    fun `start fetches the first question`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()

        val s = vm.state.value
        assertEquals(InterviewPhase.INTERVIEWING, s.phase)
        assertEquals("Q1", s.currentQuestion?.text)
        assertFalse(s.isLoading)
    }

    @Test
    fun `start is ignored when seed is incomplete`() = runTest(dispatcher) {
        vm.onIntent(InterviewIntent.Start); advanceUntilIdle()

        assertEquals(InterviewPhase.SEED, vm.state.value.phase)
        assertTrue(repo.calls.isEmpty())
    }

    @Test
    fun `selecting an option appends to the transcript and sends full history`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()
        vm.onIntent(InterviewIntent.OptionSelected("B1")); advanceUntilIdle()

        val s = vm.state.value
        assertEquals(listOf("B1"), s.transcript.map { it.answer })
        assertEquals("Q2", s.currentQuestion?.text)
        assertEquals(listOf(InterviewTurn("Q1", "B1", false)), repo.calls.last().first)
    }

    @Test
    fun `custom answer is marked custom and clears the field`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()
        vm.onIntent(InterviewIntent.CustomAnswerChanged("  Something else  "))
        vm.onIntent(InterviewIntent.SubmitCustomAnswer); advanceUntilIdle()

        val answered = vm.state.value.transcript.single()
        assertEquals("Something else", answered.answer)
        assertTrue(answered.wasCustom)
        assertEquals("", vm.state.value.customAnswer)
    }

    @Test
    fun `reaching the end produces a recommendation`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()
        repeat(3) { vm.onIntent(InterviewIntent.OptionSelected("A")); advanceUntilIdle() }

        val s = vm.state.value
        assertEquals(InterviewPhase.DONE, s.phase)
        assertNotNull(s.recommendation)
        assertNull(s.currentQuestion)
    }

    @Test
    fun `finish now forces a recommendation`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()
        vm.onIntent(InterviewIntent.OptionSelected("A1")); advanceUntilIdle()
        vm.onIntent(InterviewIntent.FinishNow); advanceUntilIdle()

        assertTrue(repo.calls.last().second)
        assertEquals(InterviewPhase.DONE, vm.state.value.phase)
    }

    @Test
    fun `editing an earlier answer rewinds locally without a network call`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()
        vm.onIntent(InterviewIntent.OptionSelected("A1")); advanceUntilIdle()
        vm.onIntent(InterviewIntent.OptionSelected("A2")); advanceUntilIdle()
        val callsBefore = repo.calls.size

        vm.onIntent(InterviewIntent.EditAnswer(0)); advanceUntilIdle()

        val s = vm.state.value
        assertEquals(callsBefore, repo.calls.size)
        assertTrue(s.transcript.isEmpty())
        assertEquals("Q1", s.currentQuestion?.text)
        assertTrue(s.facts.isEmpty())
    }

    @Test
    fun `failure shows an error and retry repeats the same request`() = runTest(dispatcher) {
        startWithSample(); advanceUntilIdle()
        repo.failNext = true
        vm.onIntent(InterviewIntent.OptionSelected("A1")); advanceUntilIdle()

        assertEquals("Network down", vm.state.value.error)
        assertEquals(1, vm.state.value.transcript.size)

        vm.onIntent(InterviewIntent.Retry); advanceUntilIdle()

        assertNull(vm.state.value.error)
        assertEquals("Q2", vm.state.value.currentQuestion?.text)
        assertEquals(repo.calls[repo.calls.size - 2], repo.calls.last())
    }

    // ---------- Save / continue ----------

    private suspend fun finishSampleInterview() {
        startWithSample()
        dispatcher.scheduler.advanceUntilIdle()
        repeat(3) { i ->
            vm.onIntent(InterviewIntent.OptionSelected("A${i + 1}"))
            dispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `save as matter creates a case file with the interview facts`() = runTest(dispatcher) {
        finishSampleInterview()
        vm.onIntent(InterviewIntent.SaveMatter); advanceUntilIdle()

        val saved = caseFiles.observeCaseFiles().first().single()
        assertEquals(SampleScenarios.all.first().seed, saved.seed)
        assertEquals(listOf("A1", "A2", "A3"), saved.facts.map { it.value })
        assertTrue(saved.facts.all { it.source == FactSource.AI })
        assertNotNull(saved.recommendation)
    }

    @Test
    fun `continuing preloads facts and attorney edits survive the update`() = runTest(dispatcher) {
        val seed = SampleScenarios.all.first().seed
        val id = caseFiles.createFromInterview(seed, listOf(CaseFact("Q1", "A1")), recommendation = null)
        val factId = caseFiles.getCaseFile(id)!!.facts.single().id
        caseFiles.upsertFact(id, factId, "Q1", "Attorney correction")

        vm = newViewModel(mapOf(InterviewViewModel.ARG_CASE_ID to id.toString()))
        advanceUntilIdle()

        val s = vm.state.value
        assertTrue(s.isContinuing)
        assertEquals(InterviewPhase.INTERVIEWING, s.phase)
        assertEquals(listOf(CaseFact("Q1", "Attorney correction")), s.facts)
        assertEquals("Q1", s.currentQuestion?.text)

        repeat(3) { i -> vm.onIntent(InterviewIntent.OptionSelected("B${i + 1}")); advanceUntilIdle() }
        vm.onIntent(InterviewIntent.SaveMatter); advanceUntilIdle()

        val updated = caseFiles.getCaseFile(id)!!
        // The fake AI re-labels Q1 with the new answer "B1"; the attorney's version must win.
        val q1 = updated.facts.single { it.label == "Q1" }
        assertEquals("Attorney correction", q1.value)
        assertEquals(FactSource.ATTORNEY, q1.source)
        assertEquals(listOf("B2", "B3"), updated.facts.filter { it.source == FactSource.AI }.map { it.value })
        assertNotNull(updated.recommendation)
        assertEquals(1, caseFiles.observeCaseFiles().first().size)
    }
}
