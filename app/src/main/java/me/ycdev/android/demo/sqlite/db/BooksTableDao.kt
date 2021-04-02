package me.ycdev.android.demo.sqlite.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import me.ycdev.android.demo.sqlite.case.BookEntry
import me.ycdev.android.lib.common.utils.IoUtils
import timber.log.Timber
import java.lang.RuntimeException

class BooksTableDao(private val dbHelper: SupportSQLiteOpenHelper, val params: SQLiteParams) {

    private fun buildContentValues(bookEntry: BookEntry): ContentValues {
        val values = ContentValues()
        values.put(FIELD_TITLE, bookEntry.title)
        values.put(FIELD_DESC, bookEntry.desc)
        return values
    }

    private fun saveBook(db: SupportSQLiteDatabase, bookEntry: BookEntry) {
        val values = buildContentValues(bookEntry)
        db.insert(TABLE_NAME, SQLiteDatabase.CONFLICT_REPLACE, values)
        db.insert(FTS4_NAME, SQLiteDatabase.CONFLICT_REPLACE, values)
        if (params.isFts5Supported()) {
            db.insert(FTS5_NAME, SQLiteDatabase.CONFLICT_REPLACE, values)
        }
    }

    fun saveBook(bookEntry: BookEntry) {
        val db = dbHelper.writableDatabase
        saveBook(db, bookEntry)
    }

    fun saveBooks(bookEntries: List<BookEntry>) {
        val db = dbHelper.writableDatabase
        for (c in bookEntries) {
            saveBook(db, c)
        }
    }

    private fun loadFromCursor(cursor: Cursor): List<BookEntry> {
        val result = arrayListOf<BookEntry>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(0)
            val desc = cursor.getString(1)
            result.add(BookEntry(title, desc))
        }
        IoUtils.closeQuietly(cursor)
        return result
    }

    fun queryAll(): List<BookEntry> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("SELECT " + FIELD_TITLE + "," + FIELD_DESC +
                " FROM " + TABLE_NAME + " ORDER BY " + BaseColumns._ID + " ASC")
        return loadFromCursor(cursor)
    }

    fun searchWithFts4(text: String): List<BookEntry> {
        val sql = "SELECT * from $FTS4_NAME WHERE $FTS4_NAME MATCH '$text'"
        Timber.tag(TAG).d("fts4 sql: $sql")
        if (text.isEmpty()) {
            return emptyList()
        }

        val db = dbHelper.readableDatabase
        val cursor = db.query(sql)
        return loadFromCursor(cursor)
    }

    fun searchWithFts5(text: String): List<BookEntry> {
        if (!params.isFts5Supported()) {
            throw RuntimeException("FTS5 not supported")
        }

        val sql = "SELECT * from $FTS5_NAME WHERE $FTS5_NAME MATCH '$text'"
        Timber.tag(TAG).d("fts5 sql: $sql")
        if (text.isEmpty()) {
            return emptyList()
        }

        val db = dbHelper.readableDatabase
        val cursor = db.query(sql)
        return loadFromCursor(cursor)
    }

    fun clearData() {
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.execSQL("DELETE FROM $FTS4_NAME")
        if (params.isFts5Supported()) {
            db.execSQL("DELETE FROM $FTS5_NAME")
        }
    }

    fun recreateFtsTables(params: SQLiteParams) {
        val db = dbHelper.writableDatabase
        createTableAndIndexes(db, params)
    }

    companion object {
        private const val TAG = "BooksTableDao"

        private const val TABLE_NAME = "books"
        private const val INDEX_TITLE = "books_title_index"
        private const val INDEX_DESC = "books_desc_index"
        private const val FTS4_NAME = "books_fts4"
        private const val FTS5_NAME = "books_fts5"

        private const val FIELD_TITLE = "title"
        private const val FIELD_DESC = "desc"

        fun createTableAndIndexes(db: SupportSQLiteDatabase, params: SQLiteParams) {
            var sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FIELD_TITLE + " TEXT," +
                    FIELD_DESC + " TEXT);"
            db.execSQL(sql)

            // index for title
            sql = ("CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_TITLE + " ON " + TABLE_NAME +
                    "(" + FIELD_TITLE + ");")
            db.execSQL(sql)

            // index for desc
            sql = ("CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_DESC + " ON " + TABLE_NAME +
                    "(" + FIELD_DESC + ");")
            db.execSQL(sql)

            // FTS4 table
            if (params.isFts4TokenizerSupported(params.fts4Tokenizer)) {
                db.execSQL("DROP TABLE IF EXISTS $FTS4_NAME")
                sql = "CREATE VIRTUAL TABLE " + FTS4_NAME + " USING fts4(" +
                        FIELD_TITLE + "," +
                        FIELD_DESC + "," +
                        "tokenize=${params.fts4Tokenizer}" +
                        ");"
                db.execSQL(sql)
            }

            // FTS5 table
            if (params.isFts5TokenizerSupported(params.fts4Tokenizer)) {
                db.execSQL("DROP TABLE IF EXISTS $FTS5_NAME")
                sql = "CREATE VIRTUAL TABLE " + FTS5_NAME + " USING fts5(" +
                        FIELD_TITLE + "," +
                        FIELD_DESC + "," +
                        "tokenize=${params.fts5Tokenizer}" +
                        ");"
                db.execSQL(sql)
            }
        }
    }
}
