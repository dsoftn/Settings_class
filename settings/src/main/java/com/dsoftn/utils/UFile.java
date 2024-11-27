package com.dsoftn.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UFile {

    // SYSTEM

    public static boolean startFile(String filePath) {
        return startFile(filePath, null, null);
    }

    public static boolean startFile(String filePath, String param1) {
        return startFile(filePath, param1, null);
    }

    /**
     * Start file with parameters if file exists and is executable
     * If file is Python file (with .py or .pyw extension), it will be started with Python interpreter
     * @param filePath
     * @param param1
     * @param param2
     * @return true if file was started
     */
    public static boolean startFile(String filePath, String param1, String param2) {
        if (!isFile(filePath)) {
            return false;
        }

        String absPath = getAbsolutePath(filePath);
        ProcessBuilder processBuilder;

        if (filePath.endsWith(".py") || filePath.endsWith(".pyw")) {
            if (param1 != null && param2 != null) {
                processBuilder = new ProcessBuilder("python", absPath, param1, param2);
            }
            else if (param1 != null) {
                processBuilder = new ProcessBuilder("python", absPath, param1);
            }
            else {
                processBuilder = new ProcessBuilder("python", absPath);
            }
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("LC_ALL", "en_US.UTF-8");
        }
        else {
            if (param1 != null && param2 != null) {
                processBuilder = new ProcessBuilder(absPath, param1, param2);
            }
            else if (param1 != null) {
                processBuilder = new ProcessBuilder(absPath, param1);
            }
            else {
                processBuilder = new ProcessBuilder(absPath);
            }
        }

        processBuilder.redirectErrorStream(true);

        try {
            processBuilder.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }



    // FILES

    /**
     * Concatenate working directory with file path
     * @param filePath
     * @return concatenated path
     */
    public static String concatWorkingDir(String filePath) {
        return System.getProperty("user.dir") + "\\" + filePath;
    }

    /**
     * Check if file exists
     */
    public static boolean isFile(String filePath) {
        try {
            return Files.exists(Path.of(filePath));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get absolute path of file
     * @param filePath
     * @return absolute path of file or null
     */
    public static String getAbsolutePath(String filePath) {
        try {
            return Path.of(filePath).toAbsolutePath().toString();
        } catch (Exception e) {
            return null;
        }
    }


}
