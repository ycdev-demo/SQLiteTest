package me.ycdev.android.demo.sqlite.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.model.DataEntry
import me.ycdev.android.lib.common.utils.IoUtils
import timber.log.Timber
import java.util.Locale

class FtsTableDao(private val dbHelper: SupportSQLiteOpenHelper, val params: SQLiteParams) {

    private fun buildMetaContentValues(item: DataEntry): ContentValues {
        val values = ContentValues()
        values.put(FIELD_DATA_ID, item.dataId)
        values.put(FIELD_AUTHOR, item.author)
        return values
    }

    private fun buildIndexContentValues(rowId: Long, item: DataEntry): ContentValues {
        val values = ContentValues()
        values.put("rowid", rowId)
        values.put(FIELD_TITLE, item.title)
        values.put(FIELD_DESC, item.desc)
        return values
    }

    private fun saveItem(db: SupportSQLiteDatabase, item: DataEntry) {
        // meta table
        val metaValues = buildMetaContentValues(item)
        val cursor = db.query(
            "SELECT " + BaseColumns._ID + " FROM " + TABLE_META + " WHERE " + FIELD_DATA_ID + "=?;",
            arrayOf(item.dataId)
        )
        val rowId: Long
        if (cursor.moveToFirst()) {
            rowId = cursor.getLong(0)
            db.update(
                TABLE_META,
                SQLiteDatabase.CONFLICT_ABORT,
                metaValues,
                "$FIELD_DATA_ID=?",
                arrayOf(item.dataId)
            )
        } else {
            rowId = db.insert(TABLE_META, SQLiteDatabase.CONFLICT_ABORT, metaValues)
        }
        IoUtils.closeQuietly(cursor)

        // index table
        val indexValues = buildIndexContentValues(rowId, item)
        db.insert(TABLE_FTS4, SQLiteDatabase.CONFLICT_REPLACE, indexValues)
        if (params.isFts5Supported()) {
            db.insert(TABLE_FTS5, SQLiteDatabase.CONFLICT_REPLACE, indexValues)
        }
    }

    fun saveItem(item: DataEntry) {
        val db = dbHelper.writableDatabase
        saveItem(db, item)
    }

    fun saveItems(items: List<DataEntry>) {
        val db = dbHelper.writableDatabase
        try {
            db.beginTransaction()
            for (item in items) {
                saveItem(db, item)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun loadFromCursor(cursor: Cursor): List<DataEntry> {
        val result = arrayListOf<DataEntry>()
        while (cursor.moveToNext()) {
            val dataId = cursor.getString(0)
            val title = cursor.getString(1)
            val desc = cursor.getString(2)
            val author = cursor.getString(3)
            result.add(DataEntry(dataId, title, desc, author))
        }
        IoUtils.closeQuietly(cursor)
        return result
    }

    fun queryAll(): List<DataEntry> {
        val db = dbHelper.writableDatabase
        val sql = String.format(Locale.US, QUERY_ALL_SQL, TABLE_FTS4)
        Timber.tag(TAG).d("query all, sql: %s", sql)
        val cursor = db.query(sql)
        return loadFromCursor(cursor)
    }

    fun queryTotalCount(): Int {
        val db = dbHelper.writableDatabase
        db.query("SELECT count(*) FROM $TABLE_META").use {
            it.moveToFirst()
            return it.getInt(0)
        }
    }

    fun searchWithFts4(keyWords: String): List<DataEntry> {
        val sql = String.format(Locale.US, SEARCH_SQL, TABLE_FTS4, keyWords)
        Timber.tag(TAG).d("fts4 sql: %s", sql)
        if (keyWords.isEmpty()) {
            return emptyList()
        }

        val db = dbHelper.writableDatabase
        val cursor = db.query(sql)
        return loadFromCursor(cursor)
    }

    fun searchWithFts5(keyWords: String): List<DataEntry> {
        if (!params.isFts5Supported()) {
            throw RuntimeException("FTS5 not supported")
        }

        val sql = String.format(Locale.US, SEARCH_SQL, TABLE_FTS5, keyWords)
        Timber.tag(TAG).d("fts5 sql: %s", sql)
        if (keyWords.isEmpty()) {
            return emptyList()
        }

        val db = dbHelper.writableDatabase
        val cursor = db.query(sql)
        return loadFromCursor(cursor)
    }

    fun clearData() {
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM $TABLE_META")
        db.execSQL("DELETE FROM $TABLE_FTS4")
        if (params.isFts5Supported()) {
            db.execSQL("DELETE FROM $TABLE_FTS5")
        }
    }

    fun recreateFtsTables(params: SQLiteParams) {
        val db = dbHelper.writableDatabase
        createTableAndIndexes(db, params)
    }

    companion object {
        private const val TAG = "FtsTableDao"

        private const val TABLE_META = "meta"
        private const val TABLE_FTS4 = "index_fts4"
        private const val TABLE_FTS5 = "index_fts5"

        private const val FIELD_DATA_ID = "dataId"
        private const val FIELD_TITLE = "title"
        private const val FIELD_DESC = "desc"
        private const val FIELD_AUTHOR = "author"

        private const val SEARCH_MAX_COUNT = 1000

        /**
         * Params:
         * + %1$s: index table name
         */
        private const val SELECT_SQL = ("SELECT " + FIELD_DATA_ID +
                "," + FIELD_TITLE + "," + FIELD_DESC + "," + FIELD_AUTHOR +
                " from " + TABLE_META + " INNER JOIN %1\$s" +
                " on " + TABLE_META + "." + BaseColumns._ID + "=" + "%1\$s.rowid")
        /**
         * Params:
         * + %1$s: index table name
         * + %2$s: key words for the search
         */
        private const val SEARCH_SQL: String = (SELECT_SQL +
                " WHERE %1\$s MATCH '%2\$s'" +
                " ORDER BY " + BaseColumns._ID + " ASC" +
                " LIMIT " + SEARCH_MAX_COUNT)
        /**
         * Params:
         * + %1$s: index table name
         */
        private const val QUERY_ALL_SQL: String =
            SELECT_SQL + " ORDER BY " + BaseColumns._ID + " ASC"

        fun createTableAndIndexes(db: SupportSQLiteDatabase, params: SQLiteParams) {
            // meta table
            var sql = "CREATE TABLE IF NOT EXISTS " + TABLE_META +
                    " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FIELD_DATA_ID + " TEXT," +
                    FIELD_AUTHOR + " TEXT," +
                    "UNIQUE(" + FIELD_DATA_ID + "));"
            db.execSQL(sql)

            // FTS4 table
            if (params.isFts4TokenizerSupported(params.fts4Tokenizer)) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_FTS4")
                sql = "CREATE VIRTUAL TABLE " + TABLE_FTS4 + " USING fts4(" +
                        FIELD_TITLE + "," +
                        FIELD_DESC + "," +
                        "tokenize=${params.fts4Tokenizer}" +
                        ");"
                db.execSQL(sql)
            }

            // FTS5 table
            if (params.isFts5TokenizerSupported(params.fts5Tokenizer)) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_FTS5")
                sql = "CREATE VIRTUAL TABLE " + TABLE_FTS5 + " USING fts5(" +
                        FIELD_TITLE + "," +
                        FIELD_DESC + "," +
                        "tokenize=${params.fts5Tokenizer}" +
                        ");"
                db.execSQL(sql)
            }
        }
    }
}
