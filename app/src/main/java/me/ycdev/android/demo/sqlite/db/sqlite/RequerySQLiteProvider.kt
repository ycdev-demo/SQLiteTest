package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.helper.RequerySQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams

class RequerySQLiteProvider : SQLiteProvider {
    override fun getDefaultParams(): SQLiteParams {
        return SQLiteParams(operatorNotSupported = false, fts5Supported = true)
    }

    override fun createOpenHelper(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        return RequerySQLiteOpenHelper.create(context, params)
    }
}