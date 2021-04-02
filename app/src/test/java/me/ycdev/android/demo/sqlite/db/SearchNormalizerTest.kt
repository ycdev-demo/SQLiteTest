package me.ycdev.android.demo.sqlite.db

import com.google.common.truth.Truth.assertThat
import me.ycdev.android.demo.sqlite.db.SearchNormalizer.normalize
import org.junit.Test

class SearchNormalizerTest {
    @Test
    fun normalize_supportedCharacters() {
        assertThat(normalize("abcdefg hijklmn opqrst uvwxyz"))
            .isEqualTo("abcdefg* hijklmn* opqrst* uvwxyz* ")
        assertThat(normalize("ABCDEFG HIJKLMN OPQRST UNVWXYZ"))
            .isEqualTo("ABCDEFG* HIJKLMN* OPQRST* UNVWXYZ* ")
        assertThat(normalize("01234_ 56789-")).isEqualTo("01234_* 56789-* ")
    }

    @Test
    fun normalize_cases() {
        assertThat(normalize("pat")).isEqualTo("pat* ")
        assertThat(normalize("go123!!!")).isEqualTo("go123* ")
        assertThat(normalize("We are family 123!?")).isEqualTo("We* are* family* 123* ")
        assertThat(normalize("!!We are family 123!! ++")).isEqualTo("We* are* family* 123* ")
    }
}
