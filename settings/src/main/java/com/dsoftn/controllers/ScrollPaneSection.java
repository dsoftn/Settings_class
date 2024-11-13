package com.dsoftn.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import com.dsoftn.Settings.LanguageItemGroup;

import java.io.IOException;

import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.utils.UTranslate.LanguagesEnum;


public class ScrollPaneSection extends VBox {
    // FXML Widgets

    @FXML
    private Label lblLangCode; // Language Code mark
    @FXML
    private Label lblLangName; // Language Name mark
    @FXML
    private Label lblMsg; // Message above text box
    @FXML
    private ComboBox<String> cmbTranslateFrom; // Language to translate from
    @FXML
    private Button btnTranslate; // Translate button
    @FXML
    private TextArea txtValue; // Text (value)

    // Properties
    private String key = "";
    private String languageCode = "";
    private LanguagesEnum langEnum = null;
    private String value = "";





    public ScrollPaneSection(String key, String languageCode, String text) {
        this.key = key;
        this.languageCode = languageCode;
        this.value = text;

        this.langEnum = LanguagesEnum.fromLangCode(languageCode);

        createWidgets();
    }

    public ScrollPaneSection(LanguageItem languageItem) {
        this(languageItem.getKey(), languageItem.getLanguageCode(), languageItem.getValue());
    }

    public ScrollPaneSection(LanguageItemGroup languageItemGroup, String languageCode) {
        this(languageItemGroup.getLanguageItemByLanguageCode(languageCode));
    }


    private void createWidgets() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LanguageScrollPaneSection.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lblLangCode.setText(languageCode);
        lblLangName.setText(langEnum.getName());
        txtValue.setText(value);
        populateLanguageComboBox();
    }

    private void populateLanguageComboBox() {
        for (LanguagesEnum lang : LanguagesEnum.values()) {
            cmbTranslateFrom.getItems().add(lang.getName());
        }
    }



    

}
