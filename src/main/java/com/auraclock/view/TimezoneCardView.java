package com.auraclock.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class TimezoneCardView extends HBox {
    private final ZoneId zoneId;
    private final String zoneSpec;
    private final Label cityNameLabel;
    private final Label regionLabel;
    private final Label offsetLabel;
    private final Label timeLabel;
    private final Label dateLabel;
    private final Button deleteButton;

    private static final DateTimeFormatter TIME_FORMATTER_24_SEC = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER_24_MIN = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER_12_SEC = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter TIME_FORMATTER_12_MIN = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM dd");

    public TimezoneCardView(String zoneSpec, Consumer<TimezoneCardView> onDelete) {
        this.zoneSpec = zoneSpec;

        // Configure HBox card styling
        this.getStyleClass().add("timezone-card");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(14, 18, 14, 18));
        this.setSpacing(15);
        this.setMaxWidth(Double.MAX_VALUE);

        // Parse custom city name and ZoneId
        String parsedCityName = "";
        String parsedZoneIdStr = "";
        int pipeIdx = zoneSpec.indexOf('|');
        if (pipeIdx >= 0) {
            parsedCityName = zoneSpec.substring(0, pipeIdx);
            parsedZoneIdStr = zoneSpec.substring(pipeIdx + 1);
        } else {
            parsedZoneIdStr = zoneSpec;
        }

        this.zoneId = ZoneId.of(parsedZoneIdStr);

        String displayCity = parsedCityName;
        String displayRegion = "";

        if (parsedCityName.isEmpty()) {
            String fullId = zoneId.getId();
            displayCity = fullId;
            int slashIdx = fullId.lastIndexOf('/');
            if (slashIdx >= 0) {
                displayCity = fullId.substring(slashIdx + 1).replace('_', ' ');
                displayRegion = fullId.substring(0, slashIdx).replace('_', ' ');
            }
        } else {
            // Find a regional context if it's a known timezone path
            String fullId = zoneId.getId();
            int slashIdx = fullId.lastIndexOf('/');
            if (slashIdx >= 0) {
                displayRegion = fullId.substring(0, slashIdx).replace('_', ' ');
            } else {
                displayRegion = "GLOBAL";
            }
        }

        // Left Panel: Name & Region & Relative Offset
        VBox nameBox = new VBox(2);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        
        cityNameLabel = new Label(displayCity);
        cityNameLabel.getStyleClass().add("card-city-name");
        
        regionLabel = new Label(displayRegion.toUpperCase());
        regionLabel.getStyleClass().add("card-region-name");

        offsetLabel = new Label();
        offsetLabel.getStyleClass().add("card-offset-label");

        nameBox.getChildren().addAll(cityNameLabel, regionLabel, offsetLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // Middle Panel: Time & Date
        VBox timeBox = new VBox(2);
        timeBox.setAlignment(Pos.CENTER_RIGHT);
        
        timeLabel = new Label();
        timeLabel.getStyleClass().add("card-time");

        dateLabel = new Label();
        dateLabel.getStyleClass().add("card-date");

        timeBox.getChildren().addAll(timeLabel, dateLabel);

        // Right Panel: Delete Button
        deleteButton = new Button("×");
        deleteButton.getStyleClass().add("card-delete-btn");
        deleteButton.setOnAction(e -> {
            if (onDelete != null) {
                onDelete.accept(this);
            }
        });

        // Add to main card layout
        getChildren().addAll(nameBox, timeBox, deleteButton);

        // Trigger an initial update
        update(ZonedDateTime.now(), true, false, 0);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public String getZoneSpec() {
        return zoneSpec;
    }

    /**
     * Updates the card content based on the target time context.
     * @param liveNow current local system time (used for live clocks and offset calculations)
     * @param use24Hour format preference
     * @param showSeconds format preference
     * @param manualHourOffset manual offset in hours (if in timezone converter mode, else 0)
     */
    public void update(ZonedDateTime liveNow, boolean use24Hour, boolean showSeconds, int manualHourOffset) {
        // Compute target time
        ZonedDateTime localContext = liveNow;
        if (manualHourOffset != 0) {
            localContext = localContext.plusHours(manualHourOffset);
        }
        
        // Convert to target timezone
        ZonedDateTime targetTime = localContext.withZoneSameInstant(zoneId);

        // 1. Digital Time String
        DateTimeFormatter formatter;
        if (use24Hour) {
            formatter = showSeconds ? TIME_FORMATTER_24_SEC : TIME_FORMATTER_24_MIN;
        } else {
            formatter = showSeconds ? TIME_FORMATTER_12_SEC : TIME_FORMATTER_12_MIN;
        }
        timeLabel.setText(targetTime.format(formatter));

        // 2. Date String
        dateLabel.setText(targetTime.format(DATE_FORMATTER));

        // 3. Offset Difference
        long localOffsetSec = liveNow.getOffset().getTotalSeconds();
        long targetOffsetSec = targetTime.getOffset().getTotalSeconds();
        long diffSec = targetOffsetSec - localOffsetSec;
        offsetLabel.setText(formatOffsetDifference(diffSec));
    }

    private String formatOffsetDifference(long diffSec) {
        if (diffSec == 0) return "Same time as local";
        long hours = Math.abs(diffSec) / 3600;
        long minutes = (Math.abs(diffSec) % 3600) / 60;
        String sign = diffSec > 0 ? "+" : "-";
        
        StringBuilder sb = new StringBuilder();
        sb.append(sign).append(hours).append("h");
        if (minutes > 0) {
            sb.append(" ").append(minutes).append("m");
        }
        sb.append(diffSec > 0 ? " ahead" : " behind");
        return sb.toString();
    }
}
