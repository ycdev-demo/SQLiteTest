package me.ycdev.android.demo.sqlite.model

import org.json.JSONObject
import java.util.UUID

data class DataEntry(
    val dataId: String,
    val title: String,
    val desc: String,
    val author: String = ""
) {
    constructor(title: String, desc: String) : this(
        UUID.randomUUID().toString(),
        title,
        desc,
        ""
    )

    companion object {
        fun fromImportDataEntry(jsonStr: String): DataEntry? {
            val json = JSONObject(jsonStr)
            val dataId = json.optString("dataId")
            val title = json.optString("title")
            val desc = json.optString("desc")
            val author = json.optString("author")
            if (desc.isEmpty() || dataId.isEmpty()) {
                return null
            }
            return DataEntry(dataId, title, desc, author)
        }
    }
}
