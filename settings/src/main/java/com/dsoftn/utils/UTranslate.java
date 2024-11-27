package com.dsoftn.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;


public class UTranslate {
    public static final String TRANSLATOR_SERVER_HOST = "127.0.0.1";
    public static final int TRANSLATOR_SERVER_PORT = 29975;

    private static final String API_KEY = "YOUR_API_KEY";
    private static final String TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2";

    private static final Map<Character, String> cyrillicToLatinMap = new HashMap<>();
    static {
        cyrillicToLatinMap.put('А', "A");
        cyrillicToLatinMap.put('Б', "B");
        cyrillicToLatinMap.put('В', "V");
        cyrillicToLatinMap.put('Г', "G");
        cyrillicToLatinMap.put('Д', "D");
        cyrillicToLatinMap.put('Ђ', "Đ");
        cyrillicToLatinMap.put('Е', "E");
        cyrillicToLatinMap.put('Ж', "Ž");
        cyrillicToLatinMap.put('З', "Z");
        cyrillicToLatinMap.put('И', "I");
        cyrillicToLatinMap.put('Ј', "J");
        cyrillicToLatinMap.put('К', "K");
        cyrillicToLatinMap.put('Л', "L");
        cyrillicToLatinMap.put('Љ', "Lj");
        cyrillicToLatinMap.put('М', "M");
        cyrillicToLatinMap.put('Н', "N");
        cyrillicToLatinMap.put('Њ', "Nj");
        cyrillicToLatinMap.put('О', "O");
        cyrillicToLatinMap.put('П', "P");
        cyrillicToLatinMap.put('Р', "R");
        cyrillicToLatinMap.put('С', "S");
        cyrillicToLatinMap.put('Т', "T");
        cyrillicToLatinMap.put('Ћ', "Ć");
        cyrillicToLatinMap.put('У', "U");
        cyrillicToLatinMap.put('Ф', "F");
        cyrillicToLatinMap.put('Х', "H");
        cyrillicToLatinMap.put('Ц', "C");
        cyrillicToLatinMap.put('Ч', "Č");
        cyrillicToLatinMap.put('Џ', "Dž");
        cyrillicToLatinMap.put('Ш', "Š");
        cyrillicToLatinMap.put('а', "a");
        cyrillicToLatinMap.put('б', "b");
        cyrillicToLatinMap.put('в', "v");
        cyrillicToLatinMap.put('г', "g");
        cyrillicToLatinMap.put('д', "d");
        cyrillicToLatinMap.put('ђ', "đ");
        cyrillicToLatinMap.put('е', "e");
        cyrillicToLatinMap.put('ж', "ž");
        cyrillicToLatinMap.put('з', "z");
        cyrillicToLatinMap.put('и', "i");
        cyrillicToLatinMap.put('ј', "j");
        cyrillicToLatinMap.put('к', "k");
        cyrillicToLatinMap.put('л', "l");
        cyrillicToLatinMap.put('љ', "lj");
        cyrillicToLatinMap.put('м', "m");
        cyrillicToLatinMap.put('н', "n");
        cyrillicToLatinMap.put('њ', "nj");
        cyrillicToLatinMap.put('о', "o");
        cyrillicToLatinMap.put('п', "p");
        cyrillicToLatinMap.put('р', "r");
        cyrillicToLatinMap.put('с', "s");
        cyrillicToLatinMap.put('т', "t");
        cyrillicToLatinMap.put('ћ', "ć");
        cyrillicToLatinMap.put('у', "u");
        cyrillicToLatinMap.put('ф', "f");
        cyrillicToLatinMap.put('х', "h");
        cyrillicToLatinMap.put('ц', "c");
        cyrillicToLatinMap.put('ч', "č");
        cyrillicToLatinMap.put('џ', "dž");
        cyrillicToLatinMap.put('ш', "š");
    }

    private static final Map<String, Character> latinToCyrillicMap = new HashMap<>();
    static {
        latinToCyrillicMap.put("Lj", 'Љ');
        latinToCyrillicMap.put("LJ", 'Љ');
        latinToCyrillicMap.put("Nj", 'Њ');
        latinToCyrillicMap.put("NJ", 'Њ');
        latinToCyrillicMap.put("Dž", 'Џ');
        latinToCyrillicMap.put("DŽ", 'Џ');
        latinToCyrillicMap.put("A", 'А');
        latinToCyrillicMap.put("B", 'Б');
        latinToCyrillicMap.put("V", 'В');
        latinToCyrillicMap.put("G", 'Г');
        latinToCyrillicMap.put("D", 'Д');
        latinToCyrillicMap.put("Đ", 'Ђ');
        latinToCyrillicMap.put("E", 'Е');
        latinToCyrillicMap.put("Ž", 'Ж');
        latinToCyrillicMap.put("Z", 'З');
        latinToCyrillicMap.put("I", 'И');
        latinToCyrillicMap.put("J", 'Ј');
        latinToCyrillicMap.put("K", 'К');
        latinToCyrillicMap.put("L", 'Л');
        latinToCyrillicMap.put("M", 'М');
        latinToCyrillicMap.put("N", 'Н');
        latinToCyrillicMap.put("O", 'О');
        latinToCyrillicMap.put("P", 'П');
        latinToCyrillicMap.put("R", 'Р');
        latinToCyrillicMap.put("S", 'С');
        latinToCyrillicMap.put("T", 'Т');
        latinToCyrillicMap.put("Ć", 'Ћ');
        latinToCyrillicMap.put("U", 'У');
        latinToCyrillicMap.put("F", 'Ф');
        latinToCyrillicMap.put("H", 'Х');
        latinToCyrillicMap.put("C", 'Ц');
        latinToCyrillicMap.put("Č", 'Ч');
        latinToCyrillicMap.put("Š", 'Ш');
        latinToCyrillicMap.put("a", 'а');
        latinToCyrillicMap.put("b", 'б');
        latinToCyrillicMap.put("v", 'в');
        latinToCyrillicMap.put("g", 'г');
        latinToCyrillicMap.put("d", 'д');
        latinToCyrillicMap.put("đ", 'ђ');
        latinToCyrillicMap.put("e", 'е');
        latinToCyrillicMap.put("ž", 'ж');
        latinToCyrillicMap.put("z", 'з');
        latinToCyrillicMap.put("i", 'и');
        latinToCyrillicMap.put("j", 'ј');
        latinToCyrillicMap.put("k", 'к');
        latinToCyrillicMap.put("l", 'л');
        latinToCyrillicMap.put("lj", 'љ');
        latinToCyrillicMap.put("m", 'м');
        latinToCyrillicMap.put("n", 'н');
        latinToCyrillicMap.put("nj", 'њ');
        latinToCyrillicMap.put("o", 'о');
        latinToCyrillicMap.put("p", 'п');
        latinToCyrillicMap.put("r", 'р');
        latinToCyrillicMap.put("s", 'с');
        latinToCyrillicMap.put("t", 'т');
        latinToCyrillicMap.put("ć", 'ћ');
        latinToCyrillicMap.put("u", 'у');
        latinToCyrillicMap.put("f", 'ф');
        latinToCyrillicMap.put("h", 'х');
        latinToCyrillicMap.put("c", 'ц');
        latinToCyrillicMap.put("č", 'ч');
        latinToCyrillicMap.put("dž", 'џ');
        latinToCyrillicMap.put("š", 'ш');
    }

    // Language ENUM
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

    // Transalate Service
    public enum TranslateServiceEnum {
        TRANSLATOR_SERVER_FREE,
        GOOGLE_API_WITH_KEY,
        GOOGLE_PUBLIC_HTTP_FREE;
    }

    // STATIC METHODS

    public static String translate(String text, LanguagesEnum fromLang, LanguagesEnum toLang) {
        return translate(text, fromLang, toLang, TranslateServiceEnum.TRANSLATOR_SERVER_FREE);
    }

    public static String translate(String text, LanguagesEnum fromLang, LanguagesEnum toLang, TranslateServiceEnum service) {
        if (text == null || fromLang == null || toLang == null) {
            return null;
        }

        // If From and To languages are the same, return the same text
        if (fromLang.equals(toLang)) {
            return text;
        }

        // If From language is Serbian (Cyrillic) and To language is Serbian (Latin), convert Serbian (Cyrillic) to Serbian (Latin)
        if (fromLang.equals(LanguagesEnum.SERBIAN_CYR) && toLang.equals(LanguagesEnum.SERBIAN_LAT)) {
            text = UTranslate.convertCyrillicToLatin(text);
            return text;
        }

        // If From language is Serbian (Latin) and To language is Serbian (Cyrillic), convert Serbian (Latin) to Serbian (Cyrillic)
        if (fromLang.equals(LanguagesEnum.SERBIAN_LAT) && toLang.equals(LanguagesEnum.SERBIAN_CYR)) {
            text = UTranslate.convertLatinToCyrillic(text);
            return text;
        }

        // Call translate service
        if (service.equals(TranslateServiceEnum.TRANSLATOR_SERVER_FREE)) {
            String translatedText = UTranslate.translateUsingTranslatorServer(text, fromLang, toLang);
            if (toLang.equals(LanguagesEnum.SERBIAN_LAT)) {
                return convertCyrillicToLatin(translatedText);
            } else {
                return translatedText;
            }
        }
        else if (service.equals(TranslateServiceEnum.GOOGLE_API_WITH_KEY)) {
            String translatedText = UTranslate.translateUsingGoogleAPI(text, fromLang, toLang);
            if (toLang.equals(LanguagesEnum.SERBIAN_LAT)) {
                return convertCyrillicToLatin(translatedText);
            } else {
                return translatedText;
            }
        }
        else if (service.equals(TranslateServiceEnum.GOOGLE_PUBLIC_HTTP_FREE)) {
            String translatedText = UTranslate.translateUsingGooglePublicHTTP(text, fromLang, toLang);
            if (toLang.equals(LanguagesEnum.SERBIAN_LAT)) {
                return convertCyrillicToLatin(translatedText);
            } else {
                return translatedText;
            }
        }

        return null;
    }

    public static String translateUsingGooglePublicHTTP(String text, LanguagesEnum fromLang, LanguagesEnum toLang) {
        // LIMITAIONS:
        // Up to 5000 characters per request
        // Up to 100 requests per minute (recommended: 60)

        OkHttpClient client = new OkHttpClient();

        // Encode the query parameters
        String query;
        try {
            query = URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }

        // Build the URL
        String url = String.format("https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s", 
                                fromLang.getGoogleCode(), toLang.getGoogleCode(), query);

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build();


        // Execute the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Unexpected code " + response);
            }

            // Parse the response body
            String responseBody = response.body().string();

            // Extract the translated text
            JSONArray jsonArray = new JSONArray(responseBody);
            String translatedText = jsonArray.getJSONArray(0).getJSONArray(0).getString(0);

            return translatedText;
        }
        catch (IOException e) {
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String translateUsingGoogleAPI(String text, LanguagesEnum fromLang, LanguagesEnum toLang) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("q", text);
        jsonRequest.put("target", toLang.getGoogleCode());

        RequestBody body = RequestBody.create(
                jsonRequest.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(TRANSLATE_URL + "?key=" + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getJSONObject("data").getJSONArray("translations")
                    .getJSONObject(0).getString("translatedText");
        }
        catch (IOException e) {
            return null;
        }
    }

    public static String translateUsingTranslatorServer(String text, LanguagesEnum fromLang, LanguagesEnum toLang) {
        try (Socket socket = new Socket(UTranslate.TRANSLATOR_SERVER_HOST, UTranslate.TRANSLATOR_SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Make Map object
            Map<String, String> data = new HashMap<>();
            data.put("text", text);
            data.put("from", fromLang.getGoogleCode());
            data.put("to", toLang.getGoogleCode());

            // Create JSON String
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonRequest = gson.toJson(data);
            byte[] jsonBytes = jsonRequest.getBytes("UTF-8");
            int size = jsonBytes.length;

            // Sending JSON size
            out.print(size + "\n");
            out.flush();

            // Sending JSON
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(jsonBytes);
            outputStream.flush();

            // Receiving JSON size byte by byte until it reaches "\n"
            StringBuilder sizeBuilder = new StringBuilder();
            char c;
            
            try {
                while ((c = (char) in.read()) != '\n') {
                    sizeBuilder.append(c);
                }
            } catch (IOException e) {
                return null;
            }
            String sizeResponse = sizeBuilder.toString();
            int responseSize = Integer.parseInt(sizeResponse.trim());

            // Receiving JSON Response
            StringBuilder responseBuilder = new StringBuilder();
            char[] buffer = new char[1024];
            int totalRead = 0;
            
            try {
                while (totalRead < responseSize) {
                    int read = in.read(buffer);
                    totalRead += read;
                    responseBuilder.append(buffer, 0, read);
                }
            }
            catch (IOException e) {
                return null;
            }
            
            String jsonResponse = responseBuilder.toString();

            // Parsing JSON Response
            Map<String, String> response = gson.fromJson(jsonResponse, new TypeToken<Map<String, String>>() {
            }.getType());

            String translatedText = response.get("translated_text");
            
            return translatedText;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isTranslatorServerRunning() {
       try (Socket socket = new Socket(UTranslate.TRANSLATOR_SERVER_HOST, UTranslate.TRANSLATOR_SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonRequest = "ignore";
            byte[] jsonBytes = jsonRequest.getBytes("UTF-8");
            int size = jsonBytes.length;

            // Sending JSON size
            out.print(size + "\n");
            out.flush();

            // Sending JSON
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(jsonBytes);
            outputStream.flush();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean startTranslatorServer() {
        boolean result;
        result = UFile.startFile("translator.py", "-server");
        if (!result) {
            result = UFile.startFile("translator.exe", "-server");
        }

        return result;
    }

    public static String convertCyrillicToLatin(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (cyrillicToLatinMap.containsKey(c)) {
                result.append(cyrillicToLatinMap.get(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();        
    }
 
    public static String convertLatinToCyrillic(String text) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < text.length()) {
            boolean matched = false;

            // Check for special cases with two letters (eg. 'lj' or 'nj')
            if (i + 1 < text.length()) {
                String twoLetters = text.substring(i, i + 2);
                if (latinToCyrillicMap.containsKey(twoLetters)) {
                    result.append(latinToCyrillicMap.get(twoLetters));
                    i += 2;
                    matched = true;
                }
            }

            // If not matched, check for single letter
            if (!matched) {
                String oneLetter = text.substring(i, i + 1);
                if (latinToCyrillicMap.containsKey(oneLetter)) {
                    result.append(latinToCyrillicMap.get(oneLetter));
                } else {
                    result.append(oneLetter);
                }
                i++;
            }
        }

        return result.toString();
    }


}
