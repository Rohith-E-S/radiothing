package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationQuery
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

/**
 * Multi-field search. Accepts field prefixes in the query string —
 * `country:germany jazz`, `tag:ambient`, `lang:french news` — and treats the
 * remaining words as the name term. Bare queries search by name only.
 */
class SearchStationsUseCase @Inject constructor(
    private val repository: StationRepository
) {

    suspend operator fun invoke(query: String, offset: Int = 0, limit: Int = 20): Result<List<RadioStation>> {
        val parsed = parseQuery(query)
        if (parsed.name == null && parsed.tag == null && parsed.country == null && parsed.language == null) {
            return Result.success(emptyList())
        }
        return repository.searchStations(parsed.copy(offset = offset, limit = limit))
    }

    suspend operator fun invoke(query: StationQuery): Result<List<RadioStation>> {
        return repository.searchStations(query)
    }

    fun parseQuery(raw: String): StationQuery {
        var name: String? = null
        var tag: String? = null
        var country: String? = null
        var language: String? = null
        val nameWords = mutableListOf<String>()

        raw.trim().split(Regex("\\s+")).forEach { token ->
            val lower = token.lowercase()
            when {
                lower.startsWith("country:") && token.length > 8 -> country = token.substring(8)
                lower.startsWith("tag:") && token.length > 4 -> tag = token.substring(4)
                lower.startsWith("lang:") && token.length > 5 -> language = token.substring(5)
                else -> nameWords.add(token)
            }
        }
        if (nameWords.isNotEmpty()) name = nameWords.joinToString(" ")
        if (name == null && tag == null && country == null && language == null) return StationQuery()
        return StationQuery(name = name, tag = tag, country = country, language = language)
    }
}
