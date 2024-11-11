package com.dsoftn.Settings.gui;

import com.dsoftn.controllers.MainWinController;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


public class GuiMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();

        MainWinController controller = loader.getController();

        Scene scene = new Scene(root);

        String css = GuiMain.class.getResource("/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Settings");

        primaryStage.setOnCloseRequest(event -> {
            onWindowClose(event, controller);
        });

        primaryStage.show();
    }

    private void onWindowClose(WindowEvent event, MainWinController controller) {
        controller.logIndentClear();
        controller.log("Application is closing...");
        
        controller.logIndentSet(1);
        controller.saveAppState(true);
        controller.logIndentClear();
        
        controller.log("Application closed.");
    }
    
}
