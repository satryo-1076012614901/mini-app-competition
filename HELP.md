# Panduan teknis — Mini Competition Manager (Android)

Referensi teknis untuk anggota tim: stack, data layer, pola ViewModel, katalog komponen UI (atomic design), `ui/shared`, penanganan error, routing, dan test. Panduan kerja tim (konsep desain, struktur folder, aturan penempatan kode, dan cara mengaktifkan modul) ada di [`README.md`](README.md).

Contoh kode di dokumen ini menulis teks secara langsung agar ringkas. Di kode layar, ambil teks dari `res/values/strings.xml`.

## Daftar isi

1. [Stack dan build](#1-stack-dan-build)
2. [Data layer](#2-data-layer)
3. [ViewModel dan UiState](#3-viewmodel-dan-uistate)
4. [Komponen UI (atomic design)](#4-komponen-ui-atomic-design)
5. [Shared UI (`ui/shared`)](#5-shared-ui-uishared)
6. [Penanganan error](#6-penanganan-error)
7. [Aturan per status](#7-aturan-per-status)
8. [Routing](#8-routing)
9. [Test](#9-test)
10. [Masalah umum](#10-masalah-umum)

---

## 1. Stack dan build

| Bagian | Versi |
|---|---|
| Android Gradle Plugin | 9.4.1, dengan Kotlin bawaan AGP; plugin Compose dan serialization 2.2.10 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 33 |
| Jetpack Compose BOM | 2026.02.01 (Compose UI 1.10.5, Material 3 1.4.0) |
| Activity Compose | 1.13.0 |
| Lifecycle (runtime-compose, viewmodel-compose) | 2.11.0 |
| Navigation Compose | 2.10.2, route type-safe dengan kotlinx.serialization |
| Retrofit dan converter kotlinx-serialization | 3.0.0 |
| OkHttp dan logging-interceptor | 5.5.0 |
| kotlinx-serialization-json | 1.9.0 |
| kotlinx-coroutines (android, test) | 1.9.0 |
| JUnit | 4.13.2 |

- Seluruh versi dikelola di `gradle/libs.versions.toml`. Menambah dependency berarti mengubah file bersama, jadi diskusikan dulu dengan tim.
- Base URL berasal dari `BuildConfig.API_BASE_URL`: build debug ke `https://api.vispro.satryo.pro/develop/`, build release ke `.../production/`.
- Base URL **diakhiri `/`** dan path endpoint ditulis **relatif tanpa `/` di depan** (`"api/competitions"`). Path yang diawali `/` membuat Retrofit membuang prefix `/develop/` atau `/production/`.
- Pada build debug, `HttpLoggingInterceptor` mencatat request dan response lengkap ke Logcat.

---

## 2. Data layer

### 2.1 Alur data

```plain
ViewModel ── refresh…() / create…() ──► Repository ── safeApiCall { ApiService … .toModel() } ──► backend
    ▲                                       │
    └──────────── Flow dari cache ◄─────────┘  hasil disimpan ke cache StateFlow, layar mengamati Flow
```

- ViewModel **membaca** data dengan mengamati `Flow` dari repository dan **memicu** pemuatan dengan fungsi `refresh…`.
- Setiap fungsi suspend repository mengembalikan `AppResult`, tidak pernah melempar exception jaringan.
- Repository yang sama dipakai semua layar, sehingga data yang dimuat di satu layar langsung terlihat di layar lain.

### 2.2 Model (`data/model`)

```kotlin
// Modul Competition (aktif)
data class Competition(
    val id: Long,
    val name: String,
    val participantType: ParticipantType,   // INDIVIDUAL, TEAM
    val status: CompetitionStatus,          // OPEN (Pendaftaran), IN_MATCH (Berlangsung), COMPLETED (Selesai)
    val createdAt: Instant,
)

// Modul Participant (dikomentari)
data class Participant(
    val id: Long,
    val competitionId: Long,
    val name: String,
    val members: List<String>,              // selalu kosong untuk kompetisi Individu
    val createdAt: Instant,
)

// Modul Match (dikomentari)
data class Match(
    val id: Long,
    val competitionId: Long,
    val round: Int,
    val matchNumber: Int,
    val participant1Id: Long?,              // null = menunggu pemenang match sebelumnya
    val participant2Id: Long?,
    val winnerId: Long?,
    val nextMatchId: Long?,                 // null = final
    val score1: Int?,                       // milik participant1Id
    val score2: Int?,                       // milik participant2Id
    val status: MatchStatus,                // PENDING (Menunggu), READY (Siap), COMPLETED (Selesai)
)
```

`Match` menyediakan `participantId(slot)`, `score(slot)`, dan `winnerSlot` dengan `MatchSlot.ONE` atau `MatchSlot.TWO`. Bye juga berupa `Match`; bedakan dengan `BracketTopology.isBye(match)`.

### 2.3 `AppResult` dan `AppError`

```kotlin
when (val result = competitionRepository.createCompetition(name, participantType)) {
    is AppResult.Success -> showCreated(result.data)    // result.data: Competition
    is AppResult.Failure -> showError(result.error)     // result.error: AppError
}
```

Fungsi bantu: `map { }`, `onSuccess { }`, `onFailure { }`, `getOrNull()`, dan `error.apiCode` (kode `ApiErrorCode` atau `null`).

| `AppError` | Kapan terjadi |
|---|---|
| `Api(code, httpStatus)` | Backend membalas body `{"code","message"}`; `code` bertipe `ApiErrorCode` |
| `Http(httpStatus)` | Respons gagal tanpa body dari backend, misalnya 502 dari reverse proxy |
| `Network` | Tidak ada respons: offline, timeout, atau server tidak dapat dihubungi |
| `InvalidResponse` | Respons tidak sesuai kontrak, misalnya JSON rusak atau status yang belum dikenal |

`ApiErrorCode` mengikuti katalog kode error backend. Kode yang belum dikenal aplikasi menjadi `ApiErrorCode.UNKNOWN`. Kode milik modul Participant dan Match masih dikomentari sampai modulnya diaktifkan.

### 2.4 Repository (`data/repository`)

Repository diambil dari container di `MiniCompetitionApplication` (lihat [bagian 3](#3-viewmodel-dan-uistate)). Nama dan anggota yang dikirim ke repository **tidak dinormalisasi otomatis**; jalankan dulu validasi dan normalisasi dari `data/rule`.

**`CompetitionRepository`** (aktif)

| Anggota | Hasil | Keterangan |
|---|---|---|
| `competitions` | `Flow<List<Competition>?>` | Terbaru lebih dulu; `null` sebelum daftar pernah dimuat |
| `observeCompetition(competitionId)` | `Flow<Competition?>` | Data yang sama dengan daftar; `null` bila belum dimuat atau sudah dihapus |
| `refreshCompetitions()` | `AppResult<List<Competition>>` | `GET /api/competitions` |
| `refreshCompetition(competitionId)` | `AppResult<Competition>` | Dipanggil setelah aksi match karena statusnya dapat berubah |
| `createCompetition(name, participantType)` | `AppResult<Competition>` | Kompetisi baru selalu `OPEN` |
| `updateCompetition(competitionId, name, participantType)` | `AppResult<Competition>` | Hanya saat `OPEN` |
| `deleteCompetition(competitionId)` | `AppResult<Unit>` | Hanya saat `OPEN`; peserta ikut terhapus |

**`ParticipantRepository`** (aktif setelah modul Participant dibuka)

| Anggota | Hasil | Keterangan |
|---|---|---|
| `observeParticipants(competitionId)` | `Flow<List<Participant>?>` | Urut pendaftaran; urutan ini menentukan pasangan Babak 1 |
| `refreshParticipants(competitionId)` | `AppResult<List<Participant>>` | |
| `createParticipant(competitionId, name, members)` | `AppResult<Participant>` | `members` dari `ParticipantRules.membersForRequest` (`null` untuk Individu) |
| `updateParticipant(competitionId, participantId, name, members)` | `AppResult<Participant>` | |
| `deleteParticipant(competitionId, participantId)` | `AppResult<Unit>` | |

**`MatchRepository`** (aktif setelah modul Match dibuka)

| Anggota | Hasil | Keterangan |
|---|---|---|
| `observeMatches(competitionId)` | `Flow<List<Match>?>` | Urut babak lalu nomor match; daftar kosong berarti bracket belum dibuat |
| `refreshMatches(competitionId)` | `AppResult<List<Match>>` | |
| `generateBracket(competitionId)` | `AppResult<List<Match>>` | Status kompetisi menjadi `IN_MATCH` |
| `submitResult(competitionId, matchId, winnerId, score1, score2)` | `AppResult<Match>` | Daftar match dimuat ulang otomatis; hasil final membuat kompetisi `COMPLETED` |
| `undoResult(competitionId, matchId)` | `AppResult<Match>` | Daftar match dimuat ulang otomatis; urungkan final membuat kompetisi kembali `IN_MATCH` |
| `resetBracket(competitionId)` | `AppResult<Unit>` | Seluruh match dihapus; kompetisi kembali `OPEN` |

Perilaku cache:

- `null` berarti data belum pernah dimuat. Tampilkan `LoadingPlaceholder`, bukan empty state.
- Hasil create, update, dan delete langsung diterapkan ke cache. Layar tidak perlu memuat ulang daftar.
- Error `COMPETITION_NOT_FOUND` atau `PARTICIPANT_NOT_FOUND` menghapus data yang sudah tidak ada dari cache.
- `MatchRepository` hanya menjaga daftar match. Setelah aksi match berhasil, ViewModel tetap memanggil `competitionRepository.refreshCompetition(competitionId)`.

### 2.5 Aturan bisnis (`data/rule`)

Seluruh aturan berupa fungsi Kotlin murni dan diuji unit. Tanda (*) berarti aktif setelah modulnya dibuka.

| Objek | Anggota |
|---|---|
| `CompetitionRules` | `NAME_MAX_LENGTH` (255), `canEdit`, `canDelete`, `canManageParticipants`*, `canGenerateBracket`*, `canResetBracket`*, `isParticipantTypeLocked(participantCount)`, `normalizeName`, `validateName` |
| `NameError` | `BLANK`, `TOO_LONG`: hasil `validateName` |
| `ParticipantRules`* | `NAME_MAX_LENGTH` (255), `normalizeName`, `validateName`, `normalizeMembers`, `membersForRequest(participantType, members)` |
| `BracketShape`* | `of(participantCount)` (`null` di luar 2–16), `MIN_PARTICIPANTS`, `MAX_PARTICIPANTS`, `isSupported`, `roundCount`, `playedMatchCount`, `byeCount`, `nodesPerRound`, `byesPerRound`, `roundsWithBye`, `roundName(round)` |
| `RoundName`* | `Final`, `Semifinal`, `Numbered(round)`; dibuat dengan `RoundName.of(round, roundCount)` |
| `BracketTopology`* | `matches`, `roundCount`, `finalMatch`, `championId`, `roundName`, `matchesInRound`, `findMatch`, `nextMatch`, `sourcesOf`, `isBye`, `slotSource`, `roundProgress` (hasil `RoundProgress(completed, total)`), `initialRound` |
| `MatchResultRules`* | `canSubmitResult`, `canUndo`, `winnerByScore`, `validateResult` (hasil `ResultError`: `NEGATIVE_SCORE`, `WINNER_REQUIRED`, `WINNER_CONTRADICTS_SCORE`) |

```kotlin
// Form kompetisi
val nameError: NameError? = CompetitionRules.validateName(form.name)
val nameToSend = CompetitionRules.normalizeName(form.name)
val typeLocked = CompetitionRules.isParticipantTypeLocked(participantCount)

// Ringkasan tab Peserta dan dialog Buat bracket: "6 tim: 3 babak, 5 pertandingan, 1 bye di Semifinal"
val shape = BracketShape.of(participants.size)          // null bila kurang dari 2 atau lebih dari 16
// Untuk 6 peserta: roundCount = 3, playedMatchCount = 5, byeCount = 1, roundsWithBye = [2] (Semifinal)

// Tab Bracket
val topology = BracketTopology(matches)
topology.isBye(match)                                   // kartu bergaris putus "Lolos otomatis"
topology.slotSource(match, MatchSlot.ONE)               // asal slot kosong: "Pemenang Babak 1 Match 1"
MatchResultRules.canUndo(competition.status, match, topology)
```

### 2.6 Menambah endpoint

1. **DTO** di `data/dto/<Modul>Dto.kt`. Enum dan timestamp dibaca sebagai `String`, lalu dipetakan di `toModel()`. Field nullable diberi nilai default `null`.
2. **Service** di `data/service/<Modul>ApiService.kt` dengan path relatif.
3. **Repository**: panggil service di dalam `safeApiCall { }` dan lakukan `toModel()` di dalamnya, agar kegagalan pemetaan ikut menjadi `AppError.InvalidResponse`.
4. **Container**: hanya diubah bila membuat repository baru.
5. **Test**: tambahkan fake service di `FakeApiServices.kt` dan test repository.

```kotlin
// data/service/CompetitionApiService.kt
@GET("api/competitions/{competitionId}")                // tanpa "/" di depan
suspend fun getCompetition(@Path("competitionId") competitionId: Long): CompetitionResponseDto

// data/repository/CompetitionRepository.kt (ringkas)
override suspend fun refreshCompetition(competitionId: Long): AppResult<Competition> =
    safeApiCall { competitionApiService.getCompetition(competitionId).toModel() }
        .onSuccess(::put)                               // simpan ke cache
```

---

## 3. ViewModel dan UiState

Pola yang dipakai:

- **Satu ViewModel per layar** di `ui/feature/<fitur>/viewmodel`. State layar diekspos sebagai `StateFlow<…UiState>`; View hanya membaca state dan memanggil fungsi ViewModel.
- **UiState** adalah `data class` yang berisi data dari repository serta state proses (memuat, menyimpan, error).
- **Factory** berada di `companion object` dan mengambil repository dari container di `MiniCompetitionApplication`.
- ViewModel tidak memakai Composable, komponen UI, atau `Context`.

Contoh (ilustrasi) ViewModel daftar kompetisi:

```kotlin
package ac.sfj.miniappcompetition.ui.feature.competition.viewmodel

import ac.sfj.miniappcompetition.MiniCompetitionApplication
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import ac.sfj.miniappcompetition.data.model.Competition
import ac.sfj.miniappcompetition.data.repository.CompetitionRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CompetitionListUiState(
    val competitions: List<Competition>? = null,   // null selama belum pernah dimuat
    val isRefreshing: Boolean = false,
    val loadError: AppError? = null,
)

class CompetitionListViewModel(
    private val competitionRepository: CompetitionRepository,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val loadError = MutableStateFlow<AppError?>(null)

    val uiState: StateFlow<CompetitionListUiState> = combine(
        competitionRepository.competitions,
        refreshing,
        loadError,
    ) { competitions, isRefreshing, error ->
        CompetitionListUiState(competitions, isRefreshing, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompetitionListUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            loadError.value = (competitionRepository.refreshCompetitions() as? AppResult.Failure)?.error
            refreshing.value = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MiniCompetitionApplication
                CompetitionListViewModel(application.competitionContainer.competitionRepository)
            }
        }
    }
}
```

Di View, ambil ViewModel dengan factory dan baca state sesuai lifecycle:

```kotlin
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CompetitionListScreen(
    onOpenCompetition: (Long) -> Unit,
    viewModel: CompetitionListViewModel = viewModel(factory = CompetitionListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CompetitionListContent(uiState = uiState, onRefresh = viewModel::refresh, onOpenCompetition = onOpenCompetition)
}
```

Pisahkan `…Screen` (mengambil ViewModel) dari `…Content` (hanya menerima state dan callback), agar `…Content` dapat di-preview dengan data contoh tanpa ViewModel.

**Pesan sekali tampil** (snackbar) disimpan di UiState, misalnya `val message: …? = null`. View menampilkannya di `LaunchedEffect(uiState.message)` dengan `SnackbarHostState`, lalu memanggil fungsi ViewModel seperti `onMessageShown()` untuk mengosongkannya.

**Aksi match** selalu diikuti pemuatan ulang detail kompetisi:

```kotlin
fun undo(matchId: Long) {
    viewModelScope.launch {
        when (val result = matchRepository.undoResult(competitionId, matchId)) {
            is AppResult.Success -> competitionRepository.refreshCompetition(competitionId)
            is AppResult.Failure -> showError(result.error)    // lihat bagian 6
        }
    }
}
```

---

## 4. Komponen UI (atomic design)

Package: `ac.sfj.miniappcompetition.ui.component.*`. Setiap komponen memiliki preview yang dapat dibuka di Android Studio sebagai contoh pemakaian.

### 4.1 Lapisan

| Lapisan | Isi | Contoh |
|---|---|---|
| `foundation` | Ikon, modifier, dan dukungan preview | `AppIcons`, `Modifier.dashedBorder`, `@ComponentPreviews` |
| `atom` | Elemen terkecil, umumnya membungkus satu komponen Material 3 | `AppButton`, `AppTextField`, `ScoreText` |
| `molecule` | Gabungan beberapa atom untuk satu tugas | `StatusLabel`, `ScoreStepper`, `InfoBanner` |
| `organism` | Bagian layar yang utuh | `AppTopBar`, `FormBottomSheet`, `EmptyState` |
| `template` | Kerangka layar tanpa data | `ListScreenTemplate`, `DetailTabsTemplate` |
| Layar (page) | Template yang diisi data dari ViewModel | `ui/feature/<fitur>/view/…Screen.kt` |

Prinsip:

1. Pakai komponen yang sudah ada sebelum membuat yang baru.
2. Komponen **stateless**: menerima nilai dan callback (`value` + `onValueChange`, `selected` + `onSelect`). State disimpan di ViewModel.
3. Komponen **tidak mengenal data domain**. Layar memetakan model ke teks, enum tampilan, dan ikon.
4. Warna, jarak, dan tipografi diambil dari theme (`MaterialTheme.colorScheme`, `Dimens`, `MaterialTheme.typography`), bukan nilai literal.
5. Ikon tanpa label wajib memiliki `contentDescription`. Status dan pemenang tidak dibedakan hanya dengan warna.

### 4.2 Theme dan token

`MiniAppCompetitionTheme` sudah dipasang di `MainActivity`; jangan menambahkan theme lain di layar.

| Token | Kegunaan (Referensi UI hal. 5) |
|---|---|
| `MaterialTheme.colorScheme.primary` | Tombol utama, tab aktif, penanda pemenang |
| `MaterialTheme.colorScheme.primaryContainer` | Status Pendaftaran, chip Siap, FAB |
| `MaterialTheme.colorScheme.secondaryContainer` | Filter terpilih, kartu info, segmen terpilih |
| `MaterialTheme.colorScheme.tertiaryContainer` | Status Berlangsung |
| `MaterialTheme.colorScheme.outlineVariant` | Garis tepi kartu, pembatas list |
| `MaterialTheme.colorScheme.error` | Field error, tombol destruktif |
| `MaterialTheme.extendedColors.championContainer` | Latar kartu Juara (juga `champion`, `onChampion`, `onChampionContainer`) |
| `MaterialTheme.typography.titleLarge` | Judul app bar dan sheet |
| `MaterialTheme.typography.bodyLarge` / `bodyMedium` | Nama kompetisi dan peserta / teks pendukung |
| `MaterialTheme.typography.labelLarge` / `labelMedium` | Tombol dan tab / label status |
| `ScoreTextStyle` | Skor 22 sp dengan digit tabular |
| `Dimens.SpaceXs` … `SpaceXxl` | Jarak 4, 8, 12, 16, 24, 32 dp |
| `Dimens.ScreenPadding` | Gutter horizontal layar (16 dp) |
| `Dimens.MinTouchTarget` | Target sentuh minimum (48 dp) |

`extendedColors` diimpor dari `ac.sfj.miniappcompetition.ui.theme.extendedColors`.

### 4.3 Ikon

Ikon adalah Material Symbols Outlined (weight 400) dalam bentuk `ImageVector`, diakses lewat `AppIcons`:

```kotlin
Icon(imageVector = AppIcons.Trophy, contentDescription = null)    // dekoratif, ada teks di sebelahnya
AppIconButton(icon = AppIcons.MoreVert, contentDescription = "Menu lainnya", onClick = onMenu)
```

| `AppIcons` | Kegunaan |
|---|---|
| `Add` | Tambah kompetisi, peserta, anggota; tombol + skor |
| `ArrowBack` | Kembali (otomatis dicerminkan untuk bahasa kanan-ke-kiri) |
| `Check` | Penanda status Selesai |
| `CheckCircle` (filled) | Penanda pemenang |
| `Close` | Tutup dialog layar penuh |
| `Delete` | Hapus |
| `Error` (filled) | Field error dan error state |
| `Groups` / `Person` | Kompetisi Tim / Individu |
| `Info` | Kartu info dan keterangan |
| `Lock` | Data terkunci |
| `MoreVert` | Menu overflow |
| `Remove` | Tombol − skor |
| `RemoveCircle` | Hapus field anggota |
| `RestartAlt` | Reset bracket |
| `Trophy` | Juara dan empty state (di Referensi UI bernama `emoji_events`) |
| `Undo` | Urungkan hasil (dicerminkan untuk bahasa kanan-ke-kiri) |
| `AccountTree` | Bracket penuh; tidak dipakai karena layar tersebut tidak digunakan |

Menambah ikon:

1. Buka <https://fonts.google.com/icons>, pilih **Material Symbols Outlined** dengan weight 400, grade 0, optical size 24, lalu unduh SVG.
2. Salin atribut `d` dari elemen `<path>` ke konstanta baru di `MaterialSymbolPaths` (`foundation/MaterialSymbols.kt`). Path yang sudah ada jangan diubah.
3. Tambahkan properti di `AppIcons`, misalnya `val Edit: ImageVector by lazy { materialSymbol("Edit", MaterialSymbolPaths.Edit) }`. Untuk ikon berarah (panah), tambahkan `autoMirror = true`.

### 4.4 Katalog komponen

**Atom**

| Komponen | Kegunaan | Parameter penting |
|---|---|---|
| `AppButton(text, onClick)` | Semua tombol teks | `variant`: `Filled` (aksi utama), `Tonal` (aksi utama di kartu, mis. Input hasil), `Outlined` (sekunder), `Text` (ringan: Batal, Urungkan), `Destructive` (Hapus, Reset di dialog); `loading`; `leadingIcon` |
| `AppIconButton(icon, contentDescription, onClick)` | Tombol ikon | `variant`: `Standard`, `Outlined`; `enabled` |
| `AppTextField(value, onValueChange, label)` | Field teks satu baris | `supportingText`; `errorText` (menggantikan `supportingText`); `maxLength` (hanya penghitung, validasi tetap di rule); `placeholder`; `keyboardOptions` |
| `AppAvatar(text)` / `AppAvatar(icon)` | Lingkaran nomor urut atau ikon | `size` (`AppAvatarDefaults.Size` 40 dp, `LargeSize` 80 dp), `containerColor`, `contentColor` |
| `CountPill(text)` | Penghitung kecil, mis. "2/3" | `highlighted` |
| `ScoreText(score)` | Angka skor; `null` tampil "—" | `emphasized` (tebal untuk pemenang), `textAlign` |

**Molecule**

| Komponen | Kegunaan | Parameter penting |
|---|---|---|
| `StatusLabel(text)` | Label status non-interaktif | `tone`: `Primary`, `Tertiary`, `Neutral`, `Outline`; `leading`: `None`, `Dot`, `Check`; `dashed` |
| `HintText(text)` | Teks keterangan kecil | `icon`; `tone`: `Neutral`, `Error` |
| `AppListItem(headline)` | Baris daftar kompetisi dan peserta | `supporting`, `leading`, `trailing`, `onClick` |
| `OverflowMenu(actions)` | Menu ⋮ | `MenuAction(label, onClick, destructive, enabled)`; daftar kosong menyembunyikan tombol |
| `FilterChipRow(options, selected, onSelect)` | Filter satu pilihan yang dapat digeser | `FilterOption(value, label)` |
| `SegmentedSelector(options, selected, onSelect)` | Pilihan tunggal, mis. jenis peserta | `SegmentOption(value, label)`; `selected` boleh `null`; `enabled` |
| `ScoreStepper(label, value, onDecrement, onIncrement)` | Input skor − nilai + | `decrementEnabled` (default nonaktif saat nilai kosong atau 0), `incrementEnabled` |
| `RadioOptionRow(label, selected, onClick)` | Opsi radio satu baris | Bungkus beberapa opsi dengan `Modifier.selectableGroup()` |
| `RemovableTextField(value, onValueChange, label, onRemove, removeContentDescription)` | Field anggota tim dengan tombol hapus | `placeholder`, `errorText` |
| `InfoBanner(text, icon)` | Kotak keterangan | `tone`: `Info`, `Neutral`; `actionLabel` dan `onAction` |
| `TabLabel(text)` | Label tab dengan penghitung (dipakai `ScrollableTabs`) | `count`, `highlighted` |

**Organism**

| Komponen | Kegunaan | Parameter penting |
|---|---|---|
| `AppTopBar(title)` | App bar | `onNavigateBack` (`null` = tanpa tombol kembali), `actions` |
| `PrimaryTabs(tabs, selectedIndex, onSelect)` | Tab utama Peserta dan Bracket | — |
| `ScrollableTabs(tabs, selectedIndex, onSelect)` | Tab per babak yang dapat digeser | `TabItem(label, count)` |
| `BottomActionBar(primary)` | Bar aksi di bawah layar | `BarAction(label, onClick, enabled, loading, icon)`; `secondary` |
| `ConfirmationDialog(title, text, confirmLabel, onConfirm, onDismiss)` | Konfirmasi hapus, buat bracket, urungkan, reset | `icon`, `destructive`, `confirmLoading`, `dismissLabel` |
| `FormBottomSheet(title, confirmLabel, onConfirm, onDismiss) { … }` | Form singkat: kompetisi, input hasil | `subtitle`, `confirmEnabled`, `confirmLoading` |
| `FullScreenFormDialog(title, confirmLabel, onConfirm, onDismiss) { … }` | Form panjang: peserta dengan anggota | `confirmEnabled`, `confirmLoading` |
| `EmptyState(title, message, icon)` | State kosong dengan ajakan | `actionLabel`, `actionIcon`, `onAction` |
| `ErrorState(message, onRetry)` | Gagal memuat dengan tombol Coba lagi | `retryLabel` |
| `LoadingPlaceholder()` | Placeholder baris saat pertama memuat | `rowCount`, `showLeading` |

**Template**

| Komponen | Kegunaan | Parameter penting |
|---|---|---|
| `ListScreenTemplate(title) { contentPadding -> }` | Layar daftar dengan filter, pull-to-refresh, FAB, snackbar | `snackbarHostState`, `isRefreshing`, `onRefresh` (mengaktifkan pull-to-refresh), `filters`, `floatingActionButton` |
| `DetailTabsTemplate(title, tabs, selectedTabIndex, onSelectTab, onNavigateBack) { contentPadding -> }` | Layar detail dengan tab | `snackbarHostState`, `actions`, `header` |

`contentPadding` dari template berisi inset navigation bar. Pasang ke `LazyColumn(contentPadding = contentPadding)` agar item terakhir tidak tertutup. Konten tab yang memakai `BottomActionBar` tidak perlu padding ini karena bar sudah menanganinya.

### 4.5 Resep pemakaian

**Label status** (Referensi UI hal. 3 dan 5)

| Status | `text` | `tone` | `leading` | `dashed` |
|---|---|---|---|---|
| Kompetisi `OPEN` | Pendaftaran | `Primary` | `None` | – |
| Kompetisi `IN_MATCH` | Berlangsung | `Tertiary` | `Dot` | – |
| Kompetisi `COMPLETED` | Selesai | `Neutral` | `Check` | – |
| Match `READY` | Siap | `Primary` | `None` | – |
| Match `PENDING` | Menunggu | `Outline` | `None` | – |
| Match `COMPLETED` | Selesai | `Neutral` | `Check` | – |
| Bye | Bye | `Outline` | `None` | `true` |

```kotlin
StatusLabel(text = "Berlangsung", tone = StatusTone.Tertiary, leading = StatusLeading.Dot)
StatusLabel(text = "Bye", tone = StatusTone.Outline, dashed = true)
```

**Layar daftar** (hal. 7–8): memuat, gagal, kosong, dan berisi.

```kotlin
ListScreenTemplate(
    title = "Kompetisi",
    snackbarHostState = snackbarHostState,
    isRefreshing = uiState.isRefreshing,
    onRefresh = onRefresh,
    filters = {
        FilterChipRow(
            options = listOf(
                FilterOption(value = null, label = "Semua"),
                FilterOption(value = CompetitionStatus.OPEN, label = "Pendaftaran"),
                FilterOption(value = CompetitionStatus.IN_MATCH, label = "Berlangsung"),
                FilterOption(value = CompetitionStatus.COMPLETED, label = "Selesai"),
            ),
            selected = uiState.statusFilter,                  // CompetitionStatus? di UiState
            onSelect = onSelectFilter,
        )
    },
    floatingActionButton = {
        if (!uiState.competitions.isNullOrEmpty()) {          // FAB disembunyikan saat empty state
            ExtendedFloatingActionButton(
                text = { Text("Buat kompetisi") },
                icon = { Icon(imageVector = AppIcons.Add, contentDescription = null) },
                onClick = onCreate,
            )
        }
    },
) { contentPadding ->
    val competitions = uiState.competitions
    when {
        competitions == null && uiState.loadError == null -> LoadingPlaceholder()
        competitions == null -> ErrorState(
            message = "Daftar kompetisi tidak dapat dimuat. Periksa koneksi, lalu coba lagi.",
            onRetry = onRefresh,
        )
        competitions.isEmpty() -> EmptyState(
            title = "Buat kompetisi pertama",
            message = "Kompetisi menampung peserta, bracket, dan hasil pertandingan dalam satu tempat.",
            icon = AppIcons.Trophy,
            actionLabel = "Buat kompetisi",
            actionIcon = AppIcons.Add,
            onAction = onCreate,
        )
        else -> LazyColumn(contentPadding = contentPadding) {
            items(competitions, key = { it.id }) { competition ->
                AppListItem(
                    headline = competition.name,
                    supporting = "Tim, dibuat 6 Okt 2026",
                    leading = {
                        AppAvatar(icon = if (competition.participantType == ParticipantType.TEAM) AppIcons.Groups else AppIcons.Person)
                    },
                    trailing = { StatusLabel(text = "Pendaftaran", tone = StatusTone.Primary) },
                    onClick = { onOpenCompetition(competition.id) },
                )
            }
        }
    }
}
```

**Layar detail dengan tab** (hal. 2 dan 8): menu disusun dari aturan status, sehingga aksi yang tidak berlaku tidak tampil.

```kotlin
DetailTabsTemplate(
    title = competition.name,
    tabs = listOf("Peserta", "Bracket"),
    selectedTabIndex = selectedTab,
    onSelectTab = onSelectTab,
    onNavigateBack = onNavigateBack,
    snackbarHostState = snackbarHostState,
    actions = {
        OverflowMenu(
            actions = buildList {
                if (CompetitionRules.canEdit(competition.status)) add(MenuAction(label = "Ubah", onClick = onEdit))
                if (CompetitionRules.canDelete(competition.status)) {
                    add(MenuAction(label = "Hapus", onClick = onDelete, destructive = true))
                }
            },
        )
    },
    header = {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceSm),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusLabel(text = "Pendaftaran", tone = StatusTone.Primary)
            Text(text = "Tim", style = MaterialTheme.typography.bodyMedium)
        }
    },
) { contentPadding ->
    when (selectedTab) {
        0 -> { /* konten tab Peserta: modul Participant */ }
        else -> { /* konten tab Bracket: modul Match */ }
    }
}
```

**Form singkat** (hal. 7): `FormBottomSheet` untuk buat dan ubah kompetisi.

```kotlin
FormBottomSheet(
    title = "Ubah kompetisi",
    confirmLabel = "Simpan",
    onConfirm = onSave,
    onDismiss = onDismiss,
    confirmLoading = form.isSaving,
) {
    AppTextField(
        value = form.name,
        onValueChange = onNameChange,
        label = "Nama kompetisi",
        modifier = Modifier.fillMaxWidth(),
        supportingText = "Wajib diisi",
        errorText = form.nameErrorText,                       // String? dari NameError
        maxLength = CompetitionRules.NAME_MAX_LENGTH,
    )
    Spacer(Modifier.height(Dimens.SpaceLg))
    SegmentedSelector(
        options = listOf(
            SegmentOption(value = ParticipantType.INDIVIDUAL, label = "Individu"),
            SegmentOption(value = ParticipantType.TEAM, label = "Tim"),
        ),
        selected = form.participantType,                      // null pada mode buat
        onSelect = onParticipantTypeChange,
        enabled = !form.participantTypeLocked,
    )
    if (form.participantTypeLocked) {
        HintText(text = "Terkunci karena kompetisi sudah memiliki peserta", icon = AppIcons.Lock)
    }
}
```

**Form panjang** (hal. 9): `FullScreenFormDialog` untuk peserta dengan anggota dinamis.

```kotlin
FullScreenFormDialog(title = "Tambah tim", confirmLabel = "Simpan", onConfirm = onSave, onDismiss = onDismiss) {
    AppTextField(
        value = form.name,
        onValueChange = onNameChange,
        label = "Nama tim",
        modifier = Modifier.fillMaxWidth(),
        errorText = form.nameErrorText,                       // mis. "Nama ini sudah dipakai di kompetisi ini."
    )
    form.members.forEachIndexed { index, member ->
        RemovableTextField(
            value = member,
            onValueChange = { onMemberChange(index, it) },
            label = "Anggota ${index + 1}",
            onRemove = { onRemoveMember(index) },
            removeContentDescription = "Hapus anggota ${index + 1}",
            placeholder = "Nama anggota",
        )
    }
    AppButton(text = "Tambah anggota", onClick = onAddMember, variant = AppButtonVariant.Text, leadingIcon = AppIcons.Add)
}
```

**Konfirmasi** (hal. 8, 10, 12, 14): teks menyebut akibat aksi; aksi permanen memakai `destructive = true`.

```kotlin
ConfirmationDialog(
    title = "Hapus kompetisi?",
    text = "Turnamen Catur Antar Kelas beserta seluruh pesertanya akan dihapus permanen.",
    confirmLabel = "Hapus",
    onConfirm = onConfirmDelete,
    onDismiss = onDismiss,
    icon = AppIcons.Delete,
    destructive = true,
    confirmLoading = uiState.isDeleting,
)
```

**Tab Peserta** (hal. 9–10): ringkasan, banner terkunci, dan bar aksi.

```kotlin
InfoBanner(text = "6 tim: 3 babak, 5 pertandingan, 1 bye di Semifinal.", icon = AppIcons.Info)
InfoBanner(
    text = "Peserta terkunci karena bracket sudah dibuat. Reset bracket dari menu untuk mengubah peserta.",
    icon = AppIcons.Lock,
    tone = BannerTone.Neutral,
    actionLabel = "Lihat bracket",
    onAction = onOpenBracketTab,
)
BottomActionBar(
    primary = BarAction(label = "Buat bracket", onClick = onGenerate, enabled = canGenerate),
    secondary = BarAction(label = "Tambah tim", onClick = onAdd, icon = AppIcons.Add),
)
```

**Input hasil** (hal. 11): skor opsional, pemenang mengikuti skor bila tidak seri.

```kotlin
FormBottomSheet(
    title = "Hasil Match 3",
    subtitle = "Babak 1",
    confirmLabel = "Simpan hasil",
    onConfirm = onSubmit,
    onDismiss = onDismiss,
    confirmEnabled = MatchResultRules.validateResult(form.score1, form.score2, form.winner) == null,
    confirmLoading = form.isSaving,
) {
    ScoreStepper(label = "Tim Kenari", value = form.score1, onDecrement = onScore1Down, onIncrement = onScore1Up)
    ScoreStepper(label = "Tim Cendrawasih", value = form.score2, onDecrement = onScore2Down, onIncrement = onScore2Up)
    if (form.score1 != null && form.score1 == form.score2) {
        HintText(text = "Skor seri. Pilih pemenang secara manual.", icon = AppIcons.Info, tone = HintTone.Error)
    }
    Column(Modifier.selectableGroup()) {
        RadioOptionRow(label = "Tim Kenari", selected = form.winner == MatchSlot.ONE, onClick = { onSelectWinner(MatchSlot.ONE) })
        RadioOptionRow(label = "Tim Cendrawasih", selected = form.winner == MatchSlot.TWO, onClick = { onSelectWinner(MatchSlot.TWO) })
    }
}
```

**Tab per babak** (hal. 11): label dan penghitung dari `BracketTopology`.

```kotlin
ScrollableTabs(
    tabs = (1..topology.roundCount).map { round ->
        val progress = topology.roundProgress(round)
        TabItem(label = roundLabel(topology.roundName(round)), count = "${progress.completed}/${progress.total}")
    },
    selectedIndex = selectedRound - 1,
    onSelect = { index -> onSelectRound(index + 1) },
)
```

`roundLabel` mengubah `RoundName` menjadi "Babak n", "Semifinal", atau "Final"; fungsi ini termasuk isi `ui/shared` (bagian 5).

**Kartu match dan kartu Juara** (hal. 11–14) adalah tampilan khusus modul Match, sehingga dibuat di `ui/feature/match/view`, bukan di `ui/component`. Susun dari `OutlinedCard`, `ScoreText`, `StatusLabel`, dan `AppButton`:

- Kartu Siap memakai garis tepi `MaterialTheme.colorScheme.primary`, dengan tombol `AppButton(variant = AppButtonVariant.Tonal)` "Input hasil".
- Pemenang ditandai teks tebal (`ScoreText(emphasized = true)`) dan ikon `AppIcons.CheckCircle`.
- Kartu bye memakai `Modifier.dashedBorder(color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium)` dan label "Lolos otomatis".
- Kartu Juara memakai `MaterialTheme.extendedColors.championContainer` dan ikon `AppIcons.Trophy`.

### 4.6 Preview

```kotlin
@ComponentPreviews                       // tiga preview: terang, gelap, font 200%
@Composable
private fun CompetitionRowPreview() {
    PreviewSurface {                     // theme aplikasi dan latar sesuai mode
        AppListItem(headline = "Futsal Cup Fakultas", supporting = "Tim, dibuat 6 Okt 2026")
    }
}
```

Untuk preview layar, bungkus `…Content` dengan `MiniAppCompetitionTheme { }` dan isi UiState dengan data contoh.

### 4.7 Membuat komponen baru

1. Tempatkan di `ui/component` hanya bila dipakai lebih dari satu fitur atau bersifat umum. Tampilan khusus satu fitur, seperti kartu match, tetap di `ui/feature/<fitur>/view`.
2. Pilih lapisan yang sesuai (atom, molecule, organism, atau template) dan beri nama `App<Nama>` bila membungkus satu komponen Material 3.
3. Buat stateless: parameter wajib dulu, lalu `modifier: Modifier = Modifier`, lalu parameter opsional.
4. Teks default (mis. "Batal") diambil dari `strings.xml`. Teks lain diterima dari parameter.
5. Tulis KDoc singkat yang menyebut halaman Referensi UI, lalu tambahkan preview `@ComponentPreviews`.
6. `ui/component` adalah file bersama; informasikan komponen baru ke tim.

---

## 5. Shared UI (`ui/shared`)

`ui/shared` berisi **helper UI lintas fitur yang bukan komponen visual**. Bedanya dengan `ui/component`:

| | `ui/component` | `ui/shared` |
|---|---|---|
| Isi | Composable visual yang dipakai ulang | Pemetaan data ke tampilan dan teks, dipakai lebih dari satu fitur |
| Mengenal data domain | Tidak | Ya (`data/model`, `data/rule`) |
| Contoh | `StatusLabel`, `FormBottomSheet` | `AppError` ke teks pesan, `CompetitionStatus` ke parameter `StatusLabel` |

**Status: folder ini belum berisi kode.** Isi yang direncanakan:

| Helper | Sumber aturan |
|---|---|
| `AppError` dan `ApiErrorCode` ke teks pesan dan jenis tampilannya (snackbar, inline, error state) | Referensi UI hal. 15 dan [bagian 6](#6-penanganan-error) |
| `CompetitionStatus` dan status match ke parameter `StatusLabel` | [Tabel label status](#45-resep-pemakaian) |
| `RoundName` ke teks "Babak n", "Semifinal", "Final" | Referensi UI hal. 4 |
| Asal slot kosong ke teks "Pemenang Babak 1 Match 1" atau "Pemenang Semifinal 1" | Referensi UI hal. 4 dan 12 |
| `ParticipantType` ke "Individu" atau "Tim" beserta ikonnya | Referensi UI hal. 6–7 |
| Format tanggal `createdAt`, mis. "dibuat 6 Okt 2026" | Referensi UI hal. 7 |

Aturan `ui/shared`:

1. Berisi fungsi murni atau fungsi `@Composable` yang membaca `stringResource`; tidak memanggil repository atau ViewModel.
2. Setiap pemetaan hanya ada satu, dipakai semua fitur, agar pesan dan label konsisten.
3. Bila Anda membutuhkan helper di atas sebelum tersedia, tambahkan ke `ui/shared` (bukan ke folder fitur) dan informasikan ke tim agar tidak terjadi duplikasi.

---

## 6. Penanganan error

Alur: repository mengembalikan `AppResult.Failure(error)`, ViewModel menyimpannya di UiState, lalu View menampilkan pesan hasil pemetaan `ui/shared`. Teks dipetakan dari `code`, bukan dari `message` backend.

| Kode / `AppError` | HTTP | Kapan muncul | Tampilan dan pesan (Referensi UI hal. 15) |
|---|---|---|---|
| `VALIDATION_ERROR` | 400 | Body tidak valid | Dicegah validasi client. Bila tetap muncul: snackbar "Periksa kembali isian yang ditandai." |
| `DUPLICATE_PARTICIPANT_NAME` | 409 | Nama peserta sudah ada | Inline di field nama: "Nama ini sudah dipakai di kompetisi ini." |
| `PARTICIPANT_TYPE_LOCKED` | 409 | Jenis peserta diubah saat sudah ada peserta | Dicegah UI (segmen dikunci). Fallback inline di bawah segmen |
| `MEMBERS_NOT_ALLOWED` | 400 | Anggota dikirim untuk kompetisi Individu | Dicegah UI. Fallback snackbar "Data peserta tidak valid." |
| `COMPETITION_NOT_OPEN` | 409 | Aksi pendaftaran saat bracket sudah dibuat | Snackbar "Kompetisi sudah berjalan. Data dimuat ulang." lalu muat ulang |
| `COMPETITION_NOT_IN_MATCH` | 409 | Input hasil atau reset saat kompetisi tidak berlangsung | Snackbar "Bracket sudah tidak aktif. Data dimuat ulang." lalu muat ulang |
| `INVALID_PARTICIPANT_COUNT` | 409 | Buat bracket dengan kurang dari 2 atau lebih dari 16 peserta | Dicegah UI. Fallback snackbar "Bracket membutuhkan 2–16 peserta." |
| `MATCH_NOT_READY` | 409 | Input hasil pada match yang belum siap atau sudah memiliki hasil | Tutup sheet, snackbar "Pertandingan ini belum siap atau sudah memiliki hasil.", muat ulang |
| `INVALID_WINNER` | 400 | `winnerId` bukan salah satu peserta match | Dicegah UI karena pemenang dipilih dari dua peserta match |
| `MATCH_UNDO_NOT_ALLOWED` | 409 | Urungkan bye, match yang belum selesai, atau match yang berikutnya sudah selesai | Snackbar "Hasil tidak dapat diurungkan karena pertandingan berikutnya sudah selesai." |
| `COMPETITION_NOT_FOUND`, `PARTICIPANT_NOT_FOUND`, `MATCH_NOT_FOUND` | 404 | Data sudah dihapus atau ID salah | Snackbar "Data tidak ditemukan, mungkin sudah dihapus." lalu kembali ke layar sebelumnya |
| `DATA_INTEGRITY_VIOLATION` | 409 | Bentrok constraint database | Snackbar "Data bentrok dengan perubahan lain. Data dimuat ulang." |
| `HTTP_ERROR` | lainnya | Error HTTP selain 400 dari Spring MVC | Snackbar "Permintaan tidak dapat diproses." |
| `INTERNAL_SERVER_ERROR` | 500 | Exception tidak tertangani di server | Snackbar "Terjadi kendala di server." dengan aksi Coba lagi |
| `AppError.Network` | – | Offline atau timeout | "Tidak dapat terhubung ke server. Periksa koneksi." dengan aksi Coba lagi |

Sebagian besar error 409 berarti data di layar sudah basi. Pola umumnya: tampilkan snackbar singkat, lalu muat ulang detail kompetisi, peserta, dan match.

`AppError.Http`, `AppError.InvalidResponse`, dan `ApiErrorCode.UNKNOWN` belum diatur di Referensi UI; pesannya ditetapkan saat `ui/shared` dibuat.

---

## 7. Aturan per status

Tabel Referensi UI hal. 3 beserta fungsi yang menerapkannya. Tanda (*) berarti aktif setelah modulnya dibuka.

| Aksi | Pendaftaran (`OPEN`) | Berlangsung (`IN_MATCH`) | Selesai (`COMPLETED`) | Fungsi |
|---|---|---|---|---|
| Ubah kompetisi | Ya | Tidak | Tidak | `CompetitionRules.canEdit` |
| Hapus kompetisi | Ya | Tidak | Tidak | `CompetitionRules.canDelete` |
| Tambah, ubah, hapus peserta | Ya | Tidak | Tidak | `CompetitionRules.canManageParticipants`* |
| Buat bracket | Ya, bila 2–16 peserta | Tidak | Tidak | `CompetitionRules.canGenerateBracket`* |
| Input hasil | Tidak | Match berstatus `READY` | Tidak | `MatchResultRules.canSubmitResult`* |
| Urungkan hasil | Tidak | Match selesai, bukan bye, dan match berikutnya belum selesai | Hanya match final | `MatchResultRules.canUndo`* |
| Reset bracket | Tidak | Ya | Tidak | `CompetitionRules.canResetBracket`* |

| Status match | Tampilan kartu | Aksi |
|---|---|---|
| `PENDING` | Label "Menunggu"; slot kosong diisi asal pemenang | Tidak dapat diketuk |
| `READY` | Label "Siap"; garis tepi primary | Input hasil |
| `COMPLETED` | Skor dan pemenang (tebal dan ikon centang) | Urungkan, sesuai aturan di atas |
| Bye | Satu slot kosong; kartu bergaris putus dengan label "Lolos otomatis" | Tanpa aksi |

Status kompetisi dibaca dari satu sumber (`Competition.status` di `CompetitionRepository`) untuk label di daftar, label di detail, dan tombol di setiap tab.

---

## 8. Routing

**Status: belum dibuat.** Rencana sesuai keputusan desain:

- Navigation Compose 2.10 dengan route type-safe: setiap route berupa `@Serializable` object atau data class.
- Route berada di `routing/AppRoute.kt` dan `NavHost` di `routing/AppRouting.kt`, dipanggil dari `MainActivity`.
- Destinasi layar mengikuti Referensi UI hal. 2: **Daftar kompetisi** (layar awal) dan **Detail kompetisi** (tab Peserta dan Bracket). Layar bracket penuh tidak digunakan.
- Sheet dan dialog (buat/ubah kompetisi, form peserta, input hasil, konfirmasi) adalah overlay di layar asal, bukan route.
- ViewModel layar detail membaca argumen route lewat `SavedStateHandle` (`createSavedStateHandle()` di factory, lalu `toRoute<…>()`).

---

## 9. Test

```powershell
.\gradlew :app:testDebugUnitTest                                                          # seluruh unit test
.\gradlew :app:testDebugUnitTest --tests "ac.sfj.miniappcompetition.data.rule.CompetitionRulesTest"   # satu kelas
```

Laporan HTML ada di `app/build/reports/tests/testDebugUnitTest/index.html`.

| Lokasi | Isi |
|---|---|
| `data/rule/*Test.kt` | Aturan diuji dengan data dari tabel Referensi UI hal. 3 dan 4 |
| `data/rule/BracketSimulator.kt`* | Port `BracketPlanner` dan alur submit/undo `MatchService` backend. Aturan bracket di aplikasi dibandingkan dengan simulasi ini untuk 2–16 peserta. Perbarui bila logika backend berubah |
| `data/network/SafeApiCallTest.kt` | Pemetaan exception ke `AppError` |
| `data/repository/*Test.kt` | Repository dengan fake service dari `FakeApiServices.kt` |

Pola test:

- Fungsi suspend diuji dengan `runTest { }` dari `kotlinx-coroutines-test`.
- Repository diuji dengan fake `…ApiService` yang menyimpan data di memori. Error backend dibuat dengan `apiError(status, code)`, dan error berikutnya diantrekan dengan `failNext(error)`.
- ViewModel diuji dengan fake repository yang mengimplementasikan interface repository. Karena `viewModelScope` memakai `Dispatchers.Main`, panggil `Dispatchers.setMain(StandardTestDispatcher())` sebelum test dan `Dispatchers.resetMain()` sesudahnya.

---

## 10. Masalah umum

| Gejala | Penyebab dan solusi |
|---|---|
| `Unresolved reference` ke kelas Participant atau Match setelah membuka komentar | Masih ada bagian modul yang dikomentari. Periksa daftar lokasi di [README bagian 5](README.md#5-status-modul-dan-cara-mengaktifkan) |
| Request mendapat 404 padahal endpoint benar | Path di `…ApiService` diawali `/` sehingga prefix `/develop/` hilang. Tulis path relatif |
| Hasil `AppError.InvalidResponse` | Respons tidak cocok dengan DTO, misalnya status baru dari backend. Bandingkan DTO dengan kontrak API backend |
| Hasil `AppError.Network` di emulator | Emulator tidak terhubung ke internet. Buka `https://api.vispro.satryo.pro/develop/system/status` di browser emulator |
| Perubahan `libs.versions.toml` belum terbaca | Jalankan **Sync Project with Gradle Files** |
| Preview gagal dirender | Build project dulu, lalu pastikan preview dibungkus `PreviewSurface` atau `MiniAppCompetitionTheme` |
| Ingin melihat request dan response | Jalankan build debug, lalu cari baris `--> GET …` dan `<-- 200 …` di Logcat |
