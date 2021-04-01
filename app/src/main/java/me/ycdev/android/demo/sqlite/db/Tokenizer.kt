package me.ycdev.android.demo.sqlite.db

object Tokenizer {
    /** SQLite FTS3/FTS4 */
    const val SIMPLE = "simple"

    /** SQLite FTS3/FTS4/FTS5 */
    /**
     * It's a wrapper tokenizer:
     * + In SQLite FTS3/FTS4, it's based on [SIMPLE]
     * + in SQLite FTS5, it's based on [UNICODE61]
     */
    const val PORTER = "porter"

    /** SQLite FTS3/FTS4 */
    const val ICU = "icu"

    /** SQLite FTS4/FTS5 */
    const val UNICODE61 = "unicode61"

    /** SQLite FTS5 */
    const val ASCII = "ascii"
}
