package ac.sfj.miniappcompetition.ui.shared

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Format tanggal seperti pada baris daftar kompetisi (Referensi UI hal. 7). */
class DisplayDateTest {

    @Test
    fun format_usesIndonesianShortMonthNames() {
        assertEquals("6 Okt 2026", formatDisplayDate(Instant.parse("2026-10-06T03:00:00Z"), ZoneOffset.UTC))
        assertEquals("1 Mei 2026", formatDisplayDate(Instant.parse("2026-05-01T08:00:00Z"), ZoneOffset.UTC))
    }

    @Test
    fun format_usesGivenZone() {
        val instant = Instant.parse("2026-10-05T17:30:00Z") // 6 Okt 2026 pukul 00.30 WIB

        assertEquals("6 Okt 2026", formatDisplayDate(instant, ZoneId.of("Asia/Jakarta")))
        assertEquals("5 Okt 2026", formatDisplayDate(instant, ZoneOffset.UTC))
    }
}
