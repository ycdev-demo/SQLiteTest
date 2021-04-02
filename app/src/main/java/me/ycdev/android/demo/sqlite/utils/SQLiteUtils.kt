package me.ycdev.android.demo.sqlite.utils

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import me.ycdev.android.lib.common.utils.IoUtils.closeQuietly
import timber.log.Timber

object SQLiteUtils {
    private const val TAG = "SQLiteUtils"

    fun isTableExist(db: SupportSQLiteDatabase, tableName: String): Boolean {
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                "SELECT count(*) FROM sqlite_master WHERE name=? and type='table'",
                arrayOf(tableName)
            )
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0
            }
        } catch (e: Throwable) {
            Timber.tag(TAG).w(e, "Failed to check table[$tableName] exist")
        } finally {
            closeQuietly(cursor)
        }
        return false
    }
}
