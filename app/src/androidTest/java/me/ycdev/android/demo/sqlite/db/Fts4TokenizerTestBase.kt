package me.ycdev.android.demo.sqlite.db

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.model.BookEntry
import org.junit.Test

abstract class Fts4TokenizerTestBase {

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
    fun simple() {
        if (isTokenizerSupported(Tokenizer.SIMPLE)) {
            executeTest(Tokenizer.SIMPLE, this::testUnicodeCharacters)
            executeTest(Tokenizer.SIMPLE, this::testArabicCharacters)
        }
    }

    @Test
    fun porter() {
        if (isTokenizerSupported(Tokenizer.PORTER)) {
            executeTest(Tokenizer.PORTER, this::testUnicodeCharacters)
            executeTest(Tokenizer.PORTER, this::testArabicCharacters)
        }
    }

    @Test
    fun mmicu() {
        if (isTokenizerSupported(Tokenizer.MMICU)) {
            executeTest(Tokenizer.MMICU, this::testUnicodeCharacters)
            executeTest(Tokenizer.MMICU, this::testArabicCharacters)
        }
    }

    private fun executeTest(tokenizer: String, task: (BooksTableDao, String) -> Unit) {
        val provider = getSQLiteProvider()
        val sqliteParams = provider.getDefaultParams()
        sqliteParams.fts4Tokenizer = tokenizer
        val dbHelper = provider.createOpenHelper(ApplicationProvider.getApplicationContext(), sqliteParams)

        dbHelper.use {
            val dao = BooksTableDao(it, sqliteParams)
            dao.recreateFtsTables(sqliteParams)
            dao.clearData()

            task.invoke(dao, tokenizer)
        }
    }

    private fun testUnicodeCharacters(dao: BooksTableDao, tokenizer: String) {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("Love", "Right now, they're very frustrated我们，哈哈"))

        // check all simple terms
        val expectedTerms = when(tokenizer) {
            Tokenizer.ICU,
            Tokenizer.MMICU -> arrayOf("right", "now", "they''re", "very")
            Tokenizer.PORTER -> arrayOf("right", "now", "thei", "they", "veri", "very")
            else -> arrayOf("right", "now", "they", "re", "very")
        }
        for (term in expectedTerms) {
            dao.searchWithFts4(term).let {
                assertWithMessage("term: %s", term).that(it).hasSize(1)
            }
        }

        // special term(s)
        dao.searchWithFts4("frustrated我们，哈哈").let {
            assertThat(it).hasSize(1)
        }

        when (tokenizer) {
            Tokenizer.SIMPLE -> {
                // 'frustrated我们，哈哈' is one term
                dao.searchWithFts4("frustrated").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("frustrated我们").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("我们*").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("哈哈").let {
                    assertThat(it).hasSize(0)
                }

                dao.searchWithFts4("frustrated我们*").let {
                    assertThat(it).hasSize(1)
                }
            }
            Tokenizer.PORTER -> {
                // 'frustrated我们，哈哈' is one term
                dao.searchWithFts4("frustrated").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("frustrated我们").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("我们*").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("哈哈").let {
                    assertThat(it).hasSize(0)
                }

                dao.searchWithFts4("frustrated*").let {
                    assertThat(it).hasSize(1)
                }
                dao.searchWithFts4("frustrated我们*").let {
                    assertThat(it).hasSize(0) // not supported!
                }
            }
            Tokenizer.UNICODE61 -> {
                // there are two terms: "frustrated我们" and "哈哈"
                dao.searchWithFts4("frustrated我们").let {
                    assertThat(it).hasSize(1)
                }
                dao.searchWithFts4("哈哈").let {
                    assertThat(it).hasSize(1)
                }

                dao.searchWithFts4("frustrated").let {
                    assertThat(it).hasSize(0)
                }
                dao.searchWithFts4("我们").let {
                    assertThat(it).hasSize(0)
                }
            }
            Tokenizer.ICU,
            Tokenizer.MMICU -> {
                // there are three terms: "frustrated", "我们" and "哈哈"
                dao.searchWithFts4("frustrated").let {
                    assertThat(it).hasSize(1)
                }
                dao.searchWithFts4("我们").let {
                    assertThat(it).hasSize(1)
                }
                dao.searchWithFts4("哈哈").let {
                    assertThat(it).hasSize(1)
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
            dao.searchWithFts4(term).let {
                assertWithMessage("term: %s", term).that(it).hasSize(1)
            }
        }

        // prefix of "الاجتماع"
        dao.searchWithFts4("الاج").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("الاج*").let {
            assertThat(it).hasSize(1)
        }

        // prefix of "سأشارك"
        dao.searchWithFts4("سأش").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("سأش*").let {
            assertThat(it).hasSize(1)
        }
    }
}