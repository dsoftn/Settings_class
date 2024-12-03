package com.dsoftn.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
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

    private static final String GOOGLE_CLOUD_API_KEY = "YOUR_API_KEY";
    private static final String GOOGLE_CLOUD_TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2";

    private static final Integer TRANSLATION_MAX_PART_SIZE = 3000;
    private static final Integer TRANSLATION_MIN_PART_SIZE = 2500;
    private static final Integer TRANSLATION_PAUSE_BETWEEN_TRANSLATIONS_MS = 1000;
    private static final Integer TRANSLATION_MAX_RETRIES = 3;
    private static final String[] TRANSLATION_DELIMITERS = {"<29975)>", "<01)34)>", "<1d2s3o4f5t6n>", "<653424>", "<21367154352>"};
    private static final TranslateServiceEnum TRANSLATION_DEFAULT_SERVICE = TranslateServiceEnum.TRANSLATOR_SERVER_FREE;

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

    // Translate Service
    public enum TranslateServiceEnum {
        TRANSLATOR_SERVER_FREE,
        GOOGLE_API_WITH_KEY,
        GOOGLE_PUBLIC_HTTP_FREE;
    }

    // STATIC METHODS

    public static Map<String, String> translateMap(Map<String, String> mapToTranslate, LanguagesEnum fromLang, LanguagesEnum toLang) {
        return translateMap(mapToTranslate, fromLang, toLang, UTranslate.TRANSLATION_DEFAULT_SERVICE);
    }

    public static Map<String, String> translateMap(Map<String, String> mapToTranslate, LanguagesEnum fromLang, LanguagesEnum toLang, TranslateServiceEnum service) {
        if (mapToTranslate == null || fromLang == null || toLang == null) {
            return null;
        }

        // Convert the map to a list
        List<String> listOfKeys = new ArrayList<>();
        List<String> listOfValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : mapToTranslate.entrySet()) {
            listOfKeys.add(entry.getKey());
            listOfValues.add(entry.getValue());
        }

        // Translate the list
        List<String> translatedListOfValues = translateList(listOfValues, fromLang, toLang, service);
        if (translatedListOfValues == null) {
            System.out.println("Translate Error: Could not translate the map.");
            return null;
        }

        // Check if the lists have the same size
        if (listOfKeys.size() != translatedListOfValues.size()) {
            System.out.println("Translate Error: The lists have different sizes.");
            return null;
        }

        // Convert the list back to a map
        Map<String, String> translatedMap = new HashMap<>();
        for (int i = 0; i < listOfKeys.size(); i++) {
            translatedMap.put(listOfKeys.get(i), translatedListOfValues.get(i));
        }
        return translatedMap;
    }

    public static List<String> translateList(List<String> listToTranslate, LanguagesEnum fromLang, LanguagesEnum toLang) {
        return translateList(listToTranslate, fromLang, toLang, UTranslate.TRANSLATION_DEFAULT_SERVICE);
    }

    public static List<String> translateList(List<String> listToTranslate, LanguagesEnum fromLang, LanguagesEnum toLang, TranslateServiceEnum service) {
        if (listToTranslate == null || fromLang == null || toLang == null) {
            return null;
        }

        List<String> translatedList = new ArrayList<>(); // List of translated text

        // Find the delimiter that is safe for the list
        String delimiterInUse = null;
        for (String delimiter : UTranslate.TRANSLATION_DELIMITERS) {
            if (isDelimiterSafeForList(listToTranslate, delimiter)) {
                delimiterInUse = delimiter;
                break;
            }
        }

        if (delimiterInUse == null) {
            System.out.println("No delimiter found for list of text to translate.");
            return null;
        }

        // Create text to translate
        String textToTranslate = String.join(delimiterInUse, listToTranslate);
        List<String> textToTranslateList = UTranslate.splitByDelimiter(textToTranslate, delimiterInUse, UTranslate.TRANSLATION_MAX_PART_SIZE, UTranslate.TRANSLATION_MIN_PART_SIZE);
        if (!UTranslate.arePartsValid(textToTranslateList, UTranslate.TRANSLATION_MAX_PART_SIZE, UTranslate.TRANSLATION_MIN_PART_SIZE)) {
            System.out.println("Parts of text to translate are not valid.");
            return null;
        }

        // Translate
        String translatedText = "";
        for (String part : textToTranslateList) {
            String translatedPart = translate(part, fromLang, toLang, service);
            if (translatedPart == null) {
                return null;
            }
            translatedText += translatedPart;
        }

        // Split translated text by delimiter
        String[] translatedParts = translatedText.split(Pattern.quote(delimiterInUse), -1);
        if (translatedParts.length != listToTranslate.size()) {
            System.out.println("Number of translated parts does not match number of original parts.");
            return null;
        }

        // Add translated text to list
        for (String translatedPart : translatedParts) {
            translatedList.add(translatedPart);
        }

        return translatedList;
    }

    private static boolean isDelimiterSafeForList(List<String> listToTranslate, String delimiter) {
        for (String text : listToTranslate) {
            if (text.contains(delimiter)) {
                return false;
            }
        }
        return true;
    }

    public static String translate(String text, LanguagesEnum fromLang, LanguagesEnum toLang) {
        return translate(text, fromLang, toLang, UTranslate.TRANSLATION_DEFAULT_SERVICE);
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
            // Translate Server has his own handler for large texts
            String translatedText = UTranslate.translateUsingTranslatorServer(text, fromLang, toLang);
            if (toLang.equals(LanguagesEnum.SERBIAN_LAT)) {
                return convertCyrillicToLatin(translatedText);
            } else {
                return translatedText;
            }
        }
        else {
            // Implement handler for large texts
            List<String> chunks = splitText(text, UTranslate.TRANSLATION_MAX_PART_SIZE, UTranslate.TRANSLATION_MIN_PART_SIZE);
            String translatedText = "";
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                String chunkTranslatedText = "";
                for (int j = 0; j < UTranslate.TRANSLATION_MAX_RETRIES; j++) {
                    try {
                        if (service.equals(TranslateServiceEnum.GOOGLE_API_WITH_KEY)) {
                            chunkTranslatedText = UTranslate.translateUsingGoogleAPI(chunk, fromLang, toLang);
                        } else if (service.equals(TranslateServiceEnum.GOOGLE_PUBLIC_HTTP_FREE)) {
                            chunkTranslatedText = UTranslate.translateUsingGooglePublicHTTP(chunk, fromLang, toLang);
                        }
                    } catch (Exception e) {
                        chunkTranslatedText = null;
                    }

                    try {
                        // Wait before next retry
                        if (chunkTranslatedText == null || i < chunks.size() - 1) {
                            Thread.sleep(UTranslate.TRANSLATION_PAUSE_BETWEEN_TRANSLATIONS_MS);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }

                    if (chunkTranslatedText != null) {
                        break;
                    }
                }

                if (chunkTranslatedText == null) {
                    return null;
                }

                translatedText += chunkTranslatedText;
            }

            if (toLang.equals(LanguagesEnum.SERBIAN_LAT)) {
                return convertCyrillicToLatin(translatedText);
            }

            return translatedText;
        }
    }

    public static List<String> splitText(String text, int maxPartSize, int minPartSize) {
        List<String> parts = new ArrayList<>();
        
        // 1. Try to split by newlines
        parts = splitByDelimiter(text, "\n", maxPartSize, minPartSize);
        if (arePartsValid(parts, maxPartSize, minPartSize)) {
            return parts;
        }
        
        // 2. Try to split by punctuation
        parts = splitByDelimiter(text, "(?<=[.?!])\\s*", maxPartSize, minPartSize);
        if (arePartsValid(parts, maxPartSize, minPartSize)) {
            return parts;
        }

        // 3. Try to split by spaces
        parts = splitByDelimiter(text, "\\s+", maxPartSize, minPartSize);
        if (arePartsValid(parts, maxPartSize, minPartSize)) {
            return parts;
        }

        // 4. Try to split by fixed size
        return splitByFixedSize(text, maxPartSize);
    }

    private static List<String> splitByDelimiter(String text, String delimiter, int maxPartSize, int minPartSize) {
        String[] segments = text.split(Pattern.quote(delimiter), -1);
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
    
        boolean hasRemainingText = false;
        int counter = 0;

        for (String segment : segments) {
            hasRemainingText = true;
            if (currentPart.length() + segment.length() + delimiter.length() > maxPartSize) {
                if (currentPart.length() >= minPartSize) {
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                    hasRemainingText = false;
                    counter = 0;
                }
            }
            if (counter > 0) {
                currentPart.append(delimiter).append(segment);
            } else {
                currentPart.append(segment);
            }
            counter++;
        }
    
        // Add the last part
        if (hasRemainingText) {
            parts.add(currentPart.toString());
        }
    
        return parts;
    }

    private static boolean arePartsValid(List<String> parts, int maxPartSize, int minPartSize) {
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (i < parts.size() - 1 && (part.length() > maxPartSize || part.length() < minPartSize)) {
                return false;
            }
            if (i == parts.size() - 1 && part.length() > maxPartSize) {
                return false;
            }
        }
        return true;
    }

    private static List<String> splitByFixedSize(String text, int maxPartSize) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < text.length(); i += maxPartSize) {
            parts.add(text.substring(i, Math.min(i + maxPartSize, text.length())));
        }
        return parts;
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
                .url(GOOGLE_CLOUD_TRANSLATE_URL + "?key=" + GOOGLE_CLOUD_API_KEY)
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
