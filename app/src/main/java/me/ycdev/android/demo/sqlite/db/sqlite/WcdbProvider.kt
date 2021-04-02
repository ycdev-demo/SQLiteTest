package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import me.ycdev.android.demo.sqlite.db.Tokenizer
import me.ycdev.android.demo.sqlite.db.helper.WcdbOpenHelper

class WcdbProvider : SQLiteProvider {
    override fun getDefaultParams(): SQLiteParams {
        return SQLiteParams(
            operatorNotSupported = false,
            fts5Supported = true,
            supportedFts4Tokenizer = arrayListOf(
                Tokenizer.SIMPLE,
                Tokenizer.PORTER,
                Tokenizer.UNICODE61,
                Tokenizer.MMICU
            )
        )
    }

    override fun createOpenHelper(context: Context, params: SQLiteParams): SupportSQLiteOpenHelper {
        return WcdbOpenHelper.create(context, params)
    }
}
