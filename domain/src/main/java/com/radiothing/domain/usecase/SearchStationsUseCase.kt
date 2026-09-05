package com.radiothing.domain.usecase

import com.radiothing.domain.model.RadioStation
import com.radiothing.domain.model.StationQuery
import com.radiothing.domain.repository.StationRepository
import javax.inject.Inject

/**
 * Multi-field search. Accepts field prefixes in the query string —
 * `country:germany jazz`, `tag:ambient`, `lang:french news` — and treats the
 * remaining words as the name term. Bare queries search by name only.
 *
 * Multi-word filter values use double quotes: `country:"united kingdom"`.
 * Quotes around a bare phrase group it into a single name term: `"new york"`.
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

        tokenize(raw).forEach { token ->
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

    /**
     * Splits on whitespace but keeps double-quoted spans as one token:
     * `country:"united kingdom"` yields a single token `country:united kingdom`
     * and `"new york"` yields `new york`. Unquoted behavior is unchanged.
     */
    private fun tokenize(raw: String): List<String> {
        val input = raw.trim()
        if (input.isEmpty()) return emptyList()
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c == '"' -> {
                    val end = input.indexOf('"', i + 1)
                    if (end == -1) {
                        tokens.add(input.substring(i + 1))
                        i = input.length
                    } else {
                        input.substring(i + 1, end).takeIf { it.isNotBlank() }?.let { tokens.add(it) }
                        i = end + 1
                    }
                }
                else -> {
                    val sb = StringBuilder()
                    while (i < input.length && !input[i].isWhitespace()) {
                        if (input[i] == '"') {
                            val end = input.indexOf('"', i + 1)
                            if (end == -1) {
                                sb.append(input.substring(i + 1))
                                i = input.length
                            } else {
                                sb.append(input.substring(i + 1, end))
                                i = end + 1
                            }
                        } else {
                            sb.append(input[i])
                            i++
                        }
                    }
                    tokens.add(sb.toString())
                }
            }
        }
        return tokens
    }
}
