package com.reefii.aeromute.data

object AppStrings {
    fun get(language: AppLanguage): Strings = when (language) {
        AppLanguage.ENGLISH -> EnglishStrings
        AppLanguage.INDONESIAN -> IndonesianStrings
    }

    interface Strings {
        // Nav & Headers
        val tabHome: String
        val tabHomeSub: String
        val tabAdvanced: String
        val tabAdvancedSub: String
        val tabGuide: String
        val tabGuideSub: String

        val statusActive: String
        val statusInactive: String

        // Hero Banner
        val heroTitle: String
        val heroSubtitleActive: String
        val heroSubtitleInactive: String
        val btnTurnOn: String
        val btnTurnOff: String

        // Widget Preview
        val previewHeader: String
        val btnEditDetail: String

        // Volume Controls
        val volumeControlHeader: String
        val btnMuteAll: String
        val btnUnmuteAll: String
        val btnSet50All: String

        // Streams
        val streamMedia: String
        val streamRing: String
        val streamNotification: String
        val streamAlarm: String
        val streamCall: String

        // Advanced Settings
        val filterStreamsHeader: String
        val filterStreamsSub: String
        val chkMedia: String
        val chkRing: String
        val chkNotif: String
        val chkAlarm: String
        val chkCall: String

        val widgetModeHeader: String
        val widgetModeSub: String
        val modeStandardLabel: String
        val modeStandardDesc: String
        val modeSimpleLabel: String
        val modeSimpleDesc: String

        val opacityHeader: String
        val opacityLabel: String
        val widgetScaleLabel: String
        val colorThemeLabel: String

        // Language & Dark Mode Settings
        val languageHeader: String
        val languageSub: String
        val darkModeHeader: String
        val darkModeSub: String
        val modeSystem: String
        val modeDark: String
        val modeLight: String

        val behaviorHeader: String
        val behaviorAutoCollapse: String
        val behaviorSnapEdge: String
        val behaviorVibrate: String
        val btnResetPos: String

        // Permissions
        val permHeader: String
        val permOverlayTitle: String
        val permOverlayDesc: String
        val permDndTitle: String
        val permDndDesc: String
        val permActive: String
        val permNeeded: String
        val btnGrantPerm: String

        // Guide & Gestures Detail
        val guideWelcomeDesc: String
        val guideGesturesTitle: String
        val gesture1Title: String
        val gesture1Desc: String
        val gesture2Title: String
        val gesture2Desc: String
        val gesture3Title: String
        val gesture3Desc: String
        val gesture4Title: String
        val gesture4Desc: String

        val heroActiveDesc: String
        val heroInactiveDesc: String
        val statusServiceActive: String
        val statusServiceInactive: String
        val statusPermGranted: String
        val statusPermNeeded: String

        // Guide & Tiles
        val guideTitle: String
        val guideSub: String
        val guideQuickTileTitle: String
        val guideQuickTileDesc: String

        // Widget Preview Detail
        val previewSub: String
        val previewStatusMuted: String
        val previewStatusActive: String

        // Tile Steps
        val tileStep1: String
        val tileStep1Desc: String
        val tileStep2: String
        val tileStep2Desc: String
        val tileStep3: String
        val tileStep3Desc: String
        val tileStep4: String
        val tileStep4Desc: String
        val tileTestMute: String
        val tileTestActivate: String
        val tileStatusReady: String
        val tileStatusInactive: String
        val optionOff: String
    }

    object EnglishStrings : Strings {
        override val tabHome = "Home"
        override val tabHomeSub = "Main Control"
        override val tabAdvanced = "Advanced"
        override val tabAdvancedSub = "Detailed Options"
        override val tabGuide = "Guide"
        override val tabGuideSub = "Usage & Quick Tile"

        override val statusActive = "ACTIVE"
        override val statusInactive = "INACTIVE"

        override val heroTitle = "Floating Volume Service"
        override val heroSubtitleActive = "Floating volume bubble is running active on screen."
        override val heroSubtitleInactive = "Tap button below to enable floating overlay widget."
        override val btnTurnOn = "ENABLE SERVICE"
        override val btnTurnOff = "DISABLE SERVICE"

        override val previewHeader = "FLOATING WIDGET PREVIEW"
        override val btnEditDetail = "Edit Details"

        override val volumeControlHeader = "DEVICE VOLUME CONTROLLER"
        override val btnMuteAll = "MUTE ALL"
        override val btnUnmuteAll = "UNMUTE ALL"
        override val btnSet50All = "SET 50% ALL"

        override val streamMedia = "Media"
        override val streamRing = "Ringtone"
        override val streamNotification = "Notification"
        override val streamAlarm = "Alarm"
        override val streamCall = "Phone Call"

        override val filterStreamsHeader = "AUDIO STREAM FILTERS"
        override val filterStreamsSub = "Select audio channels shown in floating overlay:"
        override val chkMedia = "🎵 Media Stream (Music/Video/Game)"
        override val chkRing = "🔔 Ringtone Stream (Ring)"
        override val chkNotif = "📢 Notification Stream"
        override val chkAlarm = "⏰ Alarm Stream"
        override val chkCall = "📞 Phone Call Stream"

        override val widgetModeHeader = "FLOATING WIDGET MODE"
        override val widgetModeSub = "Choose floating overlay appearance style:"
        override val modeStandardLabel = "Standard"
        override val modeStandardDesc = "Full multi-stream sliders panel"
        override val modeSimpleLabel = "Simple"
        override val modeSimpleDesc = "Compact vertical bar with mute & %"

        override val opacityHeader = "TRANSPARENCY & SCALE"
        override val opacityLabel = "Opacity Level"
        override val widgetScaleLabel = "Floating Bubble Size"
        override val colorThemeLabel = "Widget Color Theme"

        override val languageHeader = "APPLICATION LANGUAGE"
        override val languageSub = "Select language for application interface:"
        override val darkModeHeader = "THEME & DARK MODE"
        override val darkModeSub = "Choose light, dark, or system theme:"
        override val modeSystem = "Follow System"
        override val modeDark = "Dark Mode"
        override val modeLight = "Light Mode"

        override val behaviorHeader = "BEHAVIOR & GESTURES"
        override val behaviorAutoCollapse = "Auto-collapse timer"
        override val behaviorSnapEdge = "Snap to screen edge"
        override val behaviorVibrate = "Haptic feedback on tap"
        override val btnResetPos = "Reset Floating Position"

        override val permHeader = "ANDROID SYSTEM PERMISSIONS"
        override val permOverlayTitle = "Display Over Other Apps (Overlay)"
        override val permOverlayDesc = "Required so floating volume controls appear over any screen."
        override val permDndTitle = "Do Not Disturb Access (DND / Mute Ring)"
        override val permDndDesc = "Required to mute ringtone & notification sounds on modern Android."
        override val permActive = "GRANTED"
        override val permNeeded = "REQUIRED"
        override val btnGrantPerm = "GRANT PERMISSION NOW"

        override val guideWelcomeDesc = "AEROMute is designed for instant volume control and quick mute without disrupting your games, videos, or active apps."
        override val guideGesturesTitle = "FLOATING WIDGET GESTURES"
        override val gesture1Title = "1. Drag & Position"
        override val gesture1Desc = "Touch and hold the floating bubble then drag to your preferred screen edge."
        override val gesture2Title = "2. Single Tap"
        override val gesture2Desc = "Opens full volume slider panel to adjust individual sound streams."
        override val gesture3Title = "3. Quick Double-Tap"
        override val gesture3Desc = "Instant Mute Feature! Double tap to instantly mute or restore all device sounds."
        override val gesture4Title = "4. Auto Collapse"
        override val gesture4Desc = "Widget auto-collapses after a few seconds timer to stay out of your way."

        override val heroActiveDesc = "Floating volume bubble is running active on screen."
        override val heroInactiveDesc = "Tap switch to enable floating overlay widget."
        override val statusServiceActive = "SERVICE ACTIVE"
        override val statusServiceInactive = "SERVICE INACTIVE"
        override val statusPermGranted = "PERMISSIONS OK"
        override val statusPermNeeded = "PERMISSIONS NEEDED"

        override val guideTitle = "How to Use & Quick Settings Tile"
        override val guideSub = "Open gesture guide and quick settings tile setup"
        override val guideQuickTileTitle = "Quick Settings Tile Integration"
        override val guideQuickTileDesc = "Add AEROMute tile to your notification panel for quick toggle anytime."

        override val previewSub = "Adjust transparency, size & theme below to see live view"
        override val previewStatusMuted = "Status: MUTED (Tap badge to test sound)"
        override val previewStatusActive = "Status: SOUND ACTIVE (Tap badge to test mute)"

        override val tileStep1 = "Step 1"
        override val tileStep1Desc = "Pull down Android notification shade from top of screen."
        override val tileStep2 = "Step 2"
        override val tileStep2Desc = "Tap Pencil / Edit icon in Android quick settings."
        override val tileStep3 = "Step 3"
        override val tileStep3Desc = "Find 'AEROMute' tile and drag it to top active tiles."
        override val tileStep4 = "Step 4"
        override val tileStep4Desc = "Tap AEROMute tile anytime to toggle or instant mute!"
        override val tileTestMute = "Test Tile: Mute"
        override val tileTestActivate = "Test Tile: Turn On"
        override val tileStatusReady = "Status: Active & Ready"
        override val tileStatusInactive = "Status: Inactive"
        override val optionOff = "Off"
    }

    object IndonesianStrings : Strings {
        override val tabHome = "Utama"
        override val tabHomeSub = "Pengaturan Utama"
        override val tabAdvanced = "Advanced"
        override val tabAdvancedSub = "Opsi Detail"
        override val tabGuide = "Panduan"
        override val tabGuideSub = "Cara Gunakan & Ubin"

        override val statusActive = "AKTIF"
        override val statusInactive = "NONAKTIF"

        override val heroTitle = "Layanan Volume Melayang"
        override val heroSubtitleActive = "Widget volume melayang sedang berjalan di layar."
        override val heroSubtitleInactive = "Tekan tombol di bawah untuk mengaktifkan widget melayang."
        override val btnTurnOn = "AKTIFKAN LAYANAN"
        override val btnTurnOff = "MATIKAN LAYANAN"

        override val previewHeader = "PRATINJAU WIDGET MELAYANG"
        override val btnEditDetail = "Edit Detail"

        override val volumeControlHeader = "KONTROL VOLUME HP"
        override val btnMuteAll = "MUTE SEMUA"
        override val btnUnmuteAll = "UNMUTE SEMUA"
        override val btnSet50All = "SET 50% SEMUA"

        override val streamMedia = "Media"
        override val streamRing = "Nada Dering"
        override val streamNotification = "Notifikasi"
        override val streamAlarm = "Alarm"
        override val streamCall = "Panggilan Telepon"

        override val filterStreamsHeader = "FILTER STREAM SUARA"
        override val filterStreamsSub = "Pilih saluran suara yang tampil di floating overlay:"
        override val chkMedia = "🎵 Stream Media (Musik/Video/Game)"
        override val chkRing = "🔔 Stream Nada Dering (Ring)"
        override val chkNotif = "📢 Stream Notifikasi"
        override val chkAlarm = "⏰ Stream Alarm"
        override val chkCall = "📞 Stream Panggilan Telepon"

        override val widgetModeHeader = "MODE FLOATING WIDGET"
        override val widgetModeSub = "Pilih gaya tampilan floating widget melayang:"
        override val modeStandardLabel = "Standar"
        override val modeStandardDesc = "Panel slider multi-stream lengkap"
        override val modeSimpleLabel = "Simpel"
        override val modeSimpleDesc = "Bar vertikal ringkas dengan mute & %"

        override val opacityHeader = "TRANSPARENCY & UKURAN"
        override val opacityLabel = "Tingkat Opacity / Transparansi"
        override val widgetScaleLabel = "Ukuran Floating Bubble"
        override val colorThemeLabel = "Tema Warna Widget"

        override val languageHeader = "PENGATURAN BAHASA"
        override val languageSub = "Pilih bahasa untuk antarmuka aplikasi:"
        override val darkModeHeader = "TEMA & MODE GELAP"
        override val darkModeSub = "Pilih mode terang, gelap, atau ikuti sistem:"
        override val modeSystem = "Ikut Sistem"
        override val modeDark = "Mode Gelap"
        override val modeLight = "Mode Terang"

        override val behaviorHeader = "PERILAKU & GESTUR"
        override val behaviorAutoCollapse = "Timer ciut otomatis"
        override val behaviorSnapEdge = "Tempelkan ke tepi layar"
        override val behaviorVibrate = "Getar umpan balik saat ditekan"
        override val btnResetPos = "Reset Posisi Floating Widget"

        override val permHeader = "IZIN AKSES SISTEM ANDROID"
        override val permOverlayTitle = "Izin Tampilkan di Atas Aplikasi Lain (Overlay)"
        override val permOverlayDesc = "Diperlukan agar widget kontrol volume melayang dapat muncul di layar manapun."
        override val permDndTitle = "Akses Jangan Ganggu / Mute Dering (DND)"
        override val permDndDesc = "Diperlukan untuk mematikan nada dering & notifikasi pada Android versi baru."
        override val permActive = "IZIN AKTIF"
        override val permNeeded = "BUTUH IZIN"
        override val btnGrantPerm = "AKTIFKAN IZIN SEKARANG"

        override val guideWelcomeDesc = "AEROMute dirancang agar Anda bisa mengontrol volume & membungkam suara HP secara instant tanpa mengganggu aktivitas game, video, atau aplikasi lain."
        override val guideGesturesTitle = "GESTUR KONTROL FLOATING WIDGET"
        override val gesture1Title = "1. Seret & Geser Posisi"
        override val gesture1Desc = "Sentuh dan tahan gelembung melayang lalu geser ke bagian pinggir layar yang Anda sukai."
        override val gesture2Title = "2. Ketuk 1x (Single Tap)"
        override val gesture2Desc = "Membuka panel ekspansi volume slider secara penuh untuk mengatur level suara masing-masing stream."
        override val gesture3Title = "3. Ketuk 2x Cepat (Double-Tap)"
        override val gesture3Desc = "Fitur Mute Instan! Ketuk 2x pada bubble melayang untuk langsung membungkam/mengembalikan semua suara HP."
        override val gesture4Title = "4. Sembunyi Otomatis (Auto Collapse)"
        override val gesture4Desc = "Widget akan mengecil secara otomatis setelah beberapa detik sesuai timer agar tidak menutupi tampilan layar."

        override val heroActiveDesc = "Widget melayang aktif & siap digunakan"
        override val heroInactiveDesc = "Aktifkan sakelar untuk memunculkan widget melayang"
        override val statusServiceActive = "SERVICE AKTIF"
        override val statusServiceInactive = "SERVICE NONAKTIF"
        override val statusPermGranted = "IZIN LENGKAP"
        override val statusPermNeeded = "BUTUH IZIN"

        override val guideTitle = "Cara Penggunaan & Quick Tile"
        override val guideSub = "Buka panduan gestur dan pengaturan ubin notifikasi"
        override val guideQuickTileTitle = "Integrasi Quick Settings Tile"
        override val guideQuickTileDesc = "Tambahkan ubin AEROMute ke panel notifikasi untuk akses cepat kapan saja."

        override val previewSub = "Sesuaikan transparansi, ukuran & tema di bawah untuk melihat tampilan live"
        override val previewStatusMuted = "Status: TERBUNGKAM (Ketuk badge untuk tes suara)"
        override val previewStatusActive = "Status: SUARA AKTIF (Ketuk badge untuk tes mute)"

        override val tileStep1 = "Langkah 1"
        override val tileStep1Desc = "Tarik tirai notifikasi Android dari atas layar HP Anda."
        override val tileStep2 = "Langkah 2"
        override val tileStep2Desc = "Ketuk ikon Pensil / Edit di ubin cepat Android."
        override val tileStep3 = "Langkah 3"
        override val tileStep3Desc = "Cari ubin bernama 'AEROMute' lalu seret ke bagian atas."
        override val tileStep4 = "Langkah 4"
        override val tileStep4Desc = "Ketuk ubin AEROMute 1x untuk mengaktifkan atau Mute suara instan!"
        override val tileTestMute = "Uji Tile: Mute"
        override val tileTestActivate = "Uji Tile: Aktifkan"
        override val tileStatusReady = "Status: Aktif & Siap"
        override val tileStatusInactive = "Status: Nonaktif"
        override val optionOff = "Matikan"
    }
}
