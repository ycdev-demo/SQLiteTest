package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import me.ycdev.android.demo.sqlite.db.Tokenizer
import me.ycdev.android.demo.sqlite.db.helper.RequerySQLiteOpenHelper

class RequerySQLiteProvider : SQLiteProvider {
    override fun getDefaultParams(): SQLiteParams {
        return SQLiteParams(
            operatorNotSupported = false,
            supportedFts5Tokenizer = arrayListOf(
                Tokenizer.ASCII, Tokenizer.UNICODE61, Tokenizer.PORTER
            )
        )
    }

    override fun createOpenHelper(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        return RequerySQLiteOpenHelper.create(context, params, "requerysqlite_test.db")
    }

    override fun createPerfOpenHelper(
        context: Context,
        params: SQLiteParams
    ): SupportSQLiteOpenHelper {
        return RequerySQLiteOpenHelper.create(context, params, "requerysqlite_perf_test.db")
    }
}
