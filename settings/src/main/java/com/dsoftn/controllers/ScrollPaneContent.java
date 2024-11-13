package com.dsoftn.controllers;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.controllers.ScrollPaneSection;

public class ScrollPaneContent extends VBox {

    private LanguageItemGroup group;

    public ScrollPaneContent(LanguageItemGroup languageItemGroup) {
        setLanguageItemGroup(languageItemGroup);
    }

    public ScrollPaneContent() {}


    public void setLanguageItemGroup(LanguageItemGroup languageItemGroup) {
        getChildren().clear();
        for (LanguageItem languageItem : languageItemGroup.getLanguageItems()) {
            ScrollPaneSection scrollPaneSection = new ScrollPaneSection(languageItem);
            getChildren().add(scrollPaneSection);
        }
    }




}
