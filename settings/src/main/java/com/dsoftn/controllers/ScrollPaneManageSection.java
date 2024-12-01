package com.dsoftn.controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import com.dsoftn.controllers.ScrollPaneSection.MessageType;

import javafx.application.Platform;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.Settings.Settings;
import com.dsoftn.utils.UTranslate.LanguagesEnum;
import com.dsoftn.utils.UTranslate;
import com.dsoftn.events.EventAddLanguageToFile;
import com.dsoftn.events.EventWriteLog;

public class ScrollPaneManageSection extends VBox {
    public enum Action {
        NONE,
        TRANSLATE_MISSING,
        TRANSLATE_ALL,
        DELETE,
        ADD,
        ADD_AND_TRANSLATE;

        public static Action fromName(String name) {
            for (Action type : values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    // FXML Widgets

    @FXML
    private Label lblLangCode; // Language Code mark
    @FXML
    private Label lblLangName; // Language Name mark
    @FXML
    private ComboBox<String> cmbTranslateFrom; // Language to translate from
    @FXML
    private Label lblStatus; // Status mark
    @FXML
    private Label lblAction; // Action mark
    @FXML
    private Button btnNoAction; // Remove all actions
    @FXML
    private Button btnTransMissing; // Translate only missing entries
    @FXML
    private Button btnTransAll; // Retranslate all entries
    @FXML
    private Button btnDelete; // Delete this language
    @FXML
    private Button btnAdd; // Add this language
    @FXML
    private Button btnAddAndTrans; // Add and translate
    @FXML
    private Label lblAboutTo; // About to
    @FXML
    private Label lblInfo; // Info mark above buttons

    // Properties
    private LanguagesEnum langEnum = null;
    private List<String> alreadyAddedLanguages = new ArrayList<>();
    private String languageFileName = "";
    private Action action = Action.NONE;
    private Boolean langPresent = null;

    public ScrollPaneManageSection(String languageCode, String languageFileName) {
        this.langEnum = LanguagesEnum.fromLangCode(languageCode);
        this.languageFileName = languageFileName;

        if (this.langEnum == null) {
            throw new IllegalArgumentException("Invalid language code: " + languageCode);
        }

        this.langPresent = isLanguagePresent();
        createWidgets();
        updateWidgets();
    }

    public ScrollPaneManageSection(EventAddLanguageToFile event, String languageFileName) {
        this(event.getLanguageAdded().getLangCode(), languageFileName);
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

        result.put("action", action.toString());
        
        result.put("langPresent", langPresent);
        
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
        populateLanguageComboBox();

        // Current Translate Language
        if (map.get("currentTranslateLanguage") != null) {
            String currentTranslateLanguage = (String) map.get("currentTranslateLanguage");
            if (currentTranslateLanguage != null) {
                cmbTranslateFrom.getSelectionModel().select(currentTranslateLanguage);
            }
        }

        // Action
        if (map.get("action") != null) {
            String actionName = (String) map.get("action");
            if (actionName != null) {
                action = Action.fromName(actionName);
            }
        }
        setAction(action);

        // LangPresent
        if (map.get("langPresent") != null) {
            langPresent = (Boolean) map.get("langPresent");
        }

        updateWidgets();
    }

    // Setters and Getters

    public void setAlreadyAddedLanguages(List<String> alreadyAddedLanguages) {
        this.alreadyAddedLanguages = alreadyAddedLanguages;
        populateLanguageComboBox();
    }

    public LanguagesEnum getLanguage() {
        return langEnum;
    }

    public boolean isLanguagePresent() {
        Settings settings = new Settings();
        settings.languagesFilePath = languageFileName;
        settings.load(false, true, false);

        return settings.getAvailableLanguageCodes().contains(langEnum.getLangCode());
    }

    // Private Methods

    private void setAction(Action action) {
        lblAction.getStyleClass().remove("label-msg-normal");
        lblAction.getStyleClass().remove("label-msg-working");
        lblAction.getStyleClass().remove("label-msg-error");

        if (action == Action.NONE) {
            lblAboutTo.setVisible(false);
            lblAction.setVisible(true);
            lblAction.setText("No action");
            lblAction.getStyleClass().add("label-msg-normal");
        }
        else if (action == Action.TRANSLATE_MISSING) {
            lblAboutTo.setVisible(true);
            lblAction.setVisible(true);
            lblAction.setText("Translate empty entries");
            lblAction.getStyleClass().add("label-msg-working");
        }
        else if (action == Action.TRANSLATE_ALL) {
            lblAboutTo.setVisible(true);
            lblAction.setVisible(true);
            lblAction.setText("Translate all entries");
            lblAction.getStyleClass().add("label-msg-working");
        }
        else if (action == Action.DELETE) {
            lblAboutTo.setVisible(true);
            lblAction.setVisible(true);
            lblAction.setText("Delete this language");
            lblAction.getStyleClass().add("label-msg-error");
        }
        else if (action == Action.ADD) {
            lblAboutTo.setVisible(true);
            lblAction.setVisible(true);
            lblAction.setText("Add this language");
            lblAction.getStyleClass().add("label-msg-working");
        }
        else if (action == Action.ADD_AND_TRANSLATE) {
            lblAboutTo.setVisible(true);
            lblAction.setVisible(true);
            lblAction.setText("Add and translate");
            lblAction.getStyleClass().add("label-msg-working");
        }
        else {
            lblAboutTo.setVisible(false);
            lblAction.setVisible(true);
            lblAction.setText("Unexpected action");
            lblAction.getStyleClass().add("label-msg-error");
        }
            
    }

    private void createWidgets() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LanguageManageScrollPaneSection.fxml"));
        fxmlLoader.setController(this);
        try {
            VBox content = fxmlLoader.load();
            this.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lblLangCode.setText(langEnum.getLangCode());
        lblLangName.setText(langEnum.getName());
        setAction(action);
        populateLanguageComboBox();
    }

    private void updateWidgets() {
        // Load file to Settings
        Settings settings = new Settings();
        settings.languagesFilePath = languageFileName;
        settings.load(false, true, false);

        // Find missing and total translations
        List<LanguageItem> languageItems = settings.getListAllLanguageItemsForLanguage(langEnum);
        Integer totalItems;
        Integer missingTrans;
        if (languageItems == null || languageItems.isEmpty()) {
            totalItems = 0;
            missingTrans = 0;
        }
        else {
            totalItems = languageItems.size();
            missingTrans = languageItems.stream().filter(item -> item.getValue().isEmpty()).collect(Collectors.toList()).size();
        }
        
        // Update lblInfo text
        lblInfo.setText("Total items: " + totalItems + " - Missing translations: " + missingTrans);

        // Set Status label and buttons availability
        if (totalItems == 0) {
            btnTransMissing.setDisable(true);
            btnTransAll.setDisable(true);
            lblStatus.setText("Empty");
        }
        else if (missingTrans == 0) {
            btnTransMissing.setDisable(true);
            btnTransAll.setDisable(false);
            lblStatus.setText("Normal");
        }
        else {
            btnTransMissing.setDisable(false);
            btnTransAll.setDisable(false);
            lblStatus.setText("Missing" + missingTrans + " translations");
        }

        // Set buttons availability depending on language presence in Settings
        if (langPresent) {
            btnAdd.setVisible(false);
            btnAdd.setManaged(false);
            btnAddAndTrans.setVisible(false);
            btnAddAndTrans.setManaged(false);
        }
        else {
            btnTransMissing.setVisible(false);
            btnTransMissing.setManaged(false);
            btnTransAll.setVisible(false);
            btnTransAll.setManaged(false);
            btnDelete.setVisible(false);
            btnDelete.setManaged(false);
        }
    }

    private void populateLanguageComboBox() {
        for (String langCode : alreadyAddedLanguages) {
            if (!langCode.equals(langEnum.getLangCode())) {
                LanguagesEnum langEnum = LanguagesEnum.fromLangCode(langCode);
                cmbTranslateFrom.getItems().add(langEnum.getName());
            }
        }        
    }

    private void log(String message) {
        log(message, 0);
    }

    private void log(String message, int indentLevel) {
        EventWriteLog event = new EventWriteLog(message, indentLevel);
        Stage primaryStage = (Stage) lblLangCode.getScene().getWindow();
        primaryStage.fireEvent(event);
    }

    // FXML Events

    @FXML
    private void onBtnNoActionClick() {
        action = Action.NONE;
        setAction(action);
        log("Language Manager: " + langEnum.getName() + ": No action selected.", 1);
    }    

    @FXML
    private void onBtnTransMissingClick() {
        action = Action.TRANSLATE_MISSING;
        setAction(action);
        log("Language Manager: " + langEnum.getName() + ": Translate empty entries selected.", 1);
    }

    @FXML
    private void onBtnTransAllClick() {
        action = Action.TRANSLATE_ALL;
        setAction(action);
        log("Language Manager: " + langEnum.getName() + ": Translate all entries selected.", 1);
    }

    @FXML
    private void onBtnDeleteClick() {
        action = Action.DELETE;
        setAction(action);
        log("Language Manager: " + langEnum.getName() + ": Delete language selected.", 1);
    }

    @FXML
    private void onBtnAddClick() {
        action = Action.ADD;
        setAction(action);
        log("Language Manager: " + langEnum.getName() + ": Add language selected.", 1);
    }

    @FXML
    private void onBtnAddAndTransClick() {
        action = Action.ADD_AND_TRANSLATE;
        setAction(action);
        log("Language Manager: " + langEnum.getName() + ": Add language and translate selected.", 1);
    }

    

}
