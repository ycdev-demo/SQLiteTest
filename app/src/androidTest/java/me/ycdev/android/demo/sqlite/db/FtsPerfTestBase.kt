package me.ycdev.android.demo.sqlite.db

import android.content.Context
import android.os.SystemClock
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import me.ycdev.android.demo.sqlite.db.sqlite.SQLiteProvider
import me.ycdev.android.demo.sqlite.model.DataEntry
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

abstract class FtsPerfTestBase {
    private lateinit var sqliteParams: SQLiteParams
    private lateinit var dbHelper: SupportSQLiteOpenHelper
    private lateinit var dao: FtsTableDao

    abstract fun getSQLiteProvider(): SQLiteProvider

    @Before
    fun setup() {
        val provider = getSQLiteProvider()
        sqliteParams = provider.getDefaultParams()
        dbHelper = provider.createPerfOpenHelper(ApplicationProvider.getApplicationContext(), sqliteParams)

        dao = FtsTableDao(dbHelper, sqliteParams)
//        dao.clearData()
    }

    @After
    fun tearDown() {
        dbHelper.close()
    }

    @Test
    fun perf() {
        if (dao.queryTotalCount() == 0) {
            importAssetsDataFile(10_0000)
        }

        Timber.tag(TAG).i("There are %s records in DB", dao.queryTotalCount())
        val keyWords = SearchNormalizer.normalize("broken sell")

        var totalTimeUsed = 0L
        for (i in 0 until 10) {
            val timeStart = SystemClock.elapsedRealtime()
            val result = dao.searchWithFts4(keyWords)
            val timeUsed = SystemClock.elapsedRealtime() - timeStart
            totalTimeUsed += timeUsed
            Timber.tag(TAG).i("[%s] FTS4 search, found %s records, timeUsed: %s",
                javaClass.simpleName, result.size, timeUsed)
        }
        Timber.tag(TAG).i("[%s] FTS4 search average timeUsed: %s",
            javaClass.simpleName, totalTimeUsed / 10)

        if (sqliteParams.isFts5Supported()) {
            totalTimeUsed = 0L
            for (i in 0 until 10) {
                val timeStart = SystemClock.elapsedRealtime()
                val result = dao.searchWithFts5(keyWords)
                val timeUsed = SystemClock.elapsedRealtime() - timeStart
                totalTimeUsed += timeUsed
                Timber.tag(TAG).i("[%s] FTS5 search, found %s records, timeUsed: %s",
                    javaClass.simpleName, result.size, timeUsed)
            }
            Timber.tag(TAG).i("[%s] FTS5 search average timeUsed: %s",
                javaClass.simpleName, totalTimeUsed / 10)
        }
    }

    private fun importAssetsDataFile(maxCount: Int = 0, gzip: Boolean = true) {
        val fileName = "amazon_reviews_phone.dat"
        Timber.tag(TAG).i("import assets data file: %s", fileName)
        val timeStart = SystemClock.elapsedRealtime()
        getContext().assets.open(fileName).use {
            val inputStream = if (gzip) GZIPInputStream(it) else it
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            val itemsBuffer = ArrayList<DataEntry>(DB_ITEMS_BATCH_COUNT)
            var totalCount = 0
            while (line != null) {
                val item = DataEntry.fromImportDataEntry(line)
                if (item == null) {
                    // read next line
                    line = reader.readLine()
                    continue
                }

                itemsBuffer.add(item)
                totalCount++
                if (maxCount in 1..totalCount) {
                    break
                }

                if (itemsBuffer.size >= DB_ITEMS_BATCH_COUNT) {
                    Timber.tag(TAG).i("read data count: %s", itemsBuffer.size)
                    dao.saveItems(itemsBuffer)
                    itemsBuffer.clear()
                    Timber.tag(TAG).i("index data total: %s", totalCount)
                }

                // read next line
                line = reader.readLine()
            }

            if (itemsBuffer.isNotEmpty()) {
                Timber.tag(TAG).i("read data count: %s", itemsBuffer.size)
                dao.saveItems(itemsBuffer)
                itemsBuffer.clear()
                Timber.tag(TAG).i("index data total: %s", totalCount)
            }
        }
        val timeUsed = SystemClock.elapsedRealtime() - timeStart
        Timber.tag(TAG).i("import assets data file done, timeUsed: %s", timeUsed)
    }

    private fun getContext(): Context = ApplicationProvider.getApplicationContext()

    companion object {
        private const val TAG = "FtsPerfTestBase"
        private const val DB_ITEMS_BATCH_COUNT = 1000
    }
}