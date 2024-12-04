package com.dsoftn.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import javafx.concurrent.Task;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tab;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.dsoftn.utils.PyDict;
import com.dsoftn.utils.UString;
import com.dsoftn.Settings.DataType;
import com.dsoftn.Settings.Settings;
import com.dsoftn.Settings.SettingsItem;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.Settings.LanguageItemGroup;
import com.dsoftn.Settings.SettingType;
import com.dsoftn.controllers.SaveDialogController.SaveSection;
import com.dsoftn.events.EventWriteLog;
import com.dsoftn.events.EventSettingsSaved;
import com.dsoftn.events.EventEditLanguageContentChanged;
import com.dsoftn.utils.LanguagesEnum;


public class MainWinController {
    private record MsgInfo(String msgID, String msgText, String msgDescription, MsgStyle errorStyle, ErrorCode errorCode, double msgShowDurationSec, double msgLiveDurationSec, boolean deleteAfterShowing, LocalDateTime dateTime) {
        private enum MsgStyle {
            NORMAL,
            INFORMATION,
            WARNING,
            ERROR
        }

        private enum ErrorCode {
            NONE,
            FILE_NOT_FOUND
        }

        // Add constructors
        public MsgInfo(String msgID, String msgText, String msgDescription, MsgStyle errorStyle, ErrorCode errorCode, double msgShowDurationSec, double msgLiveDurationSec, boolean deleteAfterShowing) {
            this(msgID, msgText, msgDescription, errorStyle, errorCode, msgShowDurationSec, msgLiveDurationSec <= 0 ? msgShowDurationSec * 10 : msgLiveDurationSec, deleteAfterShowing, LocalDateTime.now());
        }

        public MsgInfo(String msgID, String msgText, String msgDescription, MsgStyle errorStyle) {
            this(msgID, msgText, msgDescription, errorStyle, ErrorCode.NONE, MSG_DURATION, MSG_DURATION * 10, false, LocalDateTime.now());
        }

        public String getStyleClass (MsgStyle msgStyle) {
            switch (msgStyle) {
                case INFORMATION:
                    return "info-label";
                case WARNING:
                    return "warning-label";
                case ERROR:
                    return "error-label";
                default:
                    return "title-label";
            }
        }

        public String getStyleClass () {
            return getStyleClass(errorStyle);
        }

        public String getStyleMsgID() {
            return "-fx-font-size: 20; -fx-fill: #ffffff;";
        }

        public String getStyleMsgText() {
            return "-fx-font-size: 18; -fx-fill: #ffff00;";
        }

        public String getStyleMsgDescription() {
            return "-fx-font-size: 14; -fx-fill: rgb(2, 235, 223);";
        }

        public String getPictureFilePath () {
            switch (errorStyle) {
                case INFORMATION:
                    return "/images/info.png";
                case WARNING:
                    return "/images/warning.png";
                case ERROR:
                    return "/images/error.png";
                default:
                    return "";
            }
        }

        public String getDateTimeString () {
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss"));
        }

        public boolean isActive() {
            LocalDateTime now = LocalDateTime.now();
            java.time.Duration duration = java.time.Duration.between(dateTime, now);
            return duration.getSeconds() < msgShowDurationSec;
        }

        public boolean isExpired() {
            if (!isActive() && deleteAfterShowing) {
                return true;
            }

            LocalDateTime now = LocalDateTime.now();
            java.time.Duration duration = java.time.Duration.between(dateTime, now);
            return duration.getSeconds() >= msgLiveDurationSec;
        }
    }

    public enum Section {
        SETTINGS,
        LANGUAGE,
        LANGUAGE_MANAGE,
        ALL;
    }

    // -------------------------- APPLICATION VARIABLES
    // For each enum Section dictionary messagesDict has corresponding key
    // key contains list of MsgInfo objects for that section
    // messagesDict[Section.SETTINGS.toString()] contains list of MsgInfo objects for Section.SETTINGS
    private Stage primaryStage;

    private PyDict messagesDict = new PyDict();
    private boolean messageFadeOutRunning = false;
    private Section activeSection = Section.SETTINGS; // Current section
    private boolean isMessageDetailsShown = false; // If true, message list is currently shown
    public String logGlobalIndent = ""; // This will be used to indent each log message

    private String loadSttFromPath = ""; // Path to load Settings from
    private List<String> updateSttFilesPaths = new ArrayList<>(); // List of paths to update Settings files

    private String loadLangFromPath = ""; // Path to load Languages from
    private List<String> updateLangFilesPaths = new ArrayList<>(); // List of paths to update Languages files

    private PyDict appState = getAppStateEmptyDict(); // App setting, it will be loaded at start and saved at exit

    private double fontSizeLstFiles = 14; // Font size for ListView lstFiles

    Image listItemChanged = new Image("/images/list_item_changed.png");
    private ImageView imgListItemChanged = new ImageView(listItemChanged); // To mark list item as changed
    Image listItemDeleted = new Image("/images/list_item_deleted.png");
    private ImageView imgListItemDeleted = new ImageView(listItemDeleted); // To mark list item as deleted

    // -------------------------- APPLICATION VARIABLES - SETTINGS
    private PyDict sttLoadedMap = new PyDict(); // Map of settings that are loaded from file
    private String sttLoadedCurrentItem = null; // Current item in Loaded Items on lstStt
    private PyDict sttChangedMap = new PyDict(); // Map of settings that are changed or deleted
    private String sttChangedCurrentItem = null; // Current item in Changed Items on lstStt
    private List<String> sttChangedList = new ArrayList<>(); // List of settings keys that are changed or deleted
    private String sttVisibleList = ""; // List of settings keys that are visible ("Loaded" or "Changed")
    private double fontSizeLstStt = 14; // Font size for ListView lstStt
    private String lstSttSortKey = "Created"; // Sort key for lstStt ("Key", "Created")
    private boolean sttListReloadedItem = false; // Signal to lstStt.onMouseClicked that item should be reloaded

    // -------------------------- APPLICATION VARIABLES - LANGUAGE
    private PyDict langLoadedMap = new PyDict(); // Map of languages that are loaded from file
    private String langLoadedCurrentItem = null; // Current item in Loaded Items on lstLang
    private PyDict langChangedMap = new PyDict(); // Map of languages that are changed or deleted
    private String langChangedCurrentItem = null; // Current item in Changed Items on lstLang
    private List<String> langChangedList = new ArrayList<>(); // List of languages keys that are changed or deleted
    private String langVisibleList = ""; // List of languages keys that are visible ("Loaded" or "Changed")
    private double fontSizeLstLang = 14; // Font size for ListView lstLang
    private String lstLangSortKey = "Created"; // Sort key for lstLang ("Key", "Created")
    private ScrollPaneContent scrollPaneContent = null; // ScrollPaneContent object
    private boolean langListReloadedItem = false; // Signal to lstLang.onMouseClicked that item should be reloaded

    // -------------------------- APPLICATION VARIABLES - LANGUAGE MANAGE
    private ScrollPaneManageContent scrollPaneManageContent = null; // ScrollPaneManageContent object

    // -------------------------- CONSTANTS
    // Icon size for buttons, labels...
    private final String APP_TITLE = "Settings and Language Editor";
    private final boolean PRINT_LOG_TO_CONSOLE = false;
    private final double WIDGET_GRAPHIC_SIZE = 20;
    // Messages
    private final double MSG_LABEL_FADE_IN_DURATION = 0.2;
    private final double MSG_LABEL_FADE_OUT_DURATION = 0.3;
    static double MSG_DURATION = 5.0;
    // Pane with file list to update (maximize, minimize)
    private final double FILE_LIST_PANE_HEIGHT = 100.0;
    private final double FILE_LIST_PANE_ANIMATION_DURATION_MS = 300.0;
    // Max / Min font size for ListView
    private final double FONT_SIZE_MAX_LIST = 40.0;
    private final double FONT_SIZE_MIN_LIST = 10.0;

    private final String APP_STATE_FILE_NAME = "config.json";
    private final String APP_LOG_FILE_NAME = "app.log";
    private final String LOG_INDENT = ".   -> ";
    private final int LOG_MAX_PREVIOUS_LOGS_TO_KEEP = 2;
    private final String LOG_HEADER = "*****     NEW LOG STARTED     *****";

    // -------------------------- WIDGETS NOT IN FXML
    
    // Context Menus
    private ContextMenu contextMenuLblSource = new ContextMenu();
    private ContextMenu contextMenuLstStt = new ContextMenu();
    private ContextMenu contextMenuLstLang = new ContextMenu();
   
    // WIDGETS IN FXML

    // Labels
    @FXML
    private Label lblInfo; // Title label, contains messages if any
    @FXML
    private Label lblSource; // Source file to populate Settings and Language lists
    @FXML
    private Label lblToolTip; // Tooltip label for various info
    @FXML
    private Label lblSttInfo; // Info label for DataType in Settings
    @FXML
    private Label lblSttRecords; // Counter of Settings records in lstStt
    @FXML
    private Label lblSttKeyImage; // Image on left side of Settings KEY field
    @FXML
    private Label lblSttRec; // Image on left side of Settings KEY field
    @FXML
    private Label lblLangRec; // Image on left side of Language KEY field
    @FXML
    private Label lblLangKeyImage; // Image on left side of Language KEY field
    @FXML
    private Label lblLangRecords; // Counter of Language records in lstLang
    //            Field Labels
    @FXML
    private Label fieldLabeSttFlag;
    @FXML
    private Label fieldLabeSttDataType;
    @FXML
    private Label fieldLabeSttValue;
    @FXML
    private Label fieldLabeSttDefValue;
    @FXML
    private Label fieldLabeSttMin;
    @FXML
    private Label fieldLabeSttMax;
    @FXML
    private Label fieldLabeSttDesc;
    @FXML
    private Label lblMngFileShort; // Language Manage: Short name of file
    @FXML
    private Label lblMngFileLong; // Language Manage: Long name of file
    @FXML
    private Label lblMngInfo; // Language Manage: Info label

    // Buttons
    @FXML
    private Button btnMaximize; // Shows file list pane (layoutHBoxFiles)
    @FXML
    private Button btnMinimize; // Hides file list pane (layoutHBoxFiles)
    @FXML
    private Button btnLoadFrom; // Load list of Settings/Languages to populate lstStt / lstLang
    @FXML
    private Button btnAddFile; // Add file to list of files that need to be updated (lstFiles)
    @FXML
    private Button btnRemoveFile; // Remove file from list of files that need to be updated (lstFiles)
    @FXML
    private Button btnCreateNew; // Create new Settings file
    @FXML
    private Button btnSttShowLoaded; // Show loaded Settings List
    @FXML
    private Button btnSttShowChanged; // Show changed Settings List
    @FXML
    private Button btnSttAdd; // Add new item to lstStt
    @FXML
    private Button btnSttUpdate; // Update existing item in lstStt
    @FXML
    private Button btnSttDelete; // Delete item in lstStt
    @FXML
    private Button btnSttDiscard; // If item is deleted it will be set to Changed, if item is changed it will be set to unmodified
    @FXML
    private Button btnSttFilterClear; // Clear filter in lstStt
    @FXML
    private Button btnSttSaveStt; // Save changed Settings
    @FXML
    private Button btnLangFilterClear; // Clear filter in lstLang
    @FXML
    private Button btnLangShowLoaded; // Show loaded Language List
    @FXML
    private Button btnLangShowChanged; // Show changed Language List
    @FXML
    private Button btnLangAdd; // Add new item to lstLang
    @FXML
    private Button btnLangUpdate; // Update existing item in lstLang
    @FXML
    private Button btnLangDelete; // Delete item in lstLang
    @FXML
    private Button btnLangDiscard; // If item is deleted it will be set to Changed, if item is changed it will be set to unmodified
    @FXML
    private Button btnSaveLang; // Save changed Language
    @FXML
    private Button btnSaveAll; // Save changed Settings and Language
    @FXML
    private Button btnMngNew; // Language Manage: Create new Language file
    @FXML
    private Button btnMngLoad; // Language Manage: Load Language file
    @FXML
    private Button btnMngClear; // Language Manage: Clear (Unload) Language file
    @FXML
    private Button btnMngCommit; // Language Manage: Commit changes to Language file

    // TextBoxes
    @FXML
    private TextField txtSttSearch; // Search in lstStt
    @FXML
    private TextField txtSttKey; // Key in lstStt
    @FXML
    private TextArea txtSttValue; // Value in lstStt
    @FXML
    private TextArea txtSttDefValue; // Default Value in lstStt
    @FXML
    private TextField txtSttMin; // Min Value in lstStt
    @FXML
    private TextField txtSttMax; // Max Value in lstStt
    @FXML
    private TextArea txtSttDesc; // Description in lstStt
    @FXML
    private TextField txtLangSearch; // Search in lstLang
    @FXML
    private TextField txtLangKey; // Key in lstLang
    // Checkboxes
    @FXML
    private CheckBox chkAutoUpdateFiles; // If checked, user will not be prompted for each file in lstFiles when saving changes
    @FXML
    private CheckBox chkSaveState; // If checked, app state can be saved
    @FXML
    private CheckBox chkAutoDataType; // If checked, DataType will be automatically detected
    @FXML
    private CheckBox chkSttAutoStrip; // If checked, value will be automatically stripped from white spaces before saving
    @FXML
    private CheckBox chkSttAutoDefault; // If checked, value will be automatically set to default

    // Layouts
    @FXML
    private HBox hboxLoadFile; // Contains btnLoadFrom
    @FXML
    private HBox layoutHBoxFiles; // Contains lstFiles, chkAutoUpdateFiles, btnAddFile, btnRemoveFile
    @FXML
    private VBox layoutVBoxMain; // Layout with all elements
    @FXML
    private AnchorPane layoutAnchorPaneMain; // Root layout
    @FXML
    private SplitPane sttSttSplitPane; // Split Pane in Settings tab
    @FXML
    private SplitPane sttLangSplitPane; // Split Pane in Language tab
    @FXML
    private HBox hboxSaveButtons; // Contains save buttons

    // ListBoxes
    @FXML
    private ListView<String> lstFiles; // List of files that need to be updated
    @FXML
    private ListView<String> lstStt; // List of Settings
    @FXML
    private ListView<String> lstLang; // List of Languages

    // ComboBoxes
    @FXML
    private ComboBox<String> cmbSttFlag; // List of Settings Flags
    @FXML
    private ComboBox<String> cmbSttDataType; // List of DataTypes

    // TAB
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabStt;
    @FXML
    private Tab tabLang;
    @FXML
    private Tab tabManage;

    // Scroll Panes
    @FXML
    private ScrollPane scrPaneLang; // Area for editing all languages for current key
    @FXML
    private ScrollPane scrPaneManage; // Area for editing language file content


    public void initialize() {
        fixLogSize();
        log(LOG_HEADER);
        log("Starting application...");
        logIndentSet(1);

        // Initialize messages dict
        messagesDict.setPyDictValue(String.format("[%s]",Section.SETTINGS.toString()), new ArrayList<>());
        messagesDict.setPyDictValue(String.format("[%s]",Section.LANGUAGE.toString()), new ArrayList<>());
        messagesDict.setPyDictValue(String.format("[%s]",Section.LANGUAGE_MANAGE.toString()), new ArrayList<>());
        // Add normal message to each section
        activeSection = Section.LANGUAGE_MANAGE;
        addMessage(new MsgInfo("1", APP_TITLE, "Language Manager 2.0\ndsoftn@gmail.com", MsgInfo.MsgStyle.NORMAL, MsgInfo.ErrorCode.NONE, 0, -1, false));
        activeSection = Section.LANGUAGE;
        addMessage(new MsgInfo("2", APP_TITLE, "Language Editor 2.0\ndsoftn@gmail.com", MsgInfo.MsgStyle.NORMAL, MsgInfo.ErrorCode.NONE, 0, -1, false));
        activeSection = Section.SETTINGS;
        addMessage(new MsgInfo("1", APP_TITLE, "Settings Editor 2.0\ndsoftn@gmail.com", MsgInfo.MsgStyle.NORMAL, MsgInfo.ErrorCode.NONE, 0, -1, false));

        setupWidgets();

        loadAppState(APP_STATE_FILE_NAME);

        // Populate cmbSttFlag ComboBox with enum SettingType names
        for (SettingType settingType : SettingType.values()) {
            cmbSttFlag.getItems().add(settingType.toString());
        }
        // Populate cmbSttDataType ComboBox with enum DataType names
        for (DataType dataType : DataType.values()) {
            cmbSttDataType.getItems().add(dataType.toString());
        }

        Platform.runLater(() -> {
            // Set primaryStage
            this.primaryStage = (Stage) layoutAnchorPaneMain.getScene().getWindow();

            // Connect custom events
            primaryStage.addEventHandler(EventWriteLog.EVENT_WRITE_LOG_TYPE, event -> {
                onEventWriteLog(event);
            });
            primaryStage.addEventHandler(EventSettingsSaved.EVENT_SETTINGS_SAVED_TYPE, event -> {
                onEventSettingsSaved(event);
            });
            primaryStage.addEventHandler(EventEditLanguageContentChanged.EVENT_EDIT_LANGUAGE_CONTENT_CHANGED_TYPE, event -> {
                onEventEditLanguageContentChanged(event);
            });

            // Set Button icons
            setWidgetIcon("/images/maximize.png", btnMaximize);
            setWidgetIcon("/images/minimize.png", btnMinimize);
            setWidgetIcon("/images/load_file.png", btnLoadFrom);
            setWidgetIcon("/images/add_file.png", btnAddFile);
            setWidgetIcon("/images/remove_file.png", btnRemoveFile);

            restoreAppState();

            logIndentClear();
            log("Application started");
        });
    }

    // Custom Events

    public void onEventWriteLog(EventWriteLog event) {
        for (int i = 0; i < event.getIndentLevel(); i++) {
            logIndentPlus();
        }
        log(event.getMessage());
        for (int i = 0; i < event.getIndentLevel(); i++) {
            logIndentMinus();
        }
    }

    public void onEventSettingsSaved(EventSettingsSaved event) {
        if (event.isSettingsSaved()) {
            removeSavedSettingsItems(event);
        }
        
        if (event.isLanguageSaved()) {
            removeSavedLanguageItems(event);
        }
    }

    public void removeSavedLanguageItems(EventSettingsSaved event) {
        log("Languages has been saved, updating list of changed items...");
        logIndentPlus();

        List<String> langToBeRemoved = new ArrayList<>(); // List of changed items to be removed

        for (String langKey : langChangedList) {
            langToBeRemoved.add(langKey);
        }

        // Remove changed items from Changed List and Changed Map
        for (String langKey : langToBeRemoved) {
            langChangedList.remove(langKey);
            langChangedMap.remove(langKey);
        }

        // Refresh Loaded Map
        PyDict loadedData = getLanguageFileContent(loadLangFromPath);

        if (loadedData != null) {
            changeLangVisibleList("Loaded");
            int index = lstLang.getSelectionModel().getSelectedIndex();
            String name = lstLang.getItems().get(index).toString();

            List<LanguageItemGroup> listOfItems = LanguageItemGroup.getListOfGroupLanguageObjectsFromLanguageMapObject(loadedData);
            langLoadedMap.clear();
            for (LanguageItemGroup itemGroup : listOfItems) {
                langLoadedMap.put(itemGroup.getGroupKey(), itemGroup);
            }

            populateLangList(langLoadedMap, langChangedMap,txtLangSearch.getText());
            setCurrentItemInLoadedListLang(name, index);
            showToolTipLabel("List Refreshed", lstLang.localToScene(0, 0).getX(), lstLang.localToScene(0, 0).getY(), 1);
            
            log("List of loaded languages item is refreshed");
        }

        // Refreshing display
        changeLangVisibleList(langVisibleList);
        txtLangKey.setText(txtLangKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        logIndentMinus();
        log("List of changed items has been updated.");
        MsgInfo msgInfo = new MsgInfo("DataSaved", "Languages saved", "Languages have been saved", MsgInfo.MsgStyle.INFORMATION);
        showMessage(msgInfo);
    }
    
    public void removeSavedSettingsItems(EventSettingsSaved event) {        
        log("Settings has been saved, updating list of changed items...");
        logIndentPlus();

        List<String> sttToBeRemoved = new ArrayList<>(); // List of changed items to be removed

        // If list of items to be removed is provided, remove only these items
        if (event.getSettingsList() != null && event.getSettingsList().size() > 0) {
            for (String sttKey : event.getSettingsList()) {
                if (sttChangedList.contains(sttKey)) {
                    sttToBeRemoved.add(sttKey);
                }
            }
        }
        // If flag 'isAllSettingsSaved' is true, remove all changed items
        if (event.isAllSettingsSaved()) {
            for (String sttKey : sttChangedList) {
                sttToBeRemoved.add(sttKey);
            }
        }

        // Remove changed items from Changed List and Changed Map
        for (String sttKey : sttToBeRemoved) {
            sttChangedList.remove(sttKey);
            sttChangedMap.remove(sttKey);
        }

        // Refresh Loaded Map
        PyDict loadedData = getSettingsFileContent(loadSttFromPath);

        if (loadedData != null) {
            changeSttVisibleList("Loaded");
            int index = lstStt.getSelectionModel().getSelectedIndex();
            String name = lstStt.getItems().get(index).toString();
            sttLoadedMap = loadedData;
            populateSttList(sttLoadedMap, sttChangedMap,txtSttSearch.getText());
            setCurrentItemInLoadedList(name, index);
            showToolTipLabel("List Refreshed", lstStt.localToScene(0, 0).getX(), lstStt.localToScene(0, 0).getY(), 1);
            
            log("List of loaded settings item is refreshed");
        }

        // Refreshing display
        changeSttVisibleList(sttVisibleList);
        txtSttKey.setText(txtSttKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        logIndentMinus();
        log("List of changed items has been updated.");
        MsgInfo msgInfo = new MsgInfo("DataSaved", "Settings saved", "Settings have been saved", MsgInfo.MsgStyle.INFORMATION);
        showMessage(msgInfo);
    }
    
    // Message handling

    private void addMessage(MsgInfo message) {
        List<MsgInfo> msgList = getMsgList();

        if (msgList.contains(message)) {
            return;
        }
        msgList.add(message);
        updateInfoLabelToolTip();
        if (isMessageDetailsShown) {
            showMessageDetails();
        }
    }

    private void insertMessage(MsgInfo message, int index) {
        List<MsgInfo> msgList = getMsgList();

        if (msgList.contains(message)) {
            return;
        }
        msgList.add(index, message);
        updateInfoLabelToolTip();
        if (isMessageDetailsShown) {
            showMessageDetails();
        }
    }

    private void removeMessage(String messageID) {
        int messageIdx;
        while (true) {
            messageIdx = findMessage(messageID);
            if (messageIdx >= 0) {
                removeMessage(getMsgList().get(messageIdx));
            }
            else {
                break;
            }
        }
    }
    
    private void removeMessage(MsgInfo message) {
        List<MsgInfo> msgList = getMsgList();

        int idx = findMessage(message);
        if (idx >= 0) {
            msgList.remove(idx);
        }
        updateInfoLabelToolTip();
        if (isMessageDetailsShown) {
            showMessageDetails();
        }
    }

    private int findMessage(String msgID) {
        List<MsgInfo> msgList = getMsgList();

        for (int idx = 0; idx < msgList.size(); idx++) {
            MsgInfo e = msgList.get(idx);
            if (e.msgID.equals(msgID)) {
                return idx;
            }
        }
        return -1;
    }

    private int findMessage(MsgInfo.MsgStyle msgStyle) {
        List<MsgInfo> msgList = getMsgList();

        for (int idx = 0; idx < msgList.size(); idx++) {
            MsgInfo e = msgList.get(idx);
            if (e.errorStyle == msgStyle) {
                return idx;
            }
        }
        return -1;
    }

    private int findMessage(MsgInfo message) {
        return findMessage(message.msgID);
    }

    private void removeInactiveAndExpiredMessages() {
        boolean hasChanges = true;
        boolean somethingRemoved = false;

        while (hasChanges) {
            hasChanges = false;
            List<MsgInfo> msgList = getMsgList();
            for (int idx = 0; idx < msgList.size(); idx++) {
                MsgInfo msg = msgList.get(idx);
                if (msg.errorStyle == MsgInfo.MsgStyle.NORMAL) {
                    continue;
                }
                if (msg.isExpired()) {
                    log("Message expired: (ID): " + msg.msgID + " (Text): " + msg.msgText);
                    removeMessage(msg);
                    hasChanges = true;
                    somethingRemoved = true;
                    break;
                }
            }

            if (hasChanges) {
                continue;
            }

            boolean canContinue = false;
            for (int idx = 0; idx < msgList.size(); idx++) {
                MsgInfo msg = msgList.get(idx);
                if (msg.errorStyle == MsgInfo.MsgStyle.NORMAL) {
                    canContinue = true;
                    continue;
                }
                if (!canContinue) {
                    continue;
                }

                if (!msg.isActive()) {
                    removeMessage(msg);
                    int index = findMessage(MsgInfo.MsgStyle.NORMAL);
                    if (index >= 0) {
                        insertMessage(msg, index);
                        log("Message inactive: (ID): " + msg.msgID + " (Text): " + msg.msgText);
                    }
                    hasChanges = true;
                    somethingRemoved = true;
                    break;
                }
            }
        }

        if (somethingRemoved) {
            updateMessageLabel();
        }
    }

    private void showMessage(MsgInfo message) {
        addMessage(message);
        log("Showing message: (ID): " + message.msgID + ", (Text): " + message.msgText + ", (Description): " + message.msgDescription);

        removeInactiveAndExpiredMessages();
        updateMessageLabel();
    }

    private void showMessageDetails() {
        hideMessageDetails();
        isMessageDetailsShown = true;

        List<MsgInfo> msgList = getMsgList();

        boolean hasMessages = false;

        for (int idx = 0; idx < msgList.size(); idx++) {
            MsgInfo e = msgList.get(idx);
            if (e.errorStyle != MsgInfo.MsgStyle.NORMAL) {
                hasMessages = true;
                break;
            }
        }
        
        HBox hBox = new HBox();

        if (hasMessages == false) {
            MsgInfo item = new MsgInfo(
                "Author",
                "Danijel Nišević\ndsoftn@gmail.com\n+38163593728",
                "",
                MsgInfo.MsgStyle.NORMAL,
                MsgInfo.ErrorCode.NONE,
                0,
                0,
                true
                );
            
            hBox = createMessageDetailsPane(item, "I," + item.msgID);
            hBox.setStyle("-fx-background-color: rgb(17, 9, 87);");
            layoutVBoxMain.getChildren().add(1, hBox);
            log("Showing message details:");
            log(LOG_INDENT + "(ID)" + item.msgID + ", (Text): " + item.msgText + ", (Description): " + item.msgDescription);
        }
        else {
            log("Showing message details:");
            for (int idx = 0; idx < msgList.size(); idx++) {
                MsgInfo e = msgList.get(idx);
                if (e.errorStyle != MsgInfo.MsgStyle.NORMAL) {
                    layoutVBoxMain.getChildren().add(1, createMessageDetailsPane(e, "I," + e.msgID));
                    log(LOG_INDENT + "(ID)" + e.msgID + ", (Text): " + e.msgText + ", (Description): " + e.msgDescription);
                }
            }
    
        }
    }

    private HBox createMessageDetailsPane(MsgInfo message, String buttons) {
        double buttonIgnoreSize = 15;

        HBox hBox = new HBox();

        hBox.setId("MsgDetails");

        // Define Text
        Text msgID = new Text();
        Text msgText = new Text();
        Text msgDescription = new Text();
        
        msgID.setText(message.msgID);
        msgID.setStyle(message.getStyleMsgID());
        msgText.setText(": " + message.msgText);
        msgText.setStyle(message.getStyleMsgText());
        msgDescription.setText("\n[" + message.getDateTimeString() + "] " + message.msgDescription);
        msgDescription.setStyle(message.getStyleMsgDescription());

        // Define TextFlow
        TextFlow textFlow = new TextFlow();
        textFlow.setTranslateX(10);
        
        if (!message.getPictureFilePath().isBlank()) {
            // Define image
            Image image = new Image(getClass().getResourceAsStream(message.getPictureFilePath()));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(18);
            textFlow.getChildren().add(imageView);
        }

        textFlow.getChildren().add(msgID);
        textFlow.getChildren().add(msgText);
        if (!msgDescription.getText().isBlank()) {
            textFlow.getChildren().add(msgDescription);
        }

        // Define Buttons
        if (!buttons.isBlank()) {
            List<String> buttonList = List.of(buttons.split(Pattern.quote(String.valueOf(","))));

            for (String buttonStr: buttonList) {
                if (buttonStr.isBlank()) {
                    continue;
                }
                
                if (buttonStr.toLowerCase().equals("i")) {
                    Button btnIgnore = new Button();
                    btnIgnore.setMaxWidth(buttonIgnoreSize);
                    btnIgnore.setMaxHeight(buttonIgnoreSize);
                    btnIgnore.setPrefWidth(buttonIgnoreSize);
                    btnIgnore.setPrefHeight(buttonIgnoreSize);
                    btnIgnore.getStyleClass().add("button");
                    btnIgnore.getStyleClass().add("button-mini");
                    setWidgetIcon("/images/cancel.png", btnIgnore, buttonIgnoreSize);

                    if (message.errorStyle == MsgInfo.MsgStyle.NORMAL) {
                        btnIgnore.setOnAction(event -> {
                            hideMessageDetails();
                        });
                    }
                    else {
                        btnIgnore.setOnAction(event -> {
                            removeMessage(message);
                            updateMessageLabel();
                        });
                    }


                    hBox.getChildren().add(btnIgnore);
                    continue;
                }
            }
            
        }


        // Add TextFlow to HBox
        hBox.getChildren().add(textFlow);
        // hBox.getStyleClass().add("label");
        // hBox.getStyleClass().add(message.getStyleClass());
        hBox.getStyleClass().add("hbox-message-details");

        return hBox;
    }

    private void hideMessageDetails() {
        isMessageDetailsShown = false;

        List<HBox> hBoxList = new ArrayList<>();

        for (Node node : layoutVBoxMain.getChildren()) {
            if (node instanceof HBox) {
                if (node.getId() != null && node.getId().equals("MsgDetails")) {
                    hBoxList.add((HBox) node);
                }
            }
        }
        
        if (hBoxList.size() > 0) {
            layoutVBoxMain.getChildren().removeAll(hBoxList);
        }
        log("Hiding message details.");
    }

    private List<MsgInfo> getMsgList() {
        @SuppressWarnings("unchecked")
        List<MsgInfo> msgList = (List<MsgInfo>) messagesDict.getPyDictValue(String.format("[%s]", activeSection.toString()));
        return msgList;
    }

    private void updateInfoLabelToolTip() {
        List<MsgInfo> msgList = getMsgList();
        String toolTip = "";

        for (int idx = 0; idx < msgList.size(); idx++) {
            MsgInfo e = msgList.get(idx);
            if (e.errorStyle == MsgInfo.MsgStyle.NORMAL) {
                continue;
            }
            toolTip += String.format("%s: %s\n", e.msgID, e.msgText);
        }

        if (toolTip.isEmpty()) {
            setTooltip("No information, warning, or error messages", lblInfo);
        }
        else {
            toolTip = activeSection.toString() + "\n" + toolTip;
            setTooltip(toolTip, lblInfo);
        }
    }

    private void updateInfoLabelIcon() {
        List<MsgInfo> msgList = getMsgList();
                
        List<String> icons = new ArrayList<>();
        icons.add("/images/error.png");
        icons.add("/images/warning.png");
        icons.add("/images/info.png");

        // If last message is other than normal, set appropriate icon
        if (!msgList.isEmpty() && msgList.get(msgList.size() - 1).errorStyle != MsgInfo.MsgStyle.NORMAL) {
            if (msgList.get(msgList.size() - 1).errorStyle == MsgInfo.MsgStyle.ERROR) {
                setWidgetIcon(icons.get(0), lblInfo);
            }
            else if (msgList.get(msgList.size() - 1).errorStyle == MsgInfo.MsgStyle.WARNING) {
                setWidgetIcon(icons.get(1), lblInfo);
            }
            else if (msgList.get(msgList.size() - 1).errorStyle == MsgInfo.MsgStyle.INFORMATION) {
                setWidgetIcon(icons.get(2), lblInfo);
            }
            return;
        }

        // If last message is normal and list contains other messages, set icon with higher priority
        int error = findMessage(MsgInfo.MsgStyle.ERROR);
        int warning = findMessage(MsgInfo.MsgStyle.WARNING);
        int info = findMessage(MsgInfo.MsgStyle.INFORMATION);
        
        if (error >= 0) {
            setWidgetIcon("/images/error.png", lblInfo);
        }
        else if (warning >= 0) {
            setWidgetIcon("/images/warning.png", lblInfo);
        }
        else if (info >= 0) {
            setWidgetIcon("/images/info.png", lblInfo);
        }
        else {
            setWidgetIcon(null, lblInfo);
        }
    }

    private void updateMessageLabel() {
        updateMessageLabelFadeOut();
    }

    private void updateMessageLabelFadeOut() {
        if (messageFadeOutRunning) {
            return;
        }
        messageFadeOutRunning = true;

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(MSG_LABEL_FADE_OUT_DURATION), lblInfo);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            updateMessageLabelFadeIn();
        });
        fadeOut.play();

        return;
    }

    private void updateMessageLabelFadeIn() {
        List<MsgInfo> errorList = getMsgList();

        lblInfo.getStyleClass().removeAll("info-label", "warning-label", "error-label", "title-label");

        if (errorList.isEmpty()) {
            lblInfo.setText("Internal error in Settings Editor !!!");
            lblInfo.getStyleClass().add("error-label");
            lblInfo.setId(null);
            messageFadeOutRunning = false;
            return;
        }

        MsgInfo msg = errorList.get(errorList.size() - 1);
        lblInfo.setText(msg.msgText);
        lblInfo.getStyleClass().add(msg.getStyleClass());
        lblInfo.setId(msg.msgID);
        updateInfoLabelToolTip();
        updateInfoLabelIcon();

        lblInfo.setOpacity(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(MSG_LABEL_FADE_IN_DURATION), lblInfo);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeIn.setOnFinished(event -> {
            lblInfo.setOpacity(1.0);
        });

        fadeIn.play();

        messageFadeOutRunning = false;
    }

    // Load / Save APP STATE

    private void loadAppState(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            filePath = APP_STATE_FILE_NAME;
        }

        if (Files.exists(Path.of(filePath))) {
            Gson gson = new Gson();
            try {
                appState = gson.fromJson(Files.readString(Path.of(filePath)), PyDict.class);
                if (appState.getPyDictValue("chkSaveState") == null) {
                    appState.setPyDictValue("chkSaveState", true);
                    chkSaveState.setSelected(true);
                }
                log("AppState loaded");
            } catch (Exception e) {
                MsgInfo msg = new MsgInfo(
                    "'" + APP_STATE_FILE_NAME + "'",
                    "Failed to load '" + APP_STATE_FILE_NAME + "'",
                    "Reason: " + e.getMessage(),
                    MsgInfo.MsgStyle.WARNING,
                    MsgInfo.ErrorCode.NONE,
                    MSG_DURATION,
                    MSG_DURATION + 30,
                    false
                );
                log("(EXCEPTION) " + e.getMessage());
                showMessage(msg);
                appState = getAppStateEmptyDict();
                setDefaultSttWidgetsValues();
                return;
            }

            if (appState == null) {
                log("AppState is null, creating new one");
                appState = getAppStateEmptyDict();
                setDefaultSttWidgetsValues();
            }
        }
        else {
            MsgInfo msg = new MsgInfo(
                "'" + APP_STATE_FILE_NAME + "'",
                "File not found '" + APP_STATE_FILE_NAME + "'",
                "File " + APP_STATE_FILE_NAME + " not found. Application state could not be loaded.",
                MsgInfo.MsgStyle.WARNING,
                MsgInfo.ErrorCode.NONE,
                MSG_DURATION,
                MSG_DURATION + 30,
                false
            );
            log("(EXCEPTION) File not found '" + APP_STATE_FILE_NAME + "', Application state could not be loaded.");
            showMessage(msg);
            log("Creating new AppState because file not found");
            appState = getAppStateEmptyDict();
            setDefaultSttWidgetsValues();
        }
    }

    private void setDefaultSttWidgetsValues() {
        cmbSttFlag.getSelectionModel().select(SettingType.DEFAULT.toString());
        cmbSttDataType.getSelectionModel().select(DataType.STRING.toString());
        txtSttMin.setText("null");
        txtSttMax.setText("null");
    }
    
    public void saveAppState(boolean silent) {
        saveAppState(null, silent);
    }

    private void saveAppState() {
        saveAppState(null, true);
    }

    private void saveAppState(String filePath, boolean silent) {
        if (filePath == null || filePath.isEmpty()) {
            filePath = APP_STATE_FILE_NAME;
        }

        // Check is saving app state allowed
        if (chkSaveState.isSelected() == false) {
            log("Save AppState is not allowed, user disabled it in settings");
            log("Deleting app state");
            appState = getAppStateEmptyDict();
            appState.setPyDictValue("chkSaveState", false);
        }
        else {
            createAppState();
        }

        // If file doesn't exist, create it
        if (!Files.exists(Path.of(filePath))) {
            try {
                Files.createFile(Path.of(filePath));
                log("File '" + filePath + "' created");
            } catch (Exception e) {
                MsgInfo msg = new MsgInfo(
                    "'" + APP_STATE_FILE_NAME + "'",
                    "Failed to create '" + APP_STATE_FILE_NAME + "' file. Application state could not be saved.",
                    "Reason: " + e.getMessage(),
                    MsgInfo.MsgStyle.ERROR,
                    MsgInfo.ErrorCode.NONE,
                    MSG_DURATION + 10,
                    -1,
                    false
                );
                log("(EXCEPTION) " + e.getMessage());
                if (!silent) {showMessage(msg);}
                return;
            }
        }

        // Save
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
        String json = gson.toJson(appState);
        try {
            Files.writeString(Path.of(filePath), json);
            log("AppState saved to '" + filePath + "'");
        } catch (Exception e) {
            MsgInfo msg = new MsgInfo(
                "'" + APP_STATE_FILE_NAME + "'",
                "Failed to save '" + APP_STATE_FILE_NAME + "' file. Application state could not be saved.",
                "Reason: " + e.getMessage(),
                MsgInfo.MsgStyle.ERROR,
                MsgInfo.ErrorCode.NONE,
                MSG_DURATION + 10,
                -1,
                false
            );
            log("(EXCEPTION) " + e.getMessage());
            if (!silent) {showMessage(msg);}
        return;
        }
        MsgInfo msg = new MsgInfo(
            "'" + APP_STATE_FILE_NAME + "'",
            "Application state saved to '" + APP_STATE_FILE_NAME + "'",
            "Application state saved to '" + APP_STATE_FILE_NAME + "'",
            MsgInfo.MsgStyle.INFORMATION,
            MsgInfo.ErrorCode.NONE,
            1,
            30,
            false
        );
        if (!silent) {showMessage(msg);}
    }

    private String concatKeys(String... keys) {
        return PyDict.concatKeys(keys);
    }

    private PyDict getAppStateEmptyDict() {
        PyDict result = new PyDict();

        // For all SECTIONS
        result.setPyDictValue("chkSaveState", null); // Save state checkbox
        result.setPyDictValue("lastDir", null); // Last directory from FileDialog
        result.setPyDictValue("lastSection", null); // Last shown section
        result.setPyDictValue("fontSizeLstFiles", null); // Font size for lstFiles
        result.setPyDictValue("chkAutoUpdateFiles", null); // Auto update files checkbox
        // Main Win geometry
        result.setPyDictValue("mainWinGeometryState", null); // MINIMIZED, NORMAL, MAXIMIZED
        result.setPyDictValue("mainWinGeometryX", null); // X position
        result.setPyDictValue("mainWinGeometryY", null); // Y position
        result.setPyDictValue("mainWinGeometryWidth", null); // Width
        result.setPyDictValue("mainWinGeometryHeight", null); // Height


        // For Settings SECTION
        PyDict sttDict = new PyDict();

        sttDict.setPyDictValue("loadSttFromPath", null); // Load Settings from path
        sttDict.setPyDictValue("updateSttFilesPaths", null); // Update Settings files
        sttDict.setPyDictValue("chkAutoDataType", null); // Auto detect data type
        sttDict.setPyDictValue("sttLoadedMap", null); // Map of loaded Settings
        sttDict.setPyDictValue("sttChangedMap", null); // Map of changed Settings
        sttDict.setPyDictValue("sttVisibleList", null); // List of visible Settings (Loaded or Changed)
        sttDict.setPyDictValue("fontSizeLstStt", null); // Font size for lstStt
        sttDict.setPyDictValue("sttLoadedCurrentItem", null); // Loaded Settings current item
        sttDict.setPyDictValue("sttChangedCurrentItem", null); // Changed Settings current item
        sttDict.setPyDictValue("SplitPaneDividerPosition", null); // SplitPane divider position
        sttDict.setPyDictValue("chkSttAutoStrip", null); // Auto strip Settings value
        sttDict.setPyDictValue("chkSttAutoDefault", null); // Auto set default value
        sttDict.setPyDictValue("lstSttSortKey", null); // Sort key for lstStt
        
        sttDict.setPyDictValue("CurrentItem", new PyDict()); // Current item in process of changing
            sttDict.setPyDictValue("[CurrentItem][Key]", null);
            sttDict.setPyDictValue("[CurrentItem][Value]", null);
            sttDict.setPyDictValue("[CurrentItem][DefValue]", null);
            sttDict.setPyDictValue("[CurrentItem][Min]", null);
            sttDict.setPyDictValue("[CurrentItem][Max]", null);
            sttDict.setPyDictValue("[CurrentItem][Desc]", null);
            sttDict.setPyDictValue("[CurrentItem][SettingsType]", null);
            sttDict.setPyDictValue("[CurrentItem][DataType]", null);
        

        result.setPyDictValue(Section.SETTINGS.toString(), sttDict);  // Create Settings section


        // For Language SECTION
        PyDict langDict = new PyDict();

        langDict.setPyDictValue("loadLangFromPath", null); // Load Language from path
        langDict.setPyDictValue("updateLangFilesPaths", null); // Update Language files
        langDict.setPyDictValue("langLoadedMap", null); // Map of loaded Language
        langDict.setPyDictValue("langChangedMap", null); // Map of changed Language
        langDict.setPyDictValue("langVisibleList", null); // List of visible Language (Loaded or Changed)
        langDict.setPyDictValue("fontSizeLstLang", null); // Font size for lstLang
        langDict.setPyDictValue("langLoadedCurrentItem", null); // Loaded Language current item
        langDict.setPyDictValue("langChangedCurrentItem", null); // Changed Language current item
        langDict.setPyDictValue("SplitPaneDividerPosition", null); // SplitPane divider position
        langDict.setPyDictValue("lstLangSortKey", null); // Sort key for lstLang
        
        langDict.setPyDictValue("currentTxtLangKey", null); // Current key value in txtLangKey
        langDict.setPyDictValue("scrollPaneContent", null); // Current item in process of changing

        result.setPyDictValue(Section.LANGUAGE.toString(), langDict);  // Create Language section


        // For Language Manager SECTION
        PyDict langMgrDict = new PyDict();

        langMgrDict.setPyDictValue("scrollPaneManageContent", null); // Current content of scrollPaneManage

        result.setPyDictValue(Section.LANGUAGE_MANAGE.toString(), langMgrDict);  // Create Language Manager section


        return result;
    }

    private void createAppState() {
        String lastDir = appState.getPyDictValue("lastDir");
        if (lastDir == null) {
            lastDir = "";
        }

        appState = getAppStateEmptyDict();
        appState.setPyDictValue("chkSaveState", chkSaveState.isSelected());
        appState.setPyDictValue("lastDir", lastDir);
        appState.setPyDictValue("lastSection", activeSection.toString());
        appState.setPyDictValue("fontSizeLstFiles", fontSizeLstFiles);
        appState.setPyDictValue("chkAutoUpdateFiles", chkAutoUpdateFiles.isSelected());
        // Main Win geometry
        String mainWinGeometryState = "NORMAL";
        if (primaryStage.isMaximized()) {
            mainWinGeometryState = "MAXIMIZED";
        }
        else if (primaryStage.isIconified()) {
            mainWinGeometryState = "MINIMIZED";
        }
        appState.setPyDictValue("mainWinGeometryState", mainWinGeometryState);
        if (mainWinGeometryState.equals("NORMAL")) {
            appState.setPyDictValue("mainWinGeometryX", primaryStage.getX());
            appState.setPyDictValue("mainWinGeometryY", primaryStage.getY());
            appState.setPyDictValue("mainWinGeometryWidth", primaryStage.getWidth());
            appState.setPyDictValue("mainWinGeometryHeight", primaryStage.getHeight());
        }

        // ........................... Settings SECTION
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "loadSttFromPath"), loadSttFromPath);
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "updateSttFilesPaths"), updateSttFilesPaths);
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "chkAutoDataType"), chkAutoDataType.isSelected());
        // sttLoadedMap
        for (String key : sttLoadedMap.keySet()) {
            SettingsItem sttItem = (SettingsItem) sttLoadedMap.get(key);
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttLoadedMap", key), sttItem.toMap());
        }
        // sttChangedMap
        for (String key : sttChangedMap.keySet()) {
            SettingsItem sttItem = (SettingsItem) sttChangedMap.get(key);
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttChangedMap", key), sttItem.toMap());
        }
        // sttVisibleList
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttVisibleList"), sttVisibleList);
        // Font Size for lstStt
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "fontSizeLstStt"), fontSizeLstStt);
        // sttLoadedCurrentItem and sttChangedCurrentItem
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttLoadedCurrentItem"), sttLoadedCurrentItem);
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttChangedCurrentItem"), sttChangedCurrentItem);

        // SplitPane divider position
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "SplitPaneDividerPosition"), sttSttSplitPane.getDividerPositions()[0]);

        // Auto strip Settings value
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "chkSttAutoStrip"), chkSttAutoStrip.isSelected());
        // Auto set default value
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "chkSttAutoDefault"), chkSttAutoDefault.isSelected());
        // Sort key for lstStt
        appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "lstSttSortKey"), lstSttSortKey);
        
        // Current item in changing process
        if (isUserCurrentlyChanging(txtSttKey.getText())) {
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][Key]"), txtSttKey.getText());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][Value]"), txtSttValue.getText());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][DefValue]"), txtSttDefValue.getText());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][Min]"), txtSttMin.getText());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][Max]"), txtSttMax.getText());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][Desc]"), txtSttDesc.getText());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][SettingsType]"), cmbSttFlag.getSelectionModel().getSelectedItem());
            appState.setPyDictValue(concatKeys(Section.SETTINGS.toString(), "[CurrentItem][DataType]"), cmbSttDataType.getSelectionModel().getSelectedItem());
            log("Saving current Settings item in changing process: " + txtSttKey.getText());
        }


        // ........................... Language SECTION
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "loadLangFromPath"), loadLangFromPath);
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "updateLangFilesPaths"), updateLangFilesPaths);
        // langLoadedMap
        for (String key : langLoadedMap.keySet()) {
            LanguageItemGroup langItemGroup = (LanguageItemGroup) langLoadedMap.get(key);
            appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langLoadedMap", key), langItemGroup.toMap());
        }
        // langChangedMap
        for (String key : langChangedMap.keySet()) {
            LanguageItemGroup langItemGroup = (LanguageItemGroup) langChangedMap.get(key);
            appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langChangedMap", key), langItemGroup.toMap());
        }
        // langVisibleList
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langVisibleList"), langVisibleList);
        // Font Size for lstLang
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "fontSizeLstLang"), fontSizeLstLang);
        // langLoadedCurrentItem and langChangedCurrentItem
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langLoadedCurrentItem"), langLoadedCurrentItem);
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langChangedCurrentItem"), langChangedCurrentItem);

        // SplitPane divider position
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "SplitPaneDividerPositionLang"), sttLangSplitPane.getDividerPositions()[0]);

        // Sort key for lstLang
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "lstLangSortKey"), lstLangSortKey);

        // Current item in changing process
        appState.setPyDictValue(concatKeys(Section.LANGUAGE.toString(), "currentTxtLangKey"), txtLangKey.getText());
        if (scrollPaneContent != null) {
            appState.setPyDictValue(PyDict.concatKeys(Section.LANGUAGE.toString(), "scrollPaneContent"), scrollPaneContent.toMap());
            log("Saving current Language item in changing process: " + txtLangKey.getText());
        }
        
        // ........................... Language Manage SECTION
        appState.setPyDictValue(concatKeys(Section.LANGUAGE_MANAGE.toString(), "scrollPaneManageContent"), scrollPaneManageContent.toMap());
        
        log("AppState created");
    }

    private void restoreAppState() {
        // Handle App State
        if (appState == null || !(appState instanceof PyDict) || appState.getPyDictValue("chkSaveState") == null) {
            log("Unable to restore AppState, creating new one");
            appState = getAppStateEmptyDict();
            appState.setPyDictValue("chkSaveState", true);
            chkSaveState.setSelected(true);
            mountNodeToScrollPane();
            mountNodeToScrollPaneManage();
            return;
        }

        Boolean chkSaveStateValue = appState.getPyDictValue("chkSaveState");

        if (chkSaveStateValue == false) {
            log("Restore AppState is not allowed, user disabled it in settings");
            appState = getAppStateEmptyDict();
            appState.setPyDictValue("chkSaveState", false);
            chkSaveState.setSelected(false);
            mountNodeToScrollPane();
            mountNodeToScrollPaneManage();
            return;
        }
        else if (chkSaveStateValue == null) {
            chkSaveStateValue = true;
            appState.setPyDictValue("chkSaveState", chkSaveStateValue);
        }

        // Restore AppState data
        chkSaveState.setSelected(chkSaveStateValue);
        if (appState.getPyDictValue("fontSizeLstFiles") != null) {
            fontSizeLstFiles = appState.getPyDictDoubleValueEXPLICIT("fontSizeLstFiles");
        }
        lstFiles.setStyle("-fx-font-size: " + fontSizeLstFiles + "px;");

        if (appState.getPyDictValue("chkAutoUpdateFiles") != null) {
            chkAutoUpdateFiles.setSelected(appState.getPyDictBooleanValueEXPLICIT("chkAutoUpdateFiles"));
        }

        // Main Win Geometry
        String mainWinGeometryState = "";
        if (appState.getPyDictValue("mainWinGeometryState") != null) {
            mainWinGeometryState = appState.getPyDictValue("mainWinGeometryState");
        }
        if (mainWinGeometryState.equals("MINIMIZED")) {
            primaryStage.setIconified(true);
        }
        else if (mainWinGeometryState.equals("MAXIMIZED")) {
            primaryStage.setMaximized(true);
        }
        else if (mainWinGeometryState.equals("NORMAL")) {
            primaryStage.setMaximized(false);
            primaryStage.setIconified(false);
            if (appState.getPyDictValue("mainWinGeometryX") != null) {
                primaryStage.setX(appState.getPyDictDoubleValueEXPLICIT("mainWinGeometryX"));
            }
            if (appState.getPyDictValue("mainWinGeometryY") != null) {
                primaryStage.setY(appState.getPyDictDoubleValueEXPLICIT("mainWinGeometryY"));
            }
            if (appState.getPyDictValue("mainWinGeometryWidth") != null) {
                primaryStage.setWidth(appState.getPyDictDoubleValueEXPLICIT("mainWinGeometryWidth"));
            }
            if (appState.getPyDictValue("mainWinGeometryHeight") != null) {
                primaryStage.setHeight(appState.getPyDictDoubleValueEXPLICIT("mainWinGeometryHeight"));
            }
        }
        

        
        // .................... Settings SECTION
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "loadSttFromPath")) != null) {
            loadSttFromPath = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "loadSttFromPath"));
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "updateSttFilesPaths")) != null) {
            updateSttFilesPaths = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "updateSttFilesPaths"));
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "chkAutoDataType")) != null) {
            chkAutoDataType.setSelected(appState.getPyDictBooleanValueEXPLICIT(concatKeys(Section.SETTINGS.toString(), "chkAutoDataType")));
        }

        // sttLoadedMap
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttLoadedMap")) != null) {
            Map<String, Object> map = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttLoadedMap"));
            sttLoadedMap.clear();
            for (String key : map.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapItem = (Map<String, Object>) map.get(key);
                SettingsItem sttItem = new SettingsItem();
                sttItem.fromMap(mapItem);
                sttLoadedMap.put(key, sttItem);
            }
        }

        // sttChangedMap
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttChangedMap")) != null) {
            Map<String, Object> map = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttChangedMap"));
            sttChangedMap.clear();
            for (String key : map.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapItem = (Map<String, Object>) map.get(key);
                SettingsItem sttItem = new SettingsItem();
                sttItem.fromMap(mapItem);
                sttChangedMap.put(key, sttItem);
            }
        }

        // Create sttChangedList
        sttChangedList.clear();
        for (String key : sttChangedMap.keySet()) {
            SettingsItem sttItem = (SettingsItem) sttChangedMap.get(key);
            sttChangedList.add(sttItem.getKey());
        }

        // sttVisibleList
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttVisibleList")) != null) {
            sttVisibleList = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttVisibleList"));
        }

        // sttLoadedCurrentItem and sttChangedCurrentItem
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttLoadedCurrentItem")) != null) {
            sttLoadedCurrentItem = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttLoadedCurrentItem"));
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttChangedCurrentItem")) != null) {
            sttChangedCurrentItem = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "sttChangedCurrentItem"));
        }

        changeSttVisibleList(sttVisibleList);

        // Font size for lstStt
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "fontSizeLstStt")) != null) {
            fontSizeLstStt = appState.getPyDictDoubleValueEXPLICIT(concatKeys(Section.SETTINGS.toString(), "fontSizeLstStt"));
            lstStt.setStyle("-fx-font-size: " + fontSizeLstStt + "px;");
        }

        // Split Pane divider position
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "SplitPaneDividerPosition")) != null) {
            sttSttSplitPane.setDividerPositions(appState.getPyDictDoubleValueEXPLICIT(concatKeys(Section.SETTINGS.toString(), "SplitPaneDividerPosition")));
        }

        // Auto strip value
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "chkSttAutoStrip")) != null) {
            chkSttAutoStrip.setSelected(appState.getPyDictBooleanValueEXPLICIT(concatKeys(Section.SETTINGS.toString(), "chkSttAutoStrip")));
        }

        // Auto set default value
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "chkSttAutoDefault")) != null) {
            chkSttAutoDefault.setSelected(appState.getPyDictBooleanValueEXPLICIT(concatKeys(Section.SETTINGS.toString(), "chkSttAutoDefault")));
        }

        // Sort key for lstStt
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "lstSttSortKey")) != null) {
            lstSttSortKey = appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "lstSttSortKey"));
        }

        // Current item in changing process
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Key")) != null) {
            txtSttKey.setText(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Key")).toString());
            log(LOG_INDENT + "Restoring current settings item.");
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Value")) != null) {
            txtSttValue.setText(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Value")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "DefValue")) != null) {
            txtSttDefValue.setText(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "DefValue")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Min")) != null) {
            txtSttMin.setText(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Min")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Max")) != null) {
            txtSttMax.setText(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Max")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Desc")) != null) {
            txtSttDesc.setText(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "Desc")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "SettingsType")) != null) {
            cmbSttFlag.getSelectionModel().select(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "SettingsType")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "DataType")) != null) {
            cmbSttDataType.getSelectionModel().select(appState.getPyDictValue(concatKeys(Section.SETTINGS.toString(), "CurrentItem", "DataType")).toString());
        }

        
        // .................... Language SECTION
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "loadLangFromPath")) != null) {
            loadLangFromPath = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "loadLangFromPath"));
        }
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "updateLangFilesPaths")) != null) {
            updateLangFilesPaths = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "updateLangFilesPaths"));
        }
        mountNodeToScrollPane();

        // langLoadedMap
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langLoadedMap")) != null) {
            Map<String, Object> map = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langLoadedMap"));
            langLoadedMap.clear();
            for (String key : map.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapItem = (Map<String, Object>) map.get(key);
                LanguageItemGroup langGroupItem =  new LanguageItemGroup();
                langGroupItem.fromMap(mapItem);
                langLoadedMap.put(key, langGroupItem);
            }
        }

        // langChangedMap
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langChangedMap")) != null) {
            Map<String, Object> map = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langChangedMap"));
            langChangedMap.clear();
            for (String key : map.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapItem = (Map<String, Object>) map.get(key);
                LanguageItemGroup langGroupItem =  new LanguageItemGroup();
                langGroupItem.fromMap(mapItem);
                langChangedMap.put(key, langGroupItem);
            }
        }

        // Create langChangedList
        langChangedList.clear();
        for (String key : langChangedMap.keySet()) {
            LanguageItemGroup langGroupItem = (LanguageItemGroup) langChangedMap.get(key);
            langChangedList.add(langGroupItem.getGroupKey());
        }

        // langVisibleList
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langVisibleList")) != null) {
            langVisibleList = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langVisibleList"));
        }

        // langLoadedCurrentItem and langChangedCurrentItem
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langLoadedCurrentItem")) != null) {
            langLoadedCurrentItem = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langLoadedCurrentItem"));
        }
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langChangedCurrentItem")) != null) {
            langChangedCurrentItem = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "langChangedCurrentItem"));
        }

        changeLangVisibleList(langVisibleList);

        // Font size for lstLang
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "fontSizeLstLang")) != null) {
            fontSizeLstLang = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "fontSizeLstLang"));
            lstLang.setStyle("-fx-font-size: " + fontSizeLstLang + "px;");
        }

        // Split Pane divider position
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "splitPaneDividerPositionLang")) != null) {
            sttLangSplitPane.setDividerPositions(appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "splitPaneDividerPositionLang")));
        }

        // Sort key for lstLang
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "lstLangSortKey")) != null) {
            lstLangSortKey = appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "lstLangSortKey"));
        }

        // Current item in changing process
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "currentTxtLangKey")) != null) {
            txtLangKey.setText(appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "currentTxtLangKey")).toString());
        }
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "scrollPaneContent")) != null) {
            if (scrollPaneContent != null) {
                log(LOG_INDENT + "Restoring current Language item.");
                scrollPaneContent.fromMap(appState.getPyDictValue(concatKeys(Section.LANGUAGE.toString(), "scrollPaneContent")));
            }
        }


        // .................... Language Manage SECTION
        mountNodeToScrollPaneManage();
        if (appState.getPyDictValue(concatKeys(Section.LANGUAGE_MANAGE.toString(), "scrollPaneManageContent")) != null) {
            if (scrollPaneManageContent != null) {
                scrollPaneManageContent.fromMap(appState.getPyDictValue(concatKeys(Section.LANGUAGE_MANAGE.toString(), "scrollPaneManageContent")));
                if (scrollPaneManageContent.getLanguageFileName() != null && !scrollPaneManageContent.getLanguageFileName().isEmpty()) {
                    File file = new File(scrollPaneManageContent.getLanguageFileName());
                    lblMngFileLong.setText(file.getAbsolutePath());
                    lblMngFileShort.setText(file.getName());
                }
            }
        }



        // Handle last opened Section
        if (appState.getPyDictValue("lastSection") != null) {
            try {
                activateSection(Section.valueOf(appState.getPyDictValue("lastSection").toString()));
            }
            catch (Exception e) {
                log("Unable to restore last opened section, showing Settings section");
                activateSection(Section.SETTINGS);
            }
        }
        else {
            activateSection(Section.SETTINGS);
        }

        log("AppState restored");
    }

    // SECTION CHANGING

    /**
     * Activates section
     * @param section - <b>Section</b> Section to activate
     * <p>ACTIONS:</p>
     * <ul>
     *     <li>Change selected tab</li>
     *     <li>Change lblSource text</li></li>
     *     <li>Populate lstFiles list</li>
     * </ul>
     */
    private void activateSection(Section section) {
        activeSection = section;
        if (activeSection == Section.SETTINGS) {
            showFileAndSaveWidgets(true);
            // Switch to Settings tab
            tabPane.getSelectionModel().select(tabStt);
            // Set info label to show source file for keys
            if (loadSttFromPath.isEmpty()) {
                lblSource.setText("Click to load Settings keys from file...");
            }
            else {
                lblSource.setText(loadSttFromPath);
            }
            // Populate lstFiles list
            populateList(lstFiles, updateSttFilesPaths);
            log("Active section changed to Settings");
        }
        else if (activeSection == Section.LANGUAGE) {
            showFileAndSaveWidgets(true);
            // Switch to Language tab
            tabPane.getSelectionModel().select(tabLang);
            // Set info label to show source file for keys
            if (loadLangFromPath.isEmpty()) {
                lblSource.setText("Click to load language keys from file...");
            }
            else {
                lblSource.setText(loadLangFromPath);
            }
            // Populate lstFiles list
            populateList(lstFiles, updateLangFilesPaths);
            log("Active section changed to Language");
        }
        else if (activeSection == Section.LANGUAGE_MANAGE) {
            showFileAndSaveWidgets(false);
            // Switch to Language Manage tab
            tabPane.getSelectionModel().select(tabManage);
            log("Active section changed to Language Manage");
            
        }
    }

    private void showFileAndSaveWidgets(boolean enable) {
        if (enable) {
            if (hboxLoadFile != null) {
                hboxLoadFile.setDisable(false);
            }
            if (layoutHBoxFiles != null) {
                layoutHBoxFiles.setDisable(false);
            }
            if (hboxSaveButtons != null) {
                hboxSaveButtons.setDisable(false);
            }
        }
        else {
            if (hboxLoadFile != null) {
                hboxLoadFile.setDisable(true);
            }
            if (layoutHBoxFiles != null) {
                layoutHBoxFiles.setDisable(true);
            }
            if (hboxSaveButtons != null) {
                hboxSaveButtons.setDisable(true);
            }
        }
    }

    private void populateList(ListView<String> listWidget, List<String> itemsList) {
        // clear list
        listWidget.getItems().clear();
        // add items
        for (String itemName : itemsList) {
            listWidget.getItems().add(itemName);
        }

        // Try to refresh list display
        listWidget.layout();
    }

    // LOGGING

    /**
     * Logs message to file / console
     * Time is added to message if message doesn't start with '[', also newline is added at the end
     * File is created if it doesn't exist (usually "app.log")
     * @param msg - <b>String</b> Message to log
     */
    public void log(String msg) {
        log(msg, "\n");
    }

    /**
     * Logs message to file / console
     * Time is added to message if message doesn't start with '[', also newline is added at the end
     * File is created if it doesn't exist (usually "app.log")
     * @param msg - <b>String</b> Message to log
     * @param endOfString - <b>String</b> Message delimiter (usually "\n")
     */
    public void log(String msg, String endOfString) {
        // Remove new lines, but if message is LOG_HEADER, add newline at start
        msg = msg.strip();
        msg = msg.replaceAll("\\R", " ");
        msg = logGlobalIndent + msg;

        // Add time to msg
        if (!msg.startsWith("[")) {
            msg = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")) + "] " + msg;
        }

        if (msg.endsWith(LOG_HEADER)) {
            msg = "\n" + msg;
        }
        
        if (PRINT_LOG_TO_CONSOLE) {
            System.out.println(msg);
        }

        // Create file if not exists
        if (!Files.exists(Path.of(APP_LOG_FILE_NAME))) {
            try {
                Files.createFile(Path.of(APP_LOG_FILE_NAME));
            } catch (Exception e) {
                System.out.println("Failed to create log file: " + e.getMessage());
                return;
            }
        }

        try {
            Files.writeString(Path.of(APP_LOG_FILE_NAME), msg + endOfString, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public void fixLogSize() {
        if (Files.exists(Path.of(APP_LOG_FILE_NAME))) {
            String logContent = "";
            try {
                logContent = new String(Files.readAllBytes(Path.of(APP_LOG_FILE_NAME)));
            } catch (Exception e) {
                System.out.println("Failed to read log file: " + e.getMessage());
            }

            int logCount = 0;
            for (String line : logContent.split("\n")) {
                if (line.contains(LOG_HEADER)) {
                    logCount += 1;
                }
            }

            if (logCount > LOG_MAX_PREVIOUS_LOGS_TO_KEEP) {
                String newLogContent = "";
                int currentLogCount = 0;
                boolean canKeepLog = false;
                for (String line : logContent.split("\n")) {
                    if (line.contains(LOG_HEADER)) {
                        currentLogCount += 1;
                    }
                    
                    if (canKeepLog == false && currentLogCount > (logCount - LOG_MAX_PREVIOUS_LOGS_TO_KEEP)) {
                        canKeepLog = true;
                    }

                    if (canKeepLog) {
                        newLogContent += line + "\n";
                    }
                }

                try {
                    Files.writeString(Path.of(APP_LOG_FILE_NAME), newLogContent);
                } catch (Exception e) {
                    System.out.println("Failed to write to log file: " + e.getMessage());
                }
            }
        }
    }

    public int logIndentPlus() {
        logGlobalIndent += LOG_INDENT;
        return UString.Count(logGlobalIndent, LOG_INDENT);
    }

    public int logIndentMinus() {
        if (logGlobalIndent.length() < LOG_INDENT.length()) {
            return 0;
        }
        logGlobalIndent = logGlobalIndent.substring(0, logGlobalIndent.length() - LOG_INDENT.length());
        return UString.Count(logGlobalIndent, LOG_INDENT);
    }

    public void logIndentSet(int numberOfIndents) {
        logGlobalIndent = LOG_INDENT.repeat(numberOfIndents);
    }

    public int logIndentCount() {
        return UString.Count(logGlobalIndent, LOG_INDENT);
    }

    public void logIndentClear() {
        logGlobalIndent = "";
    }

    // Setup widgets

    private void setupWidgets() {
        lstFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lblToolTip.setVisible(false);
        lblSttInfo.setText("Enter value to see available data types...");
        setWidgetIcon("/images/record.png", lblSttRec);
        setWidgetIcon("/images/record.png", lblLangRec);
        btnSttFilterClear.setStyle("-fx-border-width: 0px;");
        btnLangFilterClear.setStyle("-fx-border-width: 0px;");
        setWidgetIcon("/images/clear_filter.png", btnSttFilterClear);
        setWidgetIcon("/images/clear_filter.png", btnLangFilterClear);

        imgListItemChanged.setFitHeight(16);
        imgListItemChanged.setPreserveRatio(true);

        imgListItemDeleted.setFitHeight(16);
        imgListItemDeleted.setPreserveRatio(true);

        // SETTINGS
        
        // Listener for txtSttValue
        setupListenerForTxtSttValue();
        // Listener for Default Value
        setupListenerForDefaultValue();
        // Listener for Min Value
        setupListenerForMinValue();
        // Listener for Max Value
        setupListenerForMaxValue();
        // Listener for Setting Description
        setupListenerForDescription();
        // Listener for txtSttKey
        setupListenerForTxtSttKey();
        // Listener for txtSttSearch
        setupListenerForTxtSttSearch();
        // Set cell factory for lstFiles
        setupCellFactoryForLstFiles();
        // Set cell factory for lstStt
        setupCellFactoryForLstStt();
        // Listener for lstStt
        setupListenerForLstStt();
        // Listener for cmbDataTypes
        setupListenerForCmbDataTypes();
        // Listener for cmbSttFlag
        setupListenerForCmbSttFlag();

        // LANGUAGE

        // Listener for txtLangKey
        setupListenerForTxtLangKey();
        // Listener for txtLangSearch
        setupListenerForTxtLangSearch();
        // Set cell factory for lstLang
        setupCellFactoryForLstLang();
        // Listener for lstLang
        setupListenerForLstLang();

        // LANGUAGE MANAGE
        lblMngInfo.setVisible(false);
        lblMngInfo.setManaged(false);

        // OTHER

        // AnchorPaneMain KeyPress and MouseClick events
        setupKeyPressAndMouseClickForMainAnchorPane();

        
        setupWidgetsText();

        setupContextMenus();

        updateMessageLabel();
    }

    private void mountNodeToScrollPane() {
        List<String> updateLangFilesList = getLangAffectedFilesList();

        scrollPaneContent = new ScrollPaneContent(null, primaryStage, updateLangFilesList);

        scrPaneLang.setContent(scrollPaneContent);
    }

    private void mountNodeToScrollPaneManage() {
        scrollPaneManageContent = new ScrollPaneManageContent(primaryStage);

        scrPaneManage.setContent(scrollPaneManageContent);
    }

    private List<String> getLangAffectedFilesList() {
        List<String> updateLangFilesList = new ArrayList<>();
        if (loadLangFromPath != null && !loadLangFromPath.isEmpty() && Files.exists(Path.of(loadLangFromPath))) {
            updateLangFilesList.add(loadLangFromPath);
        }
        for (String file : updateLangFilesPaths) {
            updateLangFilesList.add(file);
        }
        
        return updateLangFilesList;
    }

    private void setupCellFactoryForLstLang() {
        lstLang.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().clear();
                } else {
                    setText(item);

                    getStyleClass().removeAll("list-cell", "list-cell-changed", "list-cell-invalid");
                    setGraphic(null);

                    // Set items style and graphic
                    if (langChangedList.contains(item)) {
                        LanguageItemGroup langItem = (LanguageItemGroup) langChangedMap.get(item);
                        if (langItem != null) {
                            if ("Changed".equals(langItem.getUserData())) {
                                getStyleClass().add("list-cell");
                                getStyleClass().add("list-cell-changed");
                                // Set graphic
                                setGraphic(new ImageView(imgListItemChanged.getImage()) {{
                                    setFitHeight(fontSizeLstLang);
                                    setPreserveRatio(true);
                                }});
                    
                            } else if ("Deleted".equals(langItem.getUserData())) {
                                getStyleClass().add("list-cell");
                                getStyleClass().add("list-cell-invalid");
                                // Set graphic
                                setGraphic(new ImageView(imgListItemDeleted.getImage()) {{
                                    setFitHeight(fontSizeLstLang);
                                    setPreserveRatio(true);
                                }});

                            } else {
                                getStyleClass().add("list-cell");
                            }
                        }   
                        else {
                            getStyleClass().add("list-cell");
                        }
                    }
                    else {
                        getStyleClass().add("list-cell");
                    }
                }
            }
        });
    }

    private void setupListenerForLstLang() {
        lstLang.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                langListReloadedItem = true;
                log("Current Language item changed to: " + newValue);
                langCurrentItemChanged(newValue);
            }
        });

        lstLang.setOnMousePressed((MouseEvent event) -> {
            langListReloadedItem = false;
        });

        lstLang.setOnMouseClicked((MouseEvent event) -> {
            if (!langListReloadedItem && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && lstLang.getSelectionModel().getSelectedItem() != null) {
                log("Current Language item reloaded to: " + lstLang.getSelectionModel().getSelectedItem());
                langCurrentItemChanged(lstLang.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupListenerForTxtLangSearch() {
        txtLangSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            changeLangVisibleList(langVisibleList);
        });
    }

    private void setupListenerForTxtLangKey() {
        txtLangKey.textProperty().addListener((observable, oldValue, newValue) -> {
            String itemName = txtLangKey.getText();
            if (isLangItemExists(itemName)) {
                if (langVisibleList.equals("Changed")) {
                    if (langChangedMap.containsKey(itemName)) {
                        setCurrentItemInChangedListLang(itemName, null);
                        populateLangItem(itemName);
                    }
                    else if (langLoadedMap.containsKey(itemName)) {
                        changeLangVisibleList("Loaded");
                        setCurrentItemInLoadedListLang(itemName, null);
                        populateLangItem(itemName);
                    }
                }
                else {
                    if (langLoadedMap.containsKey(itemName)) {
                        setCurrentItemInLoadedListLang(itemName, null);
                        populateLangItem(itemName);
                    }
                    else if (langChangedMap.containsKey(itemName)) {
                        changeLangVisibleList("Changed");
                        setCurrentItemInChangedListLang(itemName, null);
                        populateLangItem(itemName);
                    }
                }
            }
            else {
                populateLangItem(null);
            }

            updateLangWidgetsAppearance();
        });
    }

    private void setupListenerForTxtSttValue() {
        txtSttValue.textProperty().addListener((observable, oldValue, newValue) -> {
            ArrayList<String> availableDataTypes = getAvailableDataTypes(txtSttValue.getText());
            updateLblSttInfo(availableDataTypes);
            setAutoDataType(availableDataTypes);
            checkIfUserIsChangingSetting();
            if (chkSttAutoDefault.isSelected()) {
                txtSttDefValue.setText(txtSttValue.getText());
            }
            else {
                checkIfSettingsEntryIsValid();
            }
        });
    }

    private void setupListenerForDefaultValue() {
        txtSttDefValue.textProperty().addListener((observable, oldValue, newValue) -> {
            checkIfUserIsChangingSetting();
            checkIfSettingsEntryIsValid();
        });
    }

    private void setupListenerForMinValue() {
        txtSttMin.textProperty().addListener((observable, oldValue, newValue) -> {
            checkIfUserIsChangingSetting();
            checkIfSettingsEntryIsValid();
        });
    }

    private void setupListenerForMaxValue() {
        txtSttMax.textProperty().addListener((observable, oldValue, newValue) -> {
            checkIfUserIsChangingSetting();
            checkIfSettingsEntryIsValid();
        });
    }

    private void setupListenerForDescription() {
        txtSttDesc.textProperty().addListener((observable, oldValue, newValue) -> {
            checkIfUserIsChangingSetting();
        });
    }

    private void setupListenerForTxtSttKey() {
        txtSttKey.textProperty().addListener((observable, oldValue, newValue) -> {
            String itemName = txtSttKey.getText();
            if (isSttItemExists(itemName)) {
                if (sttVisibleList.equals("Changed")) {
                    if (sttChangedMap.containsKey(itemName)) {
                        setCurrentItemInChangedList(itemName, null);
                        populateSttItem(itemName);
                    }
                    else if (sttLoadedMap.containsKey(itemName)) {
                        changeSttVisibleList("Loaded");
                        setCurrentItemInLoadedList(itemName, null);
                        populateSttItem(itemName);
                    }
                }
                else {
                    if (sttLoadedMap.containsKey(itemName)) {
                        setCurrentItemInLoadedList(itemName, null);
                        populateSttItem(itemName);
                    }
                    else if (sttChangedMap.containsKey(itemName)) {
                        changeSttVisibleList("Changed");
                        setCurrentItemInChangedList(itemName, null);
                        populateSttItem(itemName);
                    }
                }
            }
            updateSttWidgetsAppearance();
        });
    }

    private void setupListenerForTxtSttSearch() {
        txtSttSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            changeSttVisibleList(sttVisibleList);
        });
    }

    private void setupCellFactoryForLstFiles() {
        lstFiles.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("list-cell", "list-cell-invalid");
                } else {
                    setText(item);

                    getStyleClass().removeAll("list-cell", "list-cell-invalid");

                    if (activeSection == Section.SETTINGS) {
                        if (isSettingsFile(item)) {
                            getStyleClass().add("list-cell");
                        } else {
                            getStyleClass().add("list-cell-invalid");
                        }
                    }
                    else if (activeSection == Section.LANGUAGE) {
                        if (isLanguageFile(item)) {
                            getStyleClass().add("list-cell");
                        } else {
                            getStyleClass().add("list-cell-invalid");
                        }
                    }
                }
            }
        });
    }

    private void setupCellFactoryForLstStt() {
        lstStt.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().clear();
                } else {
                    setText(item);

                    getStyleClass().removeAll("list-cell", "list-cell-changed", "list-cell-invalid");
                    setGraphic(null);

                    // Set items style and graphic
                    if (sttChangedList.contains(item)) {
                        SettingsItem sttItem = (SettingsItem) sttChangedMap.get(item);
                        if (sttItem != null) {
                            if ("Changed".equals(sttItem.getUserData())) {
                                getStyleClass().add("list-cell");
                                getStyleClass().add("list-cell-changed");
                                // Set graphic
                                setGraphic(new ImageView(imgListItemChanged.getImage()) {{
                                    setFitHeight(fontSizeLstStt);
                                    setPreserveRatio(true);
                                }});
                    
                            } else if ("Deleted".equals(sttItem.getUserData())) {
                                getStyleClass().add("list-cell");
                                getStyleClass().add("list-cell-invalid");
                                // Set graphic
                                setGraphic(new ImageView(imgListItemDeleted.getImage()) {{
                                    setFitHeight(fontSizeLstStt);
                                    setPreserveRatio(true);
                                }});

                            } else {
                                getStyleClass().add("list-cell");
                            }
                        }   
                        else {
                            getStyleClass().add("list-cell");
                        }
                    }
                    else {
                        getStyleClass().add("list-cell");
                    }
                }
            }
        });
    }

    private void setupListenerForLstStt() {
        lstStt.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sttListReloadedItem = true;
                log("Current Settings item changed to: " + newValue);
                sttCurrentItemChanged(newValue);
            }
        });

        lstStt.setOnMousePressed((MouseEvent event) -> {
            sttListReloadedItem = false;
        });

        lstStt.setOnMouseClicked((MouseEvent event) -> {
            if (!sttListReloadedItem && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && lstStt.getSelectionModel().getSelectedItem() != null) {
                log("Current Settings item reloaded to: " + lstStt.getSelectionModel().getSelectedItem());
                sttCurrentItemChanged(lstStt.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupListenerForCmbDataTypes() {
        cmbSttDataType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Get Scene coordinates for cmbDataTypes
                double x = cmbSttDataType.localToScene(0, cmbSttDataType.getHeight() + 5).getX();
                double y = cmbSttDataType.localToScene(0, cmbSttDataType.getHeight() + 5).getY();
                showToolTipLabel("Changed to: " + newValue.toString(), x, y, 2);
                checkIfUserIsChangingSetting();
                checkIfSettingsEntryIsValid();
            }
        });
    }

    private void setupListenerForCmbSttFlag() {
        cmbSttFlag.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Get Scene coordinates for cmbSttFlag
                double x = cmbSttFlag.localToScene(0, cmbSttFlag.getHeight() + 5).getX();
                double y = cmbSttFlag.localToScene(0, cmbSttFlag.getHeight() + 5).getY();
                showToolTipLabel("Changed to: " + newValue.toString(), x, y, 2);
                checkIfUserIsChangingSetting();
                checkIfSettingsEntryIsValid();
            }
        });
    }

    private void setupKeyPressAndMouseClickForMainAnchorPane() {
        layoutAnchorPaneMain.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            removeInactiveAndExpiredMessages();
        });

        layoutAnchorPaneMain.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() instanceof Label && event.getTarget().equals(lblInfo)) {
                return;
            }
            removeInactiveAndExpiredMessages();
        });
    }

    private void setupContextMenus() {
        // lblSource menu
        setupMenuLblSource();
        
        // lstStt menu
        setupMenuLstStt();

        // lstLang menu
        setupMenuLstLang();
    }

    private void setupMenuLblSource() {
        MenuItem menuOpenFile = new MenuItem("Open File");
        menuOpenFile.setOnAction(event1 -> {
            selectFileToLoadFrom();
        });
        MenuItem menuClear = new MenuItem("Clear");
        menuClear.setOnAction(event1 -> {
            clearFileToLoadFrom();
        });

        contextMenuLblSource.setOnHidden(event1 -> {
            log("Label 'lblSource' context menu closed.");
        });

        contextMenuLblSource.getItems().add(menuOpenFile);
        contextMenuLblSource.getItems().add(menuClear);
    }

    private void setupMenuLstStt() {
        MenuItem menuLoadFromFile = new MenuItem("Load from file");
        menuLoadFromFile.setOnAction(event1 -> {
            selectFileToLoadFrom();
        });

        MenuItem menuUnload = new MenuItem("UnLoad data");
        menuUnload.setOnAction(event1 -> {
            clearFileToLoadFrom();
        });

        MenuItem menuSortByKey = new MenuItem("Sort by Key");
        menuSortByKey.setOnAction(event1 -> {
            lstSttSortKey = "Key";
            changeSttVisibleList(sttVisibleList);
        });

        MenuItem menuSortByCreated = new MenuItem("Sort by Creation time");
        menuSortByCreated.setOnAction(event1 -> {
            lstSttSortKey = "Created";
            changeSttVisibleList(sttVisibleList);
        });

        MenuItem menuDeleteItem = new MenuItem("Remove Item");
        menuDeleteItem.setOnAction(event1 -> {
            deleteSttChangedItem();
        });

        MenuItem menuDeleteAllItems = new MenuItem("Remove All Items");
        menuDeleteAllItems.setOnAction(event1 -> {
            deleteAllSttChangedItems();
        });

        contextMenuLstStt.setOnShowing(event1 -> {
            // Sort menu items
            log("List 'lstStt' context menu opened. Preselected sort key: " + lstSttSortKey);
            Image imageSelected = new Image(getClass().getResourceAsStream("/images/menu_selected.png"));
            ImageView imageViewSelected = new ImageView(imageSelected);

            double fontSize = contextMenuLstStt.getStyle().contains("-fx-font-size") 
            ? Double.parseDouble(contextMenuLstStt.getStyle().replaceAll("[^\\d.]", ""))
            : 10;

            imageViewSelected.setFitHeight(fontSize);
            imageViewSelected.setPreserveRatio(true);

            if (lstSttSortKey.equals("Key")) {
                menuSortByKey.setGraphic(imageViewSelected);
                menuSortByCreated.setGraphic(null);
            }
            else if (lstSttSortKey.equals("Created")) {
                menuSortByKey.setGraphic(null);
                menuSortByCreated.setGraphic(imageViewSelected);
            }

            // Delete menu items
            if (sttVisibleList.equals("Changed")) {
                menuLoadFromFile.setDisable(true);
                menuUnload.setDisable(true);
                String listItem = lstStt.getSelectionModel().getSelectedItem();
                if (listItem != null) {
                    if (lstStt.getItems().size() > 0) {
                        menuDeleteItem.setDisable(false);
                        menuDeleteItem.setText("Remove Item: " + listItem);
                    }
                    else {
                        menuDeleteItem.setDisable(true);
                        menuDeleteItem.setText("Remove Item");
                    }
                }
                else {
                    menuDeleteItem.setDisable(true);
                    menuDeleteItem.setText("Remove Item");
                }
                
                if (lstStt.getItems().size() > 0) {
                    menuDeleteAllItems.setDisable(false);
                }
                else {
                    menuDeleteAllItems.setDisable(true);
                }
            }
            else {
                menuLoadFromFile.setDisable(false);
                menuUnload.setDisable(false);
                menuDeleteItem.setDisable(true);
                menuDeleteAllItems.setDisable(true);
            }
        });

        contextMenuLstStt.setOnHidden(event1 -> {
            log("List 'lstStt' context menu closed.");
        });

        contextMenuLstStt.getItems().add(menuLoadFromFile);
        contextMenuLstStt.getItems().add(menuUnload);
        contextMenuLstStt.getItems().add(new SeparatorMenuItem());
        contextMenuLstStt.getItems().add(menuSortByKey);
        contextMenuLstStt.getItems().add(menuSortByCreated);
        contextMenuLstStt.getItems().add(new SeparatorMenuItem());
        contextMenuLstStt.getItems().add(menuDeleteItem);
        contextMenuLstStt.getItems().add(menuDeleteAllItems);
    }

    private void setupMenuLstLang() {
        MenuItem menuLoadFromFile = new MenuItem("Load from file");
        menuLoadFromFile.setOnAction(event1 -> {
            selectFileToLoadFrom();
        });

        MenuItem menuUnload = new MenuItem("UnLoad data");
        menuUnload.setOnAction(event1 -> {
            clearFileToLoadFrom();
        });

        MenuItem menuSortByKey = new MenuItem("Sort by Key");
        menuSortByKey.setOnAction(event1 -> {
            lstLangSortKey = "Key";
            changeLangVisibleList(langVisibleList);
        });

        MenuItem menuSortByCreated = new MenuItem("Sort by Creation time");
        menuSortByCreated.setOnAction(event1 -> {
            lstLangSortKey = "Created";
            changeLangVisibleList(langVisibleList);
        });

        MenuItem menuDeleteItem = new MenuItem("Remove Item");
        menuDeleteItem.setOnAction(event1 -> {
            deleteLangChangedItem();
        });

        MenuItem menuDeleteAllItems = new MenuItem("Remove All Items");
        menuDeleteAllItems.setOnAction(event1 -> {
            deleteAllLangChangedItems();
        });

        contextMenuLstLang.setOnShowing(event1 -> {
            // Sort menu items
            log("List 'lstLang' context menu opened. Preselected sort key: " + lstLangSortKey);
            Image imageSelected = new Image(getClass().getResourceAsStream("/images/menu_selected.png"));
            ImageView imageViewSelected = new ImageView(imageSelected);

            double fontSize = contextMenuLstLang.getStyle().contains("-fx-font-size") 
            ? Double.parseDouble(contextMenuLstLang.getStyle().replaceAll("[^\\d.]", ""))
            : 10;

            imageViewSelected.setFitHeight(fontSize);
            imageViewSelected.setPreserveRatio(true);

            if (lstLangSortKey.equals("Key")) {
                menuSortByKey.setGraphic(imageViewSelected);
                menuSortByCreated.setGraphic(null);
            }
            else if (lstLangSortKey.equals("Created")) {
                menuSortByKey.setGraphic(null);
                menuSortByCreated.setGraphic(imageViewSelected);
            }

            // Delete menu items
            if (langVisibleList.equals("Changed")) {
                menuLoadFromFile.setDisable(true);
                menuUnload.setDisable(true);
                String listItem = lstLang.getSelectionModel().getSelectedItem();
                if (listItem != null) {
                    if (lstLang.getItems().size() > 0) {
                        menuDeleteItem.setDisable(false);
                        menuDeleteItem.setText("Remove Item: " + listItem);
                    }
                    else {
                        menuDeleteItem.setDisable(true);
                        menuDeleteItem.setText("Remove Item");
                    }
                }
                else {
                    menuDeleteItem.setDisable(true);
                    menuDeleteItem.setText("Remove Item");
                }
                
                if (lstLang.getItems().size() > 0) {
                    menuDeleteAllItems.setDisable(false);
                }
                else {
                    menuDeleteAllItems.setDisable(true);
                }
            }
            else {
                menuLoadFromFile.setDisable(false);
                menuUnload.setDisable(false);
                menuDeleteItem.setDisable(true);
                menuDeleteAllItems.setDisable(true);
            }
        });

        contextMenuLstLang.setOnHidden(event1 -> {
            log("List 'lstLang' context menu closed.");
        });

        contextMenuLstLang.getItems().add(menuLoadFromFile);
        contextMenuLstLang.getItems().add(menuUnload);
        contextMenuLstLang.getItems().add(new SeparatorMenuItem());
        contextMenuLstLang.getItems().add(menuSortByKey);
        contextMenuLstLang.getItems().add(menuSortByCreated);
        contextMenuLstLang.getItems().add(new SeparatorMenuItem());
        contextMenuLstLang.getItems().add(menuDeleteItem);
        contextMenuLstLang.getItems().add(menuDeleteAllItems);
    }

    private void setupWidgetsText() {
        // Set Text
        lblInfo.setText(APP_TITLE);
        lblSource.setText("Click to load keys from file");

        // Set ToolTips
        setTooltip("Select file (if any) to load keys from", btnLoadFrom);
        setTooltip("Add file to list of files that need to be updated", btnAddFile);
        setTooltip("Remove file from list of files that need to be updated", btnRemoveFile);
        setTooltip("Create new file", btnCreateNew);
        
    }
    
    private void setTooltip(String tooltipText, Button widget) {
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(javafx.scene.text.Font.font(15));
        if (widget instanceof Node) {
            widget.setTooltip(tooltip);
        }
    }

    private void setTooltip(String tooltipText, Label widget) {
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setFont(javafx.scene.text.Font.font(15));
        if (widget instanceof Node) {
            widget.setTooltip(tooltip);
        }
    }

    private void setWidgetIcon(String iconFilePath, Node widget, double iconSize) {
        if (iconFilePath != null && !iconFilePath.isEmpty()) {
            if (iconSize == 0) {
                iconSize = WIDGET_GRAPHIC_SIZE;
            }

            Image image = new Image(getClass().getResourceAsStream(iconFilePath));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

            if (widget instanceof Button) {
                Button button = (Button) widget;
                if (button.getHeight() == 0) {
                    imageView.setFitHeight(iconSize);
                }
                else {
                    imageView.setFitHeight(button.getHeight());
                }
                button.setGraphic(imageView);
            }
            else if (widget instanceof Label) {
                Label label = (Label) widget;
                if (label.getHeight() == 0) {
                    imageView.setFitHeight(iconSize);
                }
                else {
                    imageView.setFitHeight(label.getHeight());
                }
                label.setGraphic(imageView);
            }
        }
        else {
            if (widget instanceof Button) {
                Button button = (Button) widget;
                button.setGraphic(null);
            }
            else if (widget instanceof Label) {
                Label label = (Label) widget;
                label.setGraphic(null);
            }
        }
    }

    private void setWidgetIcon(String iconFilePath, Node widget) {
        setWidgetIcon(iconFilePath, widget, 0);
    }

    // Events

    @FXML
    private void onTabChanged() {
        if (tabStt.isSelected()) {
            activateSection(Section.SETTINGS);
        }
        else if (tabLang.isSelected()) {
            activateSection(Section.LANGUAGE);
        }
        else if (tabManage.isSelected()) {
            activateSection(Section.LANGUAGE_MANAGE);
        }
    }

    @FXML
    private void onLblSourceClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            log("Label 'lblSource' clicked");
            logIndentPlus();
            loadSettingsFrom();
            logIndentMinus();
        }
    }

    @FXML
    private void onLblSourceContextMenu(ContextMenuEvent event) {
        if (contextMenuLblSource.isShowing()) {
            log(LOG_INDENT + "Hiding 'lblSource' context menu, because it will be shown on other location.");
            contextMenuLblSource.hide();
        }

        log("Label 'lblSource' context menu shown: X=" + event.getScreenX() + ", Y=" + event.getScreenY());
        contextMenuLblSource.show(lblSource, event.getScreenX(), event.getScreenY());
    }

    @FXML
    private void onBtnMinimizeClick() {
        chkAutoUpdateFiles.setVisible(false);
        btnAddFile.setVisible(false);
        btnRemoveFile.setVisible(false);
        btnCreateNew.setVisible(false);
    
        // Animation
        Timeline timeline = new Timeline();

        KeyFrame keyFramePrefHeight = new KeyFrame(
            Duration.millis(FILE_LIST_PANE_ANIMATION_DURATION_MS),
            new KeyValue(layoutHBoxFiles.prefHeightProperty(), 0)
        );
        KeyFrame keyFrameMinHeight = new KeyFrame(
            Duration.millis(FILE_LIST_PANE_ANIMATION_DURATION_MS),
            new KeyValue(layoutHBoxFiles.minHeightProperty(), 0)
        );

        timeline.getKeyFrames().add(keyFramePrefHeight);
        timeline.getKeyFrames().add(keyFrameMinHeight);

        timeline.setOnFinished(event -> {
            lstFiles.setVisible(false);
            layoutHBoxFiles.setMinHeight(0);
        });

        timeline.play();
        log("File list pane minimized");
    }

    @FXML
    private void onBtnMaximizeClick() {
        // Animation
        Timeline timeline = new Timeline();

        KeyFrame keyFramePrefHeight = new KeyFrame(
            Duration.millis(FILE_LIST_PANE_ANIMATION_DURATION_MS),
            new KeyValue(layoutHBoxFiles.prefHeightProperty(), FILE_LIST_PANE_HEIGHT)
        );
        KeyFrame keyFrameMinHeight = new KeyFrame(
            Duration.millis(FILE_LIST_PANE_ANIMATION_DURATION_MS),
            new KeyValue(layoutHBoxFiles.minHeightProperty(), FILE_LIST_PANE_HEIGHT)
        );

        timeline.getKeyFrames().add(keyFramePrefHeight);
        timeline.getKeyFrames().add(keyFrameMinHeight);

        // Set visibility after animation is complete
        timeline.setOnFinished(event -> {
            chkAutoUpdateFiles.setVisible(true);
            btnAddFile.setVisible(true);
            btnRemoveFile.setVisible(true);
            btnCreateNew.setVisible(true);
            layoutHBoxFiles.setMinHeight(FILE_LIST_PANE_HEIGHT);
        });

        lstFiles.setVisible(true);

        timeline.play();
        log("File list pane maximized");
    }

    @FXML
    private void onLblInfoClick(MouseEvent event) {
        event.consume();
        if (isMessageDetailsShown) {
            hideMessageDetails();
        }
        else {
            showMessageDetails();
        }
    }

    @FXML
    private void onChkSaveStateAction() {
        appState.setPyDictValue("chkSaveState", chkSaveState.isSelected());
        if (chkSaveState.isSelected()) {
            log("AppState enabled");
        }
        else {
            log("AppState disabled");
        }
    }

    @FXML
    private void onBtnLoadFromAction() {
        loadSettingsFrom();
    }

    private void loadSettingsFrom() {
        boolean isSelected = selectFileToLoadFrom();

        if (!isSelected) {
            return;
        }

        if (!chkAutoUpdateFiles.isSelected()) {
            return;
        }

        // Check if file exists
        File file = new File(lblSource.getText());
        if (!file.exists()) {
            return;
        }

        String selectedFile = lblSource.getText();

        if (activeSection == Section.SETTINGS) {
            if (updateSttFilesPaths.contains(selectedFile)) {
                log("Selected source setting file already exists in update files list");
            }
            else {
                updateSttFilesPaths.add(selectedFile);
                log("File added to list of settings files to be updated: " + selectedFile);
                populateList(lstFiles, updateSttFilesPaths);
            }
        }
        else if (activeSection == Section.LANGUAGE) {
            if (updateLangFilesPaths.contains(selectedFile)) {
                log("Selected source language file already exists in update files list.");
            }
            else {
                updateLangFilesPaths.add(selectedFile);
                log("File added to list of language files to be updated: " + selectedFile);
                populateList(lstFiles, updateLangFilesPaths);
            }
        }
        sttVisibleList = "Loaded";
        changeSttVisibleList(sttVisibleList);
    }

    @FXML
    private void onBtnMngLoad() {
        loadManagedLanguageFile();
    }

    @FXML
    private void onLblMngFileShortClick() {
        loadManagedLanguageFile();
    }

    private void loadManagedLanguageFile() {
        log("Started FileDialog for selecting file to load in Settings Manager...");
        logIndentPlus();

        File selectedFile = getFileDialogSelectedFile();

        if (selectedFile != null) {
            PyDict loadedData = getLanguageFileContent(selectedFile.getAbsolutePath());
            if (loadedData != null) {
                setLangFileToScrollPaneManage(selectedFile.getAbsolutePath());
            }
            else {
                log("Failed to load language file: " + selectedFile.getAbsolutePath());
            }
        }
        else {
            log("No file selected");
        }

        logIndentMinus();
    }

    @FXML
    private void onBtnMngCommitClick() {
        lblMngInfo.setText("Performing actions ... please wait !");
        lblMngInfo.setVisible(true);
        lblMngInfo.setManaged(true);

        Task<String> executeActions = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return scrollPaneManageContent.executeAllActions();
            }
        };

        executeActions.setOnSucceeded(event -> {
            String result = executeActions.getValue();
            boolean hasActions = scrollPaneManageContent.hasActions();
            if (result.startsWith("ERROR")) {
                lblMngInfo.setText("Done with errors.");
                msgBoxInfoCritical("Language Manager", "Actions performed on language file:\n" + scrollPaneManageContent.getLanguageFileName(), result);
                setLangFileToScrollPaneManage(scrollPaneManageContent.getLanguageFileName());
            }
            else {
                lblMngInfo.setText("Done.");
                msgBoxInfo("Language Manager", "Actions performed on language file:\n" + scrollPaneManageContent.getLanguageFileName(), result);
                setLangFileToScrollPaneManage(scrollPaneManageContent.getLanguageFileName());
            }
            lblMngInfo.setVisible(false);
            lblMngInfo.setManaged(false);

            if (scrollPaneManageContent.getLanguageFileName().equals(loadLangFromPath) && hasActions) {
                if (chkAutoUpdateFiles.isSelected()) {
                    msgBoxInfo("Language Manager", "Reloading Language Section file", "You have made some changes to Language file that has been loaded in LANGUAGE SECTIOn.\nFile in LANGUAGE SECTION will be reloaded.");
                    reloadLanguageFile(scrollPaneManageContent.getLanguageFileName());
                }
                else {
                    boolean canReloadFile = msgBoxInfoQuestion("Language Manager", "Strongly recommended", "You have made some changes to Language file that has been loaded in LANGUAGE SECTIOn.\nPlease reload currently loaded language file in LANGUAGE SECTION.\n\nDo you want to reload file now ?");
                    if (canReloadFile) {
                        reloadLanguageFile(scrollPaneManageContent.getLanguageFileName());
                    }
                }
            }

        });

        executeActions.setOnFailed(event -> {
            lblMngInfo.setText("Failed.");
            String result = executeActions.getException().getMessage();
            msgBoxInfoCritical("Language Manager", "Actions performed on language file:\n" + scrollPaneManageContent.getLanguageFileName(), result);
            scrollPaneManageContent.setLanguageFileName(scrollPaneManageContent.getLanguageFileName());
            lblMngInfo.setVisible(false);
            lblMngInfo.setManaged(false);
        });

        new Thread(executeActions).start();
    }

    private void reloadLanguageFile(String filePath) {
        log("Reloading language file: " + filePath);
        logIndentPlus();
        
        // Check if file exists
        File file = new File(filePath);
        if (!file.exists()) {
            log("Reloading failed: File does not exist");
            logIndentMinus();
            return;
        }
        
        // Get file content
        PyDict loadedData = getLanguageFileContent(filePath);
        if (loadedData == null) {
            log("Failed to reload language file: No Data !");
            logIndentMinus();
            return;
        }

        // Load data to langLoadedMap
        List<LanguageItemGroup> listOfItems = LanguageItemGroup.getListOfGroupLanguageObjectsFromLanguageMapObject(loadedData);
        langLoadedMap.clear();
        for (LanguageItemGroup itemGroup : listOfItems) {
            langLoadedMap.put(itemGroup.getGroupKey(), itemGroup);
        }

        // Refresh Visible List
        changeLangVisibleList(langVisibleList);
        
        // Refresh variables
        loadLangFromPath = filePath;
        lblSource.setText(loadLangFromPath);
        
        // Finish
        log("File reloaded.");
        logIndentMinus();
    }

    private void setLangFileToScrollPaneManage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            scrollPaneManageContent.setLanguageFileName("");
            lblMngFileLong.setText("");
            lblMngFileShort.setText("NONE");
            return;
        }
        File file = new File(filePath);
        scrollPaneManageContent.setLanguageFileName(file.getAbsolutePath());
        lblMngFileLong.setText(file.getAbsolutePath());
        lblMngFileShort.setText(file.getName());
    }

    @FXML
    private void onBtnMngClear() {
        setLangFileToScrollPaneManage("");
    }

    @FXML
    private void onBtnAddFileAction() {
        log("Started FileDialog for adding file that should be updated...");
        logIndentPlus();

        File selectedFile = getFileDialogSelectedFile();

        MsgInfo msgInfoSuccess = new MsgInfo("fileAdded", "File added to list", "File added to list of files to be updated", MsgInfo.MsgStyle.INFORMATION);
        MsgInfo msgInfoFileExists = new MsgInfo("fileExists", "File already in list", "File already exists in list of files to be updated", MsgInfo.MsgStyle.WARNING);

        if (selectedFile != null) {
            if (activeSection == Section.SETTINGS) {
                if (updateSttFilesPaths.contains(selectedFile.getAbsolutePath())) {
                    log("Selected file for settings to be updated already added: " + selectedFile.getAbsolutePath());
                    showMessage(msgInfoFileExists);
                }
                else {
                    updateSttFilesPaths.add(selectedFile.getAbsolutePath());
                    log("File added to list of settings files to be updated: " + selectedFile.getAbsolutePath());
                    populateList(lstFiles, updateSttFilesPaths);
                    showMessage(msgInfoSuccess);
                }
            }
            else if (activeSection == Section.LANGUAGE) {
                if (updateLangFilesPaths.contains(selectedFile.getAbsolutePath())) {
                    log("Selected file for languages to be updated already added: " + selectedFile.getAbsolutePath());
                    showMessage(msgInfoFileExists);
                }
                else {
                    updateLangFilesPaths.add(selectedFile.getAbsolutePath());
                    log("File added to list of language files to be updated: " + selectedFile.getAbsolutePath());
                    populateList(lstFiles, updateLangFilesPaths);
                    scrollPaneContent.setFileAffected(updateLangFilesPaths);
                    showMessage(msgInfoSuccess);
                }
            }
        }
        else {
            log("No file was selected");
        }

        logIndentMinus();
    }

    @FXML
    private void onBtnRemoveFile() {
        log("Started FileDialog for removing file that should be updated...");
        logIndentPlus();

        ObservableList<String> selectedItems = lstFiles.getSelectionModel().getSelectedItems();
        if (selectedItems.size() > 0) {
            if (activeSection == Section.SETTINGS) {
                log("Removing files:");
                logIndentPlus();
                for (String selectedItem : selectedItems) {
                    updateSttFilesPaths.remove(selectedItem);
                    log("File: " + selectedItem);
                }

                logIndentMinus();
                if (selectedItems.size() == 1) {
                    MsgInfo msgInfoSuccess = new MsgInfo("fileRemoved", "File removed from list", "File removed from list of files to be updated\n" + selectedItems.get(0), MsgInfo.MsgStyle.INFORMATION);
                    showMessage(msgInfoSuccess);
                }
                else {
                    MsgInfo msgInfoSuccess = new MsgInfo("fileRemoved", selectedItems.size() + " files removed from list", "Files removed from list of files to be updated" + selectedItems.toString().replace("[", "\n->    ").replace("]", "").replace(", ", "\n->    "), MsgInfo.MsgStyle.INFORMATION);
                    showMessage(msgInfoSuccess);
                }
                populateList(lstFiles, updateSttFilesPaths);
            }
            else if (activeSection == Section.LANGUAGE) {
                log("Removing files:");
                logIndentPlus();
                for (String selectedItem : selectedItems) {
                    updateLangFilesPaths.remove(selectedItem);
                    log("File: " + selectedItem);
                }

                scrollPaneContent.setFileAffected(updateLangFilesPaths);

                logIndentMinus();
                if (selectedItems.size() == 1) {
                    MsgInfo msgInfoSuccess = new MsgInfo("fileRemoved", "File removed from list", "File removed from list of files to be updated\n" + selectedItems.get(0), MsgInfo.MsgStyle.INFORMATION);
                    showMessage(msgInfoSuccess);
                }
                else {
                    MsgInfo msgInfoSuccess = new MsgInfo("fileRemoved", selectedItems.size() + " files removed from list", "Files removed from list of files to be updated" + selectedItems.toString().replace("[", "\n->    ").replace("]", "").replace(", ", "\n->    "), MsgInfo.MsgStyle.INFORMATION);
                    showMessage(msgInfoSuccess);
                }
                populateList(lstFiles, updateLangFilesPaths);
            }
        }
        else {
            MsgInfo msgInfoInfo = new MsgInfo("noFileSelected", "Select file(s) to remove", "No file(s) selected", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoInfo);
            log("No file was selected");
        }

        logIndentMinus();
    }

    @FXML
    private void onBtnCreateNew() {
        log("Started FileDialog for creating new file...");
        logIndentPlus();
        File selectedFile = getFileDialogSaveFile();

        if (selectedFile == null) {
            log("No file was selected");
            logIndentMinus();
            return;
        }

        Settings settings = new Settings();
        if (activeSection == Section.SETTINGS) {
            settings.createNewSettingsFile(selectedFile);
        }
        else if (activeSection == Section.LANGUAGE) {
            settings.createNewLanguagesFile(selectedFile);
        }

        MsgInfo msgInfoSuccess = new MsgInfo("fileAdded", "File created and added to list", "File created and added to list of files to be updated", MsgInfo.MsgStyle.INFORMATION);
        MsgInfo msgInfoFileExists = new MsgInfo("fileExists", "File already in list", "File already exists in list of files to be updated", MsgInfo.MsgStyle.WARNING);

        if (activeSection == Section.SETTINGS) {
            if (updateSttFilesPaths.contains(selectedFile.getAbsolutePath())) {
                log("Selected file for settings to be updated already added: " + selectedFile.getAbsolutePath());
                showMessage(msgInfoFileExists);
            }
            else {
                updateSttFilesPaths.add(selectedFile.getAbsolutePath());
                log("File created and added to list of settings files to be updated: " + selectedFile.getAbsolutePath());
                populateList(lstFiles, updateSttFilesPaths);
                showMessage(msgInfoSuccess);
            }
        }
        else if (activeSection == Section.LANGUAGE) {
            if (updateLangFilesPaths.contains(selectedFile.getAbsolutePath())) {
                log("Selected file for languages to be updated already added: " + selectedFile.getAbsolutePath());
                showMessage(msgInfoFileExists);
            }
            else {
                updateLangFilesPaths.add(selectedFile.getAbsolutePath());
                log("File created and added to list of language files to be updated: " + selectedFile.getAbsolutePath());
                populateList(lstFiles, updateLangFilesPaths);
                scrollPaneContent.setFileAffected(updateLangFilesPaths);
                showMessage(msgInfoSuccess);
            }
        }
    }

    @FXML
    private void onBtnMngNew() {
        log("Started FileDialog for creating new file...");
        logIndentPlus();
        File selectedFile = getFileDialogSaveFile();

        if (selectedFile == null) {
            log("No file was selected");
            logIndentMinus();
            return;
        }

        Settings settings = new Settings();
        settings.createNewLanguagesFile(selectedFile);

        log("New language file created: " + selectedFile.getAbsolutePath());

        setLangFileToScrollPaneManage(selectedFile.getAbsolutePath());
    }

    @FXML
    private void onLstFilesScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            if (event.getDeltaY() > 0) {
                fontSizeLstFiles = Math.min(FONT_SIZE_MAX_LIST, fontSizeLstFiles + 1);
                lstFiles.setStyle("-fx-font-size: " + fontSizeLstFiles + "px;");
                log("Font increased to: " + fontSizeLstFiles + "px, (Max value: " + FONT_SIZE_MAX_LIST + ")");
                showToolTipLabel("Font size (+) : " + fontSizeLstFiles + "px", event);
            }
            else if (event.getDeltaY() < 0) {
                fontSizeLstFiles = Math.max(FONT_SIZE_MIN_LIST, fontSizeLstFiles - 1);
                lstFiles.setStyle("-fx-font-size: " + fontSizeLstFiles + "px;");
                log("Font decreased to: " + fontSizeLstFiles + "px, (Min value: " + FONT_SIZE_MIN_LIST + ")");
                showToolTipLabel("Font size (-) : " + fontSizeLstFiles + "px", event);
            }
            
        }
    }

    @FXML
    private void onLstSttScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            if (event.getDeltaY() > 0) {
                fontSizeLstStt = Math.min(FONT_SIZE_MAX_LIST, fontSizeLstStt + 1);
                lstStt.setStyle("-fx-font-size: " + fontSizeLstStt + "px;");
                log("Font increased to: " + fontSizeLstStt + "px, (Max value: " + FONT_SIZE_MAX_LIST + ")");
                showToolTipLabel("Font size (+) : " + fontSizeLstStt + "px", event);
                imgListItemChanged.setFitHeight(fontSizeLstStt);
                imgListItemDeleted.setFitHeight(fontSizeLstStt);
            }
            else if (event.getDeltaY() < 0) {
                fontSizeLstStt = Math.max(FONT_SIZE_MIN_LIST, fontSizeLstStt - 1);
                lstStt.setStyle("-fx-font-size: " + fontSizeLstStt + "px;");
                log("Font decreased to: " + fontSizeLstStt + "px, (Min value: " + FONT_SIZE_MIN_LIST + ")");
                showToolTipLabel("Font size (-) : " + fontSizeLstStt + "px", event);
                imgListItemChanged.setFitHeight(fontSizeLstStt);
                imgListItemDeleted.setFitHeight(fontSizeLstStt);
            }
        }
    }

    @FXML
    private void onLstLangScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            if (event.getDeltaY() > 0) {
                fontSizeLstLang = Math.min(FONT_SIZE_MAX_LIST, fontSizeLstLang + 1);
                lstLang.setStyle("-fx-font-size: " + fontSizeLstLang + "px;");
                log("Font increased to: " + fontSizeLstLang + "px, (Max value: " + FONT_SIZE_MAX_LIST + ")");
                showToolTipLabel("Font size (+) : " + fontSizeLstLang + "px", event);
                imgListItemChanged.setFitHeight(fontSizeLstLang);
                imgListItemDeleted.setFitHeight(fontSizeLstLang);
            }
            else if (event.getDeltaY() < 0) {
                fontSizeLstLang = Math.max(FONT_SIZE_MIN_LIST, fontSizeLstLang - 1);
                lstLang.setStyle("-fx-font-size: " + fontSizeLstLang + "px;");
                log("Font decreased to: " + fontSizeLstLang + "px, (Min value: " + FONT_SIZE_MIN_LIST + ")");
                showToolTipLabel("Font size (-) : " + fontSizeLstLang + "px", event);
                imgListItemChanged.setFitHeight(fontSizeLstLang);
                imgListItemDeleted.setFitHeight(fontSizeLstLang);
            }
        }
    }

    @FXML
    private void onLstSttContextMenu(ContextMenuEvent event) {
        showLstSttContextMenu(event);
    }

    @FXML
    private void onLstLangContextMenu(ContextMenuEvent event) {
        showLstLangContextMenu(event);
    }

    private void showLstSttContextMenu(ContextMenuEvent event) {
        if (contextMenuLstStt.isShowing()) {
            log(LOG_INDENT + "Hiding 'lstStt' context menu, because it will be shown on other location.");
            contextMenuLstStt.hide();
        }

        log("Label 'lstStt' context menu shown: X=" + event.getScreenX() + ", Y=" + event.getScreenY());
        contextMenuLstStt.show(lblSource, event.getScreenX(), event.getScreenY());
    }

    private void showLstLangContextMenu(ContextMenuEvent event) {
        if (contextMenuLstLang.isShowing()) {
            log(LOG_INDENT + "Hiding 'lstLang' context menu, because it will be shown on other location.");
            contextMenuLstLang.hide();
        }

        log("Label 'lstLang' context menu shown: X=" + event.getScreenX() + ", Y=" + event.getScreenY());
        contextMenuLstLang.show(lblSource, event.getScreenX(), event.getScreenY());
    }

    @FXML
    private void onChkAutoUpdateFilesAction() {
        if (chkAutoUpdateFiles.isSelected()) {
            MsgInfo msgInfoSuccess = new MsgInfo("autoUpdateFiles", "Auto update files is enabled", "When you save changes all files in list will be automatically updated", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto update files is enabled");
        }
        else {
            MsgInfo msgInfoSuccess = new MsgInfo("autoUpdateFiles", "Auto update files is disabled", "You will be prompted to confirm each file update when you save changes", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto update files is disabled");
        }
    }

    @FXML
    private void onChkAutoDataTypeAction() {
        if (chkAutoDataType.isSelected()) {
            MsgInfo msgInfoSuccess = new MsgInfo("autoDataType", "Auto data type is enabled", "Data type will be automatically detected", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto data type is enabled");
            ArrayList<String> availableDataTypes = getAvailableDataTypes(txtSttValue.getText());
            updateLblSttInfo(availableDataTypes);
            setAutoDataType(availableDataTypes);
        }
        else {
            MsgInfo msgInfoSuccess = new MsgInfo("autoDataType", "Auto data type is disabled", "You must manually select data type", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto data type is disabled");
        }
    }

    @FXML
    private void onBtnSttShowLoadedClick() {
        changeSttVisibleList("Loaded");
    }

    @FXML
    private void onBtnLangShowLoadedClick() {
        changeLangVisibleList("Loaded");
    }

    @FXML
    private void onBtnSttShowLoadedContextMenu(ContextMenuEvent event) {
        showLstSttContextMenu(event);
    }

    @FXML
    private void onBtnLangShowLoadedContextMenu(ContextMenuEvent event) {
        showLstLangContextMenu(event);
    }

    @FXML
    private void onBtnSttShowChangedClick() {
        changeSttVisibleList("Changed");
    }

    @FXML
    private void onBtnLangShowChangedClick() {
        changeLangVisibleList("Changed");
    }

    @FXML
    private void onBtnSttShowChangedContextMenu(ContextMenuEvent event) {
        showLstSttContextMenu(event);
    }

    @FXML
    private void onBtnLangShowChangedContextMenu(ContextMenuEvent event) {
        showLstLangContextMenu(event);
    }

    @FXML
    private void onBtnSttFilterClearAction() {
        txtSttSearch.setText("");
        log("Filter cleared for lstStt");
    }

    @FXML
    private void onBtnLangFilterClearAction() {
        txtLangSearch.setText("");
        log("Filter cleared for lstLang");
    }

    @FXML
    private void onChkSttAutoStripAction() {
        if (chkSttAutoStrip.isSelected()) {
            MsgInfo msgInfoSuccess = new MsgInfo("autoStripSettings", "Auto strip Settings value is enabled", "Settings value will be automatically stripped", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto strip settings value is enabled");
        }
        else {
            MsgInfo msgInfoSuccess = new MsgInfo("autoStripSettings", "Auto strip Settings value is disabled", "Settings value will not be automatically stripped", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto strip settings value is disabled");
        }
    }

    @FXML
    private void onChkSttAutoDefaultAction() {
        if (chkSttAutoDefault.isSelected()) {
            MsgInfo msgInfoSuccess = new MsgInfo("autoDefault", "Auto default value is enabled", "Default value will be automatically set", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto default value is enabled");
        }
        else {
            MsgInfo msgInfoSuccess = new MsgInfo("autoDefault", "Auto default value is disabled", "Default value will not be automatically set", MsgInfo.MsgStyle.INFORMATION);
            showMessage(msgInfoSuccess);
            log("Auto default value is disabled");
        }
    }

    @FXML
    private void onBtnSttAddClick() {
        addNewSettingsItem();
    }

    @FXML
    private void onBtnLangAddClick() {
        addNewLanguageItem();
    }

    @FXML
    private void onBtnSttUpdateClick() {
        updateSettingsItem();
    }

    @FXML
    private void onBtnLangUpdateClick() {
        updateLanguageItem();
    }

    @FXML
    private void onBtnSttDeleteClick() {
        deleteSettingsItem();
    }

    @FXML
    private void onBtnLangDeleteClick() {
        deleteLanguageItem();
    }

    @FXML
    private void onBtnSttDiscardClick() {
        boolean isExists = isSttItemExists(txtSttKey.getText());
        boolean isChanged = sttChangedList.contains(txtSttKey.getText());
        boolean isDeleted = isSttItemDeleted(txtSttKey.getText());
        if (isExists) {
            if (isDeleted) {
                unDeleteSettingsItem();
                return;
            }
            else if (isChanged) {
                unChangeSettingsItem();
                return;
            }
            log("Error in UNDO Settings item '" + txtSttKey.getText() + "'. Item is not deleted or changed");
            MsgInfo msg = new MsgInfo("Error", "Item not deleted or changed", "Error in UNDO Settings item '" + txtSttKey.getText() + "', Item is not deleted or changed.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
        }
        else {
            log("Error in UNDO Settings item '" + txtSttKey.getText() + "'. Item doesn't exist");
            MsgInfo msg = new MsgInfo("Error", "Item doesn't exist", "Error in UNDO Settings item '" + txtSttKey.getText() + "', Item doesn't exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
        }
    }

    @FXML
    private void onBtnLangDiscardClick() {
        boolean isExists = isLangItemExists(txtLangKey.getText());
        boolean isChanged = langChangedList.contains(txtLangKey.getText());
        boolean isDeleted = isLangItemDeleted(txtLangKey.getText());
        if (isExists) {
            if (isDeleted) {
                unDeleteLanguageItem();
                return;
            }
            else if (isChanged) {
                unChangeLanguageItem();
                return;
            }
            log("Error in UNDO Language item '" + txtLangKey.getText() + "'. Item is not deleted or changed");
            MsgInfo msg = new MsgInfo("Error", "Item not deleted or changed", "Error in UNDO Language item '" + txtLangKey.getText() + "', Item is not deleted or changed.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
        }
        else {
            log("Error in UNDO Language item '" + txtLangKey.getText() + "'. Item doesn't exist");
            MsgInfo msg = new MsgInfo("Error", "Item doesn't exist", "Error in UNDO Language item '" + txtLangKey.getText() + "', Item doesn't exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
        }
    }

    @FXML
    private void onBtnSaveSttClick() {
        openSaveDialog(SaveSection.SETTINGS);
    }

    @FXML
    private void onBtnSaveLangClick() {
        openSaveDialog(SaveSection.LANGUAGE);
    }

    @FXML
    private void onBtnSaveAllClick() {
        openSaveDialog(SaveSection.ALL);
    }

    private void openSaveDialog(SaveSection type) {
        try {
            log("Starting save dialog...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SaveDialog.fxml"));
            Parent saveDialogRoot = loader.load();

            // Get Controller and set appState data to it
            createAppState();
            appState.setPyDictValue(PyDict.concatKeys(Section.SETTINGS.toString(), "sttChangedMap"), sttChangedMap);
            appState.setPyDictValue(PyDict.concatKeys(Section.LANGUAGE.toString(), "langChangedMap"), langChangedMap);

            SaveDialogController saveDialogController = loader.getController();
            saveDialogController.setPrimaryStage(primaryStage);
            saveDialogController.setAppState(appState);
            saveDialogController.setTypeOfDataToBeSaved(type);

            Stage saveDialogStage = new Stage();
            saveDialogStage.setTitle("Save " + type + " to file(s)...");
            saveDialogStage.setScene(new Scene(saveDialogRoot));

            saveDialogStage.initModality(Modality.WINDOW_MODAL);
            saveDialogStage.initOwner(primaryStage);

            saveDialogStage.showAndWait();
            log("Save dialog closed");
            if (type == SaveSection.LANGUAGE || type == SaveSection.ALL) {
                scrollPaneManageContent.setLanguageFileName(scrollPaneManageContent.getLanguageFileName());
                logIndentPlus();
                log("Manage Language content updated");
                logIndentMinus();
            }
        }
        catch (IOException e) {
            log("Opening save dialog failed:");
            logIndentPlus();
            log("IOException: " + e.getMessage());
            logIndentMinus();
            e.printStackTrace();
        }
    }

    private void clearFileToLoadFrom() {
        if (activeSection == Section.SETTINGS) {
            log("Clearing loaded settings:");
            logIndentPlus();
            loadSttFromPath = "";
            log("'lblSource' cleared");
            lstStt.getItems().clear();
            log("List of settings cleared");
            sttLoadedMap.clear();
            log("sttLoadedMap cleared");
            txtSttSearch.setText("");
            changeSttVisibleList(sttVisibleList);
            log("Clear filter and update list");
            logIndentMinus();

            if (contextMenuLblSource.isShowing()) {
                log(LOG_INDENT + "'lblSource' cleared, working with empty settings key list");
            }
            else {
                log("'lblSource' cleared, working with empty settings key list");
            }
            
        }
        else if (activeSection == Section.LANGUAGE) {
            log("Clearing loaded languages:");
            logIndentPlus();
            loadLangFromPath = "";
            log("'lblSource' cleared");
            lstLang.getItems().clear();
            log("List of languages cleared");
            langLoadedMap.clear();
            log("langLoadedMap cleared");
            txtLangSearch.setText("");
            changeLangVisibleList(langVisibleList);
            log("Clear filter and update list");
            logIndentMinus();

            if (contextMenuLblSource.isShowing()) {
                log(LOG_INDENT + "'lblSource' cleared, working with empty language key list");
            }
            else {
                log("'lblSource' cleared, working with empty language key list");
            }
        }

        lblSource.setText("Click to load keys from file...");
    }

    private boolean selectFileToLoadFrom() {
        log("Started FileDialog for selecting file to load Settings / Languages from...");
        logIndentPlus();

        File selectedFile = getFileDialogSelectedFile();

        MsgInfo msgInfoSuccess = new MsgInfo("fileSelected", "File selected", "File selected for loading Settings / Languages keys", MsgInfo.MsgStyle.INFORMATION, MsgInfo.ErrorCode.NONE, 2, 2, true);
        
        if (selectedFile != null) {
            if (activeSection == Section.SETTINGS) {
                PyDict loadedData = getSettingsFileContent(selectedFile.getAbsolutePath());

                if (loadedData == null) {
                    lblSource.setText("Error loading settings from file: " + selectedFile.getAbsolutePath());
                    log("Error loading settings from file: " + selectedFile.getAbsolutePath());
                    logIndentPlus();
                    loadSttFromPath = "";
                    log("'lblSource' cleared");
                    sttLoadedMap.clear();
                    log("sttLoadedMap cleared");
                    changeSttVisibleList(sttVisibleList);
                    log("List of settings cleared");
                    logIndentMinus();
                    logIndentMinus();
                    return false;
                }
                else {
                    sttLoadedMap = loadedData;
                    changeSttVisibleList(sttVisibleList);
                    showToolTipLabel("List Updated", lstStt.localToScene(0, 0).getX(), lstStt.localToScene(0, 0).getY(), 1);
                    
                    loadSttFromPath = selectedFile.getAbsolutePath();
                    log("Selected file for settings to be loaded from: " + selectedFile.getAbsolutePath());
                    log("List of settings updated from: " + selectedFile.getAbsolutePath());
                }
            }
            else if (activeSection == Section.LANGUAGE) {
                PyDict loadedData = getLanguageFileContent(selectedFile.getAbsolutePath());

                if (loadedData == null) {
                    lblSource.setText("Error loading languages from file: " + selectedFile.getAbsolutePath());
                    log("Error loading languages from file: " + selectedFile.getAbsolutePath());
                    logIndentPlus();
                    loadLangFromPath = "";
                    log("'lblSource' cleared");
                    langLoadedMap.clear();
                    log("langLoadedMap cleared");
                    changeLangVisibleList(langVisibleList);
                    log("List of languages cleared");
                    logIndentMinus();
                    logIndentMinus();
                    return false;
                }
                else {
                    List<LanguageItemGroup> listOfItems = LanguageItemGroup.getListOfGroupLanguageObjectsFromLanguageMapObject(loadedData);
                    langLoadedMap.clear();
                    for (LanguageItemGroup itemGroup : listOfItems) {
                        langLoadedMap.put(itemGroup.getGroupKey(), itemGroup);
                    }

                    changeLangVisibleList(langVisibleList);
                    showToolTipLabel("List Updated", lstLang.localToScene(0, 0).getX(), lstLang.localToScene(0, 0).getY(), 1);
                    
                    loadLangFromPath = selectedFile.getAbsolutePath();
                    log("Selected file for languages to be loaded from: " + selectedFile.getAbsolutePath());
                    log("List of languages updated from: " + selectedFile.getAbsolutePath());
                }

            }

            lblSource.setText(selectedFile.getAbsolutePath());
            showMessage(msgInfoSuccess);
        }
        else {
            log("No file was selected");
            logIndentMinus();
            return false;
        }

        logIndentMinus();
        return true;
    }        

    private PyDict getSettingsFileContent(String file) {
        removeMessage("Load Settings Error");
        try {
            Settings settings = new Settings();
            settings.defaultSettingsFilePath = file;
            PyDict data = (PyDict) settings.getAllDefaultSettingsData();
            return data;
        }
        catch (Exception e) {
            log("Error in 'getSettingsFileContent': " + e.getMessage());
            MsgInfo msgInfoError = new MsgInfo("Load Settings Error", "Error loading Settings file", "Error in 'getSettingsFileContent'\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 8, -1, false);
            showMessage(msgInfoError);
            // Show ToolTip Label
            showToolTipLabel("Unable to load Settings file\nFile is not valid Settings file", lstStt.localToScene(0, 0).getX(), lstStt.localToScene(0, 0).getY(), 5);
            return null;
        }
    }

    private PyDict getLanguageFileContent(String file) {
        removeMessage("Load Languages Error");
        try {
            Settings settings = new Settings();
            settings.languagesFilePath = file;
            settings.load(false, true, false);
            if (!settings.getLastErrorString().isEmpty()) {
                log("Language file is invalid: " + file + " - " + settings.getLastErrorString());
                MsgInfo msg = new MsgInfo("Error loading language file", "Invalid Language file", "Language file is invalid: " + file + "\n" + settings.getLastErrorString(), MsgInfo.MsgStyle.ERROR);
                showMessage(msg);
                return null;
            }
            PyDict data = (PyDict) settings.getAllLanguagesData();
            return data;
        }
        catch (Exception e) {
            log("Error in 'getLanguageFileContent': " + e.getMessage());
            MsgInfo msgInfoError = new MsgInfo("Load Languages Error", "Error loading Languages file", "Error in 'getLanguageFileContent'\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 8, -1, false);
            showMessage(msgInfoError);
            // Show ToolTip Label
            showToolTipLabel("Unable to load Languages file\nFile is not valid Languages file", lstLang.localToScene(0, 0).getX(), lstLang.localToScene(0, 0).getY(), 5);
            return null;
        }
    }

    private File getFileDialogSelectedFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        if (activeSection == Section.SETTINGS) {
            fileChooser.setTitle("Select Settings File");
        }
        else if (activeSection == Section.LANGUAGE) {
            fileChooser.setTitle("Select Language File");
        }

        String lastDir = appState.getPyDictValue("lastDir");
        if (lastDir != null && !lastDir.isEmpty() && new File(lastDir).exists()) {
            fileChooser.setInitialDirectory(new File(lastDir));
        }

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            appState.setPyDictValue("lastDir", selectedFile.getParent());
        }

        return selectedFile;
    }

    private File getFileDialogSaveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        if (activeSection == Section.SETTINGS) {
            fileChooser.setTitle("Create new Settings File");
        }
        else if (activeSection == Section.LANGUAGE) {
            fileChooser.setTitle("Create new Language File");
        }

        String lastDir = appState.getPyDictValue("lastDir");
        if (lastDir != null && !lastDir.isEmpty() && new File(lastDir).exists()) {
            fileChooser.setInitialDirectory(new File(lastDir));
        }

        File selectedFile = fileChooser.showSaveDialog(primaryStage);

        if (selectedFile != null) {
            appState.setPyDictValue("lastDir", selectedFile.getParent());
        }

        return selectedFile;
    }
    
    private void showToolTipLabel(String tooltipText, ScrollEvent event) {
        // Calculate the position of the tooltip label based on the mouse position
        double scenePosX = event.getSceneX();
        double scenePosY = event.getSceneY();
        double posX = layoutAnchorPaneMain.sceneToLocal(scenePosX, scenePosY).getX();
        double posY = layoutAnchorPaneMain.sceneToLocal(scenePosX, scenePosY).getY();
        showToolTipLabel(tooltipText, posX, posY);
    }

    private void showToolTipLabel(String tooltipText, double posX, double posY) {
        showToolTipLabel(tooltipText, posX, posY, 1.5);
    }

    private void showToolTipLabel(String tooltipText, double posX, double posY, double duration) {
        lblToolTip.setText(tooltipText);
        
        lblToolTip.setVisible(true);
        lblToolTip.setOpacity(1.0);
        lblToolTip.setLayoutX(posX);
        lblToolTip.setLayoutY(posY + 10);
        lblToolTip.setPrefHeight(Region.USE_COMPUTED_SIZE);
        lblToolTip.setPrefWidth(Region.USE_COMPUTED_SIZE);

        PauseTransition pauseRemovingLabel = new PauseTransition(Duration.seconds(duration));
        pauseRemovingLabel.setOnFinished(event -> {
            removeToolTipLabel();
        });
        pauseRemovingLabel.play();

    }

    private void removeToolTipLabel() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), lblToolTip);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> lblToolTip.setVisible(false));
        fadeOut.play();

    }

    private void msgBoxInfoCritical(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private void msgBoxInfo(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private boolean msgBoxInfoQuestion(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(primaryStage);

        ButtonType yesButton = ButtonType.YES;
        ButtonType noButton = ButtonType.NO;
        ButtonType cancelButton = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        // Set button noButton as default button
        ((Button) alert.getDialogPane().lookupButton(yesButton)).setDefaultButton(false);
        ((Button) alert.getDialogPane().lookupButton(noButton)).setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            return true;
        }
        else {
            return false;
        }
    }

    // SETTINGS METHODS

    private void deleteSttChangedItem() {
        String item = lstStt.getSelectionModel().getSelectedItem().toString();
        if (item != null) {
            int index = lstStt.getSelectionModel().getSelectedIndex();
            String name = lstStt.getItems().get(index).toString();
            sttChangedMap.remove(item);
            sttChangedList.remove(item);
            populateSttList();
            setCurrentItemInChangedList(name, index);
            changeSttVisibleList(sttVisibleList);
        }
        log("Settings item '" + item + "' removed from list of changed items.");
    }

    private void deleteAllSttChangedItems() {
        boolean result = msgBoxInfoQuestion("Delete All items", "Delete All items", "Do you want to remove all changed items from list ?");

        if (result) {
            sttChangedList.clear();
            sttChangedMap.clear();
            changeSttVisibleList(sttVisibleList);
        }
        log ("All settings items removed from list of changed items.");
    }

    private void unChangeSettingsItem() {
        log("UnChange Settings item started...");
        logIndentPlus();

        // Check if item exists
        if (! isSttItemExists(txtSttKey.getText())) {
            log("Unable to UnChange Settings item: " + txtSttKey.getText() + " - Settings item does not exist.");
            log("UnChanging Settings item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Error", "Settings item does not exist", "Unable to UnChange Settings item: " + txtSttKey.getText() + " - Settings item does not exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return;
        }
        
        // UnMark item as changed
        log("UnMarking Settings item as Changed...");
        logIndentPlus();

        SettingsItem item = null;
        if (sttChangedMap.containsKey(txtSttKey.getText())) {
            item = sttChangedMap.getPyDictValue(txtSttKey.getText());
        }

        // If item is not found in 'sttChangedMap', stop and show error
        if (item == null) {
            log("Unable to UnChange item '" + txtSttKey.getText() + "'. Item is not found in 'sttChangedMap'.");
            MsgInfo msg = new MsgInfo("Error", "Item not found", "Unable to UnChange item '" + txtSttKey.getText() + "'. Item is not found in 'sttChangedMap'.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnChanging Settings item stopped.");
            logIndentMinus();
            return;
        }
        
        // If item is found in 'sttChangedMap' and marked as 'Changed' remove it from 'sttChangedMap' and 'sttChangedList'
        if (item.getUserData().equals("Changed")) {
            // Remove item from sttChangedMap and sttChangedList
            sttChangedMap.remove(txtSttKey.getText());
            sttChangedList.remove(txtSttKey.getText());
            log("Item '" + txtSttKey.getText() + "' removed from 'sttChangedMap' and 'sttChangedList'.");
        }
        else {
            log("Unable to UnChange item '" + txtSttKey.getText() + "'. Item is marked as '" + item.getUserData() + "'', not as 'Changed'.");
            MsgInfo msg = new MsgInfo("Error", "Item not marked as 'Changed'", "Unable to UnChange item '" + txtSttKey.getText() + "'. Item is not marked as 'Changed'.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnChanging Settings item stopped.");
            logIndentMinus();
            return;
        }

        logIndentMinus();

        // Refreshing display
        changeSttVisibleList(sttVisibleList);
        txtSttKey.setText(txtSttKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("UnChange Settings item", "Changes to Settings item are discarded successfully", "Settings item UnChanged: " + txtSttKey.getText(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Settings item UnChanged: " + txtSttKey.getText(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getX(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getY(), 10);

        log ("UnChanging Settings item completed successfully.");

        logIndentMinus();
    }

    private void unDeleteSettingsItem() {
        log("UnDelete Settings item started...");
        logIndentPlus();

        // Check if item exists
        if (! isSttItemExists(txtSttKey.getText())) {
            log("Unable to UnDelete Settings item: " + txtSttKey.getText() + " - Settings item does not exist.");
            log("UnDeleting Settings item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Error", "Settings item does not exist", "Unable to UnDelete Settings item: " + txtSttKey.getText() + " - Settings item does not exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return;
        }

        // UnMark item as deleted
        log("UnMarking Settings item as Deleted...");
        logIndentPlus();

        SettingsItem item = null;
        if (sttChangedMap.containsKey(txtSttKey.getText())) {
            item = sttChangedMap.getPyDictValue(txtSttKey.getText());
        }

        // If item is not found in 'sttChangedMap', stop and show error
        if (item == null) {
            log("Unable to UnMark item '" + txtSttKey.getText() + "' as Deleted. Item is not found in 'sttChangedMap'.");
            MsgInfo msg = new MsgInfo("Error", "Item not found", "Unable to UnMark item '" + txtSttKey.getText() + "' as Deleted. Item is not found in 'sttChangedMap'.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnDeleting Settings item stopped.");
            logIndentMinus();
            return;
        }

        if (item.getUserData().equals("Deleted")) {
            if (item.equals(sttLoadedMap.getPyDictValue(txtSttKey.getText()))) {
                // Case when item should be deleted from 'sttChangedMap' because it is equal to item in 'sttLoadedMap'
                sttChangedMap.remove(txtSttKey.getText());
                // Delete also from sttChangedList
                if (sttChangedList.contains(txtSttKey.getText())) {
                    sttChangedList.remove(txtSttKey.getText());
                }
                log("Item '" + txtSttKey.getText() + "'' unmarked as deleted.");
                MsgInfo msg = new MsgInfo("Info", "Item UnDeleted", "Item '" + txtSttKey.getText() + "'' unmarked as deleted.", MsgInfo.MsgStyle.INFORMATION);
                showMessage(msg);
            }
            else {
                //  Case when item exists in both 'sttLoadedMap' and 'sttChangedMap' and is NOT equal to item in 'sttLoadedMap'
                // Switch item from "Deleted" to "Changed"
                item.setUserData("Changed");
                log("Item '" + txtSttKey.getText() + "'' marked from 'Deleted' to 'Changed'.");
                MsgInfo msg = new MsgInfo("Info", "Item UnDeleted and marked as 'Changed'", "Item '" + txtSttKey.getText() + "'' marked from 'Deleted' to 'Changed'.", MsgInfo.MsgStyle.INFORMATION);
                showMessage(msg);
            }
        }
        else {
            log("Unable to UnDelete item '" + txtSttKey.getText() + "'. Item is not marked as deleted.");
            MsgInfo msg = new MsgInfo("Error", "Item not marked as deleted", "Unable to UnDelete item '" + txtSttKey.getText() + "'. Item is not marked as deleted.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnDeleting Settings item stopped.");
            logIndentMinus();
            return;
        }

        logIndentMinus();

        // Refreshing display
        changeSttVisibleList(sttVisibleList);
        txtSttKey.setText(txtSttKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("UnDelete Settings item", "Settings item UNDeleted successfully", "Settings item UnDeleted: " + txtSttKey.getText(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Settings item UnDeleted: " + txtSttKey.getText(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getX(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getY(), 10);

        log ("UnDeleting Settings item completed successfully.");

        logIndentMinus();
    }

    private void deleteSettingsItem() {
        log("Delete Settings item started...");
        logIndentPlus();

        // Check if item exists
        if (! isSttItemExists(txtSttKey.getText())) {
            log("Unable to delete Settings item: " + txtSttKey.getText() + " - Settings item does not exist.");
            log("Deleting Settings item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Error", "Settings item does not exist", "Unable to delete Settings item: " + txtSttKey.getText() + " - Settings item does not exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return;
        }

        // Mark item as deleted
        log("Marking Settings item as deleted...");
        logIndentPlus();
        
        SettingsItem item = getSettingsItem(txtSttKey.getText()).duplicate();
        
        if (item != null) {
            item.setUserData("Deleted");
            if (sttChangedMap.containsKey(txtSttKey.getText())) {
                // If item exists in 'sttChangedMap' change only userData to be able UnDelete item and mark it as "Changed"
                SettingsItem existingItem = sttChangedMap.getPyDictValue(txtSttKey.getText());
                existingItem.setUserData("Deleted");
            }
            else {
                // If item does not exist in 'sttChangedMap' add new item to 'sttChangedMap'
                sttChangedMap.setPyDictValue(txtSttKey.getText(), item);
            }
            
            // Add item to 'sttChangedList' if needed
            if (! sttChangedList.contains(txtSttKey.getText())) {
                sttChangedList.add(txtSttKey.getText());
            }

            log("Item '" + txtSttKey.getText() + "'' marked as deleted.");
        }
        else {
            log("Unable to mark item '" + txtSttKey.getText() + "' as deleted. Item is not found.");
            MsgInfo msg = new MsgInfo("Error", "Settings item not found", "Unable to mark item '" + txtSttKey.getText() + "' as deleted. Item is not found.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("Deleting Settings item stopped.");
            logIndentMinus();
            return;
        }

        logIndentMinus();

        // Refreshing display
        changeSttVisibleList(sttVisibleList);
        txtSttKey.setText(txtSttKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("Delete Settings item", "Settings item deleted", "Settings item deleted: " + txtSttKey.getText(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Settings item deleted: " + txtSttKey.getText(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getX(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getY(), 10);

        log("Settings item marked as deleted in list of changed items: " + txtSttKey.getText());
        log ("Deleting Settings item completed successfully.");

        logIndentMinus();
    }

    private void addNewSettingsItem() {
        log("Add new Settings item started...");
        logIndentPlus();

        // Check if item already exists
        if (isSttItemExists(txtSttKey.getText())) {
            log("Unable to add new Settings item: " + txtSttKey.getText() + " - Settings item already exists.");
            log("Adding new Settings item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Warning", "Settings item already exists", "Unable to add new Settings item: " + txtSttKey.getText() + " - Settings item already exists.", MsgInfo.MsgStyle.WARNING);
            showMessage(msg);
            return;
        }

        // Validate item
        log("Validating new Settings item...");
        boolean isValid = isCurrentItemCanBeAddedOrUpdated("add");
        
        if (!isValid) {
            log("Invalid item. Adding new Settings item stopped.");
            logIndentMinus();
            return;
        }
        else {
            log("Settings item is valid.");
        }

        // Create new SettingsItem Object
        SettingsItem newSettingsItem = new SettingsItem();
        newSettingsItem.setKey(txtSttKey.getText());
        newSettingsItem.setSettingType(SettingType.fromName(cmbSttFlag.getSelectionModel().getSelectedItem()).getValue());
        newSettingsItem.setDataType(DataType.fromName(cmbSttDataType.getSelectionModel().getSelectedItem()));
        if (chkSttAutoStrip.isSelected()) {
            newSettingsItem.setValue(txtSttValue.getText().strip());
            newSettingsItem.setDefaultValue(txtSttDefValue.getText().strip());
        }
        else {
            newSettingsItem.setValue(txtSttValue.getText());
            newSettingsItem.setDefaultValue(txtSttDefValue.getText());
        }
        newSettingsItem.setMin(txtSttMin.getText());
        newSettingsItem.setMax(txtSttMax.getText());
        newSettingsItem.setDescription(txtSttDesc.getText());
        newSettingsItem.setCanBeSavedInFile(true);
        newSettingsItem.setUserData("Changed");

        log ("Created new Settings item: ");
        logIndentPlus();
        log("Key: " + newSettingsItem.getKey());
        log("SettingType: " + String.valueOf(newSettingsItem.getSettingType()));
        log("DataType: " + newSettingsItem.getDataType().toString());
        log("Value: " + newSettingsItem.getValueSTRING().replace("\n", " "));
        log("DefaultValue: " + newSettingsItem.getDefaultValueSTRING().replace("\n", " "));
        log("Min: " + newSettingsItem.getMinSTRING());
        log("Max: " + newSettingsItem.getMaxSTRING());
        log("Description: " + newSettingsItem.getDescription().replace("\n", " "));
        log("CanBeSavedInFile: true");
        log("UserData: Changed");
        logIndentMinus();

        // Add new Settings item to list
        sttChangedList.add(newSettingsItem.getKey());
        sttChangedMap.put(newSettingsItem.getKey(), newSettingsItem);
        SettingsItem newSettingsItem2 = newSettingsItem.duplicate();
        sttLoadedMap.put(newSettingsItem.getKey(), newSettingsItem2);

        // Refreshing display
        changeSttVisibleList(sttVisibleList);
        txtSttKey.setText(newSettingsItem.getKey());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("Add new Settings item", "New Settings item added", "New Settings item added: " + newSettingsItem.getKey(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("New Settings item added: " + newSettingsItem.getKey(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getX(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getY(), 10);

        log("Added new Settings item to list of changed items: " + newSettingsItem.getKey());
        log ("Adding new Settings item completed successfully.");

        logIndentMinus();

    }

    private void updateSettingsItem() {
        log("Update Settings item started...");
        logIndentPlus();

        // Check if item does not exists
        if (! isSttItemExists(txtSttKey.getText()) || txtSttKey.getText().isEmpty()) {
            log("Unable to update Settings item: " + txtSttKey.getText() + " - Settings item does not exists.");
            log("Updating Settings item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Warning", "Settings item does not exists", "Unable to update Settings item: " + txtSttKey.getText() + " - Settings item does not exists.", MsgInfo.MsgStyle.WARNING);
            showMessage(msg);
            return;
        }

        // Validate item
        log("Validating Settings item...");
        boolean isValid = isCurrentItemCanBeAddedOrUpdated("update");
        
        if (!isValid) {
            log("Invalid item. Updating Settings item stopped.");
            logIndentMinus();
            return;
        }
        else {
            log("Settings item is valid.");
        }

        // Create new SettingsItem Object
        SettingsItem updatedSettingsItem = getSettingsItem(txtSttKey.getText()).duplicate();
        updatedSettingsItem.setKey(txtSttKey.getText());
        updatedSettingsItem.setSettingType(SettingType.fromName(cmbSttFlag.getSelectionModel().getSelectedItem()).getValue());
        updatedSettingsItem.setDataType(DataType.fromName(cmbSttDataType.getSelectionModel().getSelectedItem()));
        if (chkSttAutoStrip.isSelected()) {
            updatedSettingsItem.setValue(txtSttValue.getText().strip());
            updatedSettingsItem.setDefaultValue(txtSttDefValue.getText().strip());
        }
        else {
            updatedSettingsItem.setValue(txtSttValue.getText());
            updatedSettingsItem.setDefaultValue(txtSttDefValue.getText());
        }
        updatedSettingsItem.setMin(txtSttMin.getText());
        updatedSettingsItem.setMax(txtSttMax.getText());
        updatedSettingsItem.setDescription(txtSttDesc.getText());
        updatedSettingsItem.setCanBeSavedInFile(true);
        updatedSettingsItem.setUserData("Changed");

        log ("Created updated Settings item: ");
        logIndentPlus();
        log("Key: " + updatedSettingsItem.getKey());
        log("SettingType: " + String.valueOf(updatedSettingsItem.getSettingType()));
        log("DataType: " + updatedSettingsItem.getDataType().toString());
        log("Value: " + updatedSettingsItem.getValueSTRING().replace("\n", " "));
        log("DefaultValue: " + updatedSettingsItem.getDefaultValueSTRING().replace("\n", " "));
        log("Min: " + updatedSettingsItem.getMinSTRING());
        log("Max: " + updatedSettingsItem.getMaxSTRING());
        log("Description: " + updatedSettingsItem.getDescription().replace("\n", " "));
        log("CanBeSavedInFile: true");
        log("UserData: Changed");
        logIndentMinus();

        // Update Settings item in list
        if (! sttChangedList.contains(updatedSettingsItem.getKey())) {
            sttChangedList.add(updatedSettingsItem.getKey());
        }
        sttChangedMap.put(updatedSettingsItem.getKey(), updatedSettingsItem);

        // Refreshing display
        changeSttVisibleList(sttVisibleList);
        txtSttKey.setText(updatedSettingsItem.getKey());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("Update Settings item", "Settings item is updated", "Settings item updated: " + updatedSettingsItem.getKey(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Settings item updated: " + updatedSettingsItem.getKey(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getX(), txtSttKey.localToScene(0, txtSttKey.getHeight() + 3).getY(), 10);

        log("Updated Settings item in list of changed items: " + updatedSettingsItem.getKey());
        log("Updating Settings item completed successfully.");

        logIndentMinus();
    }

    private boolean isCurrentItemCanBeAddedOrUpdated(String action) {
        // Check is item valid
        log("Checking if Settings item is valid...");
        logIndentPlus();
        Map<String, String> checkedResult = getChangedItemReport();

        boolean hasErrors = false;
        boolean hasWarnings = false;
        
        for (Map.Entry<String, String> entry : checkedResult.entrySet()) {
            if (entry.getValue().startsWith("Error")) {
                hasErrors = true;
            }
            else if (entry.getValue().startsWith("Warning")) {
                hasWarnings = true;
            }
        }

        removeMessage("Add new Settings Error");
        removeMessage("Add new Settings Warning");
        removeMessage("Update Settings Error");
        removeMessage("Update Settings Warning");
        String indent = "      ";

        // If item has errors
        if (hasErrors) {
            String title = "";
            String header = "";
            if (action.equals("add")) {
                title = "Add new Settings item";
                header = "Unable to add new Settings item";
            }
            else if (action.equals("update")) {
                title = "Update Settings item";
                header = "Unable to update Settings item";
            }
            String content = checkedResult.get("Problems") + "\n\n";
            
            int count = 1;
            for (Map.Entry<String, String> entry : checkedResult.entrySet()) {
                if (entry.getValue().startsWith("Error")) {
                    content +=  count + ".) " + entry.getKey() + ": \n" + indent + entry.getValue() + "\n\n";
                    count++;
                }
            }
            for (Map.Entry<String, String> entry : checkedResult.entrySet()) {
                if (entry.getValue().startsWith("Warning")) {
                    content +=  count + ".) " + entry.getKey() + ": \n" + indent + entry.getValue() + "\n\n";
                    count++;
                }
            }
            
            if (action.equals("add")) {
                MsgInfo msg = new MsgInfo("Add new Settings Error", header, content, MsgInfo.MsgStyle.ERROR);
                showMessage(msg);
            }
            else if (action.equals("update")) {
                MsgInfo msg = new MsgInfo("Update Settings Error", header, content, MsgInfo.MsgStyle.ERROR);
                showMessage(msg);
            }
            
            log(checkedResult.get("Problems"));
            for (Map.Entry<String, String> entry : checkedResult.entrySet()) {
                if (entry.getValue().startsWith("Error") || entry.getValue().startsWith("Warning")) {
                    log(entry.getKey() + ": " + entry.getValue());
                }
                
            }
            logIndentMinus();
            if (action.equals("add")) {
                log("Adding new Settings item stopped.");
                content += "\nAdding new Settings item is cancelled.";
            }
            else if (action.equals("update")) {
                log("Updating Settings item stopped.");
                content += "\nUpdating Settings item is cancelled.";
            }
            
            msgBoxInfoCritical(title, header, content);

            return false;
        }

        // If item has warnings - ask user to continue
        if (hasWarnings) {
            String title = "";
            String header = "";
            if (action.equals("add")) {
                title = "Add new Settings item";
                header = "Settings item has warnings";
            }
            else if (action.equals("update")) {
                title = "Update Settings item";
                header = "Settings item has warnings";
            }
            String content = checkedResult.get("Problems") + "\n\n";
            
            int count = 1;
            for (Map.Entry<String, String> entry : checkedResult.entrySet()) {
                if (entry.getValue().startsWith("Warning")) {
                    content +=  count + ".) " + entry.getKey() + ": \n" + indent + entry.getValue() + "\n\n";
                    count++;
                }
            }
            
            if (action.equals("add")) {
                MsgInfo msg = new MsgInfo("Add new Settings Warning", header, content, MsgInfo.MsgStyle.WARNING);
                showMessage(msg);
            }
            else if (action.equals("update")) {
                MsgInfo msg = new MsgInfo("Update Settings Warning", header, content, MsgInfo.MsgStyle.WARNING);
                showMessage(msg);
            }
            
            log(checkedResult.get("Problems"));
            for (Map.Entry<String, String> entry : checkedResult.entrySet()) {
                if (entry.getValue().startsWith("Error") || entry.getValue().startsWith("Warning")) {
                    log(entry.getKey() + ": " + entry.getValue());
                }
                
            }
            
            log("Asking user to continue...");
            logIndentPlus();

            content += "\n\nSaving item in this state may cause issues and is not recommended.\nDo you want to continue?";
            Boolean answer = msgBoxInfoQuestion(title, header, content);

            if (answer) {
                if (action.equals("add")) {
                    log("User wants to continue, adding new Settings item...");
                }
                else if (action.equals("update")) {
                    log("User wants to continue, updating Settings item...");
                }
                logIndentMinus();
                logIndentMinus();
            }
            else {
                if (action.equals("add")) {
                    log("User does not want to continue, adding new Settings item stopped.");
                }
                else if (action.equals("update")) {
                    log("User does not want to continue, updating Settings item stopped.");
                }
                logIndentMinus();
                logIndentMinus();
                return false;
            }
        }

        // If there is no errors or warnings
        if (!hasErrors && !hasWarnings) {
            log(checkedResult.get("Problems"));
            logIndentMinus();
        }

        return true;
    }

    private boolean checkIfSettingsEntryIsValid() {
        Map<String, String> result = getChangedItemReport();
        return result.get("result").equals("true");
    }
    
    private Map<String, String> getChangedItemReport() {
        int problemCount = 0;
        
        Map<String, String> result = new HashMap<>();
        result.put("result", "true");
        result.put("Problems", "No problems found.");
        result.put("SettingsKey", "Ok.");
        result.put("SettingsType", "Ok.");
        result.put("DataType", "Ok.");
        result.put("Value", "Ok.");
        result.put("DefaultValue", "Ok.");
        result.put("Minimum", "Ok.");
        result.put("Maximum", "Ok.");
        result.put("Description", "Ok.");

        // Settings Key
        if (txtSttKey.getText().isEmpty()) {
            problemCount++;
            result.put("result", "false");
            result.put("SettingsKey", "Error: Settings Key cannot be empty");
        }

        // Settings Type (Flag)
        fieldLabeSttFlag.setTooltip(null);
        fieldLabeSttFlag.getStyleClass().clear();
        if (cmbSttFlag.getSelectionModel().getSelectedItem() == null) {
            problemCount++;
            result.put("result", "false");
            result.put("SettingsType", "Error: Settings Type (Flag) cannot be empty");
            setTooltip(result.get("SettingsType"), fieldLabeSttFlag);
            fieldLabeSttFlag.getStyleClass().add("field-label-error");
        }
        else if (! cmbSttFlag.getSelectionModel().getSelectedItem().equals(SettingType.DEFAULT.toString())) {
            problemCount++;
            result.put("result", "false");
            result.put("SettingsType", "Warning: Settings Type (Flag) should be '" + SettingType.DEFAULT.toString() + "'");
            setTooltip(result.get("SettingsType"), fieldLabeSttFlag);
            fieldLabeSttFlag.getStyleClass().add("field-label-warning");
        }
        else {
            fieldLabeSttFlag.getStyleClass().add("field-label-normal");
        }

        // Data Type
        List<String> allowedValueDataTypes = getAvailableDataTypes(txtSttValue.getText());
        for (int i = 0; i < allowedValueDataTypes.size(); i++) {
            String fixedItem = allowedValueDataTypes.get(i).toUpperCase();
            if (fixedItem.contains("(")) {
                fixedItem = fixedItem.substring(0, fixedItem.indexOf("(")).trim();
            }
            allowedValueDataTypes.set(i, fixedItem);
        }
        allowedValueDataTypes.add(DataType.STRING.toString());

        fieldLabeSttDataType.setTooltip(null);
        fieldLabeSttDataType.getStyleClass().clear();
        if (cmbSttDataType.getSelectionModel().getSelectedItem() == null) {
            problemCount++;
            result.put("result", "false");
            result.put("DataType", "Error: Data Type cannot be empty");
            setTooltip(result.get("DataType"), fieldLabeSttDataType);
            fieldLabeSttDataType.getStyleClass().add("field-label-error");
        }
        else {
            if (! allowedValueDataTypes.contains(cmbSttDataType.getSelectionModel().getSelectedItem())) {
                problemCount++;
                result.put("result", "false");
                result.put("DataType", "Error: Data Type should be one of: " + String.join(", ", allowedValueDataTypes));
                setTooltip(result.get("DataType"), fieldLabeSttDataType);
                fieldLabeSttDataType.getStyleClass().add("field-label-error");
            }
            else {
                fieldLabeSttDataType.getStyleClass().add("field-label-normal");
            }
        }

        // Value
        fieldLabeSttValue.setTooltip(null);
        fieldLabeSttValue.getStyleClass().clear();
        if ((cmbSttDataType.getSelectionModel().getSelectedItem() != null) && ! allowedValueDataTypes.contains(cmbSttDataType.getSelectionModel().getSelectedItem())) {
            problemCount++;
            result.put("result", "false");
            result.put("Value", "Warning: Value cannot be interpreted as " + cmbSttDataType.getSelectionModel().getSelectedItem().toString() + " data type");
            setTooltip(result.get("Value"), fieldLabeSttValue);
            fieldLabeSttValue.getStyleClass().add("field-label-warning");
        }
        else {
            fieldLabeSttValue.getStyleClass().add("field-label-normal");
        }

        // Default Value
        List<String> allowedDefValueDataTypes = getAvailableDataTypes(txtSttDefValue.getText());
        for (int i = 0; i < allowedDefValueDataTypes.size(); i++) {
            String fixedItem = allowedDefValueDataTypes.get(i).toUpperCase();
            if (fixedItem.contains("(")) {
                fixedItem = fixedItem.substring(0, fixedItem.indexOf("(")).trim();
            }
            allowedDefValueDataTypes.set(i, fixedItem);
        }
        allowedDefValueDataTypes.add(DataType.STRING.toString());

        fieldLabeSttDefValue.setTooltip(null);
        fieldLabeSttDefValue.getStyleClass().clear();
        if ((cmbSttDataType.getSelectionModel().getSelectedItem() != null) && ! allowedDefValueDataTypes.contains(cmbSttDataType.getSelectionModel().getSelectedItem())) {
            problemCount++;
            result.put("result", "false");
            result.put("DefaultValue", "Warning: Default Value cannot be interpreted as " + cmbSttDataType.getSelectionModel().getSelectedItem().toString() + " data type");
            setTooltip(result.get("DefaultValue"), fieldLabeSttDefValue);
            fieldLabeSttDefValue.getStyleClass().add("field-label-warning");
        }
        else {
            fieldLabeSttDefValue.getStyleClass().add("field-label-normal");
        }

        // Minimum
        fieldLabeSttMin.setTooltip(null);
        fieldLabeSttMin.getStyleClass().clear();
        if (! (isTextBoxStringNull(txtSttMin.getText()) || getDouble(txtSttMin.getText()) != null)) {
            problemCount++;
            result.put("result", "false");
            result.put("Minimum", "Error: Minimum value cannot be interpreted as NUMBER or NULL.");
            setTooltip(result.get("Minimum"), fieldLabeSttMin);
            fieldLabeSttMin.getStyleClass().add("field-label-error");
        }
        else {
            fieldLabeSttMin.getStyleClass().add("field-label-normal");
        }

        // Maximum
        fieldLabeSttMax.setTooltip(null);
        fieldLabeSttMax.getStyleClass().clear();
        if (! (isTextBoxStringNull(txtSttMax.getText()) || getDouble(txtSttMax.getText()) != null)) {
            problemCount++;
            result.put("result", "false");
            result.put("Maximum", "Error: Maximum value cannot be interpreted as NUMBER or NULL.");
            setTooltip(result.get("Maximum"), fieldLabeSttMax);
            fieldLabeSttMax.getStyleClass().add("field-label-error");
        }
        else {
            fieldLabeSttMax.getStyleClass().add("field-label-normal");
        }

        // Relations between Minimum and Maximum
        if (result.get("Minimum").equals("Ok.") && result.get("Maximum").equals("Ok.")) {
            if (getDouble(txtSttMin.getText()) != null && getDouble(txtSttMax.getText()) != null) {
                fieldLabeSttMin.setTooltip(null);
                fieldLabeSttMin.getStyleClass().clear();

                fieldLabeSttMax.setTooltip(null);
                fieldLabeSttMax.getStyleClass().clear();
        
                if (getDouble(txtSttMin.getText()) > getDouble(txtSttMax.getText())) {
                    problemCount++;
                    result.put("result", "false");
                    result.put("Minimum", "Warning: Minimum value cannot be greater than Maximum value.");
                    setTooltip(result.get("Minimum"), fieldLabeSttMin);
                    fieldLabeSttMin.getStyleClass().add("field-label-warning");

                    problemCount++;
                    result.put("Maximum", "Warning: Maximum value cannot be less than Minimum value.");
                    setTooltip(result.get("Maximum"), fieldLabeSttMax);
                    fieldLabeSttMax.getStyleClass().add("field-label-warning");
                }
                else {
                    fieldLabeSttMin.getStyleClass().add("field-label-normal");
                    fieldLabeSttMax.getStyleClass().add("field-label-normal");
                }
            }
        }

        // Description
        // No need to check, it can be anything and its always STRING

        result.put("Problems", String.valueOf(problemCount) + " problems found.");

        return result;
    }

    private boolean isSettingsFile(String filePath) {
        try {
            Settings settings = new Settings();
            settings.defaultSettingsFilePath = filePath;
            settings.getAllDefaultSettingsData();
            log("Settings file is valid: " + filePath);
            removeMessage("Error loading settings file");
            return true;
        }
        catch (Exception e) {
            log("Settings file is invalid: " + filePath + " - " + e.getMessage());
            MsgInfo msg = new MsgInfo("Error loading settings file", "Invalid Settings file", "Settings file is invalid: " + filePath + "\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return false;
        }
    }

    private boolean isLanguageFile(String filePath) {
        try {
            Settings settings = new Settings();
            settings.languagesFilePath = filePath;
            settings.load(false, true, false);
            if (settings.getLastErrorString().isEmpty()) {
                log("Language file is valid: " + filePath);
                removeMessage("Error loading language file");
                return true;
            }
            else {
                log("Language file is invalid: " + filePath + " - " + settings.getLastErrorString());
                MsgInfo msg = new MsgInfo("Error loading language file", "Invalid Language file", "Language file is invalid: " + filePath + "\n" + settings.getLastErrorString(), MsgInfo.MsgStyle.ERROR);
                showMessage(msg);
                return false;
            }
        }
        catch (Exception e) {
            log("Language file is invalid: " + filePath + " - " + e.getMessage());
            MsgInfo msg = new MsgInfo("Error loading language file", "Invalid Language file", "Language file is invalid: " + filePath + "\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return false;
        }
    }

    private void populateSttList() {
        if (sttVisibleList.equals("Changed")) {
            populateSttList(sttChangedMap, sttChangedMap, txtSttSearch.getText());
        }
        else {
            populateSttList(sttLoadedMap, sttChangedMap, txtSttSearch.getText());
        }
    }

    private void populateSttList(PyDict sttItemsMap, PyDict sttChangedItemsMap, String filterText) {
        List<SettingsItem> itemsList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : sttItemsMap.entrySet()) {
            if (entry.getValue() instanceof SettingsItem) {
                SettingsItem item = (SettingsItem) entry.getValue();

                if (filterText.isEmpty() || item.getKey().toLowerCase().contains(filterText.toLowerCase())) {
                    itemsList.add(item);
                }
            }
        }
        // Sort List ("Key" or "Created")
        if (lstSttSortKey.equals("Key")) {
            itemsList.sort(Comparator.comparing(SettingsItem::getKey));
        }
        else {
            itemsList.sort(Comparator.comparing(SettingsItem::getCreationDateForJson));
        }

        // clear list
        if (lstStt.getItems() != null) {
            // lstStt.getItems().clear();
            lstStt.setItems(null);
        }
        // add items
        ObservableList<String> items = FXCollections.observableArrayList();
        for (SettingsItem itemName : itemsList) {
            items.add(itemName.getKey());
        }

        lstStt.setItems(items);

        // Try to select old Key if it exists
        if (sttVisibleList.equals("Changed")) {
            setCurrentItemInChangedList(sttChangedCurrentItem, null);
        }
        else {
            setCurrentItemInLoadedList(sttLoadedCurrentItem, null);
        }

        // Try to refresh list display
        lstStt.layout();
    }

    private int setCurrentItemInChangedList(String itemText, Integer itemIndex) {
        if (itemIndex != null && itemIndex < 0) itemIndex = null;

        lstStt.getSelectionModel().clearSelection();
        if (sttChangedMap.containsKey(itemText)) {
            sttChangedCurrentItem = itemText;
            lstStt.getSelectionModel().select(itemText);
            return lstStt.getSelectionModel().getSelectedIndex();
        }

        if (itemIndex != null) {
            int listItemsCount = lstStt.getItems().size();
            if (itemIndex > (listItemsCount - 1)) {
                itemIndex = listItemsCount - 1;
            }
            
            if (lstStt.getItems().size() <= 0) {
                itemIndex = null;
            }
            else {
                if (itemIndex < 0) {
                    itemIndex = 0;
                }
                sttChangedCurrentItem = lstStt.getItems().get(itemIndex);
                lstStt.getSelectionModel().select(itemIndex);
                return itemIndex;
            }

        }
        
        lstStt.getSelectionModel().selectFirst();
        if (lstStt.getSelectionModel().getSelectedIndex() == -1) {
            sttChangedCurrentItem = "";
        }
        else {
            sttChangedCurrentItem = lstStt.getSelectionModel().getSelectedItem();
        }
        return lstStt.getSelectionModel().getSelectedIndex();
    }

    private int setCurrentItemInLoadedList(String itemText, Integer itemIndex) {
        if (itemIndex != null && itemIndex < 0) itemIndex = null;

        lstStt.getSelectionModel().clearSelection();
        if (sttLoadedMap.containsKey(itemText)) {
            sttLoadedCurrentItem = itemText;
            lstStt.getSelectionModel().select(itemText);
            return lstStt.getSelectionModel().getSelectedIndex();
        }

        if (itemIndex != null) {
            int listItemsCount = lstStt.getItems().size();
            if (itemIndex > (listItemsCount - 1)) {
                itemIndex = listItemsCount - 1;
            }
            
            if (lstStt.getItems().size() <= 0) {
                itemIndex = null;
            }
            else {
                if (itemIndex < 0) {
                    itemIndex = 0;
                }
                sttLoadedCurrentItem = lstStt.getItems().get(itemIndex);
                lstStt.getSelectionModel().select(itemIndex);
                return itemIndex;
            }

        }
        
        lstStt.getSelectionModel().selectFirst();
        if (lstStt.getSelectionModel().getSelectedIndex() == -1) {
            sttLoadedCurrentItem = "";
        }
        else {
            sttLoadedCurrentItem = lstStt.getSelectionModel().getSelectedItem();
        }
        return lstStt.getSelectionModel().getSelectedIndex();
    }

    private void changeSttVisibleList(String listToActivate) {
        if (listToActivate.equals("Changed")) {
            populateSttList(sttChangedMap, sttChangedMap, txtSttSearch.getText());
            sttVisibleList = "Changed";
            setCurrentItemInChangedList(sttChangedCurrentItem, null);
        }
        else {
            populateSttList(sttLoadedMap, sttChangedMap, txtSttSearch.getText());
            sttVisibleList = "Loaded";
            setCurrentItemInLoadedList(sttLoadedCurrentItem, null);
        }

        log("Settings visible list changed to: " + sttVisibleList);
        
        if (lstStt.getSelectionModel().getSelectedIndex() != -1) {
            lstStt.scrollTo(lstStt.getSelectionModel().getSelectedIndex());
        }
        
        updateSttWidgetsAppearance();
    }

    private void updateSttWidgetsAppearance() {
        // Visible Settings list
        btnSttShowLoaded.getStyleClass().removeAll("button-list-selector-selected", "button-list-selector-not-selected");
        btnSttShowChanged.getStyleClass().removeAll("button-list-selector-selected", "button-list-selector-not-selected");
        if (sttVisibleList.equals("Changed")) {
            btnSttShowChanged.getStyleClass().add("button-list-selector-selected");
            btnSttShowLoaded.getStyleClass().add("button-list-selector-not-selected");
        }
        else {
            btnSttShowLoaded.getStyleClass().add("button-list-selector-selected");
            btnSttShowChanged.getStyleClass().add("button-list-selector-not-selected");
        }

        // Record counter
        if (sttVisibleList.equals("Changed")) {
            lblSttRecords.setText("Records: " + lstStt.getItems().size() + " of " + sttChangedMap.size());
        }
        else {
            lblSttRecords.setText("Records: " + lstStt.getItems().size() + " of " + sttLoadedMap.size());
        }

        // Buttons "Show Loaded" and "Show Changed"
        btnSttShowLoaded.setText("Loaded (" + sttLoadedMap.size() + ")");
        btnSttShowChanged.setText("Changed (" + sttChangedMap.size() + ")");

        // Set command buttons availability
        boolean isExists = isSttItemExists(txtSttKey.getText());
        boolean isChanged = sttChangedList.contains(txtSttKey.getText());
        boolean isDeleted = isSttItemDeleted(txtSttKey.getText());
        if (isExists) {
            setWidgetIcon("/images/edit.png", lblSttKeyImage, 50);
            btnSttAdd.setDisable(true);
            btnSttUpdate.setDisable(false);
            
            if (isChanged) {
                btnSttDiscard.setDisable(false);
                btnSttDiscard.setText("Discard Changes");
            }
            else {
                btnSttDiscard.setDisable(true);
                btnSttDiscard.setText("Discard ...");
            }

            if (isDeleted) {
                btnSttDelete.setDisable(true);
                btnSttDiscard.setDisable(false);
                btnSttDiscard.setText("Undo Delete");
            }
            else {
                btnSttDelete.setDisable(false);
            }
        }
        else {
            setWidgetIcon("/images/new.png", lblSttKeyImage, 50);
            btnSttAdd.setDisable(false);
            btnSttUpdate.setDisable(true);
            btnSttDelete.setDisable(true);
            btnSttDiscard.setDisable(true);
            btnSttDiscard.setText("Discard ...");
        }

        // Hide btnSttDiscard if it is disabled
        if (btnSttDiscard.isDisabled()) {
            btnSttDiscard.setVisible(false);
        }
        else {
            btnSttDiscard.setVisible(true);
        }

        checkIfUserIsChangingSetting();
        checkIfSettingsEntryIsValid();

        log("Settings widgets appearance updated");
    }

    private boolean isSttItemDeleted(String itemName) {
        SettingsItem item;
        for (Map.Entry<String, Object> entry : sttChangedMap.entrySet()) {
            item = (SettingsItem) entry.getValue();
            if (item.getKey().equals(itemName) && item.getUserData().equals("Deleted")) {
                return true;
            }
        }

        return false;
    }

    /**
     * If item exists in sttChangedMap it will return it, otherwise it will return item from sttLoadedMap
     * @param itemName item name (Key)
     * @return SettingsItem or null if not found
     */
    private SettingsItem getSettingsItem(String itemName) {
        if (itemName.isEmpty()) {
            return null;
        }

        if (sttChangedMap.containsKey(itemName)) {
            return (SettingsItem) sttChangedMap.get(itemName);
        }

        if (sttLoadedMap.containsKey(itemName)) {
            return (SettingsItem) sttLoadedMap.get(itemName);
        }

        return null;
    }

    private boolean isSttItemExists(String itemName) {
        if (getSettingsItem(itemName) != null) {
            return true;
        }

        return false;
    }

    private void checkIfUserIsChangingSetting() {
        boolean isUserChanging = isUserCurrentlyChanging(txtSttKey.getText());

        if (isUserChanging) {
            setWidgetIcon("/images/record.png", lblSttRec, 25);
        }
        else {
            setWidgetIcon("/images/show.png", lblSttRec, 25);
        }
    }
 
    private boolean isUserCurrentlyChanging(String itemName) {
        SettingsItem item = getSettingsItem(itemName);
        if (item == null) {
            return true;
        }

        // Check if SettingType has changed
        if (cmbSttFlag.getSelectionModel().getSelectedItem() == null) {
            return true;
        }
        if (item.getSettingType() != SettingType.fromName(cmbSttFlag.getSelectionModel().getSelectedItem()).getValue()) {
            return true;
        }

        // Check if DataType has changed
        if (cmbSttDataType.getSelectionModel().getSelectedItem() == null) {
            return true;
        }
        if (item.getDataType().toString() != cmbSttDataType.getSelectionModel().getSelectedItem()) {
            return true;
        }

        // Check if Value has changed
        if (item.getValue() == null && ! isTextBoxStringNull(txtSttValue.getText())) {
            return true;
        }
        if (item.getValue() != null) {
            if (item.getValue() instanceof Boolean) {
                if (getBooleanValueFromTextBoxString(txtSttValue.getText()) == null || item.getValue() != getBooleanValueFromTextBoxString(txtSttValue.getText())) {
                    return true;
                }
                else {
                    if (! item.getValue().toString().equals(txtSttValue.getText())) {
                        return true;
                    }
                }
            }
            else {
                if (! item.getValue().toString().equals(txtSttValue.getText())) {
                    return true;
                }
            }
        }

        // Check if DefaultValue has changed
        if (item.getDefaultValue() == null && ! isTextBoxStringNull(txtSttDefValue.getText())) {
            return true;
        }
        if (item.getDefaultValue() != null) {
            if (item.getDefaultValue() instanceof Boolean) {
                if (getBooleanValueFromTextBoxString(txtSttDefValue.getText()) == null || item.getDefaultValue() != getBooleanValueFromTextBoxString(txtSttDefValue.getText())) {
                    return true;
                }
                else {
                    if (! item.getDefaultValue().toString().equals(txtSttDefValue.getText())) {
                        return true;
                    }
                }
            }
            else {
                if (! item.getDefaultValue().toString().equals(txtSttDefValue.getText())) {
                    return true;
                }
            }
        }

        // Check if Minimum value has changed
        if (item.getMin() == null && ! isTextBoxStringNull(txtSttMin.getText())) {
            return true;
        }
        if (item.getMin() != null) {
            if (! item.getMin().toString().equals(txtSttMin.getText())) {
                return true;
            }
        }

        // Check if Maximum value has changed
        if (item.getMax() == null && ! isTextBoxStringNull(txtSttMax.getText())) {
            return true;
        }
        if (item.getMax() != null) {
            if (! item.getMax().toString().equals(txtSttMax.getText())) {
                return true;
            }
        }

        // Check if Description has changed
        if (item.getDescription() == null) {
            return true;
        }
        if (! item.getDescription().equals(txtSttDesc.getText())) {
            return true;
        }

        return false;
    }

    private boolean isTextBoxStringNull(String str) {
        if (str.toLowerCase().equals("null")
            || str.toLowerCase().equals("none")
            || str.toLowerCase().equals("nan")) {
            return true;
        }
        return false;
    }

    private Boolean getBooleanValueFromTextBoxString(String str) {
        if (str.toLowerCase().equals("false")) {
            return false;
        }
        if (str.toLowerCase().equals("true")) {
            return true;
        }

        return null;
    }

    private void sttCurrentItemChanged(String itemName) {
        if (sttVisibleList.equals("Changed")) {
            sttChangedCurrentItem = itemName;
        }
        else {
            sttLoadedCurrentItem = itemName;
        }

        populateSttItem(itemName);
    }

    private void populateSttItem(String itemName) {
        SettingsItem item;
        if (sttChangedList.contains(itemName)) {
            item = (SettingsItem) sttChangedMap.get(itemName);
        }
        else {
            item = (SettingsItem) sttLoadedMap.get(itemName);
        }

        if (item == null) {
            log("Settings item not found: '" + itemName + "'. Creating new item.");
            item = new SettingsItem();
            item.setSettingType(SettingType.DEFAULT);
            item.setDataType(DataType.STRING);
        }

        // Key
        if (!txtSttKey.getText().equals(item.getKey())) {
            txtSttKey.setText(item.getKey());
        }
        
        // Settings flag
        cmbSttFlag.setValue(SettingType.DEFAULT.toString());
        
        // Value
        txtSttValue.setText("");
        if (item.getValue() == null) {
            txtSttValue.setText("Null");
        }
        else if (item.getValue() instanceof Boolean) {
            Boolean valBoolean = (Boolean) item.getValue();
            if (valBoolean == true) {
                txtSttValue.setText("True");
            }
            else {
                txtSttValue.setText("False");
            }
        }
        else {
            txtSttValue.setText(item.getValue().toString());
        }
        
        // Default value
        txtSttDefValue.setText("");
        if (item.getDefaultValue() == null) {
            txtSttDefValue.setText("Null");
        }
        else if (item.getDefaultValue() instanceof Boolean) {
            Boolean defValBoolean = (Boolean) item.getDefaultValue();
            if (defValBoolean == true) {
                txtSttDefValue.setText("True");
            }
            else {
                txtSttDefValue.setText("False");
            }
        }
        else {
            txtSttDefValue.setText(item.getDefaultValue().toString());
        }

        // Min
        txtSttMin.setText("");
        if (item.getMin() != null) {
            txtSttMin.setText(item.getMin().toString());
        }
        else {
            txtSttMin.setText("Null");
        }

        // Max
        txtSttMax.setText("");
        if (item.getMax() != null) {
            txtSttMax.setText(item.getMax().toString());
        }
        else {
            txtSttMax.setText("Null");
        }

        // Description
        txtSttDesc.setText("");
        if (item.getDescription() != null) {
            txtSttDesc.setText(item.getDescription());
        }

        updateSttWidgetsAppearance();
    }

    // DataType methods

    private void updateLblSttInfo(List<String> availableDataTypes) {
        List<String> listOfAvailableDataTypes = new ArrayList<>();
        if (availableDataTypes != null) {
            listOfAvailableDataTypes = availableDataTypes;
        }
        else {
           listOfAvailableDataTypes = getAvailableDataTypes(txtSttValue.getText());
        }

        String text = "";

        for (String dataType : listOfAvailableDataTypes) {
            text = text + dataType + ", ";
        }
        if (text.endsWith(", ")) { text = text.substring(0, text.length() - 2); }
        
        if (listOfAvailableDataTypes.isEmpty()) {
            if (lblSttInfo.getStyleClass().contains("stt-info-label")) {
                lblSttInfo.getStyleClass().remove("stt-info-label");
            }
            if (!lblSttInfo.getStyleClass().contains("stt-info-empty-label")) {
                lblSttInfo.getStyleClass().add("stt-info-empty-label");
            }
            text = "String only";
        }
        else {
            if (lblSttInfo.getStyleClass().contains("stt-info-empty-label")) {
                lblSttInfo.getStyleClass().remove("stt-info-empty-label");
            }
            if (!lblSttInfo.getStyleClass().contains("stt-info-label")) {
                lblSttInfo.getStyleClass().add("stt-info-label");
            }
        }

        lblSttInfo.setText(text);
    }

    private ArrayList<String> getAvailableDataTypes (String text) {
        if (text == null) {
            return new ArrayList<>();
        }

        ArrayList<String> availableDataTypes = new ArrayList<>();

        if (isTextBoxStringNull(text)) {
            availableDataTypes.add("Null");
        }
        if (getChar(text) != null) {
            availableDataTypes.add("Char");
        }
        if (getInteger(text) != null) {
            availableDataTypes.add("Integer");
        }
        if (getLong(text) != null) {
            availableDataTypes.add("Long");
        }
        if (getBoolean(text) != null) {
            availableDataTypes.add("Boolean");
        }
        if (getFloat(text) != null) {
            availableDataTypes.add("Float");
        }
        if (getDouble(text) != null) {
            availableDataTypes.add("Double");
        }
        ArrayList<Object> list = getList(text);
        if (list != null) {
            availableDataTypes.add("List(" + list.size() + ")");
        }
        PyDict dict = getMap(text);
        if (dict != null) {
            availableDataTypes.add("Map(" + dict.size() + ")");
        }
        if (getDate(text) != null) {
            availableDataTypes.add("Date");
        }
        if (getTime(text) != null) {
            availableDataTypes.add("Time");
        }
        if (getDateTime(text) != null) {
            availableDataTypes.add("DateTime");
        }

        return availableDataTypes;
    }

    private void setAutoDataType(List<String> availableDataTypes) {
        if (! chkAutoDataType.isSelected()) {
            return;
        }

        boolean isList = false;
        boolean isMap = false;

        for (String dataType : availableDataTypes) {
            if (dataType.startsWith("List")) {
                isList = true;
            }
            if (dataType.startsWith("Map")) {
                isMap = true;
            }
        }
        
        if (availableDataTypes.contains("DateTime")) {
            cmbSttDataType.getSelectionModel().select(DataType.DATETIME.toString());
        }
        else if (availableDataTypes.contains("Date")) {
            cmbSttDataType.getSelectionModel().select(DataType.DATE.toString());
        }
        else if (availableDataTypes.contains("Time")) {
            cmbSttDataType.getSelectionModel().select(DataType.TIME.toString());
        }
        else if (isList) {
            cmbSttDataType.getSelectionModel().select(DataType.LIST.toString());
        }
        else if (isMap) {
            cmbSttDataType.getSelectionModel().select(DataType.MAP.toString());
        }
        else if (availableDataTypes.contains("Boolean")) {
            cmbSttDataType.getSelectionModel().select(DataType.BOOLEAN.toString());
        }
        else if (availableDataTypes.contains("Null")) {
            cmbSttDataType.getSelectionModel().select(DataType.NULL.toString());
        }
        else if (availableDataTypes.contains("Integer")) {
            cmbSttDataType.getSelectionModel().select(DataType.INTEGER.toString());
        }
        else if (availableDataTypes.contains("Long")) {
            cmbSttDataType.getSelectionModel().select(DataType.LONG.toString());
        }
        else if (availableDataTypes.contains("Double")) {
            cmbSttDataType.getSelectionModel().select(DataType.DOUBLE.toString());
        }
        else if (availableDataTypes.contains("Float")) {
            cmbSttDataType.getSelectionModel().select(DataType.FLOAT.toString());
        }
        else {
            cmbSttDataType.getSelectionModel().select(DataType.STRING.toString());
        }
    }

    /**
     * If string can be converted to char, returns Char value, otherwise returns null
     * @param data - string to check
     * @return - char value or null
     */
    private Character getChar (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getChar', cannot check if value string is a valid Char object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getChar': Data passed to method is null");
            return null;
        }
        else if (data.length() == 1) {
            return (char) data.charAt(0);
        }
        else {
            return null;
        }
    }

    /**
     * If string can be converted to integer, returns Integer value, otherwise returns null
     * @param data - string to check
     * @return - integer value or null
     */
    private Integer getInteger (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getInteger', cannot check if value string is a valid Integer object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getInteger': Data passed to method is null");
            return null;
        }
        else {
            try {
                return Integer.parseInt(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to boolean, returns Boolean value, otherwise returns null
     * @param data - string to check
     * @return - boolean value or null
     */
    private Boolean getBoolean (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getBoolean', cannot check if value string is a valid Boolean object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getBoolean': Data passed to method is null");
            return null;
        }
        else if (data.toLowerCase().equals("true")) {
            return true;
        }
        else if (data.toLowerCase().equals("false")) {
            return false;
        }
        else {
            return null;
        }
    }

    /**
     * If string can be converted to long, returns Long value, otherwise returns null
     * @param data - string to check
     * @return - long value or null
     */
    private Long getLong (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getLong', cannot check if value string is a valid Long object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getLong': Data passed to method is null");
            return null;
        }
        else {
            try {
                return Long.parseLong(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to float, returns Float value, otherwise returns null
     * @param data - string to check
     * @return - float value or null
     */
    private Float getFloat (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getFloat', cannot check if value string is a valid Float object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getFloat': Data passed to method is null");
            return null;
        }
        else {
            try {
                return Float.parseFloat(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to double, returns Double value, otherwise returns null
     * @param data - string to check
     * @return - double value or null
     */
    private Double getDouble (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getDouble', cannot check if value string is a valid Double object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getDouble': Data passed to method is null");
            return null;
        }
        else {
            try {
                return Double.parseDouble(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }    

    /**
     * If string can be converted to Map, returns PyDict (HashMap) value, otherwise returns null
     * @param data - string to check
     * @return - PyDict value or null
     */
    private PyDict getMap (String data) {
        Gson gson = new Gson();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(data, HashMap.class);
            if (map == null) {
                return null;
            }
            PyDict pyDict = new PyDict();
            pyDict.putAll(map);
            return pyDict;
        }
        catch (JsonSyntaxException e) {
            return null;
        }
        catch (Exception e) {
            MsgInfo msg = new MsgInfo("Exception", "Unexpected error !", "Exception occurred in 'getMap', cannot check if value string is a valid Map object\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getMap': " + e.getMessage());
            return null;
        }
    }

    /**
     * If string can be converted to List, returns ArrayList value, otherwise returns null
     * @param data - string to check
     * @return - ArrayList value or null
     */
    private ArrayList<Object> getList (String data) {
        Gson gson = new Gson();
        try {
            @SuppressWarnings("unchecked")
            List<Object> list = gson.fromJson(data, ArrayList.class);
            if (list == null) {
                return null;
            }
            return (ArrayList<Object>) list;
        }
        catch (JsonSyntaxException e) {
            return null;
        }
        catch (Exception e) {
            MsgInfo msg = new MsgInfo("Exception", "Unexpected error !", "Exception occurred in 'getList', cannot check if value string is a valid List object\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getList': " + e.getMessage());
            return null;
        }
    }

    /**
     * If string can be converted to LocalDate, returns LocalDate value, otherwise returns null
     * @param data - string to check
     * @return - LocalDate value or null
     */
    private LocalDate getDate (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getDate', cannot check if value string is a valid Date object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getDate': Data passed to method is null");
            return null;
        }
        else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
                if (!data.endsWith(".")) {
                    data += ".";
                }
                return LocalDate.parse(data, formatter);
            }
            catch (DateTimeParseException e) {
                return null;
            }
            catch (Exception e) {
                MsgInfo msg = new MsgInfo("Exception", "Unexpected error !", "Exception occurred in 'getDate', cannot check if value string is a valid LocalDate object\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
                showMessage(msg);
                log("EXCEPTION in 'getDate': " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * If string can be converted to LocalTime, returns LocalTime value, otherwise returns null
     * @param data - string to check
     * @return - LocalTime value or null
     */
    private LocalTime getTime (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getTime', cannot check if value string is a valid Time object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getTime': Data passed to method is null");
            return null;
        }
        else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                return LocalTime.parse(data, formatter);
            }
            catch (DateTimeParseException e) {
                return null;
            }
            catch (Exception e) {
                MsgInfo msg = new MsgInfo("Exception", "Unexpected error !", "Exception occurred in 'getTime', cannot check if value string is a valid LocalTime object\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
                showMessage(msg);
                log("EXCEPTION in 'getTime': " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * If string can be converted to LocalDateTime, returns LocalDateTime value, otherwise returns null
     * @param data - string to check
     * @return - LocalDateTime value or null
     */
    private LocalDateTime getDateTime (String data) {
        if (data == null) {
            MsgInfo msg = new MsgInfo("Error", "Unexpected error !", "Error occurred in 'getDateTime', cannot check if value string is a valid DateTime object\nData passed to method is null", MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
            showMessage(msg);
            log("EXCEPTION in 'getDateTime': Data passed to method is null");
            return null;
        }
        else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
                return LocalDateTime.parse(data, formatter);
            }
            catch (DateTimeParseException e) {
                return null;
            }
            catch (Exception e) {
                MsgInfo msg = new MsgInfo("Exception", "Unexpected error !", "Exception occurred in 'getDateTime', cannot check if value string is a valid LocalDateTime object\n" + e.getMessage(), MsgInfo.MsgStyle.ERROR, MsgInfo.ErrorCode.NONE, 10, -1, false);
                showMessage(msg);
                log("EXCEPTION in 'getDateTime': " + e.getMessage());
                return null;
            }
        }
    }


    // LANGUAGE METHODS

    private List<LanguagesEnum> getMissingLanguagesForCurrentEditItem () {
        List<String> updateFilesList = new ArrayList<>();
        for (String file : updateLangFilesPaths) {
            updateFilesList.add(file);
        }
        if (loadLangFromPath != null && !loadLangFromPath.isEmpty()) {
            updateFilesList.add(loadLangFromPath);
        }

        List<LanguagesEnum> requiredLangs = scrollPaneContent.getListOfRequiredLanguages(updateFilesList);
        List<LanguagesEnum> hasLangs = new ArrayList<>();
        List<LanguagesEnum> missingLangs = new ArrayList<>();

        // hasLangs
        LanguageItemGroup itemGroup = scrollPaneContent.getValueAsLanguageItemGroup("DsoftN");
        for (LanguageItem item : itemGroup.getLanguageItems()) {
            hasLangs.add(LanguagesEnum.fromLangCode(item.getLanguageCode()));
        }

        // missingLangs
        for (LanguagesEnum lang : requiredLangs) {
            if (!hasLangs.contains(lang)) {
                missingLangs.add(lang);
            }
        }

        return missingLangs;
    }

    private void unChangeLanguageItem() {
        log("UnChange Language item started...");
        logIndentPlus();

        // Check if has valid key
        if (! validateEditedLanguageItem()) {
            logIndentMinus();
            return;
        }

        // Check if item exists
        if (! isLangItemExists(txtLangKey.getText())) {
            log("Unable to UnChange Language item: " + txtLangKey.getText() + " - Language item does not exist.");
            log("UnChanging Language item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Error", "Language item does not exist", "Unable to UnChange Language item: " + txtLangKey.getText() + " - Language item does not exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return;
        }
        
        // UnMark item as changed
        log("UnMarking Settings item as Changed...");
        logIndentPlus();

        LanguageItemGroup item = null;
        if (langChangedMap.containsKey(txtLangKey.getText())) {
            item = langChangedMap.getPyDictValue(txtLangKey.getText());
        }

        // If item is not found in 'langChangedMap', stop and show error
        if (item == null) {
            log("Unable to UnChange item '" + txtLangKey.getText() + "'. Item is not found in 'langChangedMap'.");
            MsgInfo msg = new MsgInfo("Error", "Item not found", "Unable to UnChange item '" + txtLangKey.getText() + "'. Item is not found in 'langChangedMap'.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnChanging Language item stopped.");
            logIndentMinus();
            return;
        }
        
        // If item is found in 'langChangedMap' and marked as 'Changed' remove it from 'langChangedMap' and 'langChangedList'
        if (item.getUserData().equals("Changed")) {
            // Remove item from langChangedMap and langChangedList
            langChangedMap.remove(txtLangKey.getText());
            langChangedList.remove(txtLangKey.getText());
            log("Item '" + txtLangKey.getText() + "' removed from 'langChangedMap' and 'langChangedList'.");
        }
        else {
            log("Unable to UnChange item '" + txtLangKey.getText() + "'. Item is marked as '" + item.getUserData() + "'', not as 'Changed'.");
            MsgInfo msg = new MsgInfo("Error", "Item not marked as 'Changed'", "Unable to UnChange item '" + txtLangKey.getText() + "'. Item is not marked as 'Changed'.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnChanging Language item stopped.");
            logIndentMinus();
            return;
        }

        logIndentMinus();

        // Refreshing display
        changeLangVisibleList(langVisibleList);
        txtLangKey.setText(txtLangKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("UnChange Language item", "Changes to Language item are discarded successfully", "Language item UnChanged: " + txtLangKey.getText(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Language item UnChanged: " + txtLangKey.getText(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getX(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getY(), 10);

        log ("UnChanging Language item completed successfully.");

        logIndentMinus();
    }

    private void unDeleteLanguageItem() {
        log("UnDelete Language item started...");
        logIndentPlus();

        // Check if has valid key
        if (! validateEditedLanguageItem()) {
            logIndentMinus();
            return;
        }
        
        // Check if item exists
        if (! isLangItemExists(txtLangKey.getText())) {
            log("Unable to UnDelete Language item: " + txtLangKey.getText() + " - Language item does not exist.");
            log("UnDeleting Language item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Error", "Language item does not exist", "Unable to UnDelete Language item: " + txtLangKey.getText() + " - Language item does not exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return;
        }

        // UnMark item as deleted
        log("UnMarking Language item as Deleted...");
        logIndentPlus();

        LanguageItemGroup item = null;
        if (langChangedMap.containsKey(txtLangKey.getText())) {
            item = langChangedMap.getPyDictValue(txtLangKey.getText());
        }

        // If item is not found in 'langChangedMap', stop and show error
        if (item == null) {
            log("Unable to UnMark item '" + txtLangKey.getText() + "' as Deleted. Item is not found in 'langChangedMap'.");
            MsgInfo msg = new MsgInfo("Error", "Item not found", "Unable to UnMark item '" + txtLangKey.getText() + "' as Deleted. Item is not found in 'langChangedMap'.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnDeleting Language item stopped.");
            logIndentMinus();
            return;
        }

        if (item.getUserData().equals("Deleted")) {
            if (item.equals(langLoadedMap.getPyDictValue(txtLangKey.getText()))) {
                // Case when item should be deleted from 'langChangedMap' because it is equal to item in 'langLoadedMap'
                langChangedMap.remove(txtLangKey.getText());
                // Delete also from langChangedList
                if (langChangedList.contains(txtLangKey.getText())) {
                    langChangedList.remove(txtLangKey.getText());
                }
                log("Item '" + txtLangKey.getText() + "'' unmarked as deleted.");
                MsgInfo msg = new MsgInfo("Info", "Item UnDeleted", "Item '" + txtLangKey.getText() + "'' unmarked as deleted.", MsgInfo.MsgStyle.INFORMATION);
                showMessage(msg);
            }
            else {
                //  Case when item exists in both 'langLoadedMap' and 'langChangedMap' and is NOT equal to item in 'langLoadedMap'
                // Switch item from "Deleted" to "Changed"
                item.setUserData("Changed");
                log("Item '" + txtLangKey.getText() + "'' marked from 'Deleted' to 'Changed'.");
                MsgInfo msg = new MsgInfo("Info", "Item UnDeleted and marked as 'Changed'", "Item '" + txtLangKey.getText() + "'' marked from 'Deleted' to 'Changed'.", MsgInfo.MsgStyle.INFORMATION);
                showMessage(msg);
            }
        }
        else {
            log("Unable to UnDelete item '" + txtLangKey.getText() + "'. Item is not marked as deleted.");
            MsgInfo msg = new MsgInfo("Error", "Item not marked as deleted", "Unable to UnDelete item '" + txtLangKey.getText() + "'. Item is not marked as deleted.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("UnDeleting Language item stopped.");
            logIndentMinus();
            return;
        }

        logIndentMinus();

        // Refreshing display
        changeLangVisibleList(langVisibleList);
        txtLangKey.setText(txtLangKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("UnDelete Language item", "Language item UNDeleted successfully", "Language item UnDeleted: " + txtLangKey.getText(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Language item UnDeleted: " + txtLangKey.getText(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getX(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getY(), 10);

        log ("UnDeleting Language item completed successfully.");

        logIndentMinus();
    }

    private void deleteLanguageItem() {
        log("Delete Language item started...");
        logIndentPlus();

        // Check if has valid key
        if (! validateEditedLanguageItem()) {
            logIndentMinus();
            return;
        }

        // Check if item exists
        if (! isLangItemExists(txtLangKey.getText())) {
            log("Unable to delete Language item: " + txtLangKey.getText() + " - Language item does not exist.");
            log("Deleting Language item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Error", "Language item does not exist", "Unable to delete Language item: " + txtLangKey.getText() + " - Language item does not exist.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            return;
        }

        // Mark item as deleted
        log("Marking Language item as deleted...");
        logIndentPlus();
        
        LanguageItemGroup groupItem = getLanguageItem(txtLangKey.getText()).duplicate();
        
        if (groupItem != null) {
            groupItem.setUserData("Deleted");
            if (langChangedMap.containsKey(txtLangKey.getText())) {
                // If item exists in 'langChangedMap' change only userData to be able UnDelete item and mark it as "Changed"
                LanguageItemGroup existingItem = langChangedMap.getPyDictValue(txtLangKey.getText());
                existingItem.setUserData("Deleted");
            }
            else {
                // If item does not exist in 'langChangedMap' add new item to 'langChangedMap'
                langChangedMap.setPyDictValue(txtLangKey.getText(), groupItem);
            }
            
            // Add item to 'langChangedList' if needed
            if (! langChangedList.contains(txtLangKey.getText())) {
                langChangedList.add(txtLangKey.getText());
            }

            log("Item '" + txtLangKey.getText() + "'' marked as deleted.");
        }
        else {
            log("Unable to mark item '" + txtLangKey.getText() + "' as deleted. Item is not found.");
            MsgInfo msg = new MsgInfo("Error", "Language item not found", "Unable to mark item '" + txtLangKey.getText() + "' as deleted. Item is not found.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            logIndentMinus();
            log("Deleting Language item stopped.");
            logIndentMinus();
            return;
        }

        logIndentMinus();

        // Refreshing display
        changeLangVisibleList(langVisibleList);
        txtLangKey.setText(txtLangKey.getText());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("Delete Language item", "Language item deleted", "Language item deleted: " + txtLangKey.getText(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Language item deleted: " + txtLangKey.getText(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getX(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getY(), 10);

        log("Language item marked as deleted in list of changed items: " + txtLangKey.getText());
        log ("Deleting Language item completed successfully.");

        logIndentMinus();
    }

    private void onEventEditLanguageContentChanged(EventEditLanguageContentChanged event) {
        if (event.isChanged()) {
            setWidgetIcon("/images/record.png", lblLangRec, 25);
        }
        else {
            setWidgetIcon("/images/show.png", lblLangRec, 25);
        }
    }

    /**
     * If item exists in langChangedMap it will return it, otherwise it will return item from langLoadedMap
     * @param itemName item name (Key)
     * @return LanguageItemGroup or null if not found
     */
    private LanguageItemGroup getLanguageItem(String itemName) {
        if (itemName.isEmpty()) {
            return null;
        }

        if (langChangedMap.containsKey(itemName)) {
            return (LanguageItemGroup) langChangedMap.get(itemName);
        }

        if (langLoadedMap.containsKey(itemName)) {
            return (LanguageItemGroup) langLoadedMap.get(itemName);
        }

        return null;
    }

    private boolean validateEditedLanguageItem() {
        if (txtLangKey.getText().isEmpty()) {
            log("Unable to process Language item: " + txtLangKey.getText() + " - Key is empty.");
            log("Processing Language item stopped.");
            
            MsgInfo msg = new MsgInfo("Error", "Key is empty", "Unable to process Language item: " + txtLangKey.getText() + " - Key is empty.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            msgBoxInfoCritical("Error", "Key is empty", "Unable to process Language item because KEY is empty.");
            return false;
        }

        LanguageItemGroup item = scrollPaneContent.getValueAsLanguageItemGroup(txtLangKey.getText());
        if (item.getLanguageItems().size() == 0) {
            log("Unable to process Language item: " + txtLangKey.getText() + " - Item has no languages.");
            log("Processing Language item stopped.");

            MsgInfo msg = new MsgInfo("Error", "Item has no languages", "Unable to process Language item: " + txtLangKey.getText() + " - Item has no languages.", MsgInfo.MsgStyle.ERROR);
            showMessage(msg);
            msgBoxInfoCritical("Error", "Item has no languages", "Unable to process Language item because item has no languages.");
            return false;
        }

        return true;
    }

    private void addNewLanguageItem() {
        log("Add new Language item started...");
        logIndentPlus();

        // Check if has valid key
        if (! validateEditedLanguageItem()) {
            logIndentMinus();
            return;
        }

        // Check if item already exists
        if (isLangItemExists(txtLangKey.getText())) {
            log("Unable to add new Language item: " + txtLangKey.getText() + " - Language item already exists.");
            log("Adding new Language item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Warning", "Language item already exists", "Unable to add new Language item: " + txtLangKey.getText() + " - Language item already exists.", MsgInfo.MsgStyle.WARNING);
            showMessage(msg);
            return;
        }

        // Check for missing languages
        List<LanguagesEnum> missingLangs = getMissingLanguagesForCurrentEditItem();

        if (missingLangs.size() > 0) {
            log("Warning:");
            logIndentPlus();
            log("Missing languages found: " + missingLangs.stream().map(LanguagesEnum::toString).collect(Collectors.joining(", ")));
            String msgText = "The files that will be updated require some languages that do not exist in the Language Item you want to add!\n\nMissing languages: ";
            msgText += missingLangs.stream().map(LanguagesEnum::toString).collect(Collectors.joining(", "));
            msgText += "\n\nDo you want to continue?";

            boolean continueResult = msgBoxInfoQuestion("Missing languages", "Missing languages", msgText);

            if (!continueResult) {
                log("User cancelled adding new Language item.");
                log("Adding new Language item stopped.");
                logIndentMinus();
                return;
            }

            log("User accepted adding new Language item ... continuing.");
            logIndentMinus();
        }

        // Create new LanguageItemGroup Object
        LanguageItemGroup newLanguageItemGroup = scrollPaneContent.getValueAsLanguageItemGroup(txtLangKey.getText());
        newLanguageItemGroup.setUserData("Changed");

        log ("Created new LanguageItemGroup item: ");
        logIndentPlus();
        for (LanguageItem item : newLanguageItemGroup.getLanguageItems()) {
            log ("Key: " + item.getKey() + ", LangCode: " + item.getLanguageCode() + ", Value: " + item.getValue());
        }
        logIndentMinus();

        // Add new Settings item to list
        langChangedList.add(newLanguageItemGroup.getGroupKey());
        langChangedMap.put(newLanguageItemGroup.getGroupKey(), newLanguageItemGroup);
        LanguageItemGroup newLanguageItemGroup2 = newLanguageItemGroup.duplicate();
        newLanguageItemGroup2.setUserData("");
        langLoadedMap.put(newLanguageItemGroup2.getGroupKey(), newLanguageItemGroup2);

        // Update content
        scrollPaneContent.setLanguageItemGroup(newLanguageItemGroup);

        // Refreshing display
        changeLangVisibleList(langVisibleList);
        txtLangKey.setText(newLanguageItemGroup.getGroupKey());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("Add new Language item", "New Language item added", "New Language item added: " + newLanguageItemGroup.getGroupKey(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("New Language item added: " + newLanguageItemGroup.getGroupKey(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getX(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getY(), 10);

        log("Added new Language item to list of changed items: " + newLanguageItemGroup.getGroupKey());
        log ("Adding new Language item completed successfully.");

        logIndentMinus();

    }

    private void updateLanguageItem() {
        log("Update Language item started...");
        logIndentPlus();

        // Check if has valid key
        if (! validateEditedLanguageItem()) {
            logIndentMinus();
            return;
        }
        
        // Check if item does not exists
        if (! isLangItemExists(txtLangKey.getText()) || txtLangKey.getText().isEmpty()) {
            log("Unable to update Language item: " + txtLangKey.getText() + " - Language item does not exists.");
            log("Updating Language item stopped.");
            logIndentMinus();
            MsgInfo msg = new MsgInfo("Warning", "Language item does not exists", "Unable to update Language item: " + txtLangKey.getText() + " - Language item does not exists.", MsgInfo.MsgStyle.WARNING);
            showMessage(msg);
            return;
        }

        // Check for missing languages
        List<LanguagesEnum> missingLangs = getMissingLanguagesForCurrentEditItem();

        if (missingLangs.size() > 0) {
            log("Warning:");
            logIndentPlus();
            log("Missing languages found: " + missingLangs.stream().map(LanguagesEnum::toString).collect(Collectors.joining(", ")));
            String msgText = "The files that will be updated require some languages that do not exist in the Language Item you want to update!\n\nMissing languages: ";
            msgText += missingLangs.stream().map(LanguagesEnum::toString).collect(Collectors.joining(", "));
            msgText += "\n\nDo you want to continue?";

            boolean continueResult = msgBoxInfoQuestion("Missing languages", "Missing languages", msgText);

            if (!continueResult) {
                log("User cancelled updating new Language item.");
                log("Updating new Language item stopped.");
                logIndentMinus();
                return;
            }

            log("User accepted updating new Language item ... continuing.");
            logIndentMinus();
        }
        
        // Create new LanguageItemGroup Object
        LanguageItemGroup updatedLanguageItemGroup = scrollPaneContent.getValueAsLanguageItemGroup(txtLangKey.getText());
        updatedLanguageItemGroup.setUserData("Changed");

        log ("Created updated LanguageItemGroup item: ");
        logIndentPlus();
        for (LanguageItem item : updatedLanguageItemGroup.getLanguageItems()) {
            log ("Key: " + item.getKey() + ", LangCode: " + item.getLanguageCode() + ", Value: " + item.getValue());
        }
        logIndentMinus();

        // Update Language item in list
        if (! langChangedList.contains(updatedLanguageItemGroup.getGroupKey())) {
            langChangedList.add(updatedLanguageItemGroup.getGroupKey());
        }
        langChangedMap.put(updatedLanguageItemGroup.getGroupKey(), updatedLanguageItemGroup);

        // Update content
        scrollPaneContent.setLanguageItemGroup(updatedLanguageItemGroup);

        // Refreshing display
        changeLangVisibleList(langVisibleList);
        txtLangKey.setText(updatedLanguageItemGroup.getGroupKey());

        // Save AppState
        if (chkSaveState.isSelected()) {
            saveAppState();
        }

        // Finish
        MsgInfo msg = new MsgInfo("Update Language item", "Language item is updated", "Language item updated: " + updatedLanguageItemGroup.getGroupKey(), MsgInfo.MsgStyle.INFORMATION);
        showMessage(msg);
        showToolTipLabel("Language item updated: " + updatedLanguageItemGroup.getGroupKey(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getX(), txtLangKey.localToScene(0, txtLangKey.getHeight() + 3).getY(), 10);

        log("Updated Language item in list of changed items: " + updatedLanguageItemGroup.getGroupKey());
        log("Updating Language item completed successfully.");

        logIndentMinus();
    }

    private void changeLangVisibleList(String listToActivate) {
        if (listToActivate.equals("Changed")) {
            populateLangList(langChangedMap, langChangedMap, txtLangSearch.getText());
            langVisibleList = "Changed";
            setCurrentItemInChangedListLang(langChangedCurrentItem, null);
        }
        else {
            populateLangList(langLoadedMap, langChangedMap, txtLangSearch.getText());
            langVisibleList = "Loaded";
            setCurrentItemInLoadedListLang(langLoadedCurrentItem, null);
        }

        log("Language visible list changed to: " + langVisibleList);
        
        if (lstLang.getSelectionModel().getSelectedIndex() != -1) {
            lstLang.scrollTo(lstLang.getSelectionModel().getSelectedIndex());
        }
        
        updateLangWidgetsAppearance();
    }

    private void populateLangList() {
        if (langVisibleList.equals("Changed")) {
            populateLangList(langChangedMap, langChangedMap, txtLangSearch.getText());
        }
        else {
            populateLangList(langLoadedMap, langChangedMap, txtLangSearch.getText());
        }
    }

    private void populateLangList(PyDict langItemsMap, PyDict langChangedItemsMap, String filterText) {
        List<LanguageItemGroup> itemsList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : langItemsMap.entrySet()) {
            if (entry.getValue() instanceof LanguageItemGroup) {
                LanguageItemGroup item = (LanguageItemGroup) entry.getValue();

                if (filterText.isEmpty() || item.getGroupKey().toLowerCase().contains(filterText.toLowerCase())) {
                    itemsList.add(item);
                }
            }
        }
        // Sort List ("Key" or "Created")
        if (lstLangSortKey.equals("Key")) {
            itemsList.sort(Comparator.comparing(LanguageItemGroup::getGroupKey));
        }
        else {
            itemsList.sort(Comparator.comparing(LanguageItemGroup::getCreationDateForJson));
        }

        // clear list
        if (lstLang.getItems() != null) {
            // lstLang.getItems().clear();
            lstLang.setItems(null);
        }
        // add items
        ObservableList<String> items = FXCollections.observableArrayList();
        for (LanguageItemGroup itemName : itemsList) {
            items.add(itemName.getGroupKey());
        }

        lstLang.setItems(items);

        // Try to select old Key if it exists
        if (langVisibleList.equals("Changed")) {
            setCurrentItemInChangedListLang(langChangedCurrentItem, null);
        }
        else {
            setCurrentItemInLoadedListLang(langLoadedCurrentItem, null);
        }

        // Try to refresh list display
        lstLang.layout();
    }

    private int setCurrentItemInChangedListLang(String itemText, Integer itemIndex) {
        if (itemIndex != null && itemIndex < 0) itemIndex = null;

        lstLang.getSelectionModel().clearSelection();
        if (langChangedMap.containsKey(itemText)) {
            langChangedCurrentItem = itemText;
            lstLang.getSelectionModel().select(itemText);
            return lstLang.getSelectionModel().getSelectedIndex();
        }

        if (itemIndex != null) {
            int listItemsCount = lstLang.getItems().size();
            if (itemIndex > (listItemsCount - 1)) {
                itemIndex = listItemsCount - 1;
            }
            
            if (lstLang.getItems().size() <= 0) {
                itemIndex = null;
            }
            else {
                if (itemIndex < 0) {
                    itemIndex = 0;
                }
                langChangedCurrentItem = lstLang.getItems().get(itemIndex);
                lstLang.getSelectionModel().select(itemIndex);
                return itemIndex;
            }

        }
        
        lstLang.getSelectionModel().selectFirst();
        if (lstLang.getSelectionModel().getSelectedIndex() == -1) {
            langChangedCurrentItem = "";
        }
        else {
            langChangedCurrentItem = lstLang.getSelectionModel().getSelectedItem();
        }
        return lstLang.getSelectionModel().getSelectedIndex();
    }

    private int setCurrentItemInLoadedListLang(String itemText, Integer itemIndex) {
        if (itemIndex != null && itemIndex < 0) itemIndex = null;

        lstLang.getSelectionModel().clearSelection();
        if (langLoadedMap.containsKey(itemText)) {
            langLoadedCurrentItem = itemText;
            lstLang.getSelectionModel().select(itemText);
            return lstLang.getSelectionModel().getSelectedIndex();
        }

        if (itemIndex != null) {
            int listItemsCount = lstLang.getItems().size();
            if (itemIndex > (listItemsCount - 1)) {
                itemIndex = listItemsCount - 1;
            }
            
            if (lstLang.getItems().size() <= 0) {
                itemIndex = null;
            }
            else {
                if (itemIndex < 0) {
                    itemIndex = 0;
                }
                langLoadedCurrentItem = lstLang.getItems().get(itemIndex);
                lstLang.getSelectionModel().select(itemIndex);
                return itemIndex;
            }

        }
        
        lstLang.getSelectionModel().selectFirst();
        if (lstLang.getSelectionModel().getSelectedIndex() == -1) {
            langLoadedCurrentItem = "";
        }
        else {
            langLoadedCurrentItem = lstLang.getSelectionModel().getSelectedItem();
        }
        return lstLang.getSelectionModel().getSelectedIndex();
    }

    private void updateLangWidgetsAppearance() {
        // Visible Language list
        btnLangShowLoaded.getStyleClass().removeAll("button-list-selector-selected", "button-list-selector-not-selected");
        btnLangShowChanged.getStyleClass().removeAll("button-list-selector-selected", "button-list-selector-not-selected");
        if (langVisibleList.equals("Changed")) {
            btnLangShowChanged.getStyleClass().add("button-list-selector-selected");
            btnLangShowLoaded.getStyleClass().add("button-list-selector-not-selected");
        }
        else {
            btnLangShowLoaded.getStyleClass().add("button-list-selector-selected");
            btnLangShowChanged.getStyleClass().add("button-list-selector-not-selected");
        }

        // Record counter
        if (langVisibleList.equals("Changed")) {
            lblLangRecords.setText("Records: " + lstLang.getItems().size() + " of " + langChangedMap.size());
        }
        else {
            lblLangRecords.setText("Records: " + lstLang.getItems().size() + " of " + langLoadedMap.size());
        }

        // Buttons "Show Loaded" and "Show Changed"
        btnLangShowLoaded.setText("Loaded (" + langLoadedMap.size() + ")");
        btnLangShowChanged.setText("Changed (" + langChangedMap.size() + ")");

        // Set command buttons availability
        boolean isExists = isLangItemExists(txtLangKey.getText());
        boolean isChanged = langChangedList.contains(txtLangKey.getText());
        boolean isDeleted = isLangItemDeleted(txtLangKey.getText());
        if (isExists) {
            setWidgetIcon("/images/edit.png", lblLangKeyImage, 50);
            btnLangAdd.setDisable(true);
            btnLangUpdate.setDisable(false);
            
            if (isChanged) {
                btnLangDiscard.setDisable(false);
                btnLangDiscard.setText("Discard Changes");
            }
            else {
                btnLangDiscard.setDisable(true);
                btnLangDiscard.setText("Discard ...");
            }

            if (isDeleted) {
                btnLangDelete.setDisable(true);
                btnLangDiscard.setDisable(false);
                btnLangDiscard.setText("Undo Delete");
            }
            else {
                btnLangDelete.setDisable(false);
            }
        }
        else {
            setWidgetIcon("/images/new.png", lblLangKeyImage, 50);
            btnLangAdd.setDisable(false);
            btnLangUpdate.setDisable(true);
            btnLangDelete.setDisable(true);
            btnLangDiscard.setDisable(true);
            btnLangDiscard.setText("Discard ...");
        }

        // Hide btnLangDiscard if it is disabled
        if (btnLangDiscard.isDisabled()) {
            btnLangDiscard.setVisible(false);
        }
        else {
            btnLangDiscard.setVisible(true);
        }

        checkIfUserIsChangingLanguage();

        log("Language widgets appearance updated");
    }

    private boolean isLangItemDeleted(String itemName) {
        LanguageItemGroup item;
        for (Map.Entry<String, Object> entry : langChangedMap.entrySet()) {
            item = (LanguageItemGroup) entry.getValue();
            if (item.getGroupKey().equals(itemName) && item.getUserData().equals("Deleted")) {
                return true;
            }
        }

        return false;
    }

    private boolean isLangItemExists(String itemName) {
        if (getLangItem(itemName) != null) {
            return true;
        }

        return false;
    }

    /**
     * If item exists in langChangedMap it will return it, otherwise it will return item from langLoadedMap
     * @param itemName item name (Key)
     * @return LanguageItemGroup or null if not found
     */
    private LanguageItemGroup getLangItem(String itemName) {
        if (itemName.isEmpty()) {
            return null;
        }

        if (langChangedMap.containsKey(itemName)) {
            return (LanguageItemGroup) langChangedMap.get(itemName);
        }

        if (langLoadedMap.containsKey(itemName)) {
            return (LanguageItemGroup) langLoadedMap.get(itemName);
        }

        return null;
    }

    private void checkIfUserIsChangingLanguage() {
        boolean isUserChanging = isUserCurrentlyChangingLang(txtLangKey.getText());

        if (isUserChanging) {
            setWidgetIcon("/images/record.png", lblLangRec, 25);
        }
        else {
            setWidgetIcon("/images/show.png", lblLangRec, 25);
        }
    }
 
    private boolean isUserCurrentlyChangingLang(String itemName) {
        LanguageItemGroup item = getLangItem(itemName);
        if (item == null) {
            return true;
        }

        if (!itemName.equals(txtLangKey.getText())) {
            return false;
        }

        return scrollPaneContent.hasChangedSections();
    }

    private void populateLangItem(String itemName) {
        if (itemName == null) {
            // Add ScrollPane content
            scrollPaneContent.setLanguageItemGroup(null);

            updateLangWidgetsAppearance();
            return;
        }

        LanguageItemGroup item;
        if (langChangedList.contains(itemName)) {
            item = (LanguageItemGroup) langChangedMap.get(itemName);
        }
        else {
            item = (LanguageItemGroup) langLoadedMap.get(itemName);
        }

        if (item == null) {
            log("Language item not found: '" + itemName + "'. Creating new item.");
            item = new LanguageItemGroup();
        }

        // Key
        if (!txtLangKey.getText().equals(item.getGroupKey())) {
            txtLangKey.setText(item.getGroupKey());
        }
        
        // Add ScrollPane content
        scrollPaneContent.setLanguageItemGroup(item);

        updateLangWidgetsAppearance();
    }

    private void langCurrentItemChanged(String itemName) {
        if (langVisibleList.equals("Changed")) {
            langChangedCurrentItem = itemName;
        }
        else {
            langLoadedCurrentItem = itemName;
        }

        populateLangItem(itemName);
    }

    private void deleteLangChangedItem() {
        String item = lstLang.getSelectionModel().getSelectedItem().toString();
        if (item != null) {
            int index = lstLang.getSelectionModel().getSelectedIndex();
            String name = lstLang.getItems().get(index).toString();
            langChangedMap.remove(item);
            langChangedList.remove(item);
            populateLangList();
            setCurrentItemInChangedListLang(name, index);
            changeLangVisibleList(langVisibleList);
        }
        log("Language Item '" + item + "' removed from list of changed items.");
    }

    private void deleteAllLangChangedItems() {
        boolean result = msgBoxInfoQuestion("Delete All items", "Delete All items", "Do you want to remove all changed items from list ?");

        if (result) {
            langChangedList.clear();
            langChangedMap.clear();
            changeLangVisibleList(langVisibleList);
        }
        log ("All language items removed from list of changed items.");
    }



}



