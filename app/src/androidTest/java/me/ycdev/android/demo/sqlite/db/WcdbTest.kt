package me.ycdev.android.demo.sqlite.db

import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.WcdbProvider
import org.junit.Test

class WcdbTest {
    @Test
    fun checkParams() {
        val params = WcdbProvider().getDefaultParams()
        assertThat(params.supportedFts4Tokenizer).containsExactly(
            Tokenizer.SIMPLE,
            Tokenizer.PORTER,
            Tokenizer.UNICODE61,
            Tokenizer.MMICU
        )
        assertThat(params.supportedFts5Tokenizer).containsExactly(
            Tokenizer.ASCII,
            Tokenizer.UNICODE61,
            Tokenizer.PORTER
        )
        assertThat(params.isFts5Supported()).isTrue()
    }
}