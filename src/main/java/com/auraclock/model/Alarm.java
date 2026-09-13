package com.auraclock.model;

import java.time.ZoneId;
import java.util.UUID;

public class Alarm {
    private final String id;
    private int hour; // 0-23
    private int minute; // 0-59
    private ZoneId timezone;
    private String description;
    private boolean active;
    private boolean triggeredToday; // Prevent repeated firing within the same minute

    public Alarm(int hour, int minute, ZoneId timezone, String description) {
        this.id = UUID.randomUUID().toString();
        this.hour = hour;
        this.minute = minute;
        this.timezone = timezone;
        this.description = description;
        this.active = true;
        this.triggeredToday = false;
    }

    public String getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.triggeredToday = false;
        }
    }

    public boolean isTriggeredToday() {
        return triggeredToday;
    }

    public void setTriggeredToday(boolean triggeredToday) {
        this.triggeredToday = triggeredToday;
    }

    public String getFormattedTime(boolean use24Hour) {
        if (use24Hour) {
            return String.format("%02d:%02d", hour, minute);
        } else {
            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            String amPm = hour >= 12 ? "PM" : "AM";
            return String.format("%02d:%02d %s", displayHour, minute, amPm);
        }
    }

    @Override
    public String toString() {
        return String.format("%s at %s (%s)", description, getFormattedTime(false), timezone.getId());
    }
}
