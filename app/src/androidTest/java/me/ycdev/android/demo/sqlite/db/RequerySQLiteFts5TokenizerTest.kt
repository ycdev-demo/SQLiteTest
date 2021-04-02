package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.RequerySQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider

class RequerySQLiteFts5TokenizerTest : Fts5TokenizerTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return RequerySQLiteProvider()
    }
}
