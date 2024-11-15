package com.dsoftn.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import com.dsoftn.Settings.LanguageItemGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.utils.UTranslate.LanguagesEnum;
import com.dsoftn.utils.UTranslate;
import com.dsoftn.events.EventEditLanguageRemoved;
import com.dsoftn.events.EventEditLanguageChanged;
import com.dsoftn.events.EventEditLanguageAdded;

public class ScrollPaneSection extends VBox {

    public enum MessageType {
        NORMAL, WORKING, ERROR
    }

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
    @FXML
    private Button btnClose; // Close button
    @FXML
    private Button btnYes; // Yes button
    @FXML
    private Button btnNo; // No button
    @FXML
    private Region regMsg;

    // Properties
    private LanguagesEnum langEnum = null;
    private List<String> alreadyAddedLanguages = new ArrayList<>();
    private boolean isChanged = false;
    private LanguageItemGroup languageItemGroup = null;
    private String originalValue = "";

    public ScrollPaneSection(String languageCode, String value) {
        this.langEnum = LanguagesEnum.fromLangCode(languageCode);

        if (langEnum == null) {
            throw new IllegalArgumentException("Invalid language code: " + languageCode);
        }

        createWidgets(value);
    }

    public ScrollPaneSection(LanguageItem languageItem) {
        this(languageItem.getLanguageCode(), languageItem.getValue());
    }

    public ScrollPaneSection(LanguageItemGroup languageItemGroup, String languageCode) {
        this(languageItemGroup.getLanguageItemByLanguageCode(languageCode));
        this.languageItemGroup = languageItemGroup;
    }

    public ScrollPaneSection(EventEditLanguageAdded event) {
        this(event.getLanguageAdded().getLangCode(), "");
    }


    // Serialization and Deserialization

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();

        if (langEnum == null) {
            result.put("langEnum", "null");
        }
        else {
            result.put("langEnum", langEnum.getName());
        }

        result.put("alreadyAddedLanguages", alreadyAddedLanguages);
        
        result.put("originalValue", originalValue);
        
        result.put("isChanged", isChanged);
        
        result.put("value", txtValue.getText());
        
        if (languageItemGroup == null) {
            result.put("languageItemGroup", "null");
        }
        else {
            result.put("languageItemGroup", languageItemGroup.toMap());
        }

        return result;
    }

    public void fromMap(Map<String, Object> map) {
        // LangEnum
        String langEnumName = (String) map.get("langEnum");
        langEnum = LanguagesEnum.fromName(langEnumName);

        // Already Added Languages
        @SuppressWarnings("unchecked")
        List<String> alreadyAddedLang = (List<String>) map.get("alreadyAddedLanguages");
        if (alreadyAddedLang != null) {
            alreadyAddedLanguages = alreadyAddedLang;
        }
        else {
            alreadyAddedLanguages = new ArrayList<>();
        }

        // Original Value
        originalValue = (String) map.get("originalValue");

        // Is Changed
        isChanged = (boolean) map.get("isChanged");

        // Value
        String value = (String) map.get("value");
        if (value != null) {
            txtValue.setText(value);
        }

        // Language Item Group
        @SuppressWarnings("unchecked")
        Map<String, Object> languageItemGroupMap = (Map<String, Object>) map.get("languageItemGroup");
        if (languageItemGroupMap != null) {
            languageItemGroup = new LanguageItemGroup();
            languageItemGroup.fromMap(languageItemGroupMap);
        }
        else {
            languageItemGroup = null;
        }

        populateLanguageComboBox();
        hideMessage();
    }

    // Setters and Getters

    public void setAlreadyAddedLanguages(List<String> alreadyAddedLanguages) {
        this.alreadyAddedLanguages = alreadyAddedLanguages;
        populateLanguageComboBox();
    }

    public String getValue() {
        hideMessage();
        return txtValue.getText();
    }

    public LanguageItem getValueAsLanguageItem() {
        LanguageItem result = new LanguageItem();
        if (langEnum != null) {
            result.setLanguageCode(langEnum.getLangCode());
        }
        result.setValue(txtValue.getText());
        return result;
    }

    public void setValue(String value) {
        txtValue.setText(value);
    }

    public String getLanguageCode() {
        return langEnum.getLangCode();
    }

    public boolean isChanged() {
        return !originalValue.equals(txtValue.getText());
    }

    public void showMessage(String message, MessageType messageType) {
        hideMessage();
        lblMsg.getStyleClass().clear();

        switch (messageType) {
            case NORMAL:
                lblMsg.getStyleClass().add("label-msg-normal");
                break;
            case WORKING:
                lblMsg.getStyleClass().add("label-msg-working");
                break;
            case ERROR:
                lblMsg.getStyleClass().add("label-msg-error");
                break;
        }

        lblMsg.setText(message);
        lblMsg.setVisible(true);
        lblMsg.setManaged(true);
    }

    public void showMessage(String message) {
        showMessage(message, MessageType.NORMAL);
    }

    public void hideMessage() {
        lblMsg.setVisible(false);
        lblMsg.setManaged(false);
        hideYesNoButtons();
    }

    public void setLanguageItemGroup(LanguageItemGroup languageItemGroup) {
        this.languageItemGroup = languageItemGroup;
        LanguageItem languageItem = languageItemGroup.getLanguageItemByLanguageCode(langEnum.getLangCode());

        if (languageItem != null) {
            txtValue.setText(languageItem.getValue());
        }

        hideMessage();
        originalValue = languageItem.getValue();
    }

    // Private Methods

    private void showYesNoButtons() {
        btnYes.setVisible(true);
        btnYes.setManaged(true);
        btnNo.setVisible(true);
        btnNo.setManaged(true);
        regMsg.setVisible(false);
        regMsg.setManaged(false);
    }

    private void hideYesNoButtons() {
        btnYes.setVisible(false);
        btnYes.setManaged(false);
        btnNo.setVisible(false);
        btnNo.setManaged(false);
        regMsg.setVisible(true);
        regMsg.setManaged(true);
    }

    private void createWidgets(String value) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LanguageScrollPaneSection.fxml"));
        fxmlLoader.setController(this);
        try {
            VBox content = fxmlLoader.load();
            this.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lblLangCode.setText(langEnum.getLangCode());
        lblLangName.setText(langEnum.getName());
        txtValue.setText(value);
        originalValue = value;
        populateLanguageComboBox();

        // Set listener for txtValue change
        txtValue.textProperty().addListener((observable, oldValue, newValue) -> {
            EventEditLanguageChanged eventLangChanged = new EventEditLanguageChanged(langEnum);
            Stage primaryStage = (Stage) txtValue.getScene().getWindow();
            primaryStage.fireEvent(eventLangChanged);
            hideMessage();
        });
    }

    private void populateLanguageComboBox() {
        String currentItem = cmbTranslateFrom.getValue();
        cmbTranslateFrom.getItems().clear();
        
        for (String langCode : alreadyAddedLanguages) {
            if (!langCode.equals(langEnum.getLangCode())) {
                cmbTranslateFrom.getItems().add(langCode);
            }
        }        

        if (currentItem != null && !currentItem.isEmpty() && cmbTranslateFrom.getItems().contains(currentItem)) {
            cmbTranslateFrom.setValue(currentItem);
        }
    }

    private void showCloseConfirmMessage() {
        if (languageItemGroup != null) {
            if (languageItemGroup.getListOfLanguageCodes().contains(lblLangCode.getText())) {
                showMessage("Can't close. Language item require this language.", MessageType.ERROR);
                return;
            }
        }

        btnYes.setId("close");
        btnNo.setId("close");
        showMessage("Remove this language ?", MessageType.NORMAL);
        showYesNoButtons();
    }

    // FXML Events

    @FXML
    public void onBtnTranslateClick() {
        showMessage("Translating...", MessageType.WORKING);
        String response = UTranslate.translate(txtValue.getText(), langEnum.getLangCode(), LanguagesEnum.fromName(cmbTranslateFrom.getValue()).getGoogleCode());
        txtValue.setText(response);
        hideMessage();
    }

    @FXML
    public void onBtnCloseClick() {
        showCloseConfirmMessage();
    }

    @FXML
    public void onBtnYesClick() {
        switch (btnYes.getId()) {
            case "close": {
                Stage primaryStage = (Stage) btnYes.getScene().getWindow();
                EventEditLanguageRemoved eventEditLanguageRemoved = new EventEditLanguageRemoved(LanguagesEnum.fromLangCode(lblLangCode.getText()));
                hideMessage();
                primaryStage.fireEvent(eventEditLanguageRemoved);
                break;
            }
        }
    }

    @FXML
    public void onBtnNoClick() {
        hideMessage();
    }
    

}
