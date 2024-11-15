package com.dsoftn.controllers;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import com.dsoftn.Settings.LanguageItemGroup;

import java.util.List;
import java.util.ArrayList;

import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.controllers.ScrollPaneSection;
import com.dsoftn.controllers.ScrollPanelSectionAdd;
import com.dsoftn.events.EventEditLanguageAdded;
import com.dsoftn.events.EventEditLanguageRemoved;

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
    }

    // Event handlers

    private void onEventEditLanguageAdded(EventEditLanguageAdded event) {
        if (event.getLanguageAdded() != null) {
            ScrollPaneSection scrollPaneSection = new ScrollPaneSection(event);
            elementList.add(elementList.size() - 1, scrollPaneSection);
        }
    }

    private void onEventEditLanguageRemoved(EventEditLanguageRemoved event) {
        if (event.getLanguageRemoved() != null) {
            for (Node node : elementList) {
                if (node instanceof ScrollPaneSection) {
                    ScrollPaneSection scrollPaneSection = (ScrollPaneSection) node;
                    if (scrollPaneSection.getLanguageCode().equals(event.getLanguageRemoved().getLangCode())) {
                        elementList.remove(scrollPaneSection);
                        break;
                    }
                }
            }
        }
    }

    // Public methods

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

    // Private methods

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
        elementList.add(scrollPanelSectionAdd);
    }


}
