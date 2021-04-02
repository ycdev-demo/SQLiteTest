package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.SQLCipherProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider

class SQLCipherFtsTokenizerTest : FtsTokenizerTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return SQLCipherProvider()
    }
}
