package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.WcdbProvider

class WcdbFts4TokenizerTest : Fts4TokenizerTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return WcdbProvider()
    }
}