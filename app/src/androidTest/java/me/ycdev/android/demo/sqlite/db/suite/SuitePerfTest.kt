package me.ycdev.android.demo.sqlite.db.suite

import me.ycdev.android.demo.sqlite.db.AndroidSQLiteFtsPerfTest
import me.ycdev.android.demo.sqlite.db.RequerySQLiteFtsPerfTest
import me.ycdev.android.demo.sqlite.db.SQLCipherFtsPerfTest
import me.ycdev.android.demo.sqlite.db.WcdbFtsPerfTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AndroidSQLiteFtsPerfTest::class,
    RequerySQLiteFtsPerfTest::class,
    SQLCipherFtsPerfTest::class,
    WcdbFtsPerfTest::class
)
class SuitePerfTest
