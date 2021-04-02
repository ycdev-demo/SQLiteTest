package me.ycdev.android.demo.sqlite.db

data class SQLiteParams(
    val walEnabled: Boolean = true,
    val enhancedQuerySupported: Boolean = false,
    val operatorNotSupported: Boolean = true,
    val password: String = "default",
    val supportedFts4Tokenizer: List<String> = arrayListOf(Tokenizer.SIMPLE, Tokenizer.PORTER, Tokenizer.UNICODE61),
    val supportedFts5Tokenizer: List<String> = emptyList(),
    var fts4Tokenizer: String = Tokenizer.SIMPLE,
    var fts5Tokenizer: String = Tokenizer.UNICODE61,
) {
    fun isFts4TokenizerSupported(tokenizer: String): Boolean {
        return supportedFts4Tokenizer.contains(tokenizer)
    }

    fun isFts5TokenizerSupported(tokenizer: String): Boolean {
        return supportedFts5Tokenizer.contains(tokenizer)
    }

    fun isFts5Supported(): Boolean = supportedFts5Tokenizer.isNotEmpty()
}
