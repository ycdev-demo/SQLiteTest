package me.ycdev.android.demo.sqlite.db.suite

import me.ycdev.android.demo.sqlite.db.AndroidSQLiteFtsSearchTest
import me.ycdev.android.demo.sqlite.db.AndroidSQLiteFtsTokenizerTest
import me.ycdev.android.demo.sqlite.db.RequerySQLiteFtsSearchTest
import me.ycdev.android.demo.sqlite.db.RequerySQLiteFtsTokenizerTest
import me.ycdev.android.demo.sqlite.db.SQLCipherFtsSearchTest
import me.ycdev.android.demo.sqlite.db.SQLCipherFtsTokenizerTest
import me.ycdev.android.demo.sqlite.db.WcdbFtsSearchTest
import me.ycdev.android.demo.sqlite.db.WcdbFtsTokenizerTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AndroidSQLiteFtsSearchTest::class,
    AndroidSQLiteFtsTokenizerTest::class,
    RequerySQLiteFtsSearchTest::class,
    RequerySQLiteFtsTokenizerTest::class,
    SQLCipherFtsSearchTest::class,
    SQLCipherFtsTokenizerTest::class,
    WcdbFtsSearchTest::class,
    WcdbFtsTokenizerTest::class
)
class SuiteFeaturesTest
