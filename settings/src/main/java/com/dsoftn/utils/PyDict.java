package com.dsoftn.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * <p>Class for working with Python dictionaries</p>
 * <p>PyDict extends <i>LinkedHashMap</i> with additional methods for working with Python dictionaries</p>
 * <p>Use PyDict as follows:</p>
 * <p><b>Example</b>:</p> 
 * <p>    PyDict dict = new PyDict();<p>
 * <p>    dict.setPyDictValue("[key1][key2]", "my string value");</p>
 * <p>    if (dict.isPyDictKeyExists("[key1][key2]")) { String value = (String) dict.getPyDictValue("[key1][key2]"); }</p>
 */
public class PyDict extends LinkedHashMap<String, Object> {
    public static final String PY_DICT_VERSION = "1.0.0";

    private static final ArrayList<String> PYDICT_KEYS_DELIMITERS = new ArrayList<String>(List.of("[]", ",", " "));
    private static final int MAX_NEW_PYDICT_KEYS_TO_SET = 10;

    // Constructors

    public PyDict() {
        super();
    }

    public PyDict(LinkedHashMap<String, Object> map) {
        super(map);
    }

    // Get/Set values

    /**
     * <p>Get value from PyDict by specified key</p>
     * <p><b>Example</b>: getPyDictValue("[key1][key2]")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Object</b> <i>value</i> - value of setting or <i>null</i> if key not found
     * @see setPyDictValue
     * @see isPyDictKeyExists
     */
    public <T> T getPyDictValue(String key) {
        List<String> keys = validatePyDictKeys(key);
        @SuppressWarnings("unchecked")
        T value = (T) getPyDictValueRecursion(keys, 0, this);
        return value;
    }

    /**
     * <p>Get Boolean value from PyDict by specified key</p>
     * <p>Use this method if you are sure that key is Boolean</p>
     * <p><b>Example</b>: getPyDictValue("[key1][key2]")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Boolean</b> <i>value</i> of setting or <i>null</i> if key not found
     * @see getPyDictValue
     */
    public Boolean getPyDictBooleanValueEXPLICIT(String key) {
        Boolean value = (Boolean) getPyDictValue(key);
        
        return value;
    }

    /**
     * <p>Get Integer value from PyDict by specified key</p>
     * <p>Use this method if you are sure that key is Number or String that can be converted to Integer</p>
     * <p><b>Example</b>: getPyDictValue("[key1][key2]")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Integer</b> <i>value</i> of setting or <i>null</i> if key not found
     * @see getPyDictValue
     */
    public Integer getPyDictIntegerValueEXPLICIT(String key) {
        Object value = getPyDictValue(key);

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
        else if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        // If value is not a number, return null
        return null;        
    }

    /**
     * <p>Get Double value from PyDict by specified key</p>
     * <p>Use this method if you are sure that key is Number or String that can be converted to Double</p>
     * <p><b>Example</b>: getPyDictValue("[key1][key2]")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>Double</b> <i>value</i> of setting or <i>null</i> if key not found
     * @see getPyDictValue
     */
    public Double getPyDictDoubleValueEXPLICIT(String key) {
        Object value = getPyDictValue(key);

        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
        else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // If value is not a number, return null
        return null;
        }

    /**
     * <p>Get String value from PyDict by specified key</p>
     * <p>Use this method if you are sure that key is String or it can be converted to String</p>
     * <p><b>Example</b>: getPyDictValue("[key1][key2]")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>String</b> <i>value</i> of setting or <i>null</i> if key not found
     * @see getPyDictValue
     */
    public String getPyDictStringValueEXPLICIT(String key) {
        Object value = getPyDictValue(key);
        return value.toString();
    }

    /**
     * <p>Set value in PyDict by specified key</p>
     * <p><b>Example</b>: setPyDictValue("[key1][key2]", "my string value")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @param value - <b>Object</b> <i>value</i> - value of setting
     * @return <b>boolean</b> <i>true</i> if value was set, <i>false</i> otherwise
     * @see getPyDictValue
     * @see isPyDictKeyExists
     */
    public boolean setPyDictValue(String key, Object value) {
        List<String> keys = validatePyDictKeys(key);

        // Add new keys if needed
        createPyDictKeysTree(keys);

        setPyDictValueRecursion(keys, 0, this, value);
        return true;
    }

    // Checkers

    /**
     * <p>Check if PyDict key exists</p>
     * <p><b>Example</b>: isPyDictKeyExists("[key1][key2]")</p>
     * @param key - <b>String</b> <i>key</i> - key of setting
     * @return <b>boolean</b> <i>true</i> if key exists, <i>false</i> otherwise
     * @see getPyDictValue
     * @see setPyDictValue
     */
    public boolean isPyDictKeyExists(String key) {
        List<String> keys = validatePyDictKeys(key);
        return isPyDictKeyExistsRecursion(keys, 0, this);
    }

    // Static methods

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> castToMap(Object obj) {
        return (Map<K, V>) obj;
    }
    
    public static String concatKeys(String... keys) {
        String result = "";
        for (String key : keys) {
            if (!key.isEmpty()) {
                key = key.strip();
                if (key.startsWith("[") && key.endsWith("]")) {
                    result += key;
                }
                else {
                    key = key.replaceAll("\\[", "").replaceAll("\\]", "");
                    result += "[" + key + "]";
                }
            }
        }
        return result;
    }


    // Private methods

    private <T> T getPyDictValueRecursion(List<String> keys, int depth, Map<String, Object> dict) {
        if (depth == keys.size() - 1) {
            @SuppressWarnings("unchecked")
            T value = (T) dict.get(keys.get(depth));
            return value;
            // return dict.get(keys.get(depth));
        }

        Object value = dict.get(keys.get(depth));

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return getPyDictValueRecursion(keys, depth + 1, map);
        }
        else {
            return null;
        }
    }

    private void setPyDictValueRecursion(List<String> keys, int depth, Map<String, Object> dict, Object setValue) {
        if (depth == keys.size() - 1) {
            dict.put(keys.get(depth), setValue);
            return;
        }

        Object value = dict.get(keys.get(depth));

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            setPyDictValueRecursion(keys, depth + 1, map, setValue);
        }
        else {
            throw new IllegalArgumentException("Key not found");
        }
    }

    private boolean isPyDictKeyExistsRecursion(List<String> keys, int depth, Map<String, Object> dict) {
        if (depth == keys.size() - 1) {
            return dict.containsKey(keys.get(depth));
        }

        Object value = dict.get(keys.get(depth));

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return isPyDictKeyExistsRecursion(keys, depth + 1, map);
        }
        else {
            return false;
        }
    }

    private ArrayList<String> validatePyDictKeys(String keys) {
        if ((keys == null) || keys.isEmpty()) {
            return new ArrayList<String>();
        }

        ArrayList<String> result = new ArrayList<String>();

        // Determine key delimiter and return list of keys
        for (String keyDelimiter: PYDICT_KEYS_DELIMITERS) {
            // Check if delimiter is a pair
            if (keyDelimiter.length() == 2 && keys.charAt(0) == keyDelimiter.charAt(0) && keys.charAt(keys.length() - 1) == keyDelimiter.charAt(1)) {
                List<String> keysList = List.of(keys.split(Pattern.quote(String.valueOf(keyDelimiter.charAt(1)))));

                // Clear delimiter trails from keys
                for (String key: keysList) {
                    if (!key.isEmpty()) {
                        key = key.trim();
                        if (key.startsWith(keyDelimiter.substring(0,1))) {
                            key = key.substring(1);
                        }
                        if (key.endsWith(keyDelimiter.substring(1,2))) {
                            key = key.substring(0, key.length() - 1);
                        }
                        result.add(key);
                    }
                }
            return result;
            }

            // Check if delimiter is a single
            else if (keyDelimiter.length() == 1 && keys.contains(keyDelimiter)) {
                List<String> keysList = List.of(keys.split(Pattern.quote(String.valueOf(keyDelimiter))));

                for (String key: keysList) {
                    if (!key.isEmpty()) {
                        key = key.trim();
                        result.add(key);
                    }
                }

                return result;
            }
        }

        // No delimiter found, add whole string as key
        result.add(keys);
        return result;
    }

    private void createPyDictKeysTree(List<String> keys) {
        int keysCount = 0;
        int newKeysCount = 0;
        while (keysCount < (keys.size() - 1)) {
            if (!(isPyDictKeyExistsRecursion(keys.subList(0, keysCount + 1), 0, this)) || (getPyDictValueRecursion(keys.subList(0, keysCount + 1), 0, this)) == null) {
                setPyDictValueRecursion(keys.subList(0, keysCount + 1), 0, this, new PyDict());
                newKeysCount++;
            }
            
            if (newKeysCount >= MAX_NEW_PYDICT_KEYS_TO_SET) {
                break;
            }
            keysCount++;
        }
    }

    // Version

    public String getPyDictVersion() {
        return PY_DICT_VERSION;
    }

}
