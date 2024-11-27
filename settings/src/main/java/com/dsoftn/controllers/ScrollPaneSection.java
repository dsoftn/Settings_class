package com.dsoftn.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import com.dsoftn.Settings.LanguageItemGroup;

import javafx.application.Platform;
import java.io.IOException;
import java.io.File;
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
import com.dsoftn.events.EventWriteLog;

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
    private String translateFromValue = "";

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
            result.put("langEnum", null);
        }
        else {
            result.put("langEnum", langEnum.getName());
        }

        result.put("alreadyAddedLanguages", alreadyAddedLanguages);

        result.put("currentTranslateLanguage", cmbTranslateFrom.getValue());
        
        result.put("originalValue", originalValue);
        
        result.put("isChanged", isChanged);
        
        result.put("value", txtValue.getText());
        
        if (languageItemGroup == null) {
            result.put("languageItemGroup", null);
        }
        else {
            result.put("languageItemGroup", languageItemGroup.toMap());
        }

        return result;
    }

    public void fromMap(Map<String, Object> map) {
        // LangEnum
        String langEnumName = (String) map.get("langEnum");
        if (langEnumName == null) {
            langEnum = LanguagesEnum.UNKNOWN;
        }
        else {
            langEnum = LanguagesEnum.fromName(langEnumName);
        }
        if (langEnum == null) {
            langEnum = LanguagesEnum.UNKNOWN;
        }
        else {
            lblLangCode.setText(langEnum.getLangCode());
            lblLangName.setText(langEnum.getName());
        }

        // Already Added Languages
        if (map.get("alreadyAddedLanguages") != null) {
            @SuppressWarnings("unchecked")
            List<String> alreadyAddedLang = (List<String>) map.get("alreadyAddedLanguages");
            if (alreadyAddedLang != null) {
                alreadyAddedLanguages = alreadyAddedLang;
            }
            else {
                alreadyAddedLanguages = new ArrayList<>();
            }
        }

        String currentTranslateLanguage = (String) map.get("currentTranslateLanguage");
        if (currentTranslateLanguage != null) {
            cmbTranslateFrom.setValue(currentTranslateLanguage);
            translateFromValue = currentTranslateLanguage;
        }

        // Original Value
        originalValue = (String) map.get("originalValue");

        // Is Changed
        if (map.get("isChanged") != null) {
            isChanged = (boolean) map.get("isChanged");
        }

        // Value
        String value = (String) map.get("value");
        if (value != null) {
            txtValue.setText(value);
        }

        // Language Item Group
        if (map.get("languageItemGroup") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> languageItemGroupMap = (Map<String, Object>) map.get("languageItemGroup");
            if (languageItemGroupMap != null) {
                languageItemGroup = new LanguageItemGroup();
                languageItemGroup.fromMap(languageItemGroupMap);
            }
            else {
                languageItemGroup = null;
            }
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

        if (languageItemGroup == null) {
            originalValue = txtValue.getText();
            showMessage("New item");
            return;
        }
        LanguageItem languageItem = languageItemGroup.getLanguageItemByLanguageCode(langEnum.getLangCode());

        if (languageItem != null) {
            originalValue = languageItem.getValue();
            txtValue.setText(languageItem.getValue());
            hideMessage();
        }
        else {
            originalValue = "";
            txtValue.setText(originalValue);
            showMessage("Language not in item");
        }
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

        // Set image for close button
        Image img = new Image(getClass().getResourceAsStream("/images/close.png"));
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(23);
        imgView.setFitHeight(23);
        imgView.setPreserveRatio(true);
        btnClose.setGraphic(imgView);

        // Set listener for txtValue change
        txtValue.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                EventEditLanguageChanged eventLangChanged = new EventEditLanguageChanged(langEnum);
                Stage primaryStage = (Stage) txtValue.getScene().getWindow();
                primaryStage.fireEvent(eventLangChanged);
                hideMessage();
            });
        });

        // Set listener for cmbTranslateFrom change
        cmbTranslateFrom.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                translateFromValue = "";
            }
            else {
                translateFromValue = newSelection;
            }
        });

        hideMessage();
    }

    private void populateLanguageComboBox() {
        String transV = translateFromValue;
        cmbTranslateFrom.getItems().clear();
        
        for (String langCode : alreadyAddedLanguages) {
            if (!langCode.equals(langEnum.getLangCode())) {
                LanguagesEnum langEnum = LanguagesEnum.fromLangCode(langCode);
                cmbTranslateFrom.getItems().add(langEnum.getName());
            }
        }        

        translateFromValue = transV;
        if (translateFromValue != null && !translateFromValue.isEmpty() && cmbTranslateFrom.getItems().contains(translateFromValue)) {
            cmbTranslateFrom.getSelectionModel().select(translateFromValue);
            cmbTranslateFrom.setValue(translateFromValue);
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
        showMessage("Remove lang ?", MessageType.NORMAL);
        showYesNoButtons();
    }

    private void log(String message) {
        log(message, 0);
    }

    private void log(String message, int indentLevel) {
        EventWriteLog event = new EventWriteLog(message, indentLevel);
        Stage primaryStage = (Stage) txtValue.getScene().getWindow();
        primaryStage.fireEvent(event);
    }

    // FXML Events

    @FXML
    public void onBtnTranslateClick() {
        if (cmbTranslateFrom.getValue() == null) {
            showMessage("Select language to translate from");
            return;
        }

        log("Translating from " + cmbTranslateFrom.getValue() + " to " + langEnum.getName() + " started...");

        // Check if translator.exe is in project working directory
        File translatorEXE = new File("translator.exe");
        if (!translatorEXE.exists()) {
            showMessage("Translator not found", MessageType.ERROR);
            log("Translator not found", 1);
            return;
        }

        // Get text to translate
        Parent parent = this.getParent();
        ScrollPaneContent content = (ScrollPaneContent) parent;
        String text;

        if (content != null) {
            text = content.getValue(LanguagesEnum.fromName(cmbTranslateFrom.getValue()).getLangCode());
            if (text == null) {
                showMessage("Can't find text to translate", MessageType.ERROR);
                log("Can't find text to translate", 2);
                return;
            }
        }
        else {
            showMessage("Can't reach Content", MessageType.ERROR);
            log("Can't reach Content", 2);
            return;
        }

        if (text == null || text.isEmpty()) {
            hideMessage();
            txtValue.setText("");
            log("No text to translate", 2);
            return;
        }

        // Check if Translator Server is running
        if (!UTranslate.isTranslatorServerRunning()) {
            showMessage("Starting Translator Server...");
            log("Translator Server not running. Starting Translator Server...", 1);

            // Starting translator server
            Task<String> translatorStartTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    boolean serverStarted = UTranslate.startTranslatorServer();
                    if (!serverStarted) {
                        showMessage("Can't start Translator Server", MessageType.ERROR);
                        log("Can't start Translator Server", 1);
                        return "Can't start Translator Server";
                    }
                        
                    // Attempt to connect to Translator Server 10 times with 2 seconds delay
                    for (int i = 0; i < 10; i++) {
                        log("Attempting to connect to Translator Server...(" + (i + 1) + "/10)", 2);
                        if (UTranslate.isTranslatorServerRunning()) {
                            log("Connected to Translator Server", 2);
                            break;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            log("InterruptedException: " + e.getMessage() , 2);
                        }
                    }
                    if (!UTranslate.isTranslatorServerRunning()) {
                        showMessage("Can't connect to Translator Server", MessageType.ERROR);
                        log("Can't connect to Translator Server", 2);
                        return "Can't connect to Translator Server";
                    }
                    else {
                        log("Connected to Translator Server", 2);
                        return "";
                    }
                }
            };

            translatorStartTask.setOnSucceeded(event -> {
                String response = translatorStartTask.getValue();
                if (response != null && !response.isEmpty()) {
                    return;
                }
                else {
                    showMessage("Translating...", MessageType.WORKING);
                    log("Translating...", 1);
                    
                    // Translate
                    Task<String> translationTask = new Task<>() {
                        @Override
                        protected String call() throws Exception {
                            return UTranslate.translate(text, LanguagesEnum.fromName(cmbTranslateFrom.getValue()), langEnum);
                        }
                    };
            
                    translationTask.setOnSucceeded(event1 -> {
                        String response1 = translationTask.getValue();
                        if (response1 == null) {
                            showMessage("Translation failed", MessageType.ERROR);
                            log("NULL Response. Translation failed", 2);
                        }
                        else {
                            txtValue.setText(response1);
                            showMessage("Translation successful", MessageType.NORMAL);
                            log("Translation successful", 2);
                        }
                    });
            
                    translationTask.setOnFailed(event1 -> {
                        showMessage("Translation failed", MessageType.ERROR);
                        System.out.println("Translation failed: " + translationTask.getException().getMessage());
                        log("Translation failed", 2);
                    });
            
                    new Thread(translationTask).start();
                }
            });

            translatorStartTask.setOnFailed(event -> {
                return;
            });

            new Thread(translatorStartTask).start();
        }
        else {
            showMessage("Translating...", MessageType.WORKING);
            log("Translating...", 1);
            
            // Translate
            Task<String> translationTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return UTranslate.translate(text, LanguagesEnum.fromName(cmbTranslateFrom.getValue()), langEnum);
                }
            };
    
            translationTask.setOnSucceeded(event1 -> {
                String response1 = translationTask.getValue();
                if (response1 == null) {
                    showMessage("Translation failed", MessageType.ERROR);
                    log("NULL Response. Translation failed", 2);
                }
                else {
                    txtValue.setText(response1);
                    showMessage("Translation successful", MessageType.NORMAL);
                    log("Translation successful", 2);
                }
            });
    
            translationTask.setOnFailed(event1 -> {
                showMessage("Translation failed", MessageType.ERROR);
                System.out.println("Translation failed: " + translationTask.getException().getMessage());
                log("Translation failed", 2);
            });
    
            new Thread(translationTask).start();
        }

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
