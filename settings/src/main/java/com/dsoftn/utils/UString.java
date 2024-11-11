package com.dsoftn.utils;

public class UString {
    public static int Count(String text, String searchString) {
        int count = 0;
        int pos = 0;
        while (true) {
            pos = text.indexOf(searchString, pos);
            if (pos >= 0) {
                count++;
                pos += searchString.length();
            } else {
                break;
            }
        }
        return count;
    }
}
