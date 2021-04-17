package me.ycdev.android.demo.sqlite.db

import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.db.sqlite.WcdbProvider

class WcdbFtsPerfTest : FtsPerfTestBase() {
    override fun getSQLiteProvider(): SQLiteProvider {
        return WcdbProvider()
    }
}
