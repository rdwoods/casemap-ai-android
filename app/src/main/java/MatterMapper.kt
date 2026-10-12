import com.casemapai.data.entity.MatterEntity
import com.casemapai.domain.model.Matter

object MatterMapper {

    fun toEntity(domain: Matter): MatterEntity {
        val now = System.currentTimeMillis()

        return MatterEntity(
            id = domain.id,
            title = domain.title,
            summary = domain.summary,
            proceduralPosture = domain.proceduralPosture,
            disputedFacts = domain.disputedFacts,
            notes = domain.notes,
            createdAt = if (domain.id == 0L) now else now, // adjust later if needed
            updatedAt = now
        )
    }

    fun fromEntity(entity: MatterEntity): Matter {
        return Matter(
            id = entity.id,
            title = entity.title,
            summary = entity.summary,
            proceduralPosture = entity.proceduralPosture,
            disputedFacts = entity.disputedFacts,
            notes = entity.notes,
            updatedAtLabel = formatTimestamp(entity.updatedAt)
        )
    }

    private fun formatTimestamp(timestamp: Long): String {
        // Keep simple for now (you can improve later)
        return "Updated recently"
    }
}