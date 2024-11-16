package com.dsoftn.controllers;

import javafx.scene.layout.HBox;
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
import java.util.Optional;

import com.dsoftn.utils.PyDict;
import com.dsoftn.utils.UTranslate.LanguagesEnum;
import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.controllers.ScrollPaneSection;
import com.dsoftn.controllers.ScrollPanelSectionAdd;
import com.dsoftn.events.EventEditLanguageAdded;
import com.dsoftn.events.EventEditLanguageRemoved;
import com.dsoftn.events.EventEditLanguageChanged;
import com.dsoftn.events.EventEditLanguageContentChanged;

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
            updateAlreadyAddedForAllElements();
            return;
        }

        this.group = languageItemGroup;

        elementList.clear();
        addFooter();

        for (LanguageItem languageItem : languageItemGroup.getLanguageItems()) {
            ScrollPaneSection scrollPaneSection = new ScrollPaneSection(languageItem);
            elementList.add(elementList.size() - 1, scrollPaneSection);
        }

        updateAlreadyAddedForAllElements();
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

    // Serialization / Deserialization

    public Map<String, Object> toMap() {
        PyDict result = new PyDict();
        
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

        // Variables
        if (group == null) {
            result.setPyDictValue("group", null);
        }
        else {
            result.setPyDictValue("group", group.toMap());
        }

        result.setPyDictValue("fileAffected", fileAffected);

        return result;
    }

    public void fromMap(Map<String, Object> mapFromJson) {
        PyDict map = (PyDict) mapFromJson;
        if (map == null) {
            return;
        }

        // File Affected
        List<String> fileAff = map.getPyDictValue("fileAffected");
        fileAffected.clear();

        if (fileAff != null) {
            for (String item : fileAff) {
                fileAffected.add(item);
            }
        }

        // LanguageGroupItem
        group = new LanguageItemGroup("Empty");
        if (map.getPyDictValue("group") != null) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> groupItem = (HashMap<String, Object>) map.getPyDictValue("group");
            if (groupItem != null) {
                group.fromMap(groupItem);
            }
        }

        // Sections
        elementList.clear();
        if (map.getPyDictValue("sections") != null) {
            @SuppressWarnings("unchecked")
            List<HashMap<String, Object>> sections = (List<HashMap<String, Object>>) map.getPyDictValue("sections");
            if (sections != null) {
                Integer count = 0;
                for (HashMap<String, Object> section : sections) {
                    if (count == sections.size() - 1) {
                        if (section != null) {
                            ScrollPanelSectionAdd scrollPanelSectionAdd = new ScrollPanelSectionAdd();
                            scrollPanelSectionAdd.fromMap(section);
                            elementList.add(scrollPanelSectionAdd);
                        }
                    }
                    else {
                        if (section != null) {
                            ScrollPaneSection scrollPaneSection = new ScrollPaneSection("", "");
                            scrollPaneSection.fromMap(section);
                            elementList.add(scrollPaneSection);
                        }
                    }
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
        for (Node node : elementList) {
            if (node instanceof ScrollPaneSection) {
                ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                scrollPaneSection.setAlreadyAddedLanguages(getAlreadyAddedLanguageCodes());
            }
            else if (node instanceof ScrollPanelSectionAdd) {
                ScrollPanelSectionAdd scrollPanelSectionAdd = (ScrollPanelSectionAdd) node;
                scrollPanelSectionAdd.setAlreadyAddedLanguages(getAlreadyAddedLanguageCodes());
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
        System.out.println("Before" + getChildren().size());
        elementList.add(scrollPanelSectionAdd);
        System.out.println("After" + getChildren().size());
    }


}
