package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.helper.SQLCipherOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams

class SQLCipherProvider : SQLiteProvider {
    override fun getDefaultParams(): SQLiteParams {
        return SQLiteParams(operatorNotSupported = false)
    }

    override fun createOpenHelper(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        return SQLCipherOpenHelper.create(context, params)
    }
}
