package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.AndroidSQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider

class AndroidSQLiteFtsPerfTest : FtsPerfTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return AndroidSQLiteProvider()
    }
}
