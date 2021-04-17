package me.ycdev.android.demo.sqlite.db

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.model.DataEntry
import me.ycdev.android.demo.sqlite.model.SearchCase
import me.ycdev.android.demo.sqlite.model.execute
import org.junit.Test

abstract class FtsTokenizerTestBase {

    abstract fun getSQLiteProvider(): SQLiteProvider

    @Test
    fun unicode61() {
        executeTest(Tokenizer.UNICODE61, this::testUnicodeCharacters)
        executeTest(Tokenizer.UNICODE61, this::testArabicCharacters)
    }

    @Test
    fun icu() {
        executeTest(Tokenizer.ICU, this::testUnicodeCharacters)
        executeTest(Tokenizer.ICU, this::testArabicCharacters)
    }

    @Test
    fun mmicu() {
        executeTest(Tokenizer.MMICU, this::testUnicodeCharacters)
        executeTest(Tokenizer.MMICU, this::testArabicCharacters)
    }

    @Test
    fun simple() {
        executeTest(Tokenizer.SIMPLE, this::testUnicodeCharacters)
        executeTest(Tokenizer.SIMPLE, this::testArabicCharacters)
    }

    @Test
    fun ascii() {
        executeTest(Tokenizer.ASCII, this::testUnicodeCharacters)
        executeTest(Tokenizer.ASCII, this::testArabicCharacters)
    }

    @Test
    fun porter() {
        executeTest(Tokenizer.PORTER, this::testUnicodeCharacters)
        executeTest(Tokenizer.PORTER, this::testArabicCharacters)
    }

    private fun executeTest(tokenizer: String, task: (FtsTableDao, FtsVersion) -> Unit) {
        val provider = getSQLiteProvider()
        val sqliteParams = provider.getDefaultParams()
        sqliteParams.fts4Tokenizer = tokenizer
        sqliteParams.fts5Tokenizer = tokenizer
        val dbHelper = provider.createOpenHelper(ApplicationProvider.getApplicationContext(), sqliteParams)

        dbHelper.use {
            val dao = FtsTableDao(it, sqliteParams)
            dao.recreateFtsTables(sqliteParams)

            if (sqliteParams.isFts4TokenizerSupported(tokenizer)) {
                dao.clearData()
                task.invoke(dao, FtsVersion.FTS4)
            }

            if (sqliteParams.isFts5TokenizerSupported(tokenizer)) {
                dao.clearData()
                task.invoke(dao, FtsVersion.FTS5)
            }
        }
    }

    private fun testUnicodeCharacters(dao: FtsTableDao, ftsVersion: FtsVersion) {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("Love", "Right now, they are very frustrated哈哈，不是这样的"))

        val tokenizer = when (ftsVersion) {
            FtsVersion.FTS4 -> dao.params.fts4Tokenizer
            FtsVersion.FTS5 -> dao.params.fts5Tokenizer
            else -> throw RuntimeException("Not supported FTS: $ftsVersion")
        }

        // check all simple terms
        val expectedTerms = when (tokenizer) {
            Tokenizer.ICU,
            Tokenizer.MMICU -> arrayOf("right", "now", "they", "are", "very")
            Tokenizer.PORTER -> arrayOf("right", "now", "thei", "they", "are", "veri", "very")
            else -> arrayOf("right", "now", "they", "are", "very")
        }

        for (term in expectedTerms) {
            dao.execute(SearchCase(term, 1, ftsVersion))
        }

        // special term(s)
        dao.execute(SearchCase("frustrated哈哈，不是这样的", 1, ftsVersion))

        val fts5YesOnly = if (ftsVersion == FtsVersion.FTS4) 0 else 1
        when (tokenizer) {
            Tokenizer.SIMPLE, // FTS4 only
            Tokenizer.ASCII -> { // FTS5 only
                // 'frustrated哈哈，不是这样的' is one term
                dao.execute(SearchCase("frustrated", 0, ftsVersion))
                dao.execute(SearchCase("frustrated哈哈", 0, ftsVersion))
                dao.execute(SearchCase("哈哈*", 0, ftsVersion))
                dao.execute(SearchCase("不是*", 0, ftsVersion))

                dao.execute(SearchCase("frustrated哈哈*", 1, ftsVersion))
            }
            Tokenizer.PORTER -> {
                // FTS4: 'frustrated哈哈，不是这样的' is one term
                // (PORTER is based on SIMPLE in FTS4)
                // FTS5: there are two terms: "frustrated哈哈" and "不是这样的"
                // (PORTER is based on UNICODE61 in FTS5)
                dao.execute(SearchCase("frustrated", 0, ftsVersion))
                dao.execute(SearchCase("frustrated哈哈", fts5YesOnly, ftsVersion))
                dao.execute(SearchCase("哈哈*", 0, ftsVersion))
                dao.execute(SearchCase("不是*", fts5YesOnly, ftsVersion))

                dao.execute(SearchCase("frustrated*", 1, ftsVersion))
                // Not supported in FTS4!!!
                dao.execute(SearchCase("frustrated哈哈*", fts5YesOnly, ftsVersion))
            }
            Tokenizer.UNICODE61 -> {
                // there are two terms: "frustrated哈哈" and "不是这样的"
                dao.execute(SearchCase("frustrated哈哈", 1, ftsVersion))
                dao.execute(SearchCase("不是*", 1, ftsVersion))

                dao.execute(SearchCase("frustrated", 0, ftsVersion))
                dao.execute(SearchCase("哈哈", 0, ftsVersion))
                dao.execute(SearchCase("哈", 0, ftsVersion))
                dao.execute(SearchCase("不是", 0, ftsVersion))
                dao.execute(SearchCase("不", 0, ftsVersion))
            }
            Tokenizer.ICU  -> { // FTS4 only
                // TODO many dependence on Android system
                // there are many terms: "frustrated", "哈哈", "不是", "这样", "的"
                dao.execute(SearchCase("frustrated", 1, ftsVersion))
                dao.execute(SearchCase("哈哈", 1, ftsVersion))
                dao.execute(SearchCase("不是", 1, ftsVersion))
                dao.execute(SearchCase("这样", 1, ftsVersion))
                dao.execute(SearchCase("的", 1, ftsVersion))

                dao.execute(SearchCase("不", 0, ftsVersion))
                dao.execute(SearchCase("是", 0, ftsVersion))
                dao.execute(SearchCase("这", 0, ftsVersion))
                dao.execute(SearchCase("样", 0, ftsVersion))
            }
            Tokenizer.MMICU -> { // FTS4 only
                // there are many terms: "frustrated", "哈哈", "不是" "这样", "的", and so on
                dao.execute(SearchCase("frustrated", 1, ftsVersion))
                dao.execute(SearchCase("哈哈", 1, ftsVersion))
                dao.execute(SearchCase("不是", 1, ftsVersion))
                dao.execute(SearchCase("这样", 1, ftsVersion))
                dao.execute(SearchCase("的", 1, ftsVersion))

                // different from ICU
                dao.execute(SearchCase("哈", 1, ftsVersion))
                dao.execute(SearchCase("不", 1, ftsVersion))
                dao.execute(SearchCase("是", 1, ftsVersion))
                dao.execute(SearchCase("这", 1, ftsVersion))
                dao.execute(SearchCase("样", 1, ftsVersion))
                dao.execute(SearchCase("的", 1, ftsVersion))
            }
            else -> {
                throw RuntimeException("Not supported: $tokenizer")
            }
        }
    }

    private fun testArabicCharacters(dao: FtsTableDao, ftsVersion: FtsVersion) {
        assertThat(dao.queryAll()).isEmpty()

        // I'll join the meeting.
        dao.saveItem(DataEntry("Meeting", "سأشارك في الاجتماع."))

        // check all simple terms
        val expectedTerms = arrayOf("سأشارك", "في", "الاجتماع")
        for (term in expectedTerms) {
            dao.execute(SearchCase(term, 1, ftsVersion))
        }

        // prefix of "الاجتماع"
        dao.execute(SearchCase("الاج", 0, ftsVersion))
        dao.execute(SearchCase("الاج*", 1, ftsVersion))

        // prefix of "سأشارك"
        dao.execute(SearchCase("سأش", 0, ftsVersion))
        dao.execute(SearchCase("سأش*", 1, ftsVersion))
    }
}
