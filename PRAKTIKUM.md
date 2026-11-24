# Catatan praktikum sesuai brief UAS

## Status hasil, bukan hasil simulasi

Perintah yang telah dijalankan:

```text
./gradlew test bootJar --console=plain
BUILD SUCCESSFUL
```

| Praktikum | Status |
|---|---|
| Kompilasi seluruh kode Chapter 1–2 | Lulus |
| Context Spring Boot | 1 tes lulus |
| Listing 1.4: service dengan WireMock, jawaban Paris | 1 tes lulus |
| Tambahan verifikasi endpoint POST /ask dengan WireMock | 1 tes lulus |
| Pembuatan executable JAR | Berhasil |
| Respons model `gpt-6-luna` melalui Naraya | Terverifikasi melalui pemanggilan model pada `liveAiTest`; model menghasilkan teks jawaban |
| Evaluator relevansi dan faktual melalui Naraya | Dijalankan: 2 tes gagal (`isPass() == false`), bukan error koneksi; lihat catatan di bawah |
| Runtime retry/evaluasi dan fallback | Kode terkompilasi; belum diverifikasi saat runtime |
| Alternatif Azure/Ollama | Petunjuk tersedia; belum dijalankan |
| Screenshot, rekaman video dan publikasi GitHub | Belum dilakukan |

Laporan tes lokal: `build/reports/tests/test/index.html`.
Hasil XML: `build/test-results/test/`.
JAR: `build/libs/board-game-buddy-0.0.1-SNAPSHOT.jar`.

Jangan menyebut respons Paris sebagai hasil model AI: respons itu berasal dari fixture WireMock.

Verifikasi live dengan skrip bantu lama pada 24 November (skrip sudah digantikan konfigurasi Gradle): model `gpt-6-luna` di Naraya mengembalikan penjelasan hamburan Rayleigh, tetapi `evaluateRelevancy` dan `evaluateFactualAccuracy` gagal karena evaluator memberi `isPass() == false`. Ini hasil nyata, bukan tes yang lulus. Evaluator bawaan Spring AI 1.0.1 mengharapkan jawaban penilaian `YES`/`NO` (fact-checking bahkan membutuhkan teks tepat `yes`), sedangkan model bisa mengeluarkan format atau penilaian lain. Tes buku dipertahankan apa adanya; jangan mengklaim kualitas/fakta sudah lolos. Untuk investigasi lanjutan, rekam jawaban evaluator secara aman atau coba model yang mengikuti instruksi YES/NO; jangan mengubah assertion hanya untuk memaksa hijau.

## Teori yang perlu dijelaskan

Spring AI menyediakan abstraksi client untuk mengirim prompt ke provider AI, sehingga kode aplikasi tidak perlu menyusun request HTTP provider secara manual. Spring Boot melakukan auto-configuration `ChatClient.Builder` berdasarkan starter dan properti konfigurasi.

Manfaat: integrasi lebih ringkas, dependency injection, dan pemisahan controller/service. Kekurangan: layanan cloud membutuhkan kredensial dan biaya, respons tidak deterministik, serta dapat berhalusinasi. Model lokal mengurangi ketergantungan cloud tetapi membutuhkan RAM dan kemampuan komputasi yang memadai.

WireMock menguji kode pemanggilan dan pemetaan respons secara deterministik, bukan kualitas model. RelevancyEvaluator menilai kesesuaian dengan pertanyaan; FactCheckingEvaluator menilai jawaban melalui model evaluator. Penilaian evaluator tetap probabilistik, bukan jaminan kebenaran atau pengganti sumber fakta tepercaya. Contoh request evaluasi di sini mengikuti Chapter 2, tanpa menambahkan RAG atau sumber dokumen bab berikutnya.

Self-evaluation menambah pemanggilan model, latensi dan biaya. Batas default `@Retryable` adalah tiga percobaan total, bukan tiga retry di luar percobaan pertama. Fallback hanya menangani kegagalan relevansi; kegagalan autentikasi atau jaringan bukan `AnswerNotRelevantException`.

## Penyesuaian minimal dari buku

- Spring Boot 3.5.5, Spring AI 1.0.1 dan WireMock 3.9.0 mengikuti buku.
- Java 23 mengikuti JDK lokal; buku memperbolehkan Java 17+. Bila memakai JDK lain, sesuaikan toolchain di `build.gradle`.
- Gradle Wrapper menggunakan 8.14.3 agar sesuai Spring Boot 3.5 dan JDK lokal.
- Package tetap `com.example.board_game_buddy`, sesuai proyek awal.
- Profile `self-evaluating` memilih service Chapter 2, agar tidak ada dua bean `BoardGameService` aktif sekaligus.
- `@EnableRetry`, Spring Retry dan AOP ditambahkan agar anotasi retry benar-benar aktif.
- Provider utama adalah Naraya melalui API OpenAI-compatible, sesuai bagian OpenAI Compatibility pada §1.2. Starter dan kode `ChatClient` tetap sama dengan buku.
Task Gradle `bootRun` dan `liveAiTest` otomatis membaca key `providers.naraya.apiKey` dari `~/.pi/agent/models.json`; jika key berupa referensi atau command, set `NARAYA_API_KEY` sendiri. Model live `gpt-6-luna` melalui endpoint Naraya.
- Model service self-evaluating dan evaluator menggunakan konfigurasi model `gpt-6-luna` yang sama. Fixture WireMock tetap menggunakan `gpt-4o-mini`; fixture itu tidak menentukan model live.
- Tes live diberi tag `live-ai` dan task terpisah. `test` tidak diam-diam memanggil API berbayar.
- Import yang tidak digunakan dalam listing tidak disalin. Interface `Evaluator` milik library tidak dibuat ulang.

## Chapter 1 §1.2 — eksperimen konfigurasi provider

Jalankan alternatif satu per satu. Simpan hasil nyata, nama model, dan screenshot masing-masing. Tutup aplikasi sebelum mengganti provider. Perubahan dependency harus diikuti build ulang.

### OpenAI melalui Naraya: konfigurasi dan mengganti model

Hentikan server lama sebelum menjalankan Gradle:

```powershell
./gradlew bootRun
```

Gradle membaca key dari provider `naraya` di `~/.pi/agent/models.json`. Untuk mengganti model praktikum, ubah ID model di `application.properties` dan sesuaikan model yang tersedia. Jangan membagikan `models.json`, API key, atau log berisi rahasia. Tes default `./gradlew test` hanya menggunakan WireMock.

### Azure OpenAI: alternatif starter

Ganti dependency starter OpenAI, jangan menambahkannya berdampingan:

```groovy
implementation 'org.springframework.ai:spring-ai-starter-model-azure-openai'
```

- Konfigurasi default Naraya ada di `application.properties` (endpoint, model, path chat completions, dan placeholder key). Saat mencoba Azure, hapus keempat properti itu untuk eksperimen; kembalikan setelah selesai. Konfigurasi memakai deployment yang benar-benar dibuat di Azure, bukan menganggap nama deployment selalu sama dengan nama model:

```powershell
$env:SPRING_AI_AZURE_OPENAI_API_KEY="isi-key-azure"
$env:SPRING_AI_AZURE_OPENAI_ENDPOINT="https://NAMA-RESOURCE.openai.azure.com/"
$env:SPRING_AI_AZURE_OPENAI_CHAT_OPTIONS_DEPLOYMENT_NAME="NAMA-DEPLOYMENT"
./gradlew bootRun
```

API key OpenAI tidak berlaku untuk Azure. Endpoint Azure adalah konfigurasi tambahan yang diperlukan untuk resource Azure Anda. Tes WireMock yang disediakan khusus untuk starter OpenAI; jangan menjalankannya sebagai tes integrasi Azure.

### Ollama: model lokal

Instal Ollama, lalu jalankan contoh command buku:

```powershell
ollama pull gemma:2b
# Alternatif yang lebih besar:
# ollama pull mistral:7b
ollama list
Invoke-RestMethod -Uri "http://localhost:11434/api/tags"
```

Ganti starter OpenAI dengan:

```groovy
implementation 'org.springframework.ai:spring-ai-starter-model-ollama'
```

Hapus keempat properti `spring.ai.openai.*` Naraya sebelum menambahkan properti Ollama:

```properties
spring.ai.ollama.chat.model=gemma:2b
spring.ai.ollama.init.pull-model-strategy=when_missing
```

Jalankan `./gradlew bootRun`, lalu coba `/ask`. Tidak perlu API key untuk server lokal. Buku juga menjelaskan strategi `always` dan `never`; pengunduhan otomatis dapat memperlambat startup. Kinerja bergantung hardware. Jangan menjalankan tes WireMock OpenAI sebagai tes Ollama.

Azure dan Ollama hanya alternatif praktikum di buku, bukan provider utama proyek. Sesudah eksperimen alternatif, kembalikan starter OpenAI dan keempat properti Naraya seperti konfigurasi semula sebelum menjalankan suite tes default.

## Chapter 2 — urutan praktikum

1. Jalankan `./gradlew liveAiTest`. Model pembuat jawaban dan evaluator menggunakan `gpt-6-luna`. Perintah memanggil API sungguhan dan dapat memakai kuota.
2. Buka `build/reports/tests/liveAiTest/index.html`, catat pass/fail kedua evaluator. Kegagalan autentikasi/kuota bukan bukti bahwa evaluator menilai jawaban salah.
3. Jalankan `./gradlew bootRun --args='--spring.profiles.active=self-evaluating'`.
4. Kirim pertanyaan `/ask`; catat jawaban nyata dan waktu respons.
5. Tunjukkan method `evaluateRelevancy`, `@Retryable`, exception, dan `@Recover`. Jangan mengklaim jalur retry/fallback telah terjadi hanya karena satu respons berhasil. Jika ingin memperagakan konfigurasi lima percobaan dari buku, ubah sementara menjadi `@Retryable(retryFor = AnswerNotRelevantException.class, maxAttempts = 5)`; default proyek tetap tiga untuk membatasi biaya.

## Screenshot dan rekaman live demo

Ambil screenshot nyata untuk build, hasil WireMock, startup server, request/response model OpenAI melalui Naraya, hasil kedua evaluator, dan mode runtime. Jangan tampilkan nilai API key.

Rekam pembuatan kode sejak awal melalui IDE/terminal bila penilaian meminta proses dari awal; kode yang sudah selesai bukan pengganti rekaman proses. Jelaskan teori singkat, konfigurasi, class diagram, alur method, eksekusi tes, request `/ask`, dan publikasi GitHub. Masukkan screenshot dan tautan video ke laporan setelah tersedia; tidak ada bukti visual yang direkayasa dalam proyek ini.

## GitHub dan versioning

Folder proyek belum merupakan repository Git. Dari folder `board-game-buddy`, setelah meninjau file dan memastikan tidak ada rahasia:

```powershell
git init
git add .
git status
git commit -m "Implement Spring AI Chapter 1 and 2 labs"
git branch -M main
git remote add origin https://github.com/USERNAME/REPOSITORY.git
git push -u origin main
```

Buat repository GitHub terlebih dahulu. Commit berikutnya sebaiknya menjelaskan perubahan nyata, misalnya penambahan hasil praktikum; jangan membuat sejarah palsu seolah-olah rekaman pengembangan sudah dilakukan. Folder `build/` diabaikan oleh Git; screenshot/laporan yang hendak diserahkan harus disimpan terpisah sebagai dokumentasi tanpa kredensial.

Share tautan repository dan laporan/video kepada `rizalbroer@uinjkt.ac.id` sesuai soal. Untuk repository private, tambahkan akun GitHub dosen melalui Settings → Collaborators menggunakan email/username yang terkonfirmasi. Publikasi dan pengiriman email belum dilakukan oleh asisten.
