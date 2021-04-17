package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.RequerySQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider

class RequerySQLiteFtsPerfTest : FtsPerfTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return RequerySQLiteProvider()
    }
}
