package me.ycdev.android.demo.sqlite.db.helper

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import me.ycdev.android.demo.sqlite.db.BooksTableDao
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import timber.log.Timber

object RequerySQLiteOpenHelper {
    private const val TAG = "RequerySQLiteOpenHelper"

    private const val DB_NAME = "books_requerysqlite.db"
    private const val DB_VERSION = 1

    fun create(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(DB_NAME)
            .callback(MyCallback(params))
            .build()
        return RequerySQLiteOpenHelperFactory().create(config).apply {
            setWriteAheadLoggingEnabled(params.walEnabled)
        }
    }

    private class MyCallback(private val params: SQLiteParams) :
        SupportSQLiteOpenHelper.Callback(DB_VERSION) {

        override fun onCreate(db: SupportSQLiteDatabase) {
            Timber.tag(TAG).i("onCreate")
            BooksTableDao.createTableAndIndexes(db, params)
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // only one version right now
            Timber.tag(TAG).i("onUpgrade: %d -> %d", oldVersion, newVersion)
        }
    }
}
