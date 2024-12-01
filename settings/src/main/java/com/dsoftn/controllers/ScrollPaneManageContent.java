package com.dsoftn.controllers;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.dsoftn.utils.UTranslate.LanguagesEnum;
import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.Settings.Settings;
import com.dsoftn.events.EventAddLanguageToFile;
import com.dsoftn.events.EventEditLanguageChanged;
import com.dsoftn.events.EventEditLanguageContentChanged;
import com.dsoftn.events.EventEditLanguageRemoved;
import com.dsoftn.events.EventWriteLog;

public class ScrollPaneManageContent extends VBox {
    // Variables
    private String langLoadedListFileName = ""; // List loaded in language edit
    private String languageFileName = ""; // File name to work with
    private Stage primaryStage;
    private ObservableList<Node> elementList = FXCollections.observableArrayList(); // list of nodes
    private List<String> alreadyAddedLanguages = new ArrayList<>();

    // Constructor
    
    public ScrollPaneManageContent(Stage primaryStage, String langLoadedListFileName) {
        this.primaryStage = primaryStage;
        // Listener for element list
        elementList.addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                this.getChildren().setAll(elementList);
            }
        });

        // Events
        primaryStage.addEventHandler(EventAddLanguageToFile.EVENT_ADD_LANGUAGE_TO_FILE_TYPE, event -> {
            if (event.getLanguageAdded() != null) {
                onEventAddLanguageToFile(event);
            }
        });

        createWidget();
    }

    // Log
    
    private void log(String message) {
        log(message, 0);
    }

    private void log(String message, int indentLevel) {
        EventWriteLog event = new EventWriteLog(message, indentLevel);
        primaryStage.fireEvent(event);
    }

    // Event handlers

    private void onEventAddLanguageToFile(EventAddLanguageToFile event) {
        if (event.getLanguageAdded() != null) {
            ScrollPaneManageSection scrollPaneSection = new ScrollPaneManageSection(event, languageFileName);
            elementList.add(elementList.size() - 1, scrollPaneSection);
            alreadyAddedLanguages.add(event.getLanguageAdded().getLangCode());
            updateAlreadyAddedForAllElements();
            
            // Write log
            log("Language Manager: Added language: " + event.getLanguageAdded().getName());
        }
    }

    // Public methods

    public void setLanguageFileName(String languageFileName) {
        this.languageFileName = languageFileName;

        if (languageFileName == null || languageFileName.isEmpty()) {
            this.languageFileName = "";
            alreadyAddedLanguages.clear();
            elementList.clear();
            log("Language Manager: Language file unloaded.");
            return;
        }
        
        // Load file
        Settings settings = new Settings();
        settings.languagesFilePath = languageFileName;
        settings.load(false, true, false);

        // Update children
        alreadyAddedLanguages.clear();
        alreadyAddedLanguages = settings.getAvailableLanguageCodes();

        // Update Sections
        elementList.clear();
        addFooter();

        for (String langCode : alreadyAddedLanguages) {
            ScrollPaneManageSection scrollPaneSection = new ScrollPaneManageSection(langCode, languageFileName);
            elementList.add(elementList.size() - 1, scrollPaneSection);
        }
        updateAlreadyAddedForAllElements();

        // Write log
        log("Language Manager: Loaded language file: " + languageFileName);
    }

    public String getLanguageFileName() {
        return languageFileName;
    }

    // Serialization / Deserialization

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        
        // Sections
        // List<Map<String, Object>> sections = new ArrayList<>();
        // for (Node node : elementList) {
        //     if (node instanceof ScrollPaneSection) {
        //         ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
        //         sections.add(scrollPaneSection.toMap());
        //     }
        //     else if (node instanceof ScrollPanelSectionAdd) {
        //         ScrollPanelSectionAdd scrollPanelSectionAdd = (ScrollPanelSectionAdd) node;
        //         sections.add(scrollPanelSectionAdd.toMap());
        //     }
        // }

        // result.put("languageFileName", sections);

        // Variables
        result.put("languageFileName", languageFileName);

        return result;
    }

    public void fromMap(Map<String, Object> mapFromJson) {
        if (mapFromJson == null) {
            return;
        }

        // Variables
        languageFileName = (String) mapFromJson.get("languageFileName");
        if (languageFileName == null) {
            languageFileName = "";
        }
        setLanguageFileName(languageFileName);

        // Sections
        // elementList.clear();
        // if (mapFromJson.get("sections") != null) {
        //     @SuppressWarnings("unchecked")
        //     List<Map<String, Object>> sections = (List<Map<String, Object>>) mapFromJson.get("sections");
        //     if (sections != null) {
        //         Integer count = 0;
        //         for (Map<String, Object> section : sections) {
        //             if (count == sections.size() - 1) {
        //                 if (section != null) {
        //                     ScrollPanelSectionAdd scrollPanelSectionAdd = new ScrollPanelSectionAdd();
        //                     scrollPanelSectionAdd.fromMap(section);
        //                     elementList.add(scrollPanelSectionAdd);
        //                 }
        //             }
        //             else {
        //                 if (section != null) {
        //                     ScrollPaneSection scrollPaneSection = new ScrollPaneSection("?", "");
        //                     scrollPaneSection.fromMap(section);
        //                     elementList.add(scrollPaneSection);
        //                 }
        //             }
        //             count++;
        //         }
        //     }
        // }

        // if (elementList.size() == 0) {
        //     addFooter();
        // }
    }

    // Private methods

    private void updateAlreadyAddedForAllElements() {
        List<String> alreadyAdded = getAlreadyAddedLanguageCodes();
        for (Node node : elementList) {
            if (node instanceof ScrollPaneManageSection) {
                ScrollPaneManageSection scrollPaneSection = (ScrollPaneManageSection) node;
                scrollPaneSection.setAlreadyAddedLanguages(alreadyAdded);
            }
            else if (node instanceof ScrollPanelSectionAdd) {
                ScrollPanelSectionAdd scrollPanelSectionAdd = (ScrollPanelSectionAdd) node;
                scrollPanelSectionAdd.setAlreadyAddedLanguages(alreadyAdded);
            }
        }
    }

    private List<String> getAlreadyAddedLanguageCodes() {
        List<String> result = new ArrayList<>();
        for (Node node : elementList) {
            if (node instanceof ScrollPaneManageSection) {
                ScrollPaneManageSection scrollPaneSection = (ScrollPaneManageSection) node;
                String langCode = scrollPaneSection.getLanguage().getLangCode();
                result.add(langCode);
            }
        }
        return result;
    }

    private void createWidget() {
        this.setSpacing(5);
    }

    private void addFooter() {
        ScrollPanelSectionAdd scrollPanelSectionAdd = new ScrollPanelSectionAdd(ScrollPanelSectionAdd.Role.LANGUAGE_MANAGE);
        elementList.add(scrollPanelSectionAdd);
    }


}
