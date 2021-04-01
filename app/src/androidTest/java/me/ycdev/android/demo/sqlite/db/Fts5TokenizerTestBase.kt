package me.ycdev.android.demo.sqlite.db

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.model.BookEntry
import org.junit.Test

abstract class Fts5TokenizerTestBase {
    abstract fun getSQLiteProvider(): SQLiteProvider

    private fun isTokenizerSupported(tokenizer: String): Boolean {
        return getSQLiteProvider().getDefaultParams().supportedFts4Tokenizer.contains(tokenizer)
    }

    @Test
    fun unicode61() {
        if (isTokenizerSupported(Tokenizer.UNICODE61)) {
            executeTest(Tokenizer.UNICODE61, this::testUnicodeCharacters)
            executeTest(Tokenizer.UNICODE61, this::testArabicCharacters)
        }
    }

    @Test
    fun icu() {
        if (isTokenizerSupported(Tokenizer.ICU)) {
            executeTest(Tokenizer.ICU, this::testUnicodeCharacters)
            executeTest(Tokenizer.ICU, this::testArabicCharacters)
        }
    }

    @Test
    fun porter() {
        if (isTokenizerSupported(Tokenizer.PORTER)) {
            executeTest(Tokenizer.PORTER, this::testUnicodeCharacters)
            executeTest(Tokenizer.PORTER, this::testArabicCharacters)
        }
    }

    private fun executeTest(tokenizer: String, testCases: (BooksTableDao, String) -> Unit) {
        val provider = getSQLiteProvider()
        val sqliteParams = provider.getDefaultParams()
        sqliteParams.fts5Tokenizer = tokenizer
        val dbHelper = provider.createOpenHelper(ApplicationProvider.getApplicationContext(), sqliteParams)

        dbHelper.use {
            val dao = BooksTableDao(it, sqliteParams)
            dao.recreateFtsTables(sqliteParams)
            dao.clearData()

            // execute the test cases
            testCases.invoke(dao, tokenizer)
        }
    }

    private fun testUnicodeCharacters(dao: BooksTableDao, tokenizer: String) {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("Love", "Right now, they're very frustrated我们，哈哈"))

        // check all simple terms
        val expectedTerms = when(tokenizer) {
            Tokenizer.ICU -> arrayOf("right", "now", "they''re", "very")
            Tokenizer.PORTER -> arrayOf("right", "now", "thei", "they", "veri", "very")
            else -> arrayOf("right", "now", "they", "re", "very")
        }
        for (term in expectedTerms) {
            dao.searchWithFts5(term).let {
                assertWithMessage("term: %s", term).that(it).hasSize(1)
            }
        }

        // special term(s)
        dao.searchWithFts5("frustrated我们，哈哈").let {
            assertThat(it).hasSize(1)
        }

        when (tokenizer) {
            Tokenizer.ASCII -> {
                // 'frustrated我们，哈哈' is one term
                dao.searchWithFts5("frustrated").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts5("frustrated我们").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts5("我们*").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts5("哈哈").let {
                    assertThat(it).hasSize(0)
                }

                dao.searchWithFts5("frustrated我们*").let {
                    assertThat(it).hasSize(1)
                }
            }
            Tokenizer.UNICODE61,
            Tokenizer.PORTER -> {
                // there are two terms: "frustrated我们" and "哈哈"
                dao.searchWithFts5("frustrated我们").let {
                    assertThat(it).hasSize(1)
                }
                dao.searchWithFts5("哈哈").let {
                    assertThat(it).hasSize(1)
                }

                dao.searchWithFts5("frustrated").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts5("我们").let {
                    assertThat(it).hasSize(0)
                }
            }
            else -> {
                throw RuntimeException("Not supported")
            }
        }
    }

    private fun testArabicCharacters(dao: BooksTableDao, tokenizer: String) {
        assertThat(dao.queryAll()).isEmpty()

        // I'll join the meeting.
        dao.saveBook(BookEntry("Meeting", "سأشارك في الاجتماع."))

        // check all simple terms
        val expectedTerms = arrayOf("سأشارك", "في", "الاجتماع")
        for (term in expectedTerms) {
            dao.searchWithFts5(term).let {
                assertWithMessage("term: %s", term).that(it).hasSize(1)
                assertWithMessage("term: %s", term).that(it[0].title).isEqualTo("Meeting")
            }
        }

        // prefix of "الاجتماع"
        dao.searchWithFts5("الاج").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts5("الاج*").let {
            assertThat(it).hasSize(1)
        }

        // prefix of "سأشارك"
        dao.searchWithFts5("سأش").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts5("سأش*").let {
            assertThat(it).hasSize(1)
        }
    }
}