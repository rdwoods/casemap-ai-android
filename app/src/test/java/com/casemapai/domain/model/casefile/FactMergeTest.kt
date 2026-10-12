package com.casemapai.domain.model.casefile

import com.casemapai.domain.model.interview.CaseFact
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class FactMergeTest {

    private fun fact(label: String, value: String, source: FactSource, id: UUID = UUID.randomUUID()) =
        MatterFact(id, label, value, source)

    @Test
    fun `attorney facts win over incoming facts with the same label`() {
        val existing = listOf(fact("Entry date", "2019 (corrected)", FactSource.ATTORNEY))
        val merged = FactMerge.merge(existing, listOf(CaseFact("entry date ", "2021"), CaseFact("Prior removals", "None")))

        assertEquals(listOf("2019 (corrected)", "None"), merged.map { it.value })
        assertEquals(listOf(FactSource.ATTORNEY, FactSource.AI), merged.map { it.source })
    }

    @Test
    fun `old AI facts are replaced and keep their id when the label survives`() {
        val keptId = UUID.randomUUID()
        val existing = listOf(
            fact("Corroboration", "Partial", FactSource.AI, keptId),
            fact("Dropped", "x", FactSource.AI)
        )
        val merged = FactMerge.merge(existing, listOf(CaseFact("Corroboration", "Extensive")))

        assertEquals(1, merged.size)
        assertEquals(keptId, merged.single().id)
        assertEquals("Extensive", merged.single().value)
    }

    @Test
    fun `blank and duplicate incoming facts are ignored`() {
        val merged = FactMerge.merge(
            emptyList(),
            listOf(CaseFact("A", "1"), CaseFact("a", "2"), CaseFact("", "3"), CaseFact("B", " "))
        )
        assertEquals(listOf("A" to "1"), merged.map { it.label to it.value })
    }
}
