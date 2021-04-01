package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams

interface SQLiteProvider {
    fun getDefaultParams(): SQLiteParams
    fun createOpenHelper(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper
}
