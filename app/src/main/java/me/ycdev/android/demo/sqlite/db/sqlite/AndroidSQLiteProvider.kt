package me.ycdev.android.demo.sqlite.db.sqlite

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.helper.AndroidSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.db.SQLiteParams
import me.ycdev.android.demo.sqlite.db.Tokenizer

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
        return AndroidSQLiteOpenHelper.create(context, params)
    }
}
