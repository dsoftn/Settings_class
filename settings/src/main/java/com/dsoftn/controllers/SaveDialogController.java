package com.dsoftn.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import com.dsoftn.utils.PyDict;
import com.dsoftn.utils.LanguagesEnum;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.Settings.Settings;
import com.dsoftn.Settings.SettingsItem;
import com.dsoftn.controllers.MainWinController.Section;
import com.dsoftn.events.EventWriteLog;
import com.dsoftn.events.EventSettingsSaved;


public class SaveDialogController {
    public enum SaveSection { SETTINGS, LANGUAGE, ALL, COMPLETED, COMPLETED_WITH_CANCEL, ERROR, ERROR_KEEP, WORKING };
    
    // Variables
    private Stage primaryStage = null;
    private PyDict appState = null;
    private SaveSection saveType = null;
    private SaveSection workingWith = null;
    private boolean resultValue = false;
    private List<List<Path>> rollBackData = new ArrayList<List<Path>>();


    // FXML Widgets

    // Section Settings
    @FXML
    private VBox layoutSectionStt; // Section Settings
    @FXML
    private Label lblSttAffected; // Number of settings keys affected
    @FXML
    private Label lblSttDeleted; // Number of settings keys deleted
    @FXML
    private Label lblSttChanged; // Number of settings keys changed
    @FXML
    private VBox layoutSectionSttFiles; // Settings files to be updated

    // Section Language
    @FXML
    private VBox layoutSectionLang; // Section Language
    @FXML
    private Label lblLangAffected; // Number of language keys affected
    @FXML
    private Label lblLangDeleted; // Number of language keys deleted
    @FXML
    private Label lblLangChanged; // Number of language keys changed
    @FXML
    private VBox layoutSectionLangFiles; // Language files to be updated

    // Section Completed successfully
    @FXML
    private VBox layoutSectionCompleted; // Section Completed successfully
    @FXML
    private Label lblImageCompleted; // Image of success
    @FXML
    private Label lblCompleted; // Success text

    // Section Error
    @FXML
    private VBox layoutSectionError; // Section Error
    @ FXML
    private Label lblImageError; // Image of error
    @FXML
    private Label lblError; // Error text

    // Section Working
    @FXML
    private VBox layoutSectionWorking; // Section Working
    @FXML
    private Label lblImageWorking; // Image of working


    // Buttons
    @FXML
    private Button btnSave; // Save all data
    @FXML
    private Button btnCancel; // Cancel
    @FXML
    private Button btnOk; // OK
    @FXML
    private Button btnRollBack; // Roll back changes
    @FXML
    private Button btnKeepChanges; // Keep changes
    

    public void initialize() {
        // Put image in lblImageCompleted
        Image imageSuccess = new Image(getClass().getResourceAsStream("/images/success.png"));
        ImageView imageViewSuccess = new ImageView(imageSuccess);
        lblImageCompleted.setGraphic(imageViewSuccess);
        // Put image in lblImageError
        Image imageError = new Image(getClass().getResourceAsStream("/images/save_error.png"));
        ImageView imageViewError = new ImageView(imageError);
        lblImageError.setGraphic(imageViewError);
        // Put image in lblImageWorking
        Image imageWorking = new Image(getClass().getResourceAsStream("/images/working.png"));
        ImageView imageViewWorking = new ImageView(imageWorking);
        lblImageWorking.setGraphic(imageViewWorking);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setAppState(PyDict appState) {
        this.appState = appState;
        log("App state received", 1);
    }

    /**
     * Select what you want to save in the dialog
     * SaveSection.SETTINGS - will save only settings
     * SaveSection.LANGUAGE - will save only language
     * SaveSection.ALL - will save both settings and language
     * @param type - <b>SaveSection</b> - type of data to save
     */
    public void setTypeOfDataToBeSaved(SaveSection type) {
        this.workingWith = type;
        setSaveType(type);
    }

    private void setSaveType(SaveSection type) {
        log("Save type changed to: " + type.name(), 1);
        this.saveType = type;
        
        // Disable all sections and buttons
        layoutSectionStt.setVisible(false);
        layoutSectionStt.setManaged(false);
        layoutSectionLang.setVisible(false);
        layoutSectionLang.setManaged(false);
        layoutSectionCompleted.setVisible(false);
        layoutSectionCompleted.setManaged(false);
        layoutSectionError.setVisible(false);
        layoutSectionError.setManaged(false);
        layoutSectionWorking.setVisible(false);
        layoutSectionWorking.setManaged(false);

        btnCancel.setVisible(false);
        btnCancel.setManaged(false);
        btnOk.setVisible(false);
        btnOk.setManaged(false);
        btnSave.setVisible(false);
        btnSave.setManaged(false);
        btnRollBack.setVisible(false);
        btnRollBack.setManaged(false);
        btnKeepChanges.setVisible(false);
        btnKeepChanges.setManaged(false);

        if (type == SaveSection.SETTINGS) {
            List<String> sttFilesToUpdate = appState.getPyDictValue(PyDict.concatKeys(Section.SETTINGS.toString(), "updateSttFilesPaths"));
            layoutSectionStt.setVisible(true);
            layoutSectionStt.setManaged(true);
            if (getChangedSettingsItemsList().isEmpty() || sttFilesToUpdate == null || sttFilesToUpdate.isEmpty()) {
                if (getChangedSettingsItemsList().isEmpty()) {
                    layoutSectionCompleted.setVisible(true);
                    layoutSectionCompleted.setManaged(true);
                    lblCompleted.setText("No settings to save");
                    btnOk.setVisible(true);
                    btnOk.setManaged(true);
                }
                else {
                    layoutSectionCompleted.setVisible(true);
                    layoutSectionCompleted.setManaged(true);
                    lblCompleted.setText("No selected settings files to update");
                    btnOk.setVisible(true);
                    btnOk.setManaged(true);
                }
            }
            else {
                btnSave.setVisible(true);
                btnSave.setManaged(true);
                btnCancel.setVisible(true);
                btnCancel.setManaged(true);
            }
            populateWidgets();
        }
        else if (type == SaveSection.LANGUAGE) {
            List<String> langFilesToUpdate = appState.getPyDictValue(PyDict.concatKeys(Section.LANGUAGE.toString(), "updateLangFilesPaths"));
            layoutSectionLang.setVisible(true);
            layoutSectionLang.setManaged(true);
            if (getChangedLanguageItemsList().isEmpty() || langFilesToUpdate == null || langFilesToUpdate.isEmpty()) {
                if (getChangedLanguageItemsList().isEmpty()) {
                    layoutSectionCompleted.setVisible(true);
                    layoutSectionCompleted.setManaged(true);
                    lblCompleted.setText("No language to save");
                    btnOk.setVisible(true);
                    btnOk.setManaged(true);
                }
                else {
                    layoutSectionCompleted.setVisible(true);
                    layoutSectionCompleted.setManaged(true);
                    lblCompleted.setText("No selected language files to update");
                    btnOk.setVisible(true);
                    btnOk.setManaged(true);
                }
            }
            else {
                btnSave.setVisible(true);
                btnSave.setManaged(true);
                btnCancel.setVisible(true);
                btnCancel.setManaged(true);
            }

            populateWidgets();
        }
        else if (type == SaveSection.ALL) {
            boolean hasSttItems = true;
            boolean hasLangItems = true;

            // Show Settings
            List<String> sttFilesToUpdate = appState.getPyDictValue(PyDict.concatKeys(Section.SETTINGS.toString(), "updateSttFilesPaths"));
            layoutSectionStt.setVisible(true);
            layoutSectionStt.setManaged(true);
            lblCompleted.setText("Settings");
            if (getChangedSettingsItemsList().isEmpty() || sttFilesToUpdate == null || sttFilesToUpdate.isEmpty()) {
                hasSttItems = false;
                if (getChangedSettingsItemsList().isEmpty()) {
                    lblCompleted.setText(lblCompleted.getText() + "\nNo settings items to save");
                }
                else {
                    lblCompleted.setText(lblCompleted.getText() + "\nNo selected settings files to update");
                }
            }
            else {
                lblCompleted.setText(lblCompleted.getText() + "\nAll Settings files are successfully saved.");
                btnSave.setVisible(true);
                btnSave.setManaged(true);
                btnCancel.setVisible(true);
                btnCancel.setManaged(true);
            }

            // Show Language
            List<String> langFilesToUpdate = appState.getPyDictValue(PyDict.concatKeys(Section.LANGUAGE.toString(), "updateLangFilesPaths"));
            layoutSectionLang.setVisible(true);
            layoutSectionLang.setManaged(true);
            lblCompleted.setText(lblCompleted.getText() + "\nLanguage");
            if (getChangedLanguageItemsList().isEmpty() || langFilesToUpdate == null || langFilesToUpdate.isEmpty()) {
                hasLangItems = false;
                if (getChangedLanguageItemsList().isEmpty()) {
                    lblCompleted.setText(lblCompleted.getText() + "\nNo language items to save");
                }
                else {
                    lblCompleted.setText(lblCompleted.getText() + "\nNo selected language files to update");
                }
            }
            else {
                lblCompleted.setText(lblCompleted.getText() + "\nAll Language files are successfully saved.");
                btnSave.setVisible(true);
                btnSave.setManaged(true);
                btnCancel.setVisible(true);
                btnCancel.setManaged(true);
            }

            if (!hasSttItems && !hasLangItems) {
                layoutSectionCompleted.setVisible(true);
                layoutSectionCompleted.setManaged(true);
                btnOk.setVisible(true);
                btnOk.setManaged(true);
            }

            populateWidgets();
        }
        else if (type == SaveSection.COMPLETED) {
            layoutSectionCompleted.setVisible(true);
            layoutSectionCompleted.setManaged(true);
            btnOk.setVisible(true);
            btnOk.setManaged(true);
        }
        else if (type == SaveSection.COMPLETED_WITH_CANCEL) {
            layoutSectionCompleted.setVisible(true);
            layoutSectionCompleted.setManaged(true);
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
        }
        else if (type == SaveSection.ERROR) {
            layoutSectionError.setVisible(true);
            layoutSectionError.setManaged(true);
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
        }
        else if (type == SaveSection.ERROR_KEEP) {
            layoutSectionError.setVisible(true);
            layoutSectionError.setManaged(true);
            btnRollBack.setVisible(true);
            btnRollBack.setManaged(true);
            btnKeepChanges.setVisible(true);
            btnKeepChanges.setManaged(true);
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
        }
        else if (type == SaveSection.WORKING) {
            layoutSectionWorking.setVisible(true);
            layoutSectionWorking.setManaged(true);
        }

    }

    public boolean getResult() { return resultValue; } // Return value for other classes

    // Private methods

    private void populateWidgets() {
        log("Starting populate widgets", 1);
        if (appState == null) {
            log("appState is null, populating widgets failed", 2);
            return;
        }

        // Section SETTINGS
        
        log("Populating section SETTINGS", 2);
        List<SettingsItem> changedSttItems = getChangedSettingsItemsList();
        int sttDeleted = (int) changedSttItems.stream().filter(item -> item.getUserData().equals("Deleted")).count();
        lblSttAffected.setText(Integer.toString(changedSttItems.size()));
        log("Affected items: " + changedSttItems.size(), 3);
        lblSttDeleted.setText(Integer.toString(sttDeleted));
        log("Deleted items: " + sttDeleted, 3);
        lblSttChanged.setText(Integer.toString(changedSttItems.size() - sttDeleted));
        log("Changed items: " + (changedSttItems.size() - sttDeleted), 3);

        List<String> filesToUpdate = getSettingsFilesToUpdateList();
        layoutSectionSttFiles.getChildren().clear();
        Label titleLabel = new Label("Files to update:");
        titleLabel.setStyle("-fx-text-fill: #ffff00;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        layoutSectionSttFiles.getChildren().add(titleLabel);

        if (filesToUpdate.isEmpty()) {
            Label fileLabel = new Label("You did not select any files to update!\nPlease select at least one file.");
            // Align label text to center
            fileLabel.setAlignment(Pos.CENTER);
            fileLabel.setMaxWidth(Double.MAX_VALUE);
            fileLabel.setWrapText(true);
            fileLabel.setStyle("-fx-text-fill: rgb(226, 106, 136); -fx-font-size: 16px;");

            layoutSectionSttFiles.getChildren().add(fileLabel);
            log ("There is no selected settings file(s) to update", 3);
        }
        else {
            for (String path : filesToUpdate) {
                Label fileLabel = new Label(path);
                // Align label text to center
                fileLabel.setAlignment(Pos.CENTER);
                fileLabel.setMaxWidth(Double.MAX_VALUE);
                fileLabel.setWrapText(true);
                fileLabel.setStyle("-fx-text-fill: #00ffff;");

                layoutSectionSttFiles.getChildren().add(fileLabel);
                log ("File to update: " + path, 3);
            }
        }

        // Section LANGUAGE

        log("Populating section LANGUAGE", 2);
        List<LanguageItemGroup> changedLangItems = getChangedLanguageItemsList();
        int langDeleted = (int) changedLangItems.stream().filter(item -> item.getUserData().equals("Deleted")).count();
        lblLangAffected.setText(Integer.toString(changedLangItems.size()));
        log("Affected items: " + changedLangItems.size(), 3);
        lblLangDeleted.setText(Integer.toString(langDeleted));
        log("Deleted items: " + langDeleted, 3);
        lblLangChanged.setText(Integer.toString(changedLangItems.size() - langDeleted));
        log("Changed items: " + (changedLangItems.size() - langDeleted), 3);

        List<String> filesToUpdateLang = getLanguageFilesToUpdateList();
        layoutSectionLangFiles.getChildren().clear();
        Label titleLabelLang = new Label("Files to update:");
        titleLabelLang.setStyle("-fx-text-fill: #ffff00;");
        titleLabelLang.setMaxWidth(Double.MAX_VALUE);
        titleLabelLang.setWrapText(true);
        titleLabelLang.setAlignment(Pos.CENTER);
        layoutSectionLangFiles.getChildren().add(titleLabelLang);

        if (filesToUpdateLang.isEmpty()) {
            Label fileLabelLang = new Label("You did not select any files to update!\nPlease select at least one file.");
            // Align label text to center
            fileLabelLang.setAlignment(Pos.CENTER);
            fileLabelLang.setMaxWidth(Double.MAX_VALUE);
            fileLabelLang.setWrapText(true);
            fileLabelLang.setStyle("-fx-text-fill: rgb(226, 106, 136); -fx-font-size: 16px;");

            layoutSectionLangFiles.getChildren().add(fileLabelLang);
            log ("There is no selected language file(s) to update", 3);
        }
        else {
            for (String path : filesToUpdateLang) {
                Label fileLabelLang = new Label(path);
                // Align label text to center
                fileLabelLang.setAlignment(Pos.CENTER);
                fileLabelLang.setMaxWidth(Double.MAX_VALUE);
                fileLabelLang.setWrapText(true);
                fileLabelLang.setStyle("-fx-text-fill: #00ffff;");

                layoutSectionLangFiles.getChildren().add(fileLabelLang);
                log ("File to update: " + path, 3);
            }
        }

        log("Finished populating widgets", 1);
    }

    private List<String> getSettingsFilesToUpdateList() {
        if (appState == null) {
            return new ArrayList<String>();
        }

        ArrayList<String> filesToUpdate;
        filesToUpdate = appState.getPyDictValue(PyDict.concatKeys(Section.SETTINGS.toString(), "updateSttFilesPaths"));
        if (filesToUpdate == null) {
            return new ArrayList<String>();
        }

        return filesToUpdate;
    }

    private List<String> getLanguageFilesToUpdateList() {
        if (appState == null) {
            return new ArrayList<String>();
        }

        ArrayList<String> filesToUpdate;
        filesToUpdate = appState.getPyDictValue(PyDict.concatKeys(Section.LANGUAGE.toString(), "updateLangFilesPaths"));
        if (filesToUpdate == null) {
            return new ArrayList<String>();
        }

        return filesToUpdate;
    }

    private List<SettingsItem> getChangedSettingsItemsList() {
        if (appState == null) {
            return new ArrayList<SettingsItem>();
        }

        PyDict settingsToSave;
        settingsToSave = (PyDict) appState.getPyDictValue(PyDict.concatKeys(Section.SETTINGS.toString(), "sttChangedMap"));
        if (settingsToSave == null) {
            return new ArrayList<SettingsItem>();
        }

        List<SettingsItem> settingsList = new ArrayList<>();

        SettingsItem settingsItem;
        if (settingsToSave != null) {
            for (Map.Entry<String, Object> entry : settingsToSave.entrySet()) {
                settingsItem = (SettingsItem) entry.getValue();
                settingsList.add(settingsItem);
            }
        }

        return settingsList;
    }

    private List<LanguageItemGroup> getChangedLanguageItemsList() {
        if (appState == null) {
            return new ArrayList<LanguageItemGroup>();
        }

        PyDict languageToSave;
        languageToSave = (PyDict) appState.getPyDictValue(PyDict.concatKeys(Section.LANGUAGE.toString(), "langChangedMap"));
        if (languageToSave == null) {
            return new ArrayList<LanguageItemGroup>();
        }

        List<LanguageItemGroup> languageList = new ArrayList<>();

        LanguageItemGroup languageItem;
        if (languageToSave != null) {
            for (Map.Entry<String, Object> entry : languageToSave.entrySet()) {
                languageItem = (LanguageItemGroup) entry.getValue();
                languageList.add(languageItem);
            }
        }

        return languageList;
    }

    private void saveData() {
        SaveSection saveTypePrev = this.saveType;
        setSaveType(SaveSection.WORKING);
        this.saveType = saveTypePrev;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                saveDataTask();
                return null;
            }
        };

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                log("Unexpected error: Failed to save data", 1);
                log(task.getException().getMessage(), 2);
                String strErrors = "Unexpected error: Unable to save data:\n" + task.getException().getMessage();
                lblError.setText(strErrors);
                setSaveType(SaveSection.ERROR);
            });
        });

        Thread thread = new Thread(task);
        thread.start();

    }

    private void saveDataTask() {
        String strErrors = "";
        log("Starting data save...", 1);

        // If appState is null, return
        if (appState == null) {
            log("appState is null, saving data failed", 2);
            resultValue = false;
            strErrors = "Unable to save data: appState is null.";
            lblError.setText(strErrors);
            setSaveType(SaveSection.ERROR);
            return;
        }

        // Get list of files to update
        List<String> settingsFilesToUpdate = getSettingsFilesToUpdateList();
        List<String> languageFilesToUpdate = getLanguageFilesToUpdateList();

        // Rollback files
        for (String path : settingsFilesToUpdate) {
            log("File added to RollBack: " + path, 2);
            if (!rollBackAdd(path).isEmpty()) {
                log("Cannot create RollBackState for file: " + path, 3);
                strErrors += "Cannot create RollBackState for file: " + path + "\n";
            }
        }

        for (String path : languageFilesToUpdate) {
            log("File added to RollBack: " + path, 2);
            if (!rollBackAdd(path).isEmpty()) {
                log("Cannot create RollBackState for file: " + path, 3);
                strErrors += "Cannot create RollBackState for file: " + path + "\n";
            }
        }

        // If RollBackState creation failed, return
        if (!strErrors.isEmpty()) {
            log("RollBackState creation failed, saving data failed", 2);
            resultValue = false;
            lblError.setText(strErrors.strip());
            setSaveType(SaveSection.ERROR);
            return;
        }

        // Section SETTINGS
        if (workingWith == SaveSection.SETTINGS || workingWith == SaveSection.ALL) {
            log("Saving settings...", 2);
            List<SettingsItem> changedSttItems = getChangedSettingsItemsList();
            Settings settings = new Settings();
            
            // Check if all files are valid Settings files
            for (String path : settingsFilesToUpdate) {
                settings.userSettingsFilePath = path;
                try {
                    settings.load(true, false, false);
                    if (!settings.getLastErrorString().isEmpty()) {
                        strErrors += "Error in loading Settings file.\nFile: " + path + "\nError: " + settings.getLastErrorString() + "\n";
                        settings.clearErrorString();
                        log("Error in loading Settings file. File: " + path + " Error: " + settings.getLastErrorString(), 3);
                    }
                }
                catch (Exception e) {
                    log("Error in loading Settings file. File: " + path + " Error: " + e.getMessage(), 3);
                    strErrors += "Error in loading Settings file.\nFile: " + path + "\nError: " + e.getMessage() + "\n";
                }
            }

            if (strErrors.isEmpty()) {
                for (String path : settingsFilesToUpdate) {
                    if (appState.getPyDictBooleanValueEXPLICIT("chkAutoUpdateFiles") != true) {
                        boolean canBeUpdated = msgBoxInfoQuestion("Save", "Update file", "You are about to update file:\n" + path + "\n\nDo you want to continue?\n\n(This question is asked because you have 'AutoUpdateFiles' checkbox turned off.)");
                        if (!canBeUpdated) {
                            continue;
                        }
                    }

                    settings.userSettingsFilePath = path;
                    if (!settings.load(true, false, false)) {
                        log("Failed to load file: " + path, 3);
                        continue;
                    }
                    else {
                        log("Loaded file: " + path, 3);
                    }

                    for (SettingsItem originalItem : changedSttItems) {
                        SettingsItem item = originalItem.duplicate();
                        if (item.getUserData().equals("Deleted")) {
                            log("Deleting item: " + item.getKey(), 4);
                            if (settings.isUserSettingExists(item.getKey())) {
                                if (! settings.deleteUserSettingsItem(item.getKey())) {
                                    log("Failed to delete item: " + item.getKey(), 5);
                                    strErrors += "Item '" + item.getKey() + "' was not deleted in file: " + path + "\n";
                                }
                                else {
                                    log("Item: " + item.getKey() + " deleted", 5);
                                }
                            }
                            else {
                                log("Item: " + item.getKey() + " does not exist", 5);
                            }
                        }
                        else if (item.getUserData().equals("Changed")) {
                            log("Changing item: " + item.getKey(), 4);
                            if (!item.isValid()) {
                                log("Item: " + item.getKey() + " has Warning(s), did not pass validation test", 5);
                                strErrors += "Item '" + item.getKey() + "' has Warning(s), did not pass validation test\n";
                            }
                            item.setUserData("");
                            SettingsItem itemToSet = item.duplicate();
                            if (settings.isUserSettingExists(item.getKey())) {
                                itemToSet.setCreationDate(settings.getUserSettingsItem(item.getKey()).getCreationDate());
                            }
                            if (! settings.setUserSettingsItem(itemToSet)) {
                                log("Failed to change item: " + item.getKey(), 5);
                                strErrors += "Item '" + item.getKey() + "' was not changed in file: " + path + "\n";
                            }
                            else {
                                log("Item: " + item.getKey() + " changed", 5);
                            }
                        }
                    }
                    settings.clearErrorString();
                    try {
                        if (! settings.save(true, false, false)) {
                            log("Failed to save file: " + path, 3);
                            strErrors += "Error in saving Settings file.\nFile: " + path + "\nError: " + settings.getLastErrorString() + "\n";
                        }
                        else {
                            log("Saved file: " + path, 3);
                        }
                    }
                    catch (Exception e) {
                        log("Error in saving Settings file. File: " + path + " Error: " + e.getMessage(), 3);
                        strErrors += "Error in saving Settings file.\nFile: " + path + "\nError: " + e.getMessage() + "\n";
                    }
                }
            }
            log("Finished saving settings", 2);
        }

        // Section LANGUAGE
        if (workingWith == SaveSection.LANGUAGE || workingWith == SaveSection.ALL) {
            log("Saving language...", 2);
            List<LanguageItemGroup> changedLangItems = getChangedLanguageItemsList();
            Settings settings = new Settings();
            
            // Check if all files are valid Language files
            for (String path : languageFilesToUpdate) {
                settings.languagesFilePath = path;
                try {
                    settings.load(false, true, false);
                    if (!settings.getLastErrorString().isEmpty()) {
                        strErrors += "Error in loading Language file.\nFile: " + path + "\nError: " + settings.getLastErrorString() + "\n";
                        settings.clearErrorString();
                        log("Error in loading Language file. File: " + path + " Error: " + settings.getLastErrorString(), 3);
                    }
                }
                catch (Exception e) {
                    log("Error in loading Language file. File: " + path + " Error: " + e.getMessage(), 3);
                    strErrors += "Error in loading Language file.\nFile: " + path + "\nError: " + e.getMessage() + "\n";
                }
            }

            if (strErrors.isEmpty()) {
                for (String path : languageFilesToUpdate) {
                    if (appState.getPyDictBooleanValueEXPLICIT("chkAutoUpdateFiles") != true) {
                        boolean canBeUpdated = msgBoxInfoQuestion("Save", "Update file", "You are about to update file:\n" + path + "\n\nDo you want to continue?\n\n(This question is asked because you have 'AutoUpdateFiles' checkbox turned off.)");
                        if (!canBeUpdated) {
                            continue;
                        }
                    }

                    settings.languagesFilePath = path;
                    if (!settings.load(false, true, false)) {
                        log("Failed to load file: " + path, 3);
                        continue;
                    }
                    else {
                        log("Loaded file: " + path, 3);
                    }

                    for (LanguageItemGroup originalItem : changedLangItems) {
                        LanguageItemGroup item = originalItem.duplicate();

                        // If file is empty add languages to base
                        if (settings.getAvailableLanguageCodes().size() == 0) {
                            log("File has no languages in base, adding new languages...", 3);
                            for (LanguageItem langItem : item.getLanguageItems()) {
                                settings.addNewLanguageInBase(LanguagesEnum.fromLangCode(langItem.getLanguageCode()));
                                log("Added language: " + LanguagesEnum.fromLangCode(langItem.getLanguageCode()).getName(), 4);
                            }
                        }

                        if (item.getUserData().equals("Deleted")) {
                            log("Deleting item: " + item.getGroupKey(), 3);
                            if (settings.deleteLanguageItem(item.getGroupKey())) {
                                log("Item: " + item.getGroupKey() + " deleted", 4);
                            }
                            else {
                                log("Item: " + item.getGroupKey() + " does not exist", 4);
                            }
                        }
                        else if (item.getUserData().equals("Changed")) {
                            log("Changing item: " + item.getGroupKey(), 3);
                            List<String> requiredCodes = settings.getAvailableLanguageCodes();
                            if (settings.isLanguageItemGroupHasAllRequiredLanguages(item)) {
                                settings.mergeLanguageItemGroup(item);
                                log("Item: " + item.getGroupKey() + " changed", 4);
                            }
                            else {
                                log("Item: " + item.getGroupKey() + " does not have all required languages. Required languages:" + requiredCodes.toString(), 4);
                                log("Item skipped!", 4);
                            }
                        }
                    }
                    settings.clearErrorString();
                    try {
                        if (! settings.save(false, true, false)) {
                            log("Failed to save file: " + path, 3);
                            strErrors += "Error in saving Language file.\nFile: " + path + "\nError: " + settings.getLastErrorString() + "\n";
                        }
                        else {
                            log("Saved file: " + path, 3);
                        }
                    }
                    catch (Exception e) {
                        log("Error in saving Language file. File: " + path + " Error: " + e.getMessage(), 3);
                        strErrors += "Error in saving Language file.\nFile: " + path + "\nError: " + e.getMessage() + "\n";
                    }
                }
            }
            log("Finished saving language", 2);
        }

        if (strErrors.isEmpty()) {
            log("Finished saving data, completed without errors", 1);
            resultValue = true;
            setSaveType(SaveSection.COMPLETED);

            Boolean sttHasSaved;
            if (workingWith == SaveSection.SETTINGS || workingWith == SaveSection.ALL) {
                sttHasSaved = true;
            }
            else {
                sttHasSaved = false;
            }

            Boolean langHasSaved;
            if (workingWith == SaveSection.LANGUAGE || workingWith == SaveSection.ALL) {
                langHasSaved = true;
            }
            else {
                langHasSaved = false;
            }

            EventSettingsSaved eventSaved = new EventSettingsSaved(sttHasSaved, langHasSaved );
            Platform.runLater(() -> {
                primaryStage.fireEvent(eventSaved);
            });
        }
        else {
            log("Finished saving data, completed WITH ERRORS", 1);
            resultValue = false;
            lblError.setText(strErrors.strip());
            setSaveType(SaveSection.ERROR_KEEP);
        }
    }

    private String rollBackAdd(String sourcePath) {
        Path sourceFilePath = Paths.get(sourcePath);
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path tempDir = currentDir.resolve("temp");

        String tempFileName = rollBackData.size() + ".bak";
        Path destinationFile = tempDir.resolve(tempFileName);

        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectory(tempDir);
            }

            Files.copy(sourceFilePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            List<Path> rollBackItem = new ArrayList<Path>();
            rollBackItem.add(sourceFilePath);
            rollBackItem.add(destinationFile);
            rollBackData.add(rollBackItem);
            return "";
        }
        catch (IOException e) {
            return e.getMessage();
        }

    }

    private void rollBackExecute() {
        log("Rolling back changes", 1);
        for (List<Path> item : rollBackData) {
            try {
                Files.move(item.get(1), item.get(0), StandardCopyOption.REPLACE_EXISTING);
                log("Rolling back file: " + item.get(0), 2);
            }
            catch (IOException e) {
                log("Rolling back failed. File: " + item.get(0), 2);
                System.out.println(e.getMessage());
            }
        }

        rollBackData.clear();

        rollBackRemoveTempDir();
        log("'temp' directory removed", 2);
        log("RollBack completed", 1);
    }

    private void rollBackRemoveTempDir() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path tempDir = currentDir.resolve("temp");

        if (!Files.exists(tempDir)) {
            return;
        }

        try {
            Files.walk(tempDir)
                 .sorted((path1, path2) -> path2.compareTo(path1))
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         System.out.println("Cannot delete file: " + path + " - " + e.getMessage());
                     }
                 });

        } catch (IOException e) {
            System.out.println("Cannot delete directory: " + tempDir + " - " + e.getMessage());
        }
    }

    private boolean msgBoxInfoQuestion(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);

        ButtonType yesButton = ButtonType.YES;
        ButtonType noButton = ButtonType.NO;

        alert.getButtonTypes().setAll(yesButton, noButton);

        // Set button noButton as default button
        ((Button) alert.getDialogPane().lookupButton(yesButton)).setDefaultButton(true);
        ((Button) alert.getDialogPane().lookupButton(noButton)).setDefaultButton(false);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            return true;
        }
        else {
            return false;
        }
    }


    @FXML
    private void onBtnCancelClick() {
        resultValue = false;
        if (btnRollBack.isVisible()) {
            rollBackExecute();
            lblCompleted.setText("RollBack executed\nData was not saved.");
            setSaveType(SaveSection.COMPLETED_WITH_CANCEL);
            return;
        }

        Stage stage = (Stage) btnCancel.getScene().getWindow();
        log("Dialog closed. CANCEL pressed", 1);
        stage.close();
    }

    @FXML
    private void onBtnOkClick() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        log("Dialog closed. OK pressed", 1);
        rollBackRemoveTempDir();
        log("'temp' directory removed", 1);
        stage.close();
    }

    @FXML
    private void onBtnSaveClick() {
        saveData();
    }

    @FXML
    private void onBtnRollBackClick() {
        log("ROLLBACK button pressed", 1);
        resultValue = false;
        rollBackExecute();
        lblCompleted.setText("RollBack executed\nData was not saved.");
        setSaveType(SaveSection.COMPLETED_WITH_CANCEL);
    }

    @FXML
    private void onBtnKeepChanges() {
        log("KEEP CHANGES button pressed", 1);
        resultValue = true;
        lblCompleted.setText("Data is saved.\nSome items may not be saved correctly.");
        log("Data is saved. Some items may not behave as expected!", 2);
        setSaveType(SaveSection.COMPLETED);
        rollBackRemoveTempDir();
        log("'temp' directory removed", 1);

        Boolean sttHasSaved;
        if (workingWith == SaveSection.SETTINGS || workingWith == SaveSection.ALL) {
            sttHasSaved = true;
        }
        else {
            sttHasSaved = false;
        }

        Boolean langHasSaved;
        if (workingWith == SaveSection.LANGUAGE || workingWith == SaveSection.ALL) {
            langHasSaved = true;
        }
        else {
            langHasSaved = false;
        }

        EventSettingsSaved eventSaved = new EventSettingsSaved(sttHasSaved, langHasSaved);
        primaryStage.fireEvent(eventSaved);

    }


    // Log
    private void log(String msg, int level) {
        EventWriteLog logEvent = new EventWriteLog(msg, level);
        Platform.runLater(() -> {
            primaryStage.fireEvent(logEvent);
        });
    }


}
