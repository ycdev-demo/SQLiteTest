package me.ycdev.android.demo.sqlite.model

import com.google.common.truth.Truth.assertWithMessage
import me.ycdev.android.demo.sqlite.db.FtsTableDao
import me.ycdev.android.demo.sqlite.db.FtsVersion
import me.ycdev.android.demo.sqlite.db.SearchNormalizer
import org.junit.Assert.fail

class SearchCase(
    private val searchWords: String,
    private val matchedCount: Int,
    private val ftsVersion: FtsVersion = FtsVersion.ANY,
    private val normalize: Boolean = false,
    private val illegal: Boolean = false,
    private val titlesToCheck: Array<String>? = null,
) {
    constructor(
        searchWords: String, titlesToCheck: Array<String>, ftsVersion: FtsVersion = FtsVersion.ANY,
        normalize: Boolean = false
    ) : this(searchWords, titlesToCheck.size, ftsVersion, normalize, false, titlesToCheck)

    constructor(
        searchWords: String, titleToCheck: String, ftsVersion: FtsVersion = FtsVersion.ANY,
        normalize: Boolean = false
    ) : this(searchWords, 1, ftsVersion, normalize, false, arrayOf(titleToCheck))

    fun execute(dao: FtsTableDao) {
        val words = if (normalize) SearchNormalizer.normalize(searchWords) else searchWords
        if (ftsVersion == FtsVersion.ANY || ftsVersion == FtsVersion.FTS4) {
            executeSearchAndCheckResult("FTS4 search failed") { dao.searchWithFts4(words) }
        }
        if ((ftsVersion == FtsVersion.ANY || ftsVersion == FtsVersion.FTS5) && dao.params.isFts5Supported()) {
            executeSearchAndCheckResult("FTS5 search failed") { dao.searchWithFts5(words) }
        }
    }

    private fun executeSearchAndCheckResult(message: String, searchTask: () -> List<DataEntry>) {
        try {
            val result = searchTask.invoke()
            if (illegal) {
                fail("\"$searchWords\" is expected to be illegal")
            }

            assertWithMessage(message).that(result).hasSize(matchedCount)
            if (titlesToCheck != null) {
                for (i in 0 until matchedCount) {
                    assertWithMessage("$message at index#$i").that(result[i].title).isEqualTo(titlesToCheck[i])
                }
            }
        } catch (e: Exception) {
            if (!illegal) {
                throw e
            }
        }
    }

    companion object {
        fun illegal(searchWords: String, ftsVersion: FtsVersion = FtsVersion.ANY): SearchCase {
            return SearchCase(searchWords, 0, ftsVersion, illegal = true)
        }
    }
}
