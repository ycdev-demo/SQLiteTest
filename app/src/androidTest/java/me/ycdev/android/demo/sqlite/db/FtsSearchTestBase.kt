package me.ycdev.android.demo.sqlite.db

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.model.DataEntry
import me.ycdev.android.demo.sqlite.model.SearchCase
import me.ycdev.android.demo.sqlite.model.execute
import org.junit.After
import org.junit.Before
import org.junit.Test

abstract class FtsSearchTestBase {
    private lateinit var sqliteParams: SQLiteParams
    private lateinit var dbHelper: SupportSQLiteOpenHelper
    private lateinit var dao: FtsTableDao

    abstract fun getSQLiteProvider(): SQLiteProvider

    @Before
    fun setup() {
        val provider = getSQLiteProvider()
        sqliteParams = provider.getDefaultParams()
        dbHelper = provider.createOpenHelper(ApplicationProvider.getApplicationContext(), sqliteParams)

        dao = FtsTableDao(dbHelper, sqliteParams)
        dao.clearData()
        dao.recreateFtsTables(sqliteParams)
    }

    @After
    fun tearDown() {
        dbHelper.close()
    }

    @Test
    fun checkDb() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.queryAll().let {
            assertThat(it).hasSize(2)
            assertThat(it[0].title).isEqualTo("The Pirate Pat")
            assertThat(it[1].title).isEqualTo("C++ Primer")
        }
    }

    @Test
    fun search_basic() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("Primer", "C++ Primer"))
        dao.execute(SearchCase("treasure", "The Pirate Pat"))
        dao.execute(SearchCase("the", arrayOf("The Pirate Pat", "C++ Primer")))
    }

    @Test
    fun search_noCase() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("Primer", "C++ Primer"))
        dao.execute(SearchCase("primer", "C++ Primer"))
        dao.execute(SearchCase("treasure", "The Pirate Pat"))
    }

    @Test
    fun search_spacesAround() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("Primer ", "C++ Primer"))
        dao.execute(SearchCase(",primer ", "C++ Primer", FtsVersion.FTS4))
        dao.execute(SearchCase.illegal(",primer ", FtsVersion.FTS5))
    }

    @Test
    fun search_partial() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        // supported prefix
        dao.execute(SearchCase("prim*", "C++ Primer"))
        dao.execute(SearchCase(" Prim*", "C++ Primer"))

        // not supported cases
        dao.execute(SearchCase("prim", 0))
        dao.execute(SearchCase("rimer", 0))
        dao.execute(SearchCase("*rimer", 0, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("*rimer", FtsVersion.FTS5))
    }

    @Test
    fun search_begin() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("^C", "C++ Primer"))
        dao.execute(SearchCase("^Primer", 0))
        dao.execute(SearchCase("^a", arrayOf("The Pirate Pat", "C++ Primer")))
    }

    @Test
    fun search_phrase() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("\"famous book\"", "C++ Primer"))
        // In FTS4, "famous + book" is same to "famous book"
        // In FTS5, "famous + book" is same to the phrase "\"famous book\""
        dao.execute(SearchCase("famous + book", "C++ Primer"))

        // FTS5 "+" operator in phrase
        dao.execute(SearchCase("\"famous about\"", 0))
        dao.execute(SearchCase("famous about", 1))
        // In FTS4, "famous + about" is same to "famous about"
        // In FTS5, "famous + about" is same to the phrase "\"famous about\""
        dao.execute(SearchCase("famous + about", 1, FtsVersion.FTS4))
        dao.execute(SearchCase("famous + about", 0, FtsVersion.FTS5))

        dao.execute(SearchCase("\"fam* boo*\"", "C++ Primer", FtsVersion.FTS4))
        dao.execute(SearchCase("\"fam* boo*\"", 0, FtsVersion.FTS5))
        dao.execute(SearchCase("\"famous boo*\"", "C++ Primer", FtsVersion.FTS4))
        dao.execute(SearchCase("\"famous boo*\"", 0, FtsVersion.FTS5))
        dao.execute(SearchCase("\"famous boo\"*", 0, FtsVersion.FTS4))
        dao.execute(SearchCase("\"famous boo\"*", "C++ Primer", FtsVersion.FTS5))
    }

    @Test
    fun search_near() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("Kotlin Primer", "An incoming book about the Kotlin language"))

        dao.execute(SearchCase("book NEAR/2 Kotlin", "Kotlin Primer", FtsVersion.FTS4))
        dao.execute(SearchCase("NEAR(book Kotlin, 2)", "Kotlin Primer", FtsVersion.FTS5))

        // order!
        dao.execute(SearchCase("Kotlin NEAR/2 book", "Kotlin Primer", FtsVersion.FTS4))
        dao.execute(SearchCase("NEAR(Kotlin book, 2)", "Kotlin Primer", FtsVersion.FTS5))
        dao.execute(SearchCase("book NEAR/1 Kotlin", 0, FtsVersion.FTS4))
        dao.execute(SearchCase("NEAR(book Kotlin, 1)", 0, FtsVersion.FTS5))
        dao.execute(SearchCase("story NEAR/2 pirate NEAR/2 pat", "The Pirate Pat", FtsVersion.FTS4))
        dao.execute(SearchCase("NEAR(story pirate pat, 4)", "The Pirate Pat", FtsVersion.FTS5))
        dao.execute(SearchCase("NEAR(story pirate pat, 3)", 0, FtsVersion.FTS5))

        // operator case
        dao.execute(SearchCase("book near/2 Kotlin", 0, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("near(book Kotlin, 2)", FtsVersion.FTS5))
    }

    @Test
    fun search_and() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        // implicit "AND"
        dao.execute(SearchCase("C++ primer book", "C++ Primer", FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("C++ primer book", FtsVersion.FTS5))
        dao.execute(SearchCase("\"C++\" primer book", "C++ Primer", FtsVersion.ANY))

        dao.execute(SearchCase("C++ primer book hahaha", 0, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("C++ primer book hahaha", FtsVersion.FTS5))
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

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

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

        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language"))

        dao.execute(SearchCase("the", arrayOf("The Pirate Pat", "C++ Primer")))
        dao.execute(SearchCase("the -primer", "The Pirate Pat"))
    }

    @Test
    fun search_normalizeQuery() {
        assertThat(dao.queryAll()).isEmpty()

        // FTS4/simple & FTS5/ascii terms:
        //   the, pirate, pat, a, story, about, a, pirate, named, pat,
        //   who, searched, and, found, the, treasure
        dao.saveItem(DataEntry("The Pirate Pat", "A story about a pirate named Pat who searched and found the treasure."))

        // FTS4/simple & FTS5/ascii terms: c, primer, a, famous, book, about, the, c, language
        dao.saveItem(DataEntry("C++ Primer", "A famous book about the C++ language!!!"))

        // FTS4/simple & FTS5/ascii terms: goodbye, h, m, was, closed
        dao.saveItem(DataEntry("Goodbye", "H&M was closed"))

        // uer input: pat stor
        // In FTS4/simple & FTS5/ascii, the query "pat story" terms: pat, story
        // choice 1: raw input
        dao.execute(SearchCase("pat stor", 0))
        // choice 2: prefix match
        dao.execute(SearchCase("pat* stor*", "The Pirate Pat"))
        // choice 3: normalize
        dao.execute(SearchCase("pat stor", "The Pirate Pat", normalize = true))

        // user input: H&M
        // In FTS4/simple, the query "H&M" or "\"H&M\"" terms: h, m
        // In FTS5/ascii, the query "H&M" is illegal, but "\"H&M\"" terms: h, m
        dao.execute(SearchCase("h m", "Goodbye"))
        dao.execute(SearchCase("H&M", "Goodbye", FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("H&M", FtsVersion.FTS5))
        dao.execute(SearchCase("H&M", "Goodbye", normalize = true))

        // user input: language!!!
        // In FTS4/simple, the query "language!!!" or "\"language!!!\"" terms: language
        // In FTS5/ascii, the query "language!!!" is illegal, but "\"language!!!\"" terms: language
        dao.execute(SearchCase("language", "C++ Primer"))
        dao.execute(SearchCase("language!!!", "C++ Primer", FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("language!!!", FtsVersion.FTS5))
        dao.execute(SearchCase("language!!!", "C++ Primer", normalize = true))

        // user input: lang!!!
        // In FTS4/simple, the query "lang!!!*" or "\"language!!!\"" terms: language
        // In FTS5/ascii, the query "language!!!" is illegal, but "\"language!!!\"" terms: language
        dao.execute(SearchCase("lang!!!", 0, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("lang!!!", FtsVersion.FTS5))
        dao.execute(SearchCase("lang!!!", "C++ Primer", normalize = true))
    }

    @Test
    fun search_queryPatterns() {
        assertThat(dao.queryAll()).isEmpty()

        dao.saveItem(DataEntry("Go", "We are family 123!?"))

        // user input: we are family
        dao.execute(SearchCase("we are family", 1))
        dao.execute(SearchCase("we are family", 1, normalize = true))
        // user input: we are not family
        dao.execute(SearchCase("we are not family", 0))
        dao.execute(SearchCase("we are not family", 0, normalize = true))

        // user input: WE 123!?
        dao.execute(SearchCase("WE 123!?", 1, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("WE 123!?", FtsVersion.FTS5))
        dao.execute(SearchCase("WE 123!?", 1, normalize = true))

        // user input: 123 WE
        dao.execute(SearchCase("123 WE", 1))
        dao.execute(SearchCase("123 WE", 1, normalize = true))

        // user input: WE 12
        dao.execute(SearchCase("WE 12", 0))
        dao.execute(SearchCase("WE 12", 1, normalize = true))

        // user input: WE 12!?
        dao.execute(SearchCase("WE 12!?", 0, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("WE 12!?", FtsVersion.FTS5))
        dao.execute(SearchCase("WE 12!?", 1, normalize = true))

        // user input: 123
        dao.execute(SearchCase("123", 1))
        dao.execute(SearchCase("123", 1, normalize = true))

        // user input: ar
        dao.execute(SearchCase("ar", 0))
        dao.execute(SearchCase("ar", 1, normalize = true))

        // user input: 123!
        dao.execute(SearchCase("123!", 1, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("123!", FtsVersion.FTS5))
        dao.execute(SearchCase("123!", 1, normalize = true))

        // user input: wea
        dao.execute(SearchCase("wea", 0))
        dao.execute(SearchCase("wea", 0, normalize = true))

        // user input: re
        dao.execute(SearchCase("re", 0))
        dao.execute(SearchCase("re", 0, normalize = true))

        // user input: W e
        dao.execute(SearchCase("W e", 0))
        dao.execute(SearchCase("W e", 0, normalize = true))

        // user input: e a
        dao.execute(SearchCase("e a", 0))
        dao.execute(SearchCase("e a", 0, normalize = true))

        // user input: W
        dao.execute(SearchCase("W", 0))
        dao.execute(SearchCase("W", 1, normalize = true))

        // user input: !
        dao.execute(SearchCase("!", 0, FtsVersion.FTS4))
        dao.execute(SearchCase.illegal("!", FtsVersion.FTS5))
        dao.execute(SearchCase("!", 0, normalize = true))

        // user input: 1
        dao.execute(SearchCase("1", 0))
        dao.execute(SearchCase("1", 1, normalize = true))

        // user input: 12
        dao.execute(SearchCase("12", 0))
        dao.execute(SearchCase("12", 1, normalize = true))
    }
}
