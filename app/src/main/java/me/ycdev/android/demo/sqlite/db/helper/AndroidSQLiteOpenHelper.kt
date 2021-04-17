package me.ycdev.android.demo.sqlite.db.helper

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import me.ycdev.android.demo.sqlite.db.FtsTableDao
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import timber.log.Timber

object AndroidSQLiteOpenHelper {
    private const val TAG = "AndroidSQLiteOpenHelper"

    private const val DB_VERSION = 1

    fun create(context: Context, params: SQLiteParams, dbName: String): SupportSQLiteOpenHelper {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(MyCallback(params))
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).apply {
            setWriteAheadLoggingEnabled(params.walEnabled)
        }
    }

    private class MyCallback(private val params: SQLiteParams) :
        SupportSQLiteOpenHelper.Callback(DB_VERSION) {

        override fun onCreate(db: SupportSQLiteDatabase) {
            Timber.tag(TAG).i("onCreate")
            FtsTableDao.createTableAndIndexes(db, params)
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // only one version right now
            Timber.tag(TAG).i("onUpgrade: %d -> %d", oldVersion, newVersion)
        }
    }
}
