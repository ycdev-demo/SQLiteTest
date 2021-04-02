package me.ycdev.android.demo.sqlite.db

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.case.BookEntry
import me.ycdev.android.demo.sqlite.case.SearchCase
import me.ycdev.android.demo.sqlite.case.execute
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import org.junit.After
import org.junit.Before
import org.junit.Test

abstract class FtsSearchTestBase {
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

        dao.execute(SearchCase("Primer", "C++ Primer"))
        dao.execute(SearchCase("treasure", "The Pirate Pat"))
        dao.execute(SearchCase("the", arrayOf("The Pirate Pat", "C++ Primer")))
    }

    @Test
    fun search_noCase() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("Primer", "C++ Primer"))
        dao.execute(SearchCase("primer", "C++ Primer"))
        dao.execute(SearchCase("treasure", "The Pirate Pat"))
    }

    @Test
    fun search_spacesAround() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("Primer ", "C++ Primer"))
        dao.execute(SearchCase(",primer ", "C++ Primer"))
    }

    @Test
    fun search_partial() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        // supported prefix
        dao.execute(SearchCase("prim*", "C++ Primer"))
        dao.execute(SearchCase(" Prim*", "C++ Primer"))

        // not supported cases
        dao.execute(SearchCase("prim", 0))
        dao.execute(SearchCase("rimer", 0))
        dao.execute(SearchCase("*rimer", 0))
    }

    @Test
    fun search_begin() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("^C++", "C++ Primer"))
        dao.execute(SearchCase("^Primer", 0))
        dao.execute(SearchCase("^a", arrayOf("The Pirate Pat", "C++ Primer")))
    }

    @Test
    fun search_phrase() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("\"famous book\"", "C++ Primer"))
        dao.execute(SearchCase("\"fam* boo*\"", "C++ Primer"))

        // not supported case
        dao.execute(SearchCase("\"famous about\"", 0))
    }

    @Test
    fun search_near() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("book NEAR/2 C++", "C++ Primer"))
        // order!
        dao.execute(SearchCase("C++ NEAR/2 book", 0))
        dao.execute(SearchCase("book NEAR/1 C++", 0))
        dao.execute(SearchCase("story NEAR/2 pirate NEAR/2 pat", "The Pirate Pat"))

        // operator case
        dao.execute(SearchCase("book near/2 C++", 0))
    }

    @Test
    fun search_and() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        // implicit "AND"
        dao.execute(SearchCase("C++ primer book", "C++ Primer"))
        dao.execute(SearchCase("C++ primer book hahaha", 0))
        dao.execute(SearchCase("pirate treasure", "The Pirate Pat"))
        dao.execute(SearchCase("pirate treasure haha", 0))

        if (sqliteParams.enhancedQuerySupported) {
            // explicit "AND"
            dao.execute(SearchCase("C++ AND primer AND book", "C++ Primer"))
            dao.execute(SearchCase("pirate AND treasure", "The Pirate Pat"))
        }
    }

    @Test
    fun search_or() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("primer OR pirate", arrayOf("The Pirate Pat", "C++ Primer")))
        dao.execute(SearchCase("WeChat OR pirate", "The Pirate Pat"))

        // operator MUST be capital letters
        dao.execute(SearchCase("primer or pirate", 0))
    }

    @Test
    fun search_not() {
        if (!sqliteParams.operatorNotSupported) {
            return
        }

        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveBook(BookEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("the", arrayOf("The Pirate Pat", "C++ Primer")))
        dao.execute(SearchCase("the -primer", "The Pirate Pat"))
    }

    @Test
    fun search_special() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveBook(BookEntry("Go", "We are family 123!?"))

        dao.execute(SearchCase("we* are* family*", 1))
        dao.execute(SearchCase("we* are* not* family*", 0))

        // "WE* 123!?*" will be "WE* 123"
        dao.execute(SearchCase("WE* 123!?*", 1))
        dao.execute(SearchCase("123 WE*", 1))
        dao.execute(SearchCase("WE* 12*", 1))
        // "WE* 12!?*" will be "WE* 12"
        dao.execute(SearchCase("WE* 12!?*", 0))
        dao.execute(SearchCase("12 WE* ", 0))

        dao.execute(SearchCase("123*", 1))
        dao.execute(SearchCase("ar*", 1))
        dao.execute(SearchCase("123!*", 1))
        dao.execute(SearchCase("wea*", 0))
        dao.execute(SearchCase("re*", 0))
        dao.execute(SearchCase("W* e*", 0))
        dao.execute(SearchCase("e* a*", 0))
        dao.execute(SearchCase("W*", 1))
        dao.execute(SearchCase("!*", 0))
        dao.execute(SearchCase("1*", 1))
        dao.execute(SearchCase("12*", 1))
    }
}
