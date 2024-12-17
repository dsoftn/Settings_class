package com.dsoftn.controllers;

import javafx.application.Platform;
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

import com.dsoftn.utils.LanguagesEnum;
import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.events.EventEditLanguageAdded;
import com.dsoftn.events.EventEditLanguageRemoved;
import com.dsoftn.events.EventEditLanguageChanged;
import com.dsoftn.events.EventEditLanguageContentChanged;
import com.dsoftn.events.EventWriteLog;

public class ScrollPaneContent extends VBox {
    // Variables
    private LanguageItemGroup group = null;
    private Stage primaryStage;
    private List<String> fileAffected = new ArrayList<>();
    private ObservableList<Node> elementList = FXCollections.observableArrayList(); // list of nodes

    // Constructor
    
    public ScrollPaneContent(LanguageItemGroup languageItemGroup, Stage primaryStage, List<String> fileAffected) {
        this.primaryStage = primaryStage;
        this.fileAffected = fileAffected;
        // Listener for element list
        elementList.addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                this.getChildren().setAll(elementList);
            }
        });

        setLanguageItemGroup(languageItemGroup);

        // Events
        primaryStage.addEventHandler(EventEditLanguageAdded.EVENT_EDIT_LANGUAGE_ADDED_TYPE, event -> {
            if (event.getLanguageAdded() != null) {
                onEventEditLanguageAdded(event);
            }
        });
        primaryStage.addEventHandler(EventEditLanguageRemoved.EVENT_EDIT_LANGUAGE_REMOVED_TYPE, event -> {
            if (event.getLanguageRemoved() != null) {
                onEventEditLanguageRemoved(event);
            }
        });
        primaryStage.addEventHandler(EventEditLanguageChanged.EVENT_EDIT_LANGUAGE_CHANGED_TYPE, event -> {
            onEventEditLanguageChanged(event);
        });
    }

    // Event handlers

    private void onEventEditLanguageAdded(EventEditLanguageAdded event) {
        if (event.getLanguageAdded() != null) {
            ScrollPaneSection scrollPaneSection = new ScrollPaneSection(event);
            elementList.add(elementList.size() - 1, scrollPaneSection);
            updateAlreadyAddedForAllElements();
            
            // Write log
            EventWriteLog eventWriteLog = new EventWriteLog("Added language: " + event.getLanguageAdded().getName());
            primaryStage.fireEvent(eventWriteLog);
        }
    }

    private void onEventEditLanguageRemoved(EventEditLanguageRemoved event) {
        if (event.getLanguageRemoved() != null) {
            for (Node node : elementList) {
                if (node instanceof ScrollPaneSection) {
                    ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                    if (scrollPaneSection.getLanguageCode().equals(event.getLanguageRemoved().getLangCode())) {
                        elementList.remove(node);
                        break;
                    }
                }
            }
            updateAlreadyAddedForAllElements();

            // Write log
            EventWriteLog eventWriteLog = new EventWriteLog("Removed language: " + event.getLanguageRemoved().getName());
            primaryStage.fireEvent(eventWriteLog);
        }
    }

    private List<String> getChangedLangCodes() {
        List<String> result = new ArrayList<>();
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                if (scrollPaneSection.isChanged()) {
                    result.add(scrollPaneSection.getLanguageCode());
                }
            }
        }
        return result;
    }

    private void onEventEditLanguageChanged(EventEditLanguageChanged event) {
        List<String> changedLangCodes = getChangedLangCodes();
        EventEditLanguageContentChanged eventEditLanguageContentChanged = new EventEditLanguageContentChanged(changedLangCodes);
        primaryStage.fireEvent(eventEditLanguageContentChanged);
        checkIfSectionValueNeedsUpdate();
    }
    
    // Public methods

    public String getValue(String languageCode) {
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                if (scrollPaneSection.getLanguageCode().equals(languageCode)) {
                    return scrollPaneSection.getValue();
                }
            }
        }
        return null;
    }

    public LanguageItemGroup getValueAsLanguageItemGroup(String setLanguageGroupKey) {
        LanguageItemGroup result = new LanguageItemGroup("ScrollPaneContent");

        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                LanguageItem languageItem = ((ScrollPaneSection) node).getValueAsLanguageItem();
                languageItem.setKey(setLanguageGroupKey);
                result.addToGroup(languageItem);
            }
        }

        return result;
    }

    public boolean hasChangedSections() {
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                if (scrollPaneSection.isChanged()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setLanguageItemGroup(LanguageItemGroup languageItemGroup) {
        if (languageItemGroup == null) {
            LanguageItemGroup languageItemGroupEmpty = new LanguageItemGroup("Empty");
            this.group = languageItemGroupEmpty;
            if (elementList.size() == 0) {
                addFooter();
            }

            for (Node node : elementList) {
                if (node instanceof ScrollPaneSection) {
                    ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                    scrollPaneSection.setLanguageItemGroup(null);
                }
            }

            updateAlreadyAddedForAllElements();
            return;
        }

        this.group = languageItemGroup;

        // Deprecated block: This code try to use old widgets and just change data, i want all element widgets to be recreated
        // Check if all required languages are already added
        // boolean allLanguagesAdded = true;
        // for (LanguageItem languageItem : languageItemGroup.getLanguageItems()) {
        //     boolean languageAdded = false;
        //     for (Node node : elementList) {
        //         if (node instanceof ScrollPaneSection) {
        //             ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
        //             if (scrollPaneSection.getLanguageCode().equals(languageItem.getLanguageCode())) {
        //                 languageAdded = true;
        //                 break;
        //             }
        //         }
        //     }
        //     if (!languageAdded) {
        //         allLanguagesAdded = false;
        //         break;
        //     }
        // }

        // if (allLanguagesAdded) {
        //     for (Node node : elementList) {
        //         if (node instanceof ScrollPaneSection) {
        //             ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
        //             scrollPaneSection.setLanguageItemGroup(languageItemGroup);
        //         }
        //     }
            
        //     updateAlreadyAddedForAllElements();
        //     return;
        // }
        
        // Remember all translation languages that are already set
        Map<String, LanguagesEnum> alreadySetLanguages = new HashMap<>();
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                if (scrollPaneSection.getLanguageCode() != null && scrollPaneSection.getTranslateFromLanguage() != null) {
                    alreadySetLanguages.put(scrollPaneSection.getLanguageCode(), scrollPaneSection.getTranslateFromLanguage());
                }
            }
        }

        // Clear all elements
        elementList.clear();

        // Add new elements
        for (LanguageItem languageItem : languageItemGroup.getLanguageItems()) {
            ScrollPaneSection scrollPaneSection = new ScrollPaneSection(languageItem);
            scrollPaneSection.setLanguageItemGroup(languageItemGroup);

            elementList.add(scrollPaneSection);
        }
        addFooter();

        updateAlreadyAddedForAllElements();

        // Set all translation languages that are already set
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                if (scrollPaneSection.getLanguageCode() != null && scrollPaneSection.getTranslateFromLanguage() == null) {
                    scrollPaneSection.setTranslateFromLanguage(alreadySetLanguages.get(scrollPaneSection.getLanguageCode()));
                }
            }
        }

        Platform.runLater(() -> this.layout());
        
    }

    public List<LanguagesEnum> getListOfRequiredLanguages(List<String> listOfAffectedFiles) {

        List<LanguagesEnum> result = new ArrayList<>();
        for (Node node : elementList) {
            if (node instanceof ScrollPanelSectionAdd) {
                ScrollPanelSectionAdd scrollPaneSectionAdd = (ScrollPanelSectionAdd) node;
                for (String lName : scrollPaneSectionAdd.getListOfRequiredLanguageNames(listOfAffectedFiles)) {
                    result.add(LanguagesEnum.fromName(lName));
                }
            }
        }
        return result;
    }

    public void setFileAffected(List<String> fileAffectedList) {
        this.fileAffected = fileAffectedList;

        for (Node node : elementList) {
            if (node instanceof ScrollPanelSectionAdd) {
                ScrollPanelSectionAdd scrollPaneSectionAdd = (ScrollPanelSectionAdd) node;
                scrollPaneSectionAdd.setAffectedFiles(fileAffectedList);
            }
        }
    }

    // Serialization / Deserialization

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        
        // Sections
        List<Map<String, Object>> sections = new ArrayList<>();
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                sections.add(scrollPaneSection.toMap());
            }
            else if (node instanceof ScrollPanelSectionAdd) {
                ScrollPanelSectionAdd scrollPanelSectionAdd = (ScrollPanelSectionAdd) node;
                sections.add(scrollPanelSectionAdd.toMap());
            }
        }

        result.put("sections", sections);

        // Variables
        if (group == null) {
            result.put("group", null);
        }
        else {
            result.put("group", group.toMap());
        }

        result.put("fileAffected", fileAffected);

        return result;
    }

    public void fromMap(Map<String, Object> mapFromJson) {
        if (mapFromJson == null) {
            return;
        }

        // File Affected
        if (mapFromJson.get("fileAffected") != null) {
            @SuppressWarnings("unchecked")
            List<String> fileAff = (List<String>) mapFromJson.get("fileAffected");
            fileAffected.clear();

            if (fileAff != null) {
                for (String item : fileAff) {
                    if (item == null || item.isEmpty()) {
                        continue;
                    }
                    fileAffected.add(item);
                }
            }
        }

        // LanguageGroupItem
        group = new LanguageItemGroup("Empty");
        if (mapFromJson.get("group") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> groupItem = (Map<String, Object>) mapFromJson.get("group");
            if (groupItem != null) {
                group.fromMap(groupItem);
            }
        }

        // Sections
        elementList.clear();
        if (mapFromJson.get("sections") != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sections = (List<Map<String, Object>>) mapFromJson.get("sections");
            if (sections != null) {
                Integer count = 0;
                for (Map<String, Object> section : sections) {
                    if (count == sections.size() - 1) {
                        if (section != null) {
                            ScrollPanelSectionAdd scrollPanelSectionAdd = new ScrollPanelSectionAdd();
                            scrollPanelSectionAdd.fromMap(section);
                            elementList.add(scrollPanelSectionAdd);
                        }
                    }
                    else {
                        if (section != null) {
                            ScrollPaneSection scrollPaneSection = new ScrollPaneSection("?", "");
                            scrollPaneSection.fromMap(section);
                            elementList.add(scrollPaneSection);
                        }
                    }
                    count++;
                }
            }
        }

        if (elementList.size() == 0) {
            addFooter();
        }
    }

    // Private methods

    private void checkIfSectionValueNeedsUpdate() {
        if (hasChangedSections()) {
            for (Node node : elementList) {
                if (node instanceof ScrollPaneSection) {
                    ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                    if (!scrollPaneSection.isChanged()) {
                        scrollPaneSection.showMessage("Is this entry needs to be updated?");
                    }
                    else {
                        scrollPaneSection.hideMessage();
                    }
                }
            }
        }
        else {
            for (Node node : elementList) {
                if (node instanceof ScrollPaneSection) {
                    ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                    scrollPaneSection.hideMessage();
                }
            }
        }
    }

    private void updateAlreadyAddedForAllElements() {
        List<String> alreadyAdded = getAlreadyAddedLanguageCodes();
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
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
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                String langCode = scrollPaneSection.getLanguageCode();
                result.add(langCode);
            }
        }
        return result;
    }

    private void addFooter() {
        ScrollPanelSectionAdd scrollPanelSectionAdd = new ScrollPanelSectionAdd(fileAffected, getAlreadyAddedLanguageCodes());
        scrollPanelSectionAdd.setAffectedFiles(fileAffected);
        elementList.add(scrollPanelSectionAdd);
    }


}
