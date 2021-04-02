package me.ycdev.android.demo.sqlite.case

import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.BooksTableDao
import me.ycdev.android.demo.sqlite.db.FtsVersion

class SearchCase(
    private val searchWords: String,
    private val matchedCount: Int,
    private val ftsVersion: FtsVersion = FtsVersion.ANY,
    private val titlesToCheck: Array<String>? = null,
) {
    constructor(
        searchWords: String, titlesToCheck: Array<String>, ftsVersion: FtsVersion = FtsVersion.ANY,
    ) : this(searchWords, titlesToCheck.size, ftsVersion, titlesToCheck)

    constructor(
        searchWords: String, titleToCheck: String, ftsVersion: FtsVersion = FtsVersion.ANY,
    ) : this(searchWords, 1, ftsVersion, arrayOf(titleToCheck))

    fun execute(dao: BooksTableDao) {
        if (ftsVersion == FtsVersion.ANY || ftsVersion == FtsVersion.FTS4) {
            checkSearchResult(dao.searchWithFts4(searchWords))
        }
        if ((ftsVersion == FtsVersion.ANY || ftsVersion == FtsVersion.FTS5) && dao.isFts5Supported()) {
            checkSearchResult(dao.searchWithFts5(searchWords))
        }
    }

    private fun checkSearchResult(result: List<BookEntry>) {
        assertThat(result).hasSize(matchedCount)
        if (titlesToCheck != null) {
            for (i in 0 until matchedCount) {
                assertThat(result[i].title).isEqualTo(titlesToCheck[i])
            }
        }
    }
}
