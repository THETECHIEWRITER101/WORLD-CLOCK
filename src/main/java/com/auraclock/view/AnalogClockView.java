package com.auraclock.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class AnalogClockView extends Pane {
    private Circle face;
    private Circle centerPin;
    private Line hourHand;
    private Line minuteHand;
    private Line secondHand;
    
    private Circle[] hourMarkers;
    private Text[] hourTexts;

    private static final Color BORDER_COLOR = Color.web("#ffffff", 0.08);

    public AnalogClockView() {
        // Initialize shapes
        face = new Circle();
        face.getStyleClass().add("analog-face");

        hourHand = new Line();
        hourHand.setStroke(Color.web("#f1f5f9"));
        hourHand.setStrokeWidth(4.5);
        hourHand.getStyleClass().add("analog-hour-hand");

        minuteHand = new Line();
        minuteHand.setStroke(Color.web("#94a3b8"));
        minuteHand.setStrokeWidth(2.5);
        minuteHand.getStyleClass().add("analog-minute-hand");

        secondHand = new Line();
        secondHand.setStroke(Color.web("#ef4444"));
        secondHand.setStrokeWidth(1.2);
        secondHand.getStyleClass().add("analog-second-hand");

        centerPin = new Circle();
        centerPin.setFill(Color.web("#0f172a"));
        centerPin.setStroke(Color.web("#ef4444"));
        centerPin.setStrokeWidth(1.5);
        centerPin.setRadius(4.5);

        // Hour markers (dots) and text labels for 12, 3, 6, 9
        hourMarkers = new Circle[12];
        for (int i = 0; i < 12; i++) {
            Circle marker = new Circle();
            if (i % 3 == 0) {
                marker.setRadius(3);
                marker.setFill(Color.web("#94a3b8"));
            } else {
                marker.setRadius(1.5);
                marker.setFill(Color.web("#475569"));
            }
            hourMarkers[i] = marker;
        }

        hourTexts = new Text[4];
        String[] labels = {"12", "3", "6", "9"};
        for (int i = 0; i < 4; i++) {
            Text text = new Text(labels[i]);
            text.setFill(Color.web("#cbd5e1"));
            text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            hourTexts[i] = text;
        }

        // Add to children
        getChildren().addAll(face);
        getChildren().addAll(hourMarkers);
        getChildren().addAll(hourTexts);
        getChildren().addAll(hourHand, minuteHand, secondHand, centerPin);

        // Listen for layout changes to dynamically resize and redraw
        widthProperty().addListener((obs, oldVal, newVal) -> requestLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> requestLayout());
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double width = getWidth();
        double height = getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) / 2 * 0.9;

        if (radius <= 0) return;

        // Position Face
        face.setCenterX(centerX);
        face.setCenterY(centerY);
        face.setRadius(radius);
        
        // Give face a dynamic glassmorphic gradient
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#1e293b", 0.45)), 
            new Stop(1, Color.web("#0f172a", 0.75)) 
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        face.setFill(gradient);
        face.setStroke(BORDER_COLOR);
        face.setStrokeWidth(1.5);

        // Position Center Pin
        centerPin.setCenterX(centerX);
        centerPin.setCenterY(centerY);

        // Position Hour Markers
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            double markerRadius = radius * 0.85;
            double mX = centerX + markerRadius * Math.cos(angle);
            double mY = centerY + markerRadius * Math.sin(angle);
            hourMarkers[i].setCenterX(mX);
            hourMarkers[i].setCenterY(mY);
        }

        // Position Texts (12, 3, 6, 9)
        double textOffset = radius * 0.70;
        
        // 12
        hourTexts[0].setX(centerX - hourTexts[0].getLayoutBounds().getWidth() / 2);
        hourTexts[0].setY(centerY - textOffset + hourTexts[0].getLayoutBounds().getHeight() / 3);
        
        // 3
        hourTexts[1].setX(centerX + textOffset - hourTexts[1].getLayoutBounds().getWidth() / 2);
        hourTexts[1].setY(centerY + hourTexts[1].getLayoutBounds().getHeight() / 3);
        
        // 6
        hourTexts[2].setX(centerX - hourTexts[2].getLayoutBounds().getWidth() / 2);
        hourTexts[2].setY(centerY + textOffset + hourTexts[2].getLayoutBounds().getHeight() / 3);
        
        // 9
        hourTexts[3].setX(centerX - textOffset - hourTexts[3].getLayoutBounds().getWidth() / 2);
        hourTexts[3].setY(centerY + hourTexts[3].getLayoutBounds().getHeight() / 3);
    }

    public void updateTime(double hours, double minutes, double seconds) {
        double width = getWidth();
        double height = getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius = Math.min(width, height) / 2 * 0.9;

        if (radius <= 0) return;

        // Angle math: subtract 90 degrees because 0 deg is pointing right (East), but 12 o'clock is pointing up (North)
        double hourAngle = Math.toRadians((hours % 12 + minutes / 60.0 + seconds / 3600.0) * 30 - 90);
        double minuteAngle = Math.toRadians((minutes + seconds / 60.0) * 6 - 90);
        double secondAngle = Math.toRadians(seconds * 6 - 90);

        // Hour Hand
        double hLength = radius * 0.48;
        hourHand.setStartX(centerX);
        hourHand.setStartY(centerY);
        hourHand.setEndX(centerX + hLength * Math.cos(hourAngle));
        hourHand.setEndY(centerY + hLength * Math.sin(hourAngle));

        // Minute Hand
        double mLength = radius * 0.72;
        minuteHand.setStartX(centerX);
        minuteHand.setStartY(centerY);
        minuteHand.setEndX(centerX + mLength * Math.cos(minuteAngle));
        minuteHand.setEndY(centerY + mLength * Math.sin(minuteAngle));

        // Second Hand
        double sLength = radius * 0.82;
        secondHand.setStartX(centerX);
        secondHand.setStartY(centerY);
        secondHand.setEndX(centerX + sLength * Math.cos(secondAngle));
        secondHand.setEndY(centerY + sLength * Math.sin(secondAngle));
    }
}
