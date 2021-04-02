package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.RequerySQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider

class RequerySQLiteFts4TokenizerTest : Fts4TokenizerTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return RequerySQLiteProvider()
    }
}
