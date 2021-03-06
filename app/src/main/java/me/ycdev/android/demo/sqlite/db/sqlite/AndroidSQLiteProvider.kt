package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import me.ycdev.android.demo.sqlite.db.Tokenizer
import me.ycdev.android.demo.sqlite.db.helper.AndroidSQLiteOpenHelper

class AndroidSQLiteProvider : SQLiteProvider {
    override fun getDefaultParams(): SQLiteParams {
        return SQLiteParams(
            supportedFts4Tokenizer = arrayListOf(
                Tokenizer.SIMPLE,
                Tokenizer.PORTER,
                Tokenizer.UNICODE61,
                Tokenizer.ICU
            )
        )
    }

    override fun createOpenHelper(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        return AndroidSQLiteOpenHelper.create(context, params, "androidsqlite_test.db")
    }

    override fun createPerfOpenHelper(
        context: Context,
        params: SQLiteParams
    ): SupportSQLiteOpenHelper {
        return AndroidSQLiteOpenHelper.create(context, params, "androidsqlite_perf_test.db")
    }
}
