package me.ycdev.android.demo.sqlite.db

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.model.BookEntry
import org.junit.After
import org.junit.Before
import org.junit.Test

abstract class Fts4SearchTestBase {
    private lateinit var sqliteParams: SQLiteParams
    private lateinit var dbHelper: SupportSQLiteOpenHelper
    private lateinit var dao: BooksTableDao

    abstract fun getSQLiteProvider(): SQLiteProvider

    @Before
    fun setup() {
        val provider = getSQLiteProvider()
        sqliteParams = provider.getDefaultParams()
        dbHelper = provider.createOpenHelper(ApplicationProvider.getApplicationContext(), sqliteParams)

        dao = BooksTableDao(dbHelper, sqliteParams)
        dao.recreateFtsTables(sqliteParams)
        dao.clearData()
    }

    @After
    fun tearDown() {
        dbHelper.close()
    }

    @Test
    fun checkDb() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.queryAll().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
            assertThat(it[1].title).isEqualTo("C++ Primer")
        }
    }

    @Test
    fun search_basic() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("Primer").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("treasure").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
        }
        dao.searchWithFts4("the").let {
            assertThat(it).hasSize(2)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
            assertThat(it[1].title).isEqualTo("C++ Primer")
        }
    }

    @Test
    fun search_noCase() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("Primer").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("primer").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }

        dao.searchWithFts4("treasure").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
        }
    }

    @Test
    fun search_spacesAround() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("Primer ").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4(",primer ").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
    }

    @Test
    fun search_partial() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        // supported prefix
        dao.searchWithFts4("prim*").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4(" Prim*").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }

        // not supported cases
        dao.searchWithFts4("prim").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("rimer").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("*rimer").let {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun search_begin() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("^C++").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("^Primer").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("^a").let {
            assertThat(it).hasSize(2)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
            assertThat(it[1].title).isEqualTo("C++ Primer")
        }
    }

    @Test
    fun search_phrase() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("\"famous book\"").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("\"fam* boo*\"").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }

        // not supported case
        dao.searchWithFts4("\"famous about\"").let {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun search_near() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("book NEAR/2 C++").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("book NEAR/1 C++").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("story NEAR/2 pirate NEAR/2 pat").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
        }

        // operator case
        dao.searchWithFts4("book near/2 C++").let {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun search_and() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        // implicit "AND"
        dao.searchWithFts4("C++ primer book").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("C++ primer book hahaha").let {
            assertThat(it).hasSize(0)
        }
        dao.searchWithFts4("pirate treasure").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
        }
        dao.searchWithFts4("pirate treasure haha").let {
            assertThat(it).hasSize(0)
        }

        if (sqliteParams.enhancedQuerySupported) {
            // explicit "AND"
            dao.searchWithFts4("C++ AND primer AND book").let {
                assertThat(it).hasSize(1)
                assertThat(it[0].title).isEqualTo("C++ Primer")
            }
            dao.searchWithFts4("pirate AND treasure").let {
                assertThat(it).hasSize(1)
                assertThat(it[0].title).isEqualTo("The Pirate Pat")
            }
        }
    }

    @Test
    fun search_or() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("primer OR pirate").let {
            assertThat(it).hasSize(2)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
            assertThat(it[1].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("pirate OR WeChat").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
        }

        // operator case
        dao.searchWithFts4("primer or pirate").let {
            assertThat(it).hasSize(0)
        }
    }

    @Test
    fun search_not() {
        if (!sqliteParams.operatorNotSupported) {
            return
        }

        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.searchWithFts4("the").let {
            assertThat(it).hasSize(2)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
            assertThat(it[1].title).isEqualTo("C++ Primer")
        }
        dao.searchWithFts4("the -primer").let {
            assertThat(it).hasSize(1)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
        }
    }

    @Test
    fun search_special() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("Go", "We are family 123!?"))

        assertThat(dao.searchWithFts4("we* are* family*")).hasSize(1)
        assertThat(dao.searchWithFts4("we* are* not* family*")).hasSize(0)

        // "WE* 123!?*" will be "WE* 123"
        assertThat(dao.searchWithFts4("WE* 123!?*")).hasSize(1)
        assertThat(dao.searchWithFts4("123 WE*")).hasSize(1)
        assertThat(dao.searchWithFts4("WE* 12*")).hasSize(1)
        // "WE* 12!?*" will be "WE* 12"
        assertThat(dao.searchWithFts4("WE* 12!?*")).hasSize(0)
        assertThat(dao.searchWithFts4("12 WE* ")).hasSize(0)

        assertThat(dao.searchWithFts4("123*")).hasSize(1)
        assertThat(dao.searchWithFts4("ar*")).hasSize(1)
        assertThat(dao.searchWithFts4("123!*")).hasSize(1)
        assertThat(dao.searchWithFts4("wea*")).hasSize(0)
        assertThat(dao.searchWithFts4("re*")).hasSize(0)
        assertThat(dao.searchWithFts4("W* e*")).hasSize(0)
        assertThat(dao.searchWithFts4("e* a*")).hasSize(0)
        assertThat(dao.searchWithFts4("W*")).hasSize(1)
        assertThat(dao.searchWithFts4("!*")).hasSize(0)
        assertThat(dao.searchWithFts4("1*")).hasSize(1)
        assertThat(dao.searchWithFts4("12*")).hasSize(1)
    }
}