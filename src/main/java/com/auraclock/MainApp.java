package com.auraclock;

import com.auraclock.audio.SoundSynthesizer;
import com.auraclock.controller.ClockController;
import com.auraclock.view.ClockDashboardView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private ClockController controller;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the controller
        controller = new ClockController();
        controller.start();

        // Create the main dashboard view
        ClockDashboardView root = new ClockDashboardView(controller);

        // Configure the scene
        Scene scene = new Scene((Parent) root, 1080, 680);

        // Load the CSS stylesheet
        try {
            String cssPath = getClass().getResource("/styles/app.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Could not load app.css: " + e.getMessage());
        }

        // Configure primary stage
        primaryStage.setTitle("Aura Clock - World Time Dashboard & Planner");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(920);
        primaryStage.setMinHeight(600);

        // Ensure clean application exit
        primaryStage.setOnCloseRequest(event -> {
            controller.stop();
            SoundSynthesizer.stopAlarmSound();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
