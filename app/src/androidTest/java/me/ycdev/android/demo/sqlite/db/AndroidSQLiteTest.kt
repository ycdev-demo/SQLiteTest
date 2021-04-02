package me.ycdev.android.demo.sqlite.db

import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.sqlite.AndroidSQLiteProvider
import org.junit.Test

class AndroidSQLiteTest {
    @Test
    fun checkParams() {
        val params = AndroidSQLiteProvider().getDefaultParams()
        assertThat(params.supportedFts4Tokenizer).containsExactly(
            Tokenizer.SIMPLE,
            Tokenizer.PORTER,
            Tokenizer.UNICODE61,
            Tokenizer.ICU
        )
        assertThat(params.isFts5Supported()).isFalse()
        assertThat(params.supportedFts5Tokenizer).isEmpty()
    }
}
