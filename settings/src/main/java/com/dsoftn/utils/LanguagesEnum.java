package com.dsoftn.utils;

import java.util.ArrayList;
import java.util.List;

public enum LanguagesEnum {
    UNKNOWN("?", "Unknown", "?", ""),

    AFRIKAANS("af", "Afrikaans", "Afrikaans", "af"),
    ALBANIAN("sq", "Albanian", "Shqip", "sq"),
    AMHARIC("am", "Amharic", "አማርኛ", "am"),
    ARABIC("ar", "Arabic", "العربية", "ar"),
    ARMENIAN("hy", "Armenian", "Հայերեն", "hy"),
    AZERBAIJANI("az", "Azerbaijani", "Azərbaycan dili", "az"),
    BASQUE("eu", "Basque", "Euskara", "eu"),
    BELARUSIAN("be", "Belarusian", "Беларуская", "be"),
    BENGALI("bn", "Bengali", "বাংলা", "bn"),
    BOSNIAN("bs", "Bosnian", "Bosanski", "bs"),
    BULGARIAN("bg", "Bulgarian", "Български", "bg"),
    CATALAN("ca", "Catalan", "Català", "ca"),
    CEBUANO("ceb", "Cebuano", "Bisaya", "ceb"),
    CHICHEWA("ny", "Chichewa", "Chichewa", "ny"),
    CHINESE_SIMPLIFIED("zh-cn", "Chinese (simplified)", "简体中文", "zh-cn"),
    CHINESE_TRADITIONAL("zh-tw", "Chinese (traditional)", "繁體中文", "zh-tw"),
    CORSICAN("co", "Corsican", "Corsu", "co"),
    CROATIAN("hr", "Croatian", "Hrvatski", "hr"),
    CZECH("cs", "Czech", "Čeština", "cs"),
    DANISH("da", "Danish", "Dansk", "da"),
    DUTCH("nl", "Dutch", "Nederlands", "nl"),
    ENGLISH("en", "English", "English", "en"),
    ESPERANTO("eo", "Esperanto", "Esperanto", "eo"),
    ESTONIAN("et", "Estonian", "Eesti", "et"),
    FILIPINO("tl", "Filipino", "Filipino", "tl"),
    FINNISH("fi", "Finnish", "Suomi", "fi"),
    FRENCH("fr", "French", "Français", "fr"),
    FRISIAN("fy", "Frisian", "Frysk", "fy"),
    GALICIAN("gl", "Galician", "Galego", "gl"),
    GEORGIAN("ka", "Georgian", "ქართული", "ka"),
    GERMAN("de", "German", "Deutsch", "de"),
    GREEK("el", "Greek", "Ελληνικά", "el"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", "gu"),
    HAITIAN_CREOLE("ht", "Haitian creole", "Kreyòl ayisyen", "ht"),
    HAUSA("ha", "Hausa", "Hausa", "ha"),
    HAWAIIAN("haw", "Hawaiian", "ʻŌlelo Hawaiʻi", "haw"),
    HEBREW("he", "Hebrew", "עברית", "he"),
    HINDI("hi", "Hindi", "हिन्दी", "hi"),
    HMONG("hmn", "Hmong", "Hmoob", "hmn"),
    HUNGARIAN("hu", "Hungarian", "Magyar", "hu"),
    ICELANDIC("is", "Icelandic", "Íslenska", "is"),
    IGBO("ig", "Igbo", "Asụsụ Igbo", "ig"),
    INDONESIAN("id", "Indonesian", "Bahasa Indonesia", "id"),
    IRISH("ga", "Irish", "Gaeilge", "ga"),
    ITALIAN("it", "Italian", "Italiano", "it"),
    JAPANESE("ja", "Japanese", "日本語", "ja"),
    JAVANESE("jw", "Javanese", "Basa Jawa", "jw"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", "kn"),
    KAZAKH("kk", "Kazakh", "Қазақ тілі", "kk"),
    KHMER("km", "Khmer", "ខ្មែរ", "km"),
    KOREAN("ko", "Korean", "한국어", "ko"),
    KURDISH_KURMANJI("ku", "Kurdish (kurmanji)", "Kurmancî", "ku"),
    KYRGYZ("ky", "Kyrgyz", "Кыргызча", "ky"),
    LAO("lo", "Lao", "ລາວ", "lo"),
    LATIN("la", "Latin", "Lingua Latina", "la"),
    LATVIAN("lv", "Latvian", "Latviešu", "lv"),
    LITHUANIAN("lt", "Lithuanian", "Lietuvių", "lt"),
    LUXEMBOURGISH("lb", "Luxembourgish", "Lëtzebuergesch", "lb"),
    MACEDONIAN("mk", "Macedonian", "Македонски", "mk"),
    MALAGASY("mg", "Malagasy", "Malagasy", "mg"),
    MALAY("ms", "Malay", "Bahasa Melayu", "ms"),
    MALAYALAM("ml", "Malayalam", "മലയാളം", "ml"),
    MALTESE("mt", "Maltese", "Malti", "mt"),
    MAORI("mi", "Maori", "Te Reo Māori", "mi"),
    MARATHI("mr", "Marathi", "मराठी", "mr"),
    MONGOLIAN("mn", "Mongolian", "Монгол хэл", "mn"),
    MYANMAR_BURMESE("my", "Myanmar (burmese)", "မြန်မာစာ", "my"),
    NEPALI("ne", "Nepali", "नेपाली", "ne"),
    NORWEGIAN("no", "Norwegian", "Norsk", "no"),
    ODIA("or", "Odia", "ଓଡ଼ିଆ", "or"),
    PASHTO("ps", "Pashto", "پښتو", "ps"),
    PERSIAN("fa", "Persian", "فارسی", "fa"),
    POLISH("pl", "Polish", "Polski", "pl"),
    PORTUGUESE("pt", "Portuguese", "Português", "pt"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", "pa"),
    ROMANIAN("ro", "Romanian", "Română", "ro"),
    RUSSIAN("ru", "Russian", "Русский", "ru"),
    SAMOAN("sm", "Samoan", "Gagana Samoa", "sm"),
    SCOTS_GAELIC("gd", "Scots gaelic", "Gàidhlig", "gd"),
    SERBIAN_CYR("sr-cyr", "Serbian (Cyrillic)", "Српски (Ћирилица)", "sr"),
    SERBIAN_LAT("sr-lat", "Serbian (Latin)", "Srpski (Latinica)", "sr"),
    SESOTHO("st", "Sesotho", "Sesotho", "st"),
    SHONA("sn", "Shona", "ChiShona", "sn"),
    SINDHI("sd", "Sindhi", "سنڌي", "sd"),
    SINHALA("si", "Sinhala", "සිංහල", "si"),
    SLOVAK("sk", "Slovak", "Slovenčina", "sk"),
    SLOVENIAN("sl", "Slovenian", "Slovenščina", "sl"),
    SOMALI("so", "Somali", "Af Soomaali", "so"),
    SPANISH("es", "Spanish", "Español", "es"),
    SUNDANESE("su", "Sundanese", "Basa Sunda", "su"),
    SWAHILI("sw", "Swahili", "Kiswahili", "sw"),
    SWEDISH("sv", "Swedish", "Svenska", "sv"),
    TAJIK("tg", "Tajik", "Тоҷикӣ", "tg"),
    TAMIL("ta", "Tamil", "தமிழ்", "ta"),
    TELUGU("te", "Telugu", "తెలుగు", "te"),
    THAI("th", "Thai", "ไทย", "th"),
    TURKISH("tr", "Turkish", "Türkçe", "tr"),
    UKRAINIAN("uk", "Ukrainian", "Українська", "uk"),
    URDU("ur", "Urdu", "اردو", "ur"),
    UYGHUR("ug", "Uyghur", "ئۇيغۇرچە", "ug"),
    UZBEK("uz", "Uzbek", "Oʻzbekcha", "uz"),
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt", "vi"),
    WELSH("cy", "Welsh", "Cymraeg", "cy"),
    XHOSA("xh", "Xhosa", "isiXhosa", "xh"),
    YIDDISH("yi", "Yiddish", "ייִדיש", "yi"),
    YORUBA("yo", "Yoruba", "Èdè Yorùbá", "yo"),
    ZULU("zu", "Zulu", "isiZulu", "zu");

    private String langCode;
    private String name;
    private String nativeName;
    private String googleCode;

    LanguagesEnum(String langCode, String name, String nativeName, String googleCode) {
        this.langCode = langCode;
        this.name = name;
        this.nativeName = nativeName;
        this.googleCode = googleCode;
    }

    public String getLangCode() {
        return langCode;
    }

    public String getName() {
        return name;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getGoogleCode() {
        return googleCode;
    }

    public static LanguagesEnum fromLangCode(String code) {
        for (LanguagesEnum lang : values()) {
            if (lang.getLangCode().equals(code)) {
                return lang;
            }
        }
        return null;
    }

    public static LanguagesEnum fromGoogleCode(String code) {
        for (LanguagesEnum lang : values()) {
            if (lang.getGoogleCode().equals(code)) {
                return lang;
            }
        }
        return null;
    }

    public static LanguagesEnum fromName(String name) {
        for (LanguagesEnum lang : values()) {
            if (lang.getName().equals(name)) {
                return lang;
            }
        }
        return null;
    }

    public static LanguagesEnum fromNativeName(String name) {
        for (LanguagesEnum lang : values()) {
            if (lang.getNativeName().equals(name)) {
                return lang;
            }
        }
        return null;
    }

    public static List<String> getLanguageNames() {
        List<String> names = new ArrayList<>(); // List of language names
        for (LanguagesEnum lang : values()) {
            names.add(lang.getName());
        }
        return names;
    }

}
