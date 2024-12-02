package com.dsoftn.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.File;
import java.util.stream.Collectors;
import java.lang.reflect.Type;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import com.dsoftn.utils.PyDict;
import com.dsoftn.utils.UTranslate.LanguagesEnum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

// Structure of settings and language, Note: appSettings has same structure as userSettings
//
// Settings:
// {"key1":"value1",
//  "key2":"value2",
//  ...
//  "keyN":"valueN"}
//
// Language:
// { "available_languages": [ [ "code": "en", "name": "English" ],
//                            [ "code": "de", "name": "Deutsch" ],
//                            ...],
//  "active_language": "en",
//  "data": { "langCode1": { "key1":"value1", "key2":"value2", ... },
//            "langCode2": { "key1":"value1", "key2":"value2", ... },
//            ... }
// }


/**
 * <h3>Contains App and User settings as well as language information</h3>
 * <ul>
 * <li><p>Init with:</p>
 * <p><i>>>> Settings settings = new Settings();</i></p></li>
 * <li><p>If you want to work with SettingsItems you must provide file name from which SettingsItems will be loaded</p>
 * <p><i>>>> settings.userSettingsFilePath = "your_file_path";</i></p></li>
 * <li><p>If you want to work with LanguageItems you must provide file name from which LanguageItems will be loaded and active language</p>
 * <p><i>>>> settings.languagesFilePath = "your_file_path";</i></p>
 * <p><i>>>> settings.activeLanguage = "en";</i> // (language code)</p></li>
 * <li><p>If you want to work with saved Application settings you must provide file name from which Application settings will be loaded</p>
 * <p><i>>>> settings.appDataFilePath = "your_file_path";</i></p></li>
 * <li><p>Optionally you can provide file name from which DefaultSettings will be loaded</p>
 * <p><i>>>> settings.defaultSettingsFilePath = "your_file_path";</i></p></li>
 * </ul>
 * <p>If you set DefaultSettings all SettingsItems that missing in UserSettings will be loaded from DefaultSettings</p>
 * <p>Also all SettingsItems in UserSettings that have SettingsType=DEFAULT will be updated from DefaultSettings</p>
  */
public class Settings {

    public String languagesFilePath;
    public String defaultSettingsFilePath;
    public String userSettingsFilePath;
    public String appDataFilePath;

    private PyDict data = new PyDict();
    private PyDict lang = new PyDict();
    private PyDict appData = new PyDict();

    private String activeLanguage = "";

    private String lastErrorString = "";

    // Constructors
    
    public Settings(String userSettingsFilePath, String defaultSettingsFilePath, String languageFilePath, String activeLanguage, String appSettingsFilePath) {
        this.userSettingsFilePath = userSettingsFilePath;
        this.defaultSettingsFilePath = defaultSettingsFilePath;
        this.languagesFilePath = languageFilePath;
        this.activeLanguage = activeLanguage;
        this.appDataFilePath = appSettingsFilePath;
    }

    public Settings() {
        this.userSettingsFilePath = "";
        this.defaultSettingsFilePath = "";
        this.languagesFilePath = "";
        this.activeLanguage = "";
        this.appDataFilePath = "";
    }

    // Checkers

    /**
     * <p>Check if User setting with specified key exists</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>boolean</b> <i>true</i> if setting exists, <i>false</i> otherwise
     */
    public boolean isUserSettingExists(String key) {
        return data.containsKey(key);
    }

    /**
     * <p>Check if Application setting with specified key exists</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>boolean</b> <i>true</i> if setting exists, <i>false</i> otherwise
     */
    public boolean isAppSettingExists(String key) {
        return appData.containsKey(key);
    }

    /**
     * <p>Check if language with specified key exists</p>
     * <p><b>NOTE:</b> If activeLanguage is empty, this method returns false</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>boolean</b> <i>true</i> if setting exists, <i>false</i> otherwise
     */
    public boolean isLanguageKeyExists(String key) {
        if (activeLanguage.isEmpty()) {
            return false;
        }

        return lang.isPyDictKeyExists(PyDict.concatKeys("data", activeLanguage, key));
    }

    /**
     * <p>Check if language with specified code exists</p>
     * @param code - <b>String</b> <i>code</i> - code of language
     * @return <b>boolean</b> <i>true</i> if setting exists, <i>false</i> otherwise
     */
    public boolean isLanguageCodeAvailable(String code) {
        List<String> langCodes = getAvailableLanguageCodes();

        return langCodes.contains(code);
    }

    /**
     * <p>Check if language with specified name exists</p>
     * @param name - <b>String</b> <i>name</i> - name of language
     * @return <b>boolean</b> <i>true</i> if setting exists, <i>false</i> otherwise
     */
    public boolean isLanguageNameAvailable(String name) {
        List<String> langNames = getAvailableLanguageNames();

        return langNames.contains(name);
    }

    // Get / Set USER, APP and LANG objects (SettingsItem, LanguageItem)

    /**
     * <p>Get User settings object (SettingsItem) for specified key</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>SettingsItem</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     */
    public SettingsItem getUserSettingsItem(String key) {
        if (! isUserSettingExists(key)) {
            printError("User settings " + key + " not found");
            throw new RuntimeException("User settings " + key + " not found");
        }
        return (SettingsItem) data.get(key);
    }

    /**
     * <p>Set User settings object (SettingsItem) for specified key</p>
     * @param value - <b>SettingsItem</b> <i>value</i> - value of setting
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     */
    public boolean setUserSettingsItem(SettingsItem value) {
        return setUserSettingsItem(value.getKey(), value);
    }

    /**
     * <p>Set User settings object (SettingsItem) for specified key</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>SettingsItem</b> <i>value</i> - value of setting
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     */
    public boolean setUserSettingsItem(String key, SettingsItem value) {
        return data.setPyDictValue(key, value);
    }

    public boolean deleteUserSettingsItem(String key) {
        return data.remove(key) != null;
    }

    /**
     * <p>Get Application settings object (SettingsItem) for specified key</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>SettingsItem</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     */
    public SettingsItem getAppSettingsItem(String key) {
        if (! isAppSettingExists(key)) {
            printError("App settings " + key + " not found");
            throw new RuntimeException("App settings " + key + " not found");
        }
        return (SettingsItem) appData.get(key);
    }

    /**
     * <p>Set Application settings object (SettingsItem) for specified key</p>
     * @param value - <b>SettingsItem</b> <i>value</i> - value of setting
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     */
    public boolean setAppSettingsItem(SettingsItem value) {
        return setAppSettingsItem(value.getKey(), value);
    }

    /**
     * <p>Set Application settings object (SettingsItem) for specified key</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>SettingsItem</b> <i>value</i> - value of setting
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     */
    public boolean setAppSettingsItem(String key, SettingsItem value) {
        return appData.setPyDictValue(key, value);
    }

    /**
     * <p>Get Language settings object (LanguageItem) for specified key and LangCode</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param fromLangCode - <b>String</b> <i>fromLangCode</i> - Language Code
     * @return <b>LanguageItem</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     */
    public LanguageItem getLanguageItem(String key, String fromLangCode) {
        return (LanguageItem) lang.getPyDictValue(PyDict.concatKeys("data", fromLangCode, key));
    }

    /**
     * <p>Get Language settings object (LanguageItem) for specified key</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>LanguageItem</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found or active language not set
     */
    public LanguageItem getLanguageItem(String key) {
        if (activeLanguage.isEmpty()) {
            printError("No active language set");
            throw new RuntimeException("No active language set");
        }

        return getLanguageItem(key, activeLanguage);
    }

    /**
     * <p>Set Language settings object (LanguageItem) for specified key</p>
     * @param value - <b>LanguageItem</b> <i>value</i> - value of setting
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     * @throws RuntimeException if active language not set
     */
    public boolean setLanguageItem(LanguageItem value) {
        return setLanguageItem(value.getKey(), value, value.getLanguageCode());
    }

    /**
     * <p>Set Language settings object (LanguageItem) for specified key and LangCode</p>
     * <p>If Language code not found return false</p>
     * @param value - <b>LanguageItem</b> <i>value</i> - value of setting
     * @param forLangCode - <b>String</b> <i>forLangCode</i> - Language Code
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     */
    public boolean setLanguageItem(LanguageItem value, String forLangCode) {
        return setLanguageItem(value.getKey(), value, forLangCode);
    }

    /**
     * <p>Set Language settings object (LanguageItem) for specified key and LangCode</p>
     * <p>If Language code not found return false</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>LanguageItem</b> <i>value</i> - value of setting
     * @param forLangCode - <b>String</b> <i>forLangCode</i> - Language Code
     * @return <b>boolean</b> <i>true</i> if setting was set, <i>false</i> otherwise
     */
    public boolean setLanguageItem(String key, LanguageItem value, String forLangCode) {
        if (!getAvailableLanguageCodes().contains(forLangCode)) {
            return false;
        }

        return lang.setPyDictValue(PyDict.concatKeys("data", forLangCode, key), value);
    }

    public boolean deleteLanguageItem(String key) {
        PyDict langData;
        langData = lang.getPyDictValue("data");

        boolean isDeleted = false;

        for (String langCode : getAvailableLanguageCodes()) {
            if (langData.containsKey(langCode)) {
                PyDict item;
                item = langData.getPyDictValue(langCode);
                item.remove(key);
                isDeleted = true;
            }
        }

        return isDeleted;
    }

    public boolean isLanguageItemGroupHasAllRequiredLanguages(LanguageItemGroup langGroup) {
        List<String> langCodes = getAvailableLanguageCodes();

        for (String langCode : langCodes) {
            if (!langGroup.getListOfLanguageCodes().contains(langCode)) {
                return false;
            }
        }

        return true;
    }

    public boolean mergeLanguageItemGroup(LanguageItemGroup langGroup) {
        List<String> langCodes = getAvailableLanguageCodes();

        if (!isLanguageItemGroupHasAllRequiredLanguages(langGroup)) {
            return false;
        }

        boolean isMerged = false;
        for (LanguageItem item : langGroup.getLanguageItems()) {
            if (!langCodes.contains(item.getLanguageCode())) {
                continue;
            }

            isMerged = true;
            LanguageItem originalItem = getLanguageItem(item.getKey(), item.getLanguageCode());
            if (originalItem == null) {
                item.setUserData("");
                setLanguageItem(item, item.getLanguageCode());
            }
            else {
                originalItem.setValue(item.getValue());
            }
        }

        return isMerged;
    }

    /**
     * <p>Add new language in base if it doesn't exist</p>
     * @param newLang - <b>LanguagesEnum</b> <i>newLang</i> - Language
     * @return <b>boolean</b> <i>true</i> if language was added, <i>false</i> otherwise
     */
    public boolean addNewLanguageInBase(LanguagesEnum newLang) {
        List<String> availableLangCodes = getAvailableLanguageCodes();
        
        if (availableLangCodes.contains(newLang.getLangCode())) {
            return false;
        }

        ArrayList<ArrayList<String>> langCodes = lang.getPyDictValue("available_languages");
        langCodes.add(new ArrayList<>(List.of(newLang.getLangCode(), newLang.getName())));

        lang.setPyDictValue("available_languages", langCodes);

        if (!lang.isPyDictKeyExists(PyDict.concatKeys("data", newLang.getLangCode()))) {
            PyDict newDataKey = new PyDict();
            lang.setPyDictValue(PyDict.concatKeys("data", newLang.getLangCode()), newDataKey);
        }

        // Copy data from first available language
        if (availableLangCodes.size() > 0) {
            List<LanguageItem> items = getListAllLanguageItemsForLanguage(LanguagesEnum.fromLangCode(availableLangCodes.get(0)));

            for (LanguageItem item : items) {
                LanguageItem newItem = item.duplicate();
                newItem.setCreationDate(item.getCreationDate());
                newItem.setLanguageCode(newLang.getLangCode());
                newItem.setValue("");

                setLanguageItem(newItem, newLang.getLangCode());
            }
        }

        return true;
    }

    /**
     * <p>Remove language from base</p>
     * @param langToRemove - <b>LanguagesEnum</b> <i>langToRemove</i> - Language
     * @return <b>boolean</b> <i>true</i> if language was removed, <i>false</i> otherwise
     */
    public boolean removeLanguageFromBase(LanguagesEnum langToRemove) {
        if (!getAvailableLanguageCodes().contains(langToRemove.getLangCode())) {
            return false;
        }

        ArrayList<ArrayList<String>> langCodes = lang.getPyDictValue("available_languages");
        ArrayList<ArrayList<String>> newLangCodes = new ArrayList<>();

        for (ArrayList<String> langCode : langCodes) {
            if (!langCode.get(0).equals(langToRemove.getLangCode())) {
                newLangCodes.add(langCode);
            }
        }

        lang.setPyDictValue("available_languages", newLangCodes);

        PyDict langData;
        langData = lang.getPyDictValue("data");

        if (langData.containsKey(langToRemove.getLangCode())) {
            langData.remove(langToRemove.getLangCode());
        }

        return true;
    }

    public Integer countLanguagesInBase() {
        ArrayList<ArrayList<String>> langCodes = lang.getPyDictValue("available_languages");

        return langCodes.size();
    }

    public List<LanguageItem> getListAllLanguageItemsForLanguage(LanguagesEnum forLanguage) {
        Map<String, LanguageItem> langItems = lang.getPyDictValue(PyDict.concatKeys("data", forLanguage.getLangCode()));

        ArrayList<LanguageItem> langItemsList = new ArrayList<>();
        if (langItems != null) {
            langItemsList = new ArrayList<>(langItems.values());
        }
        return langItemsList;
    }

    // Getters and Setters for User values

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns ANY Object according to DataType set</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Object getvOBJECT(String key) {
        return checkAndGetUserSettingsItem(key).getValue();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns String Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public String getvSTRING(String key) {
        return checkAndGetUserSettingsItem(key).getValueSTRING();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns Integer Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Integer getvINTEGER(String key) {
        return checkAndGetUserSettingsItem(key).getValueINT();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns Double Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Double getvDOUBLE(String key) {
        return checkAndGetUserSettingsItem(key).getValueDOUBLE();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns Boolean Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Boolean getvBOOLEAN(String key) {
        return checkAndGetUserSettingsItem(key).getValueBOOLEAN();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns List Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public List<Object> getvLIST(String key) {
        return checkAndGetUserSettingsItem(key).getValueLIST();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns Map Object (PyDict|HashMap) representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Map<String, Object> getvMAP(String key) {
        return checkAndGetUserSettingsItem(key).getValueMAP();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns PyDict Object (PyDict|HashMap) representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public PyDict getvPYDICT(String key) {
        return checkAndGetUserSettingsItem(key).getValuePYDICT();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns LocalDate Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public LocalDate getvDATE(String key) {
        return checkAndGetUserSettingsItem(key).getValueDATE();
    }

    /**
     *  <p><b>User Settings</b></p>
     * <p>Returns LocalTime Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public LocalTime getvTIME(String key) {
        return checkAndGetUserSettingsItem(key).getValueTIME();
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Returns LocalDateTime Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public LocalDateTime getvDATETIME(String key) {
        return checkAndGetUserSettingsItem(key).getValueDATETIME();
    }

    /**
     * <p>Get User settings value for specified key</p>
     * <p>Alias for <i>getv</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     * @see setSettingsValue
     */
    private SettingsItem checkAndGetUserSettingsItem(String key) {
        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        if (! isUserSettingExists(key)) {
            printError("User settings " + key + " not found");
            throw new RuntimeException("User settings " + key + " not found");
        }
        SettingsItem item = (SettingsItem) data.get(key);

        return item;
    }

    /**
     * <p><b>User Settings</b></p>
     * <p>Sets User settings value for specified key</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     */
    public void setv(String key, Object value) {
        setUserSettingsValue(key, value);
    }

    private void setUserSettingsValue(String key, Object value) {
        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        if (! isUserSettingExists(key)) {
            printError("User settings " + key + " not found");
            throw new RuntimeException("User settings " + key + " not found");
        }
        SettingsItem item = (SettingsItem) data.get(key);
        item.removeSettingsType(SettingType.DEFAULT);
        item.addSettingsType(SettingType.USER);
        item.setValue(value);
    }

    // Getters and Setters for language

    /**
     * <p>Get available language codes</p>
     * @return <b>List</b> <i>codes</i> - list of available language codes
     */
    public List<String> getAvailableLanguageCodes() {
        ArrayList<ArrayList<String>> langCodes = lang.getPyDictValue("available_languages");

        List<String> result = langCodes.stream()
            .map(item -> item.get(0))
            .collect(Collectors.toList());

        return result;
    }

    /**
     * <p>Get available language names</p>
     * @return <b>List</b> <i>names</i> - list of available language names
     */
    public List<String> getAvailableLanguageNames() {
        ArrayList<ArrayList<String>> langCodes = lang.getPyDictValue("available_languages");

        List<String> result = langCodes.stream()
            .map(item -> item.get(1))
            .collect(Collectors.toList());

        return result;
    }

    /**
     * <p>Get language value for specified key</p>
     * <p>Alias for <i>getLanguageValue</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - value of setting
     */
    public String getl(String key) {
        return getLanguageValue(key);
    }
    
    /**
     * <p>Get language value for specified key</p>
     * <p>Alias for <i>getl</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - value of setting
     */
    public String getLanguageValue(String key) {
        if (activeLanguage.isEmpty()) {
            printError("Active language not set");
            throw new RuntimeException("Active language not set");
        }

        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        if (! isLanguageKeyExists(key)) {
            printError(key + " Language key not found");
            throw new RuntimeException(key + " Language key not found");
        }

        LanguageItem item = getLanguageItem(key);
        if (item == null) {
            printError("Language settings " + key + " not found");
            throw new RuntimeException("Language settings " + key + " not found");
        }

        return item.getValue();
    }

    /**
     * <p>Set language value for specified key</p>
     * <p>Alias for <i>setLanguageValue</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     */
    public void setl(String key, String value) {
        setLanguageValue(key, value);
    }

    /**
     * <p>Set language value for specified key</p>
     * <p>Alias for <i>setl</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     */
    public void setLanguageValue(String key, String value) {
        if (activeLanguage.isEmpty()) {
            printError("Active language not set");
            throw new RuntimeException("Active language not set");
        }

        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        LanguageItem item = lang.getPyDictValue(PyDict.concatKeys("data", activeLanguage, key));
        if (item == null) {
            printError("Language settings " + key + " not found");
            throw new RuntimeException("Language settings " + key + " not found");
        }

        item.setValue(value);
    }

    /**
     * <p>Get active language code</p>
     * @return <b>String</b> <i>code</i> - active language code
     */
    public String getActiveLanguage() {
        return activeLanguage;
    }

    /**
     * <p>Set active language code</p>
     * @param language - <b>String</b> <i>language</i> - active language code
     */
    public void setActiveLanguage(String language) {
        activeLanguage = language;
    }

        // Getters and Setters for App values

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns ANY Object according to DataType set</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Object getAppOBJECT(String key) {
        return checkAndGetApplicationSettingsItem(key).getValue();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns String Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public String getAppSTRING(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueSTRING();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns Integer Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Integer getAppINTEGER(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueINT();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns Double Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Double getAppDOUBLE(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueDOUBLE();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns Boolean Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Boolean getAppBOOLEAN(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueBOOLEAN();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns List Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public List<Object> getAppLIST(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueLIST();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns Map Object (PyDict|HashMap) representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public Map<String, Object> getAppMAP(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueMAP();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns PyDict Object (PyDict|HashMap) representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public PyDict getAppPYDICT(String key) {
        return checkAndGetApplicationSettingsItem(key).getValuePYDICT();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns LocalDate Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public LocalDate getAppDATE(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueDATE();
    }

    /**
     *  <p><b>Application Settings</b></p>
     * <p>Returns LocalTime Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public LocalTime getAppTIME(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueTIME();
    }

    /**
     * <p><b>Application Settings</b></p>
     * <p>Returns LocalDateTime Object representation of value</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - Returns User settings value for specified key
     * @throws RuntimeException if key not found
     */
    public LocalDateTime getAppDATETIME(String key) {
        return checkAndGetApplicationSettingsItem(key).getValueDATETIME();
    }
    
    public SettingsItem checkAndGetApplicationSettingsItem(String key) {
        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        if (! isAppSettingExists(key)) {
            printError("App settings " + key + " not found");
            throw new RuntimeException("App settings " + key + " not found");
        }
        SettingsItem item = (SettingsItem) appData.get(key);

        return item;
    }

    /**
     * <p>Set Application settings value for specified key</p>
     * <p>Alias for <i>setAppSettingsValue</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     * @see getApp
     */
    public void setApp(String key, Object value) {
        setAppSettingsValue(key, value);
    }

    /**
     * <p>Set Application settings value for specified key</p>
     * <p>Alias for <i>setApp</i></p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     * @throws RuntimeException if key not found
     * @see getAppSettingsValue
     */
    public void setAppSettingsValue(String key, Object value) {
        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        if (! isAppSettingExists(key)) {
            printError("App settings " + key + " not found");
            throw new RuntimeException("App settings " + key + " not found");
        }
        SettingsItem item = (SettingsItem) appData.get(key);
        item.setValue(value);
    }

    /**
     * Add new application setting
     * @param item - <b>SettingsItem</b>
     * @throws RuntimeException if key already exists
     */
    public void addAppSettings(SettingsItem item) {
        if (item.getKey().isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        // Check if key already exists
        if (isAppSettingExists(item.getKey())) {
            throw new RuntimeException("App settings " + item.getKey() + " already exists");
        }
        // Set data type if not set
        if (item.getDataType() == null || item.getDataType() == DataType.UNDEFINED) {
            item.setDataType();
        }

        item.addSettingsType(SettingType.APP);
        appData.put(item.getKey(), item);
    }

    /**
     * Add new application setting
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     * @param saveInFile - <b>boolean</b> <i>saveInFile</i> - true if setting should be saved in file
     * @throws RuntimeException if key already exists
     */
    public void addAppSettings(String key, Object value, boolean saveInFile) {
        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }

        // Create new SettingsItem
        SettingsItem newItem = new SettingsItem();
        newItem.setKey(key);
        newItem.setValue(value);
        newItem.setDataType();
        newItem.setSettingType(SettingType.APP);
        newItem.setCanBeSavedInFile(saveInFile);
        // Pass new item to addAppSettings method
        addAppSettings(newItem);
    }        

    // Handling complete dictionaries

    /**
     * <p>Returns dictionary with all application data</p>
     * @return <b>Map</b> <i>appData</i> - dictionary with all application data
     * @see setAllAppData
     */
    public Map<String, Object> getAllAppData() {
        return appData;
    }

    /**
     * <p>Sets dictionary with all application data</p>
     * @param appData
     * @see getAllAppData
     */
    public void setAllAppData(Map<String, Object> appData) {
        this.appData = new PyDict();
        this.appData.putAll(appData);
    }

    /**
     * <p>Returns dictionary with all user settings data</p>
     * <p>Any changes in <b>data</b> will be reflected in <i>userSettingsFilePath</i></p>
     * @return <b>Map</b> <i>data</i> - dictionary with all user settings data
     * @see setAllUserSettingsData
     */
    public Map<String, Object> getAllUserSettingsData() {
        return data;
    }

    /**
     * <p>Sets dictionary with all user settings data</p>
     * @param data - <b>Map</b> <i>data</i> - dictionary with all user settings data
     * @see getAllUserSettingsData
     */
    public void setAllUserSettingsData(Map<String, Object> data) {
        this.data = new PyDict();
        this.data.putAll(data);
    }

    /**
     * <p>Returns dictionary with all default settings data</p>
     * @return <b>Map</b> <i>data</i> - dictionary with all default settings data
     * @see saveAllDefaultSettingsData
     */
    public PyDict getAllDefaultSettingsData() {
        if (defaultSettingsFilePath.isEmpty()) {
            // Throw error if file path is empty
            printError("Default settings file path is empty");
            throw new RuntimeException("Default settings file path is empty");
        }

        PyDict result = new PyDict();
        
        result = (PyDict) loadData(defaultSettingsFilePath, true, false);

        return result;
    }

    /**
     * <p>Saves dictionary with all default settings data</p>
     * <p>This will replace all existing data in <i>defaultSettingsFilePath</i></p>
     * <p>All user settings will be switched from USER to DEFAULT</p>
     * @param data - <b>Map</b> <i>data</i> - dictionary with all default settings data
     * @see getAllDefaultSettingsData
     * @see getAllUserSettingsData
     */
    public void saveInDefaultSettingsData(Map<String, Object> dataToSave) {
        // Switch all user settings to default
        for (Map.Entry<String, Object> entry : dataToSave.entrySet()) {
            SettingsItem item = (SettingsItem) entry.getValue();
            item.removeSettingsType(SettingType.USER);
            item.addSettingsType(SettingType.DEFAULT);
        }

        saveUserSettingsData(dataToSave, defaultSettingsFilePath);
    }

    /**
     * <p>Returns dictionary with all languages data</p>
     * <p>Any changes in <b>lang</b> will be reflected in <i>languagesFilePath</i></p>
     * @return <b>Map</b> <i>lang</i> - dictionary with all languages data
     * @see setAllLanguagesData
     */
    public Map<String, Object> getAllLanguagesData() {
        return lang;
    }

    /**
     * <p>Sets dictionary with all languages data</p>
     * @param lang - <b>Map</b> <i>lang</i> - dictionary with all languages data
     * @see getAllLanguagesData
     */
    public void setAllLanguagesData(Map<String, Object> lang) {
        this.lang = new PyDict();
        this.lang.putAll(lang);
    }

    // Save/Load data (Settings, Languages, AppData)

    /**
     * <p>Creates new empty file that can be used to store user or application settings</p>
     * @param filePath - <b>String</b> <i>filePath</i> - path to file
     */
    public void createNewSettingsFile(File file) {
        createNewSettingsFile(file.getAbsolutePath());
    }

    /**
     * <p>Creates new empty file that can be used to store user or application settings</p>
     * @param filePath - <b>String</b> <i>filePath</i> - path to file
     */
    public void createNewSettingsFile(String filePath) {
        PyDict newData = new PyDict();
        
        saveUserSettingsData(newData, filePath);
    }

    /**
     * <p>Creates new empty file that can be used to store language settings</p>
     * @param file - <b>File</b> <i>file</i> - file to create
     */
    public void createNewLanguagesFile(File file) {
        createNewLanguagesFile(file.getAbsolutePath());
    }

    /**
     * <p>Creates new empty file that can be used to store language settings</p>
     * @param file - <b>File</b> <i>file</i> - file to create
     */
     public void createNewLanguagesFile(String filePath) {
        PyDict newData = new PyDict();
        newData.setPyDictValue("available_languages", new ArrayList<ArrayList<String>>());
        newData.setPyDictValue("default_language", "");
        PyDict newDataKey = new PyDict();
        newData.setPyDictValue("data", newDataKey);
        
        saveLanguageData(newData, filePath);
    }

    /**
     * <p>Saves data to <b>userSettingsFilePath</b> and <b>languagesFilePath</b></p>
     * <p>If <b>userSettingsFilePath</b> is not set, <b>defaultSettingsFilePath</b> is used</p>
     * @param saveUserSettings - if <i>true</i> save data to <b>userSettingsFilePath</b>
     * @param saveLanguages - if <i>true</i> save data to <b>languagesFilePath</b>
     * @param saveApplicationSettings - if <i>true</i> save data to <b>appDataFilePath</b>
     * @return <b>boolean</b> <i>true</i> if data was saved successfully, <i>false</i> otherwise (see <i>lastErrorString</i> for details)
     */
    public boolean save(boolean saveUserSettings, boolean saveLanguages, boolean saveApplicationSettings) {
        clearErrorString();
        boolean success = true;
        if (saveUserSettings) {
            if (userSettingsFilePath.isEmpty()) {
                printError("User path is empty, you must set userSettingsFilePath");
                updateLastErrorString("User path is empty, you must set userSettingsFilePath");
                success = false;
            }
            else {
                try {
                    saveUserSettingsData(data, userSettingsFilePath);
                } catch (Exception e) {
                    updateLastErrorString(e.getMessage());
                    success = false;
                }
            }
        }
        
        if (saveLanguages) {
            if (languagesFilePath.isEmpty()) {
                printError("Language path is empty, you must set languagesFilePath");
                updateLastErrorString("Language path is empty, you must set languagesFilePath");
                success = false;
            }
            else {
                try {
                    saveLanguageData(lang, languagesFilePath);
                } catch (Exception e) {
                    updateLastErrorString(e.getMessage());
                    success = false;
                }
            }
        }

        if (saveApplicationSettings) {
            if (appDataFilePath.isEmpty()) {
                printError("Application settings path is empty, you must set appDataFilePath");
                updateLastErrorString("Application settings path is empty, you must set appDataFilePath");
                success = false;
            }
            else {
                try {
                    saveApplicationSettingsData(appData, appDataFilePath);
                } catch (Exception e) {
                    updateLastErrorString(e.getMessage());
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * <p>Saves data to <b>userSettingsFilePath</b> and <b>appDataFilePath</b></p>
     * <p>Data in <b>languagesFilePath</b> is not saved</p>
     * @return <b>boolean</b> <i>true</i> if data was saved successfully, <i>false</i> otherwise (see <i>lastErrorString</i> for details)
     * @see lastErrorString
     */
    public boolean save() {
        return save(true, false, true);
    }

    /**
     * <p>Loads data from <b>userSettingsFilePath</b> and <b>languagesFilePath</b></p>
     * <p>If <b>userSettingsFilePath</b> is not set, <b>defaultSettingsFilePath</b> is used</p>
     * @param loadSettings - if <i>true</i> load data from <b>userSettingsFilePath</b>
     * @param loadLanguages - if <i>true</i> load data from <b>languagesFilePath</b>
     * @param loadAppData - if <i>true</i> load data from <b>appDataFilePath</b>
     * @return <b>boolean</b> <i>true</i> if data was loaded successfully, <i>false</i> otherwise (see <i>lastErrorString</i> for details)
     * @see save
     * @see getLastErrorString
     */
    public boolean load(boolean loadSettings, boolean loadLanguages, boolean loadAppData) {
        clearErrorString();
        boolean success = true;
        if (loadSettings) {
            if (userSettingsFilePath.isEmpty()) {
                printError("User file path is empty, you must set userSettingsFilePath");
                updateLastErrorString("User file path is empty, you must set userSettingsFilePath");
                success = false;
            }
            else {
                // Check if file exists if UserSettingsFilePath is not empty
                if (!Files.exists(Path.of(userSettingsFilePath))) {
                    // Create empty JSON file if it doesn't exist
                    saveUserSettingsData(new PyDict(), userSettingsFilePath);
                }

                // Load data
                try {
                    data = (PyDict) loadData(userSettingsFilePath, true, false);
                } catch (Exception e) {
                    printError(e.getMessage());
                    updateLastErrorString(e.getMessage());
                    success = false;
                }
                updateUserSettingsWithDefaultData();
            }
        }

        if (loadLanguages) {
            if (!Files.exists(Path.of(languagesFilePath))) {
                printError("Language file not found: " + languagesFilePath);
                updateLastErrorString("Language file not found: " + languagesFilePath);
                success = false;
            }
            else {
                try {
                    lang = (PyDict) loadData(languagesFilePath, false, true);
                } catch (Exception e) {
                    updateLastErrorString(e.getMessage());
                    success = false;
                }
            }
        }

        if (loadAppData) {
            if (!Files.exists(Path.of(appDataFilePath))) {
                printError("Application settings file not found: " + appDataFilePath);
                updateLastErrorString("Application settings file not found: " + appDataFilePath);
                success = false;
            }
            else {
                try {
                    appData = (PyDict) loadData(appDataFilePath, true, false);
                } catch (Exception e) {
                    printError(e.getMessage());
                    updateLastErrorString(e.getMessage());
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * <p>Loads data from <b>userSettingsFilePath</b>, <b>languagesFilePath</b> and <b>appDataFilePath</b></p>
     * @return <b>boolean</b> <i>true</i> if data was loaded successfully, <i>false</i> otherwise (see <i>lastErrorString</i> for details)
     * @see lastErrorString
     */
    public boolean load() {
        return load(true, true, true);
    }

    // Error handling: lastErrorString

    /**
     * <p>Returns last error string</p>
     * <p><i>lastErrorString</i> contains details about last error</p>
     * @return <b>String</b> <i>lastErrorString</i> if set, <i>""</i> otherwise
     * @see clearErrorString
     */
    public String getLastErrorString() {
        return lastErrorString;
    }

    /**
     * <p>Clears last error string</p>
     * <p><i>lastErrorString</i> will be set to <i>"" (empty string)</i></p>
     * @see getLastErrorString
     */
    public void clearErrorString() {
        lastErrorString = "";
    }

    // Private methods

    /**
     * <p>Saves SettingsData (Data that have SettingsItems) <b>dataToSave</b> to <b>filePath</b></p>
     * @param dataToSave - <b>Map</b> <i>dataToSave</i> - dictionary with data to save
     * @param filePath - <b>String</b> <i>filePath</i> - path to file
     * @throws Exception if error occurs during saving data
     */
    private void saveUserSettingsData(Map<String, Object> dataToSave, String filePath) {
        if (dataToSave == null) {
            // Throw error if data is not loaded
            printError("Data not loaded");
            throw new RuntimeException("Data not loaded");
        }

        if (filePath.isEmpty()) {
            // Throw error if file path is empty
            printError("File path is empty");
            throw new RuntimeException("File path is empty");
        }

        // Translate all SettingsItem to Map
        Map<String, Object> translatedData = new PyDict();
        for (Map.Entry<String, Object> entry : dataToSave.entrySet()) {
            Map<String, Object> map = new PyDict();
            if (entry.getValue() instanceof SettingsItem) {
                SettingsItem item = (SettingsItem) entry.getValue();
                if (! item.isValid()) {
                    printError("SettingsItem is not valid: " + item.getKey());
                }
                map = item.toMap();
                translatedData.put(entry.getKey(), map);
            } else {
                translatedData.put(entry.getKey(), entry.getValue());
            }
        }

        // Save data to file
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
            
        String json = gson.toJson(translatedData);
        // Write data to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(json);
        } catch (Exception e) {
            printError("Error saving data: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * <p>Saves SettingsData (Data that have SettingsItems) <b>dataToSave</b> to <b>filePath</b></p>
     * @param dataToSave - <b>Map</b> <i>dataToSave</i> - dictionary with data to save
     * @param filePath - <b>String</b> <i>filePath</i> - path to file
     * @throws Exception if error occurs during saving data
     */
    private void saveApplicationSettingsData(Map<String, Object> dataToSave, String filePath) {
        if (dataToSave == null) {
            // Throw error if data is not loaded
            printError("Data not loaded");
            throw new RuntimeException("Data not loaded");
        }

        if (filePath.isEmpty()) {
            // Throw error if file path is empty
            printError("File path is empty");
            throw new RuntimeException("File path is empty");
        }

        // Translate all SettingsItem to Map
        Map<String, Object> translatedData = new PyDict();
        for (Map.Entry<String, Object> entry : dataToSave.entrySet()) {
            Map<String, Object> map = new PyDict();
            SettingsItem item = (SettingsItem) entry.getValue();

            if (item == null || (! item.canBeSavedInFile)) {
                if (! item.isValid()) {
                    printError("SettingsItem is not valid: " + item.getKey());
                }
                map = item.toMap();
                translatedData.put(entry.getKey(), map);
            } else {
                translatedData.put(entry.getKey(), entry.getValue());
            }
        }

        // Save data to file
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
            
        String json = gson.toJson(translatedData);
        // Write data to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(json);
        } catch (Exception e) {
            printError("Error saving data: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * <p>Saves LanguageData (Data that have LanguageItems) <b>dataToSave</b> to <b>filePath</b></p>
     * @param dataToSave - <b>Map</b> <i>dataToSave</i> - dictionary with data to save
     * @param filePath - <b>String</b> <i>filePath</i> - path to file
     * @throws Exception if error occurs during saving data
     */
    private void saveLanguageData(Map<String, Object> dataToSave, String filePath) {
        if (dataToSave == null) {
            // Throw error if data is not loaded
            printError("Data not loaded");
            throw new RuntimeException("Data not loaded");
        }

        if (filePath.isEmpty()) {
            // Throw error if file path is empty
            printError("File path is empty");
            throw new RuntimeException("File path is empty");
        }

        // Translate all LanguageItems to Map
        PyDict translatedData = new PyDict();
        translatedData.setPyDictValue("data", new PyDict());
        for (Map.Entry<String, Object> entry : dataToSave.entrySet()) {
            if (entry.getKey().equals("data")) {
                PyDict dataLanguagesBases = (PyDict) dataToSave.get("data");

                for (Map.Entry<String, Object> languageBaseEntry : dataLanguagesBases.entrySet()) {
                    translatedData.setPyDictValue(PyDict.concatKeys("data", languageBaseEntry.getKey()), new PyDict());

                    for (Map.Entry<String, Object> languageEntry : ((PyDict) languageBaseEntry.getValue()).entrySet()) {
                        if (languageEntry.getValue() instanceof LanguageItem) {
                            LanguageItem item = (LanguageItem) languageEntry.getValue();
                            Map<String, Object> map = item.toMap();
                            translatedData.setPyDictValue(PyDict.concatKeys("data", languageBaseEntry.getKey(), languageEntry.getKey()), map);
                        }
                        else {
                            translatedData.setPyDictValue(PyDict.concatKeys("data", languageBaseEntry.getKey(), languageEntry.getKey()), languageEntry.getValue());
                            printError("LanguageItem is not valid: " + languageEntry.getKey());
                        }
                    }
                }

            }
            else {
                translatedData.put(entry.getKey(), entry.getValue());
            }
        }

        // Save data to file
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
            
        String json = gson.toJson(translatedData);
        // Write data to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(json);
        } catch (Exception e) {
            printError("Error saving data: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private PyDict convertToMapOfSettingsItems(Map<String, Object> mapOfMaps) {
        PyDict result = new PyDict();
        for (Map.Entry<String, Object> entry : mapOfMaps.entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                // Throw error if value is not a dictionary
                printError("Value is not a dictionary: " + entry.getKey());
                throw new RuntimeException("Value is not a dictionary: " + entry.getKey());
            }

            SettingsItem item = new SettingsItem();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) entry.getValue();
            item.fromMap(map);
            if (item.getKey() == null || item.getKey().isEmpty()) {
                // Throw error if key is empty
                printError("Key is empty or null");
                throw new RuntimeException("Key is empty or null");
            }

            result.put(entry.getKey(), item);
        }

        return result;
    }

    private PyDict convertToMapOfLanguageItems(Map<String, Object> mapOfMaps) {
        PyDict result = new PyDict();
        PyDict languages = new PyDict();
        result.put("data", languages);

        if (mapOfMaps == null || mapOfMaps.get("data") == null) {
            // Throw error if data is not loaded
            printError("Language data is invalid");
            throw new RuntimeException("Language data is invalid");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> origLanguages = (Map<String, Object>) mapOfMaps.get("data");

        for (Map.Entry<String, Object> entry : origLanguages.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> langKeys = (Map<String, Object>) entry.getValue();
            languages.setPyDictValue(entry.getKey(), new PyDict());
            for (Map.Entry<String, Object> langKeyEntry : langKeys.entrySet()) {
                LanguageItem languageItem = new LanguageItem();
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) langKeyEntry.getValue();
                languageItem.fromMap(map);
                languages.setPyDictValue(PyDict.concatKeys(entry.getKey(), langKeyEntry.getKey()), languageItem);
            }
        }

        for (Map.Entry<String, Object> entry : mapOfMaps.entrySet()) {
            if (! entry.getKey().equals("data")) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * <p>Loads data from <b>filePath</b></p>
     * @param filePath - <b>String</b> <i>filePath</i> - path to file
     * @return <b>Map</b> <i>data</i> - dictionary with loaded data
     * @throws Exception if error occurs during loading data
     */
    private Map<String, Object> loadData(String filePath, boolean convertToSettingsItems, boolean convertToLanguageItems) {
        if (filePath.isEmpty()) {
            // Throw error if file path is empty
            printError("File path is empty");
            throw new RuntimeException("File path is empty");
        }

        if (!Files.exists(Path.of(filePath))) {
            printError("File not found: " + filePath);
            throw new RuntimeException("File not found: " + filePath);
        }

        Gson gson = new Gson();
        try {
            String json = new String(Files.readAllBytes(Path.of(filePath)));
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> loadedData = gson.fromJson(json, type);
            if (convertToSettingsItems) {
                loadedData = convertToMapOfSettingsItems(loadedData);
            }
            else if (convertToLanguageItems) {
                loadedData = convertToMapOfLanguageItems(loadedData);
            }
            return loadedData;
        } catch (Exception e) {
            printError("Error loading data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void printError(String message) {
        System.out.println("Settings: " + message);
    }

    private void updateLastErrorString(String message) {
        lastErrorString = message;
    }

    /**
     * <p>Replaces user settings with default data if user settings SettingsData contains mark DEFAULT</p>
     * <p>If user settings not exists, it will be created</p>
     * <p>Otherwise user settings will not be changed</p>
     */
    private void updateUserSettingsWithDefaultData() {
        if (defaultSettingsFilePath.isEmpty()) {
            return;
        }

        Map<String, Object> defaultData = getAllDefaultSettingsData();

        for (Map.Entry<String, Object> entry : defaultData.entrySet()) {
            if (isUserSettingExists(entry.getKey())) {
                SettingsItem userItem = (SettingsItem) data.get(entry.getKey());
                if (userItem.hasSettingType(SettingType.DEFAULT)) {
                    // Replace user item with default item
                    data.put(entry.getKey(), entry.getValue());
                }
            } else {
                data.put(entry.getKey(), entry.getValue());
            }
        }
    }






}
