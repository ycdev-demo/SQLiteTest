package me.ycdev.android.demo.sqlite.db.helper

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.tencent.wcdb.database.SQLiteCipherSpec
import com.tencent.wcdb.room.db.WCDBOpenHelperFactory
import me.ycdev.android.demo.sqlite.db.BooksTableDao
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import timber.log.Timber

object WcdbOpenHelper {
    private const val TAG = "WcdbOpenHelper"

    private const val DB_NAME = "books_wcdb.db"
    private const val DB_VERSION = 1

    fun create(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(DB_NAME)
            .callback(MyCallback(params))
            .build()

        val cipherSpec: SQLiteCipherSpec = SQLiteCipherSpec()
            .setPageSize(4096)
            .setKDFIteration(64000)

        return WCDBOpenHelperFactory()
            .passphrase(params.password.toByteArray())
            .cipherSpec(cipherSpec)
            .writeAheadLoggingEnabled(params.walEnabled)
            .asyncCheckpointEnabled(true)
            .create(config)
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