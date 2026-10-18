# AFL3 — Mini Competition Manager (Android)

Aplikasi Android **Mini Competition Manager** untuk tugas AFL3 Visual Programming. Panitia dapat membuat kompetisi *single-elimination*, mendaftarkan peserta, membuat bracket, mencatat hasil pertandingan, dan melihat juara. Seluruh data diambil dari backend [`afl3-miniapp`](https://github.com/satryo-1076012614901/afl3-miniapp).

Dokumen ini adalah panduan kerja tim: konsep desain, struktur folder, aturan penempatan kode, status modul, dan langkah pengerjaan. Referensi teknis yang lebih rinci (data layer, pola ViewModel, katalog komponen UI, `ui/shared`, penanganan error, dan test) ada di [`HELP.md`](HELP.md).

Acuan tampilan adalah dokumen **Mini Competition Manager — Referensi UI Android** (selanjutnya disebut *Referensi UI*). Nomor halaman di dokumen ini mengacu ke dokumen tersebut. Halaman Match (3/4), yaitu layar bracket penuh, **tidak digunakan**.

## Pembagian modul

| Modul | Anggota | Layar (Referensi UI) | Status data layer |
|---|---|---|---|
| Competition | Satryo Nugroho | Halaman Kompetisi (hal. 7–8): daftar, detail, form, dan hapus kompetisi | Aktif |
| Participant | Jessy Rosinta | Halaman Peserta (hal. 9–10): tab Peserta dan form peserta | Aktif |
| Match | Fariz | Halaman Match (hal. 11, 12, 14): tab Bracket, input dan urungkan hasil, reset bracket | Aktif |

Data layer Participant dan Match sudah aktif. Tab Peserta dan Bracket terhubung ke detail kompetisi. Layar bracket penuh pada Referensi UI halaman 13 dikeluarkan dari scope Android dan akan memakai UI web; tautan web belum ditambahkan.

---

## 1. Menjalankan aplikasi

Prasyarat: Android Studio versi terbaru (project memakai Android Gradle Plugin 9.4.1), serta emulator atau perangkat **Android 13 (API 33)** ke atas yang terhubung ke internet.

```sh
git clone https://github.com/satryo-1076012614901/mini-app-competition.git
```

1. Buka folder project di Android Studio dan tunggu Gradle sync selesai.
2. Pilih konfigurasi `app`, lalu **Run**. Build debug memakai backend `/develop/`.
3. Sebelum commit, jalankan unit test dari Terminal Android Studio:

```powershell
.\gradlew :app:testDebugUnitTest
```

Di macOS atau Linux, ganti `.\gradlew` dengan `./gradlew`.

| Build type | Base URL (`BuildConfig.API_BASE_URL`) | Keterangan |
|---|---|---|
| `debug` | `https://api.vispro.satryo.pro/develop/` | Data bersama untuk pengembangan; request dan response HTTP tercatat di Logcat |
| `release` | `https://api.vispro.satryo.pro/production/` | Data production; optimisasi R8 belum diaktifkan |

---

## 2. Konsep desain

Arsitektur mengikuti diagram **MVVM** dari dosen, digabung dengan **hybrid Atomic Design + feature-based UI**.

```plain
MainActivity
  └─ routing/            NavHost Navigation Compose (route type-safe)
       └─ View           ui/feature/<fitur>/view/        Composable layar
            │  ▲
     aksi   │  │  UiState (StateFlow)
            ▼  │
          ViewModel      ui/feature/<fitur>/viewmodel/   state layar dan aksi pengguna
            │
            ▼
          Repository     data/repository/   dibuat oleh Container (data/container/)
            │            cache StateFlow, hasil berupa AppResult<model>
            ▼
          Service        data/service/      interface Retrofit (JSON di data/dto/, klien di data/network/)
            │
            ▼
          Backend afl3-miniapp (/develop atau /production)

Model = data/model/ (data domain) + data/rule/ (aturan bisnis), dipakai ViewModel dan View.
UI bersama: ui/component/ (atomic design), ui/shared/ (helper UI lintas fitur), ui/theme/.
```

Keputusan desain yang sudah disepakati:

1. **MVVM sesuai diagram dosen**: MainActivity → routing → View ↔ ViewModel → Model. Data layer terdiri dari Container, Repository, dan Service yang memanggil API.
2. **Hybrid Atomic Design + feature-based UI**: komponen yang dipakai ulang berada di `ui/component` (foundation, atom, molecule, organism, template); layar dan ViewModel berada di `ui/feature/<fitur>`.
3. **Data layer dipisah per peran, bukan per fitur**: `model`, `rule`, `dto`, `service`, `network`, `repository`, `container`.
4. **Model adalah data dan logika bisnis yang dipakai View**: data domain di `data/model`, aturan bisnis di `data/rule` sebagai Kotlin murni yang diuji unit.
5. **Tiga container** (Competition, Participant, Match) berbagi satu `ApiClient` dan dibuat di `MiniCompetitionApplication` (manual dependency injection).
6. **Repository adalah satu sumber kebenaran**: data disimpan di cache `StateFlow`; layar mengamati `Flow` dari repository dan memanggil fungsi `refresh…` untuk memuat ulang.
7. **Routing** memakai Navigation Compose type-safe.
8. **Tampilan** memakai Jetpack Compose dan Material 3. Palet dihasilkan dari seed `#1B6B4F` dengan tertiary `#FFDBCC` dan warna kustom Juara `#FFDF9E`; dynamic color dimatikan agar warna tidak berubah mengikuti wallpaper. Ikon memakai Material Symbols sebagai `ImageVector` Kotlin, tanpa vector drawable XML dan tanpa library ikon.
9. **Tombol dan menu mengikuti aturan status** di `data/rule`, bukan dicoba lalu menunggu ditolak backend (Referensi UI hal. 3).

---

## 3. Struktur folder

```plain
MiniAppCompetition/
├── README.md                     # Panduan kerja tim (dokumen ini)
├── HELP.md                       # Referensi teknis: data layer, ViewModel, komponen UI, error, test
├── gradle/libs.versions.toml     # Versi dependency (file bersama)
├── app/build.gradle.kts          # Konfigurasi build dan base URL per build type (file bersama)
└── app/src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── res/values/strings.xml          # Seluruh teks yang tampil ke pengguna
    │   └── java/ac/sfj/miniappcompetition/
    │       ├── MainActivity.kt              # Memasang theme, lalu memanggil routing
    │       ├── MiniCompetitionApplication.kt  # Membuat ApiClient dan ketiga container
    │       ├── routing/                     # AppRoute (daftar route) dan AppRouting (NavHost)
    │       ├── data/
    │       │   ├── model/        # Competition, Participant, Match, AppResult, AppError
    │       │   ├── rule/         # CompetitionRules, ParticipantRules, BracketShape, BracketTopology, ...
    │       │   ├── dto/          # Bentuk JSON API dan mapper toModel()
    │       │   ├── service/      # <Modul>ApiService (Retrofit)
    │       │   ├── network/      # ApiClient, NetworkJson, safeApiCall
    │       │   ├── repository/   # <Modul>Repository + Network<Modul>Repository
    │       │   └── container/    # <Modul>Container + Default<Modul>Container
    │       └── ui/
    │           ├── theme/        # Warna, tipografi, Dimens, warna Juara
    │           ├── component/    # Komponen atomic design yang dipakai ulang
    │           │   ├── foundation/  # AppIcons, dashedBorder, ComponentPreviews
    │           │   ├── atom/        # AppButton, AppTextField, AppAvatar, ScoreText, ...
    │           │   ├── molecule/    # StatusLabel, AppListItem, ScoreStepper, InfoBanner, ...
    │           │   ├── organism/    # AppTopBar, FormBottomSheet, ConfirmationDialog, EmptyState, ...
    │           │   └── template/    # ListScreenTemplate, DetailTabsTemplate
    │           ├── shared/       # Helper UI lintas fitur: pesan error, label status, format tanggal
    │           └── feature/
    │               ├── competition/ {view, viewmodel}   # Daftar, form, hapus, dan detail kompetisi
    │               ├── participant/ {view, viewmodel}
    │               └── match/       {view, viewmodel}
    └── test/java/ac/sfj/miniappcompetition/
        ├── data/{rule, network, repository}   # Unit test data layer
        ├── ui/shared/                         # Unit test pemetaan error dan format tanggal
        └── ui/feature/competition/viewmodel/  # Unit test ViewModel Competition
```

Folder yang masih kosong berisi `.gitkeep` agar tetap ter-commit. Hapus `.gitkeep` setelah folder berisi file Kotlin.

---

## 4. Aturan penempatan kode

| Layer | Folder | Isi | Boleh memakai | Tidak boleh |
|---|---|---|---|---|
| View | `ui/feature/<fitur>/view` | Composable layar dan bagian layar khusus fitur (mis. kartu match) | ViewModel fiturnya, `ui/component`, `ui/shared`, `ui/theme`, `data/model`, `data/rule` | Memanggil repository atau service secara langsung |
| ViewModel | `ui/feature/<fitur>/viewmodel` | UiState, state form, aksi pengguna, factory | `data/repository`, `data/model`, `data/rule` | Memakai Composable atau komponen UI, menyimpan `Context` |
| Model | `data/model` | Data class dan enum domain, `AppResult`, `AppError` | — | Bergantung pada Android, Retrofit, atau Compose |
| Rule | `data/rule` | Aturan bisnis murni: status, bracket, validasi isian | `data/model` | Bergantung pada Android, Retrofit, atau Compose |
| DTO | `data/dto` | Bentuk JSON request/response dan mapper `toModel()` | `data/model` | Logika selain pemetaan |
| Service | `data/service` | Interface Retrofit `<Modul>ApiService` | `data/dto` | Logika apa pun |
| Network | `data/network` | `ApiClient`, `NetworkJson`, `safeApiCall` | `data/model`, `data/dto` | Kode khusus satu fitur |
| Repository | `data/repository` | Interface repository, implementasi `Network…`, cache | `data/service`, `data/dto`, `data/network`, `data/model` | Mengembalikan DTO atau melempar exception jaringan ke ViewModel |
| Container | `data/container` | Membuat repository | `data/network`, `data/service`, `data/repository` | Logika bisnis |
| Routing | `routing` | NavHost dan daftar route | View dari setiap fitur | Logika bisnis |
| Component | `ui/component` | Komponen visual yang dipakai ulang | `ui/theme`, sesama komponen | Mengenal `data/` atau ViewModel |
| Shared UI | `ui/shared` | Helper UI lintas fitur: teks error, label status, format tanggal | `data/model`, `data/rule`, `ui/component`, `ui/theme` | Memanggil repository atau ViewModel |
| Theme | `ui/theme` | Warna, tipografi, `Dimens` | — | — |

Aturan tambahan:

1. **Komponen tidak mengenal data domain.** Komponen di `ui/component` hanya menerima teks, angka, enum tampilan, dan ikon. Pemetaan model (mis. `Competition.status`) ke parameter komponen (mis. `StatusLabel(text, tone)`) dilakukan di layar fitur, atau di `ui/shared` bila pemetaannya dipakai bersama (mis. `CompetitionStatusLabel`).
2. **Teks untuk pengguna ada di `res/values/strings.xml`** dan diambil dengan `stringResource(...)`.
3. **Ketersediaan tombol berasal dari `data/rule`.** Aksi yang tidak berlaku karena status kompetisi disembunyikan, bukan dinonaktifkan satu per satu (Referensi UI hal. 10). Aksi yang masih menunggu syarat lain, misalnya jumlah peserta untuk Buat bracket, dinonaktifkan dan disertai teks bantuan (hal. 9).
4. **Setelah aksi match berhasil**, ViewModel memuat ulang detail kompetisi dengan `CompetitionRepository.refreshCompetition`, karena status kompetisi dapat berubah (Referensi UI hal. 2).
5. **Pesan error dipetakan dari `AppError` dan `ApiErrorCode`** dengan `toErrorMessage()` di `ui/shared`, bukan dari `message` backend yang bahasanya bercampur (Referensi UI hal. 15). Detail di [`HELP.md`](HELP.md#6-penanganan-error).
6. **File bersama** dipakai semua modul. Perubahan pada file berikut didiskusikan dulu dengan tim: `gradle/libs.versions.toml`, `app/build.gradle.kts`, `AndroidManifest.xml`, `MiniCompetitionApplication.kt`, `data/model/AppError.kt`, `data/rule/CompetitionRules.kt`, `data/network/*`, `ui/theme/*`, `ui/component/*`, `ui/shared/*`, `routing/*`, dan `res/values/strings.xml`.

### Konvensi penamaan

| Jenis | Pola | Contoh |
|---|---|---|
| Model | `<Nama>` | `Competition`, `Match` |
| Aturan | `<Modul>Rules` atau nama konsep | `CompetitionRules`, `BracketShape` |
| DTO | `<Nama>RequestDto`, `<Nama>ResponseDto` di `dto/<Modul>Dto.kt` | `CompetitionResponseDto` |
| Service | `<Modul>ApiService` | `CompetitionApiService` |
| Repository | Interface `<Modul>Repository`, implementasi `Network<Modul>Repository` | `NetworkCompetitionRepository` |
| Container | Interface `<Modul>Container`, implementasi `Default<Modul>Container` | `DefaultMatchContainer` |
| Komponen | `App<Nama>` untuk pembungkus komponen Material 3, nama deskriptif untuk lainnya | `AppButton`, `StatusLabel` |
| Layar | `<Nama>Screen` | `CompetitionListScreen` |
| ViewModel dan state | `<Nama>ViewModel`, `<Nama>UiState` | `CompetitionListViewModel` |
| Test | `<KelasYangDiuji>Test` | `CompetitionRulesTest` |

---

## 5. Status modul dan cara mengaktifkan

| Bagian | Status |
|---|---|
| Theme dan `ui/component` | Selesai, dipakai semua modul |
| Data layer Competition | Aktif, termasuk unit test |
| Data layer Participant dan Match | Aktif |
| `routing/` dan `MainActivity` | Aktif: route Daftar kompetisi (layar awal) dan Detail kompetisi |
| `ui/shared/` | Aktif untuk Competition dan bagian bersama; bagian Participant dan Match ditambahkan pemilik modul |
| `ui/feature/competition` | Aktif: daftar dengan filter status, buat, ubah, hapus, dan detail kompetisi, termasuk unit test ViewModel. Tab Peserta dan Bracket sudah terhubung |
| `ui/feature/participant/*`, `ui/feature/match/*` | Aktif: CRUD peserta dan pengelolaan hasil per babak |

### Peta aktivasi modul

Participant dan Match sudah diaktifkan, termasuk bagian bersama dan test. Tabel berikut menjadi peta lokasi kode; tidak perlu membuka komentar lagi.

### Modul Participant

| Jenis | Lokasi |
|---|---|
| File penuh | `data/model/Participant.kt`, `data/rule/ParticipantRules.kt`, `data/dto/ParticipantDto.kt`, `data/service/ParticipantApiService.kt`, `data/repository/ParticipantRepository.kt`, `data/container/ParticipantContainer.kt` |
| Baris di file bersama | `data/model/AppError.kt`: `MEMBERS_NOT_ALLOWED`, `PARTICIPANT_NOT_FOUND`, `DUPLICATE_PARTICIPANT_NAME`<br>`data/rule/CompetitionRules.kt`: `canManageParticipants`<br>`MiniCompetitionApplication.kt`: import, properti, dan pembuatan `participantContainer` |
| Test | File penuh `ParticipantRulesTest.kt` dan `NetworkParticipantRepositoryTest.kt`; di `FakeApiServices.kt`: import participant, `participantDto`, `FakeParticipantApiService`; di `CompetitionRulesTest.kt`: assertion `canManageParticipants` |
| Ditambahkan sendiri | `ui/shared/ErrorMessage.kt`: pesan untuk `MEMBERS_NOT_ALLOWED`, `PARTICIPANT_NOT_FOUND`, dan `DUPLICATE_PARTICIPANT_NAME`, teksnya di `strings.xml` ([HELP bagian 5 dan 6](HELP.md#5-shared-ui-uishared)) |

### Modul Match

| Jenis | Lokasi |
|---|---|
| File penuh | `data/model/Match.kt`, `data/rule/RoundName.kt`, `data/rule/BracketShape.kt`, `data/rule/BracketTopology.kt`, `data/rule/MatchResultRules.kt`, `data/dto/MatchDto.kt`, `data/service/MatchApiService.kt`, `data/repository/MatchRepository.kt`, `data/container/MatchContainer.kt` |
| Baris di file bersama | `data/model/AppError.kt`: `INVALID_WINNER`, `MATCH_NOT_FOUND`, `COMPETITION_NOT_IN_MATCH`, `INVALID_PARTICIPANT_COUNT`, `MATCH_NOT_READY`, `MATCH_UNDO_NOT_ALLOWED`<br>`data/rule/CompetitionRules.kt`: `canGenerateBracket`, `canResetBracket`<br>`MiniCompetitionApplication.kt`: import, properti, dan pembuatan `matchContainer` |
| Test | File penuh `BracketSimulator.kt`, `BracketShapeTest.kt`, `BracketTopologyTest.kt`, `MatchResultRulesTest.kt`, `NetworkMatchRepositoryTest.kt`; di `FakeApiServices.kt`: import match, `matchDto`, `FakeMatchApiService`; di `CompetitionRulesTest.kt`: import `IN_MATCH` dan `COMPLETED`, test `generateBracket_…` dan `resetBracket_…` |
| Ditambahkan sendiri | `ui/shared/ErrorMessage.kt`: pesan untuk keenam kode error Match di atas; `ui/shared/MatchDisplay.kt`: label status match, nama babak, dan asal slot kosong; teksnya di `strings.xml` ([HELP bagian 5 dan 6](HELP.md#5-shared-ui-uishared)) |

### Ketergantungan antarmodul

Beberapa layar memakai kode modul lain, sehingga urutan pengerjaan perlu dikoordinasikan:

| Layar | Modul pemilik | Memakai dari modul lain |
|---|---|---|
| Form ubah kompetisi: jenis peserta terkunci bila sudah ada peserta (hal. 7) | Competition | Jumlah peserta dari `ParticipantRepository`, diteruskan ke `CompetitionFormState.edit(competition, participantCount)` di `CompetitionDetailViewModel.openEditForm`. Sebelum modul Participant aktif, kunci baru diterapkan setelah backend menolak perubahan dengan `PARTICIPANT_TYPE_LOCKED`. |
| Tab Peserta dan tab Bracket (hal. 2) | Participant, Match | Slot `participantContent` dan `bracketContent` di `CompetitionDetailScreen` milik modul Competition. Pola integrasi ada di [HELP bagian 8](HELP.md#menyambungkan-tab-peserta-dan-bracket) |
| Tab Peserta: ringkasan bracket dan tombol Buat bracket (hal. 9–10) | Participant | `BracketShape`, `CompetitionRules.canGenerateBracket`, dan `MatchRepository.generateBracket` dari modul Match |
| Kartu match: nama peserta (hal. 11) | Match | `ParticipantRepository`, karena `MatchResponse` hanya berisi ID peserta |
| Setiap aksi match (hal. 2) | Match | `CompetitionRepository.refreshCompetition` dari modul Competition |
| Menu Reset bracket di detail kompetisi (hal. 14) | Match | Layar detail milik modul Competition |

---

## 6. Langkah kerja

1. Baca halaman Referensi UI untuk modul Anda, lalu [`HELP.md`](HELP.md).
2. Baca peta data layer modul Anda ([bagian 5](#5-status-modul-dan-cara-mengaktifkan)), lalu jalankan build dan unit test.
3. Buat ViewModel di `ui/feature/<fitur>/viewmodel` dengan pola di [HELP.md bagian 3](HELP.md#3-viewmodel-dan-uistate).
4. Buat layar di `ui/feature/<fitur>/view` memakai komponen dari `ui/component` ([HELP.md bagian 4](HELP.md#4-komponen-ui-atomic-design)).
5. Tambahkan teks baru ke `res/values/strings.xml`.
6. Tambahkan unit test untuk aturan atau logika ViewModel yang baru.
7. Periksa checklist berikut, lalu commit perubahan modul Anda.

### Checklist sebelum commit

- [ ] Aplikasi dapat di-build dan `.\gradlew :app:testDebugUnitTest` lulus.
- [ ] Preview komponen atau layar baru diperiksa di tema terang, tema gelap, dan font 200%.
- [ ] Tidak ada teks UI yang ditulis langsung di kode layar.
- [ ] Ikon tanpa label memiliki `contentDescription`.
- [ ] Status dan pemenang tidak hanya dibedakan dengan warna, tetapi juga dengan teks atau ikon.
- [ ] Ketersediaan tombol mengikuti fungsi di `data/rule`.
- [ ] View tidak memanggil repository atau service secara langsung.
- [ ] Perubahan pada file bersama sudah disepakati tim.

---

## 7. Referensi

- Referensi UI: *Mini Competition Manager — Referensi UI Android* (hal. 2 navigasi, hal. 3 aturan status, hal. 4 bentuk bracket, hal. 5–6 sistem visual dan komponen, hal. 15 penanganan error, hal. 16 celah API).
- Backend dan kontrak API: [`afl3-miniapp`](https://github.com/satryo-1076012614901/afl3-miniapp), terutama bagian *Kontrak API aplikasi* di `HELP.md` repositori tersebut.
- Material Design 3 untuk Compose: <https://developer.android.com/develop/ui/compose/designsystems/material3>
- Material Symbols: <https://fonts.google.com/icons>

## Implementasi Match Android

- `BracketViewModel` dibuat sekali di detail kompetisi dan dipakai kedua tab: generation dari Peserta, hasil/undo dari Bracket, reset dari menu detail.
- Konten Bracket dan field hasil bersifat stateless. Preview mencakup terang, gelap, dan font 200%; tidak membuat ViewModel atau mengakses jaringan.
- `MatchRepository.submitResult` dan `undoResult` menyimpan respons mutasi ke cache, lalu **pemanggil wajib memuat ulang seluruh bracket dan kompetisi**. ViewModel membedakan kegagalan mutasi dari kegagalan sinkronisasi. Tombol muat ulang hanya mengulang GET, bukan mutasi.
- Setelah generation berhasil dan data tersinkron, buka Bracket. Setelah reset, buka Peserta. Setelah hasil final, pilih Final dan tampilkan juara.
- Skor opsional; skor lengkap tidak seri menentukan pemenang otomatis. Skor kosong/parsial/seri membutuhkan pilihan manual. Mengubah skor menghapus pilihan manual sebelumnya.
- Snackbar hasil menyediakan Urungkan tanpa dialog; aksi kartu memakai konfirmasi. Keduanya memeriksa kelayakan terbaru, termasuk alur bye.
- Tidak ada Canvas pohon bracket, landscape route, zoom, atau pembuka web sementara.

Verifikasi lokal: `sh ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
Test UI tanpa backend: `sh ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=ac.sfj.miniappcompetition.MatchContentTest` (perlu emulator).
