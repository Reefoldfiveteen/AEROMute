@echo off
echo ======================================
echo     AeroMute Auto Commit ^& Push      
echo ======================================

git remote get-url origin >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Remote 'origin' tidak ditemukan/corrupt. Memperbaiki konfigurasi Git...
    if exist .git rmdir /s /q .git
    git init >nul 2>&1
    git remote add origin https://github.com/Reefoldfiveteen/AEROMute.git
    git branch -M main
    echo [✓] Perbaikan Git ^& Remote Origin selesai.
    echo.
)

set MSG=%~1
if "%MSG%"=="" (
    set /p MSG="Masukkan pesan commit (tekan Enter untuk default): "
)

if "%MSG%"=="" (
    set MSG=update: %date% %time%
)

echo.
echo [1/3] Staging semua perubahan (git add .)...
git add .

echo [2/3] Membuat commit (%MSG%)...
git commit -m "%MSG%"

echo [3/3] Pushing ke GitHub (origin main)...
git push -u origin main --force

echo.
if %errorlevel% equ 0 (
    echo ==============================================
    echo   SUCCESS: Kode berhasil di-push ke GitHub!   
    echo ==============================================
) else (
    echo ==============================================
    echo   ERROR: Terjadi kesalahan saat push.         
    echo ==============================================
)
