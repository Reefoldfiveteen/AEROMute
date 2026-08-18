#!/bin/bash

# AeroMute Auto Commit & Push Script (Self-Healing)

echo "======================================"
echo "    AeroMute Auto Commit & Push      "
echo "======================================"

# Periksa apakah .git atau origin bermasalah (otomatis perbaiki jika ZIP menimpa .git)
if ! git remote get-url origin >/dev/null 2>&1; then
    echo "[!] Remote 'origin' tidak ditemukan/corrupt. Memperbaiki konfigurasi Git..."
    rm -rf .git
    git init >/dev/null 2>&1
    git remote add origin https://github.com/Reefoldfiveteen/AEROMute.git
    git branch -M main
    echo "[✓] Perbaikan Git & Remote Origin selesai."
    echo ""
fi

# Ambil pesan commit dari argumen atau minta input dari pengguna
if [ -n "$1" ]; then
    COMMIT_MSG="$1"
else
    read -p "Masukkan pesan commit (tekan Enter untuk timestamp default): " INPUT_MSG
    if [ -n "$INPUT_MSG" ]; then
        COMMIT_MSG="$INPUT_MSG"
    else
        COMMIT_MSG="update: $(date +'%Y-%m-%d %H:%M:%S')"
    fi
fi

echo ""
echo "[1/3] Staging semua perubahan (git add .)..."
git add .

echo "[2/3] Membuat commit ($COMMIT_MSG)..."
git commit -m "$COMMIT_MSG"

echo "[3/3] Pushing ke GitHub (origin main)..."
git push -u origin main --force

if [ $? -eq 0 ]; then
    echo ""
    echo "=============================================="
    echo "  SUCCESS: Kode berhasil di-push ke GitHub!   "
    echo "=============================================="
else
    echo ""
    echo "=============================================="
    echo "  ERROR: Terjadi kesalahan saat push.         "
    echo "=============================================="
fi
