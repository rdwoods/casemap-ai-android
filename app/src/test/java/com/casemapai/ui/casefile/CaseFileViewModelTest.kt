package com.casemapai.ui.casefile

import androidx.lifecycle.SavedStateHandle
import com.casemapai.data.repository.InMemoryCaseFileRepository
import com.casemapai.data.sample.SampleScenarios
import com.casemapai.domain.model.casefile.FactSource
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.usecase.EditFactUseCase
import com.casemapai.domain.usecase.ObserveCaseFileUseCase
import com.casemapai.domain.usecase.UpdateCaseHeaderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CaseFileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: InMemoryCaseFileRepository
    private lateinit var caseId: UUID
    private lateinit var vm: CaseFileViewModel

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(dispatcher)
        repo = InMemoryCaseFileRepository()
        caseId = repo.createFromInterview(
            SampleScenarios.all.first().seed,
            listOf(CaseFact("Entry", "2019"), CaseFact("Corroboration", "Partial")),
            recommendation = null
        )
        vm = CaseFileViewModel(
            SavedStateHandle(mapOf(CaseFileViewModel.ARG_CASE_ID to caseId.toString())),
            ObserveCaseFileUseCase(repo),
            EditFactUseCase(repo),
            UpdateCaseHeaderUseCase(repo)
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun firstFactId() = vm.state.value.caseFile!!.facts.first().id

    @Test
    fun `loads the matter`() = runTest(dispatcher) {
        advanceUntilIdle()
        assertEquals(2, vm.state.value.caseFile?.facts?.size)
    }

    @Test
    fun `editing a fact in the sheet marks it as an attorney fact`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.onIntent(CaseFileIntent.OpenFact(firstFactId()))
        vm.onIntent(CaseFileIntent.SheetFieldChanged(SheetField.VALUE, "2018"))
        vm.onIntent(CaseFileIntent.SaveSheet)
        advanceUntilIdle()

        val fact = vm.state.value.caseFile!!.facts.first()
        assertEquals("2018", fact.value)
        assertEquals(FactSource.ATTORNEY, fact.source)
        assertNull(vm.state.value.sheet)
    }

    @Test
    fun `adding a fact appends it`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.onIntent(CaseFileIntent.AddFact)
        vm.onIntent(CaseFileIntent.SheetFieldChanged(SheetField.LABEL, "Prior removals"))
        vm.onIntent(CaseFileIntent.SheetFieldChanged(SheetField.VALUE, "None"))
        vm.onIntent(CaseFileIntent.SaveSheet)
        advanceUntilIdle()

        assertEquals("Prior removals", vm.state.value.caseFile!!.facts.last().label)
    }

    @Test
    fun `delete emits undo and restore brings the fact back`() = runTest(dispatcher) {
        advanceUntilIdle()
        val effects = mutableListOf<CaseFileEffect>()
        val job = launch { vm.effect.collect { effects += it } }

        val id = firstFactId()
        vm.onIntent(CaseFileIntent.OpenFact(id))
        vm.onIntent(CaseFileIntent.DeleteFactInSheet)
        advanceUntilIdle()

        assertTrue(vm.state.value.caseFile!!.facts.none { it.id == id })
        val deleted = (effects.single() as CaseFileEffect.FactDeleted).fact

        vm.onIntent(CaseFileIntent.RestoreFact(deleted))
        advanceUntilIdle()
        assertTrue(vm.state.value.caseFile!!.facts.any { it.id == id && it.value == "2019" })
        job.cancel()
    }

    @Test
    fun `header edit updates title and summary`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.onIntent(CaseFileIntent.EditHeader)
        vm.onIntent(CaseFileIntent.SheetFieldChanged(SheetField.TITLE, "Asylum — Test A"))
        vm.onIntent(CaseFileIntent.SaveSheet)
        advanceUntilIdle()

        assertEquals("Asylum — Test A", repo.observeCaseFile(caseId).first()!!.title)
    }

    @Test
    fun `continue interview emits navigation with this matter's id`() = runTest(dispatcher) {
        advanceUntilIdle()
        val effects = mutableListOf<CaseFileEffect>()
        val job = launch { vm.effect.collect { effects += it } }
        vm.onIntent(CaseFileIntent.ContinueInterview)
        advanceUntilIdle()

        assertEquals(CaseFileEffect.OpenInterview(caseId), effects.single())
        job.cancel()
    }
}
