package ac.sfj.miniappcompetition.ui.shared

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tanggal untuk ditampilkan, misalnya "6 Okt 2026" (Referensi UI hal. 7).
 *
 * Nama bulan selalu berbahasa Indonesia karena seluruh teks aplikasi berbahasa Indonesia, tidak
 * mengikuti bahasa perangkat. Tanggal dihitung di zona waktu [zone], secara bawaan zona perangkat.
 */
fun formatDisplayDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
    DisplayDateFormatter.withZone(zone).format(instant)

private val DisplayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID"))
