package com.dsoftn.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class UTranslate {

    // Language ENUM
    public enum LanguagesEnum {
        ENGLISH("en", "English", "English", "en"),
        SERBIAN("sr", "Serbian", "Srpski", "sr"),
        SERBIAN_CYR("sr-cyr", "Serbian (Cyrillic)", "Srpski (Ćirilica)", "sr"),
        SERBIAN_LAT("sr-lat", "Serbian (Latin)", "Srpski (Latinica)", "sr"),
        CROATIAN("hr", "Croatian", "Hrvatski", "hr"),
        GERMAN("de", "German", "Deutsch", "de"),
        FRENCH("fr", "French", "Francais", "fr"),
        SPANISH("es", "Spanish", "Espagnol", "es"),
        PORTUGUESE("pt", "Portuguese", "Português", "pt"),
        ITALIAN("it", "Italian", "Italiano", "it");

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

    public static String translate(String text, String fromLang, String toLang) {
        return text;
    }
    
}
