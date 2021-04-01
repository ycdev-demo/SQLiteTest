package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.AndroidSQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider

class AndroidSQLiteFts4TokenizerTest : Fts4TokenizerTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return AndroidSQLiteProvider()
    }
}