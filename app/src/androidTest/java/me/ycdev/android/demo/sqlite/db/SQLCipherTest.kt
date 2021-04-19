package me.ycdev.android.demo.sqlite.db

import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.SQLCipherProvider
import org.junit.Test

class SQLCipherTest {
    @Test
    fun checkParams() {
        val params = SQLCipherProvider().getDefaultParams()
        assertThat(params.supportedFts4Tokenizer).containsExactly(
            Tokenizer.SIMPLE,
            Tokenizer.PORTER,
            Tokenizer.UNICODE61
        )
        assertThat(params.supportedFts5Tokenizer).containsExactly(
            Tokenizer.ASCII,
            Tokenizer.UNICODE61,
            Tokenizer.PORTER,
        )
        assertThat(params.isFts5Supported()).isTrue()
    }
}