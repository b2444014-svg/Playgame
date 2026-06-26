# 🎮 Top Up Game — Portal Top Up (Tampilan ala Esports)

Aplikasi Android ringan: halaman depan ada **kotak pencarian di tengah** (mirip Google),
pelanggan tinggal ketik nama game → langsung diarahkan ke halaman top-up-nya di dalam app.
Plus tab **Riwayat** yang otomatis nyimpen web yang sudah pernah dibuka.

Tampilan dark, glow neon, kartu kaca (glassmorphism), animasi halus.
Kamu **tidak perlu Android Studio** — APK di-build otomatis di cloud (gratis) lewat GitHub.

---

## ✏️ LANGKAH 1 — Isi daftar game-nya (PENTING, ini bagian kamu)

Buka file: **`app/src/main/assets/sites.js`**

Isi/ubah daftarnya seperti contoh ini:

```js
window.SITES = [
  { name: "Mobile Legends", url: "https://example.com/topup/ml", category: "MOBA", image: "https://link-logo.png" },
  { name: "Free Fire",      url: "https://example.com/topup/ff", category: "Battle Royale" },
  { name: "Valorant",       url: "https://example.com/topup/val", category: "FPS", premium: true },
];
```

- **name**     = nama game yang muncul di app
- **url**       = link halaman top-up game (HARUS pakai `https://`)
- **category** = opsional (mis. "MOBA", "FPS", "Battle Royale")
- **image**     = opsional, link logo game (kalau kosong → huruf otomatis)
- **premium**  = opsional, `true` untuk tandai game khusus premium
- **color**     = opsional, warna kartu (mis. `"#e11d48"`). Kalau kosong → otomatis.

> Tinggal copy salah satu baris, ganti nama & url-nya. Jangan lupa koma `,` di akhir tiap baris.

(Opsional) Ganti **nama app** di `app/src/main/res/values/strings.xml`.
(Opsional) Ganti **warna tema** di `app/src/main/assets/home.html` (bagian `:root` paling atas).

---

## ☁️ LANGKAH 2 — Build APK lewat GitHub (tanpa install apa pun)

1. Buat akun gratis di **github.com** (kalau belum punya).
2. Klik **New repository** → kasih nama bebas → **Create repository**.
3. Klik **uploading an existing file**.
4. **Drag semua isi folder ini** ke kotak upload → **Commit changes**.
5. Buka tab **Actions** → tunggu proses **Build APK** sampai centang hijau ✅ (±3 menit).
6. Klik build hijau itu → bagian **Artifacts** → download **app-debug**.
7. Ekstrak zip-nya → dapat **app-debug.apk**.

---

## 📲 LANGKAH 3 — Install di HP

1. Kirim `app-debug.apk` ke HP.
2. Buka → aktifkan **"Izinkan install dari sumber ini"** kalau diminta.
3. Install. Selesai! 🎉

---

## 🔁 Mau tambah / ganti web nanti?

Edit lagi file `sites.js` langsung di GitHub (klik file → pensil ✏️ → ubah → Commit).
Tiap commit, APK baru otomatis di-build. Tinggal download ulang.

---

## ⚙️ Cara kerja singkat

- Halaman depan = file `assets/home.html` (search + daftar media + riwayat).
- Daftar media dibaca dari `assets/sites.js`.
- Riwayat disimpan otomatis di HP pengguna (localStorage) — muncul di tab **Riwayat**.
- Favorit: tap ikon bintang ⭐ di kartu media untuk menyimpan; semua favorit ngumpul di tab **Favorit**.
- Tab (Semua / Favorit / Riwayat) cuma ada di halaman depan. Saat baca web, tampil full-screen tanpa tab.
- Saat media dipilih, webnya kebuka di dalam app; tombol back balik ke halaman depan.

## ❓ Catatan
- APK ini versi **debug**, langsung bisa dipakai. Untuk Play Store nanti perlu versi **release** (signed).
- Kalau build gagal (❌), klik build-nya untuk lihat log error, lalu kirim ke saya.
