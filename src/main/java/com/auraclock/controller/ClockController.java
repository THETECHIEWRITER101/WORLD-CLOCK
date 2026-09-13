package com.auraclock.controller;

import com.auraclock.model.Alarm;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClockController {
    public enum ClockMode {
        LIVE,
        CONVERTER
    }

    // Observable States
    private final ObservableList<String> monitoredZones = FXCollections.observableArrayList();
    private final ObservableList<Alarm> alarms = FXCollections.observableArrayList();
    
    private final BooleanProperty use24Hour = new SimpleBooleanProperty(false);
    private final BooleanProperty showSeconds = new SimpleBooleanProperty(true);
    private final ObjectProperty<ZoneId> activeZone = new SimpleObjectProperty<>(ZoneId.of("Asia/Kolkata"));
    private final ObjectProperty<ClockMode> currentMode = new SimpleObjectProperty<>(ClockMode.LIVE);
    private final IntegerProperty manualOffsetHours = new SimpleIntegerProperty(0); // For converter slider

    private AnimationTimer timer;
    private ZonedDateTime currentLiveTime = ZonedDateTime.now();
    private Consumer<Alarm> alarmTriggerHandler;
    private Runnable tickHandler;

    public ClockController() {
        // Add default time zones
        addMonitoredZone("New York|America/New_York");
        addMonitoredZone("London|Europe/London");
        addMonitoredZone("Tokyo|Asia/Tokyo");
        addMonitoredZone("Delhi|Asia/Kolkata");
    }

    public void start() {
        if (timer != null) return;

        timer = new AnimationTimer() {
            private int lastSecond = -1;

            @Override
            public void handle(long nowNano) {
                // Update live time
                currentLiveTime = ZonedDateTime.now(activeZone.get());

                // Calculate smooth sweep times for the main clock
                double smoothSeconds;
                if (currentMode.get() == ClockMode.LIVE) {
                    smoothSeconds = currentLiveTime.getSecond() + (System.currentTimeMillis() % 1000) / 1000.0;
                } else {
                    // In converter mode, lock the second hand at 0
                    smoothSeconds = 0;
                }

                // If a tick/frame handler is registered (e.g. for Analog Clock)
                if (tickHandler != null) {
                    tickHandler.run();
                }

                // Run heavy operations only when seconds tick
                int currentSec = (int) smoothSeconds;
                if (currentSec != lastSecond) {
                    lastSecond = currentSec;
                    checkAlarms();
                }
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public ObservableList<String> getMonitoredZones() {
        return monitoredZones;
    }

    public void addMonitoredZone(String zoneSpec) {
        try {
            String parsedZoneIdStr = zoneSpec;
            int pipeIdx = zoneSpec.indexOf('|');
            if (pipeIdx >= 0) {
                parsedZoneIdStr = zoneSpec.substring(pipeIdx + 1);
            }
            ZoneId.of(parsedZoneIdStr);
            if (!monitoredZones.contains(zoneSpec)) {
                monitoredZones.add(zoneSpec);
            }
        } catch (Exception e) {
            System.err.println("Invalid monitored zone: " + zoneSpec);
        }
    }

    public void removeMonitoredZone(String zoneSpec) {
        monitoredZones.remove(zoneSpec);
    }

    public ObservableList<Alarm> getAlarms() {
        return alarms;
    }

    public void addAlarm(Alarm alarm) {
        alarms.add(alarm);
    }

    public void removeAlarm(Alarm alarm) {
        alarms.remove(alarm);
    }

    public BooleanProperty use24HourProperty() {
        return use24Hour;
    }

    public BooleanProperty showSecondsProperty() {
        return showSeconds;
    }

    public ObjectProperty<ZoneId> activeZoneProperty() {
        return activeZone;
    }

    public ObjectProperty<ClockMode> currentModeProperty() {
        return currentMode;
    }

    public IntegerProperty manualOffsetHoursProperty() {
        return manualOffsetHours;
    }

    public ZonedDateTime getCurrentLiveTime() {
        return currentLiveTime;
    }

    public void setAlarmTriggerHandler(Consumer<Alarm> handler) {
        this.alarmTriggerHandler = handler;
    }

    public void setTickHandler(Runnable handler) {
        this.tickHandler = handler;
    }

    /**
     * Checks all alarms and fires trigger handler if alarms are active and match target timezone times.
     */
    private void checkAlarms() {
        if (currentMode.get() != ClockMode.LIVE) return;

        ZonedDateTime systemTime = ZonedDateTime.now();
        List<Alarm> triggeredAlarms = new ArrayList<>();

        for (Alarm alarm : alarms) {
            if (!alarm.isActive()) continue;

            // Get current time inside the alarm's target timezone
            ZonedDateTime alarmZoneTime = systemTime.withZoneSameInstant(alarm.getTimezone());
            int targetHour = alarmZoneTime.getHour();
            int targetMin = alarmZoneTime.getMinute();

            if (targetHour == alarm.getHour() && targetMin == alarm.getMinute()) {
                if (!alarm.isTriggeredToday()) {
                    alarm.setTriggeredToday(true);
                    triggeredAlarms.add(alarm);
                }
            } else {
                // Reset alarm once the time shifts past the minute
                alarm.setTriggeredToday(false);
            }
        }

        // Fire alarm trigger handlers
        if (!triggeredAlarms.isEmpty() && alarmTriggerHandler != null) {
            for (Alarm alarm : triggeredAlarms) {
                alarmTriggerHandler.accept(alarm);
            }
        }
    }
}
