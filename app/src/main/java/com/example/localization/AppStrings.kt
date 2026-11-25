package com.example.localization

import java.util.Locale

data class StringPack(
    val channels: String,
    val schedule: String,
    val news: String,
    val series: String,
    val settings: String,
    val all: String,
    val movies: String,
    val favorites: String,
    val loading: String,
    val loadingChannels: String,
    val loadingMatches: String,
    val loadingNews: String,
    val loadingVod: String,
    val loadingEpisodes: String,
    val noContent: String,
    val noFavoritesTitle: String,
    val noFavoritesHint: String,
    val retry: String,
    val errorLoad: String,
    val errorRetry: String,
    val season: String,
    val episode: String,
    val movieBadge: String,
    val seriesBadge: String,
    val language: String,
    val technicalSupport: String,
    val autoLang: String,
    val search: String,
    val telegram: String,
    val youtube: String,
    val email: String,
    val statusLive: String,
    val statusFinal: String,
    val statusUpcoming: String,
    val appSourceName: String,
    val liveBadge: String
)

object AppStrings {
    private val ar = StringPack(
        channels = "قنوات",
        schedule = "جدول",
        news = "أخبار",
        series = "مسلسلات",
        settings = "إعدادات",
        all = "الكل",
        movies = "أفلام",
        favorites = "المفضلة",
        loading = "جاري تحميل...",
        loadingChannels = "جاري تحميل...",
        loadingMatches = "جاري تحميل...",
        loadingNews = "جاري تحميل...",
        loadingVod = "جاري تحميل...",
        loadingEpisodes = "جاري تحميل...",
        noContent = "لا يوجد محتوى في هذا التصنيف",
        noFavoritesTitle = "لا توجد قنوات في المفضلة بعد",
        noFavoritesHint = "اضغط على ❤️ بجانب أي قناة لإضافتها",
        retry = "🔄 إعادة المحاولة",
        errorLoad = "تعذّر التحميل",
        errorRetry = "تحقق من الاتصال بالإنترنت",
        season = "الموسم",
        episode = "حلقة",
        movieBadge = "فيلم",
        seriesBadge = "مسلسل",
        language = "اللغة",
        technicalSupport = "دعم فني",
        autoLang = "تلقائي (لغة الهاتف)",
        search = "بحث...",
        telegram = "تليجرام",
        youtube = "يوتيوب",
        email = "البريد الإلكتروني",
        statusLive = "مباشر",
        statusFinal = "انتهت",
        statusUpcoming = "لم تبدأ",
        appSourceName = "نجم الشاشات",
        liveBadge = "● LIVE"
    )

    private val en = StringPack(
        channels = "Channels",
        schedule = "Schedule",
        news = "News",
        series = "Movies & Series",
        settings = "Settings",
        all = "All",
        movies = "Movies",
        favorites = "Favorites",
        loading = "Loading...",
        loadingChannels = "Loading...",
        loadingMatches = "Loading...",
        loadingNews = "Loading...",
        loadingVod = "Loading...",
        loadingEpisodes = "Loading...",
        noContent = "No content in this category",
        noFavoritesTitle = "No favorites yet",
        noFavoritesHint = "Tap ❤️ next to any channel to add it",
        retry = "🔄 Retry",
        errorLoad = "Failed to load",
        errorRetry = "Check your internet connection",
        season = "Season",
        episode = "Episode",
        movieBadge = "Movie",
        seriesBadge = "Series",
        language = "Language",
        technicalSupport = "Technical Support",
        autoLang = "Auto (Device Language)",
        search = "Search...",
        telegram = "Telegram",
        youtube = "YouTube",
        email = "Email",
        statusLive = "Live",
        statusFinal = "Final",
        statusUpcoming = "Upcoming",
        appSourceName = "Screen Star",
        liveBadge = "● LIVE"
    )

    private val fa = StringPack(
        channels = "کانال‌ها",
        schedule = "برنامه",
        news = "اخبار",
        series = "سریال‌ها",
        settings = "تنظیمات",
        all = "همه",
        movies = "فیلم‌ها",
        favorites = "مورد علاقه",
        loading = "در حال بارگذاری...",
        loadingChannels = "در حال بارگذاری...",
        loadingMatches = "در حال بارگذاری...",
        loadingNews = "در حال بارگذاری...",
        loadingVod = "در حال بارگذاری...",
        loadingEpisodes = "در حال بارگذاری...",
        noContent = "محتوایی در این دسته وجود ندارد",
        noFavoritesTitle = "هنوز مورد علاقه‌ای وجود ندارد",
        noFavoritesHint = "برای افزودن روی ❤️ کنار هر کانال ضربه بزنید",
        retry = "🔄 تلاش مجدد",
        errorLoad = "بارگیری ناموفق بود",
        errorRetry = "اتصال اینترنت خود را بررسی کنید",
        season = "فصل",
        episode = "قسمت",
        movieBadge = "فیلم",
        seriesBadge = "سریال",
        language = "زبان",
        technicalSupport = "پشتیبانی فنی",
        autoLang = "خودکار (زبان دستگاه)",
        search = "جستجو...",
        telegram = "تلگرام",
        youtube = "یوتیوب",
        email = "ایمیل",
        statusLive = "زنده",
        statusFinal = "پایان یافت",
        statusUpcoming = "شروع نشده",
        appSourceName = "ستاره صفحه",
        liveBadge = "● LIVE"
    )

    private val ku = StringPack(
        channels = "کەناڵەکان",
        schedule = "خشتە",
        news = "هەواڵ",
        series = "زنجیرەکان",
        settings = "ڕێکخستن",
        all = "هەموو",
        movies = "فیلمەکان",
        favorites = "دڵخوازەکان",
        loading = "بارکردن...",
        loadingChannels = "بارکردن...",
        loadingMatches = "بارکردن...",
        loadingNews = "بارکردن...",
        loadingVod = "بارکردن...",
        loadingEpisodes = "بارکردن...",
        noContent = "هیچ ناوەڕۆکێک نییە لەم پۆلێنەدا",
        noFavoritesTitle = "هێشتا هیچ دڵخوازێک نییە",
        noFavoritesHint = "کلیک لە ❤️ لەتەنیشت هەر کەناڵێک بکە بۆ زیادکردنی",
        retry = "🔄 دووبارە هەوڵدانەوە",
        errorLoad = "بارکردن سەرکەوتوو نەبوو",
        errorRetry = "پەیوەندی ئینتەرنێتەکەت بپشکنە",
        season = "وەرز",
        episode = "ئەڵقە",
        movieBadge = "فیلم",
        seriesBadge = "زنجیرە",
        language = "زمان",
        technicalSupport = "پشتگیری تەکنیکی",
        autoLang = "خۆکار (زمانی ئامێر)",
        search = "گەڕان...",
        telegram = "تێلێگرام",
        youtube = "یوتیوب",
        email = "ئیمەیڵ",
        statusLive = "ڕاستەوخۆ",
        statusFinal = "کۆتاییهات",
        statusUpcoming = "دەستپێنەکردووە",
        appSourceName = "ئەستێرەی شاشە",
        liveBadge = "● LIVE"
    )

    fun detectLanguage(setting: String): String {
        if (setting != "auto") return setting
        val deviceLang = Locale.getDefault().language.lowercase()
        return when {
            deviceLang.startsWith("ar") -> "ar"
            deviceLang.startsWith("fa") -> "fa"
            deviceLang.startsWith("ku") || deviceLang.startsWith("ckb") -> "ku"
            else -> "en"
        }
    }

    fun isRtl(lang: String): Boolean = lang in setOf("ar", "fa", "ku")

    fun forLang(lang: String): StringPack = when (lang) {
        "ar" -> ar
        "fa" -> fa
        "ku" -> ku
        else -> en
    }

    private val channelTranslations = listOf(
        "shasha sports" to "شاشة الرياضية",
        "starzplay sports" to "ستارزبلاي الرياضية",
        "abu dhabi sports" to "أبوظبي الرياضية",
        "shahid sports" to "شاهد الرياضية",
        "dubai sports" to "دبي الرياضية",
        "bein sports" to "بي إن سبورت",
        "bein sport" to "بي إن سبورت",
        "ad sports" to "أبوظبي الرياضية",
        "ad sport" to "أبوظبي الرياضية",
        "starzplay" to "ستارزبلاي",
        "abu dhabi" to "أبوظبي",
        "documentary" to "وثائقي",
        "al kass" to "الكأس",
        "updates" to "تحديثات",
        "channel" to "قناة",
        "shasha" to "شاشة",
        "shahid" to "شاهد",
        "rotana" to "روتانا",
        "sports" to "الرياضية",
        "series" to "مسلسلات",
        "movies" to "أفلام",
        "family" to "عائلي",
        "update" to "تحديث",
        "dubai" to "دبي",
        "sport" to "الرياضية",
        "movie" to "فيلم",
        "music" to "موسيقى",
        "ultra" to "ألترا",
        "bein" to "بي إن",
        "kass" to "الكأس",
        "news" to "أخبار",
        "kids" to "أطفال",
        "live" to "مباشر",
        "hevc" to "HEVC",
        "h265" to "H265",
        "ssc" to "السعودية الرياضية",
        "mbc" to "إم بي سي",
        "low" to "منخفضة",
        "fhd" to "FHD",
        "hd" to "HD",
        "sd" to "SD",
        "4k" to "4K"
    ).map { (k, v) -> Regex(Regex.escape(k), RegexOption.IGNORE_CASE) to v }

    fun getArabicChannelName(engName: String?): String {
        if (engName.isNullOrBlank()) return "---"
        var result: String = engName
        for ((regex, replacement) in channelTranslations) {
            result = regex.replace(result, replacement)
        }
        return result
    }

    private val teamTranslations = linkedMapOf(
        "Real Madrid" to "ريال مدريد",
        "Barcelona" to "برشلونة",
        "Liverpool" to "ليفربول",
        "Manchester City" to "مانشستر سيتي",
        "Manchester United" to "مانشستر يونايتد",
        "Arsenal" to "أرسنال",
        "Chelsea" to "تشيلسي",
        "Tottenham Hotspur" to "توتنهام هوتسبير",
        "Bayern Munich" to "بايرن ميونخ",
        "Borussia Dortmund" to "بوروسيا دورتموند",
        "Juventus" to "يوفنتوس",
        "AC Milan" to "ميلان",
        "Inter Milan" to "إنتر ميلان",
        "Paris Saint-Germain" to "باريس سان جيرمان",
        "PSG" to "باريس سان جيرمان",
        "Atletico Madrid" to "أتلتيكو مدريد",
        "Sevilla" to "إشبيلية",
        "Real Betis" to "ريال بيتيس",
        "Napoli" to "نابولي",
        "Roma" to "روما",
        "Lazio" to "لاتسيو",
        "FC Porto" to "بورتو",
        "Benfica" to "بنفيكا",
        "Ajax" to "أياكس",
        "Fenerbahce" to "فنربخشة",
        "Galatasaray" to "غلطة سراي",
        "AEK Athens" to "أيك أثينا",
        "Al Hilal" to "الهلال",
        "Al Nassr" to "النصر",
        "Al Ittihad" to "الاتحاد",
        "Al Ahly" to "الأهلي",
        "Zamalek" to "الزمالك",
        "Leicester City" to "ليستر سيتي",
        "West Ham United" to "وست هام يونايتد",
        "Newcastle United" to "نيوكاسل يونايتد",
        "Aston Villa" to "أستون فيلا",
        "Everton" to "إيفرتون",
        "Bayer Leverkusen" to "باير ليفركوزن",
        "RB Leipzig" to "لايبزيغ",
        "Atalanta" to "أتالانتا",
        "Fiorentina" to "فيورنتينا",
        "Lyon" to "ليون",
        "Marseille" to "مارسيليا",
        "Monaco" to "موناكو",
        "Lille" to "ليل",
        "Sporting CP" to "سبورتينغ لشبونة"
    )

    fun getArabicTeamName(engName: String?): String {
        if (engName.isNullOrBlank()) return "فريق"
        teamTranslations[engName]?.let { return it }
        val lower = engName.lowercase()
        for ((key, value) in teamTranslations) {
            if (lower.contains(key.lowercase())) return value
        }
        return engName
    }
}
