package com.dsoftn.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.events.EventEditLanguageAdded;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.utils.UTranslate.LanguagesEnum;
import com.dsoftn.Settings.Settings;


public class ScrollPanelSectionAdd extends VBox{
    // Constants
    private static final int MAX_RECOMMENDED_LANGUAGES = 2;

    // widgets
    @FXML
    private HBox hbxAdd; // Add new language section
    @FXML
    private HBox hbxMore; // Header line with Add New and Recommended languages
    @FXML
    private Button btnMore; // More languages
    @FXML
    private Label lblDots; // If there are more than MAX recommended languages this label is visible
    @FXML
    private TextField txtFilter; // Filter languages
    @FXML
    private ListView<String> lstLanguages; // List of all languages
    @FXML
    private Label lblLangCode; // Language code
    @FXML
    private Label lblLangName; // Language name
    @FXML
    private Label lblLangNativeName; // Language native name
    @FXML
    private Button btnAdd; // Add language
    @FXML
    private Label lblExists; // Language already exists label than is shown instead of add button

    // Variables
    private List<String> fileAffected = new ArrayList<>();
    private List<String> recommendedLanguages = new ArrayList<>();
    private List<String> alreadyAddedLanguages = new ArrayList<>();

    // Constructors

    public ScrollPanelSectionAdd(List<String> fileAffected, List<String> alreadyAddedLanguages) {
        createWidgets();
        setAffectedFiles(fileAffected);
        setAlreadyAddedLanguages(alreadyAddedLanguages);
    }

    public ScrollPanelSectionAdd() {
        createWidgets();
    }

    // Serialization and deserialization

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();

        // File Affected
        result.put("fileAffected", fileAffected);

        // Recommended Languages
        result.put("recommendedLanguages", recommendedLanguages);

        // Already Added Languages
        result.put("alreadyAddedLanguages", alreadyAddedLanguages);

        // Filter
        result.put("filter", txtFilter.getText());

        // Current selection
        result.put("currentSelection", lstLanguages.getSelectionModel().getSelectedItem());


        return result;
    }

    public void fromMap(Map<String, Object> map) {
        // File Affected
        @SuppressWarnings("unchecked")
        List<String> fileAff = (List<String>) map.get("fileAffected");
        setAffectedFiles(fileAff);

        // Recommended Languages
        // Updated by setAffectedFiles method

        // Already Added Languages
        @SuppressWarnings("unchecked")
        List<String> alreadyAddedLang = (List<String>) map.get("alreadyAddedLanguages");
        setAlreadyAddedLanguages(alreadyAddedLang);

        // Filter
        String filter = (String) map.get("filter");
        txtFilter.setText(filter);

        // Current selection
        String currentSelection = (String) map.get("currentSelection");
        if (currentSelection != null) {
            lstLanguages.getSelectionModel().select(currentSelection);
        }
    }

    // Public methods

    public void setAlreadyAddedLanguages(List<String> alreadyAddedLanguages) {
        if (alreadyAddedLanguages == null) {
            alreadyAddedLanguages = new ArrayList<>();
        }

        this.alreadyAddedLanguages = alreadyAddedLanguages;
    }

    public void setAffectedFiles(List<String> fileAffected) {
        if (fileAffected == null) {
            fileAffected = new ArrayList<>();
        }

        this.fileAffected = fileAffected;
        recommendedLanguages = getListOfRequiredLanguageNames(fileAffected);
        updateRecommendedLanguages();
    }

    public List<String> getListOfRequiredLanguageNames(List<String> fileAffected) {
        // Required language names
        List<String> recLang = new ArrayList<>();

        Settings setting = new Settings();

        for (String file : fileAffected) {
            setting.languagesFilePath = file;
            setting.load(false, true, false);
            if (setting.getLastErrorString().isEmpty()) {
                List<String> langs = setting.getAvailableLanguageCodes();
                for (String lang : langs) {
                    String langItem = LanguagesEnum.fromLangCode(lang).getName();
                    if (!recLang.contains(langItem)) {
                        recLang.add(langItem);
                    }
                }
            }
        }

        return recLang;
    }

    // Private methods

    private void createWidgets() {
        URL fxmlLocation = getClass().getResource("/fxml/LanguageScrollPaneSectionAdd.fxml");
        if (fxmlLocation == null) {
            System.out.println("FXML file not found!");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LanguageScrollPaneSectionAdd.fxml"));
        // FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/test.fxml"));
        fxmlLoader.setController(this);
        try {
            VBox content = fxmlLoader.load();
            this.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        defineWidgets();
    }

    private void defineWidgets() {
        btnMore.setId("Protected");
        hbxAdd.setVisible(false);
        hbxAdd.setManaged(false);

        lblDots.setVisible(false);
        lblDots.setManaged(false);

        // Populate list of languages
        populateListOfLanguages(null);

        // Set listener for lstLanguages change current item
        lstLanguages.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                LanguagesEnum lang = LanguagesEnum.fromName(newSelection);
                lblLangCode.setText(lang.getLangCode());
                lblLangName.setText(lang.getName());
                lblLangNativeName.setText(lang.getNativeName());
                if (alreadyAddedLanguages.contains(lang.getName())) {
                    btnAdd.setVisible(false);
                    btnAdd.setManaged(false);
                    lblExists.setVisible(true);
                    lblExists.setManaged(true);
                }
                else {
                    btnAdd.setVisible(true);
                    btnAdd.setManaged(true);
                    lblExists.setVisible(false);
                    lblExists.setManaged(false);
                }
            }
            else {
                btnAdd.setVisible(false);
                btnAdd.setManaged(false);
                lblExists.setVisible(false);
                lblExists.setManaged(false);
            }
        });

        // Set listener for txtFilter change
        txtFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            populateListOfLanguages(newValue);
        });
        
    }

    private void populateListOfLanguages(String filter) {
        lstLanguages.getItems().clear();
        for (String lang : LanguagesEnum.getLanguageNames()) {
            if (filter != null && !filter.isEmpty()) {
                if (lang.toLowerCase().contains(filter.toLowerCase())) {
                    lstLanguages.getItems().add(lang);
                }
            }
            else {
                lstLanguages.getItems().add(lang);
            }
        }

        btnAdd.setVisible(false);
        btnAdd.setManaged(false);
        lblExists.setVisible(false);
        lblExists.setManaged(false);

    }

    private void updateRecommendedLanguages() {
        // Remove custom buttons
        for (int i = hbxMore.getChildren().size() - 1; i >= 0 ; i--) {
            if (hbxMore.getChildren().get(i) instanceof Button && hbxMore.getChildren().get(i).getId() != null && !hbxMore.getChildren().get(i).getId().equals("Protected")) {
                hbxMore.getChildren().remove(i);
            }
        }

        lblDots.setVisible(false);
        lblDots.setManaged(false);

        int count = 0;
        for (String lang : recommendedLanguages) {
            if (count == MAX_RECOMMENDED_LANGUAGES) {
                lblDots.setVisible(true);
                lblDots.setManaged(true);
                break;
            }
            else {
                Button btn = createButton(lang);
                btn.setText(lang);
                btn.setOnAction(event -> {
                    addLanguage(lang);
                });
                hbxMore.getChildren().add(hbxMore.getChildren().size() - 3, btn);
            }
            count++;
        }
    }

    private Button createButton(String btnID) {
        Button btn = new Button();
        btn.setId(btnID);
        
        return btn;
    }

    private void addLanguage(String lang) {
        Stage primaryStage = (Stage) hbxMore.getScene().getWindow();
        EventEditLanguageAdded event = new EventEditLanguageAdded(LanguagesEnum.fromName(lang));
        primaryStage.fireEvent(event);
    }


    // FXML events

    @FXML
    public void onBtnMoreClick() {
        if (hbxAdd.isVisible()) {
            hbxAdd.setVisible(false);
            hbxAdd.setManaged(false);
            btnMore.setText("More languages");
        }
        else {
            hbxAdd.setVisible(true);
            hbxAdd.setManaged(true);
            btnMore.setText("Hide languages");
        }
    }

    @FXML
    public void onBtnAddClick() {
        addLanguage(lblLangName.getText());
        btnAdd.setVisible(false);
        btnAdd.setManaged(false);
        lblExists.setVisible(true);
        lblExists.setManaged(true);
    }



}
