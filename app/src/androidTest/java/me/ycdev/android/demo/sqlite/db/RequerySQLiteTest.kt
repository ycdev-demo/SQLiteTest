package me.ycdev.android.demo.sqlite.db

import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.RequerySQLiteProvider
import org.junit.Test

class RequerySQLiteTest {
    @Test
    fun checkParams() {
        val params = RequerySQLiteProvider().getDefaultParams()
        assertThat(params.supportedFts4Tokenizer).containsExactly(
            Tokenizer.SIMPLE,
            Tokenizer.PORTER,
            Tokenizer.UNICODE61
        )
        assertThat(params.supportedFts5Tokenizer).containsExactly(
            Tokenizer.ASCII,
            Tokenizer.UNICODE61,
            Tokenizer.PORTER
        )
        assertThat(params.isFts5Supported()).isTrue()
    }
}