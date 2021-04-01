package me.ycdev.android.demo.sqlite.db

data class SQLiteParams(
    val walEnabled: Boolean = true,
    val fts5Supported: Boolean = false,
    val enhancedQuerySupported: Boolean = false,
    val operatorNotSupported: Boolean = true,
    val password: String = "default",
    val supportedFts4Tokenizer: List<String> = arrayListOf(Tokenizer.SIMPLE, Tokenizer.PORTER, Tokenizer.UNICODE61),
    val supportedFts5Tokenizer: List<String> = arrayListOf(Tokenizer.ASCII, Tokenizer.UNICODE61, Tokenizer.PORTER),
    var fts4Tokenizer: String = Tokenizer.SIMPLE,
    var fts5Tokenizer: String = Tokenizer.ASCII,
)
