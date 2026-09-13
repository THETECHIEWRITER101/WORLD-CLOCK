package com.auraclock.view;

import com.auraclock.audio.SoundSynthesizer;
import com.auraclock.controller.ClockController;
import com.auraclock.model.Alarm;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ClockDashboardView extends StackPane {
    private final ClockController controller;

    // View Components
    private AnalogClockView analogClock;
    private Label digitalClockLabel;
    private Label dateLabel;
    private Label activeZoneLabel;
    
    private Slider converterSlider;
    private Button resetSliderBtn;
    private HBox converterIndicatorBox;
    private Label converterIndicatorLabel;

    private TextField searchField;
    private ListView<CitySearchResult> suggestionList;
    private VBox cardsContainer;
    private VBox alarmsContainer;

    // Overlay layers
    private StackPane modalOverlay;
    private List<String> sortedZoneIds;
    private List<CitySearchResult> allCities;

    public static class CitySearchResult {
        private final String displayName;
        private final String zoneId;

        public CitySearchResult(String displayName, String zoneId) {
            this.displayName = displayName;
            this.zoneId = zoneId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getZoneId() {
            return zoneId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public ClockDashboardView(ClockController controller) {
        this.controller = controller;
        this.sortedZoneIds = ZoneId.getAvailableZoneIds().stream().sorted().collect(Collectors.toList());
        initializeCityDatabase();

        // Configure main layout
        this.getStyleClass().add("dashboard-root");

        // Two-Column Split View: Main Hero Clock (Center) and Control Sidebar (Right)
        BorderPane contentLayout = new BorderPane();
        
        // Center Panel (Main Dashboard with Hero Clock and Converter)
        VBox centerPanel = createCenterPanel();
        contentLayout.setCenter(centerPanel);

        // Right Panel (Tracked Cities, Settings & Alarms)
        VBox sidePanel = createRightPanel();
        contentLayout.setRight(sidePanel);

        // Modal Overlay Layer (starts invisible)
        modalOverlay = new StackPane();
        modalOverlay.getStyleClass().add("modal-overlay");
        modalOverlay.setVisible(false);

        getChildren().addAll(contentLayout, modalOverlay);

        // Connect data listeners
        setupListeners();

        // Connect frame updates from controller
        controller.setTickHandler(this::onTickFrame);
        controller.setAlarmTriggerHandler(this::onAlarmTriggered);
    }

    private void initializeCityDatabase() {
        allCities = new ArrayList<>();
        
        // Custom popular cities that might not be standard ZoneId names or are commonly searched
        addCustomCity("Delhi", "India", "Asia/Kolkata");
        addCustomCity("New Delhi", "India", "Asia/Kolkata");
        addCustomCity("Mumbai", "India", "Asia/Kolkata");
        addCustomCity("Bombay", "India", "Asia/Kolkata");
        addCustomCity("Bangalore", "India", "Asia/Kolkata");
        addCustomCity("Bengaluru", "India", "Asia/Kolkata");
        addCustomCity("Chennai", "India", "Asia/Kolkata");
        addCustomCity("Madras", "India", "Asia/Kolkata");
        addCustomCity("Kolkata", "India", "Asia/Kolkata");
        addCustomCity("Calcutta", "India", "Asia/Kolkata");
        addCustomCity("Hyderabad", "India", "Asia/Kolkata");
        addCustomCity("Pune", "India", "Asia/Kolkata");
        addCustomCity("Ahmedabad", "India", "Asia/Kolkata");
        addCustomCity("Jaipur", "India", "Asia/Kolkata");
        addCustomCity("Gurgaon", "India", "Asia/Kolkata");
        addCustomCity("Gurugram", "India", "Asia/Kolkata");
        addCustomCity("Noida", "India", "Asia/Kolkata");
        addCustomCity("Lucknow", "India", "Asia/Kolkata");
        addCustomCity("Kanpur", "India", "Asia/Kolkata");
        addCustomCity("Patna", "India", "Asia/Kolkata");
        addCustomCity("Indore", "India", "Asia/Kolkata");
        addCustomCity("Bhopal", "India", "Asia/Kolkata");
        addCustomCity("Surat", "India", "Asia/Kolkata");
        addCustomCity("Nagpur", "India", "Asia/Kolkata");
        addCustomCity("Kochi", "India", "Asia/Kolkata");
        addCustomCity("Cochin", "India", "Asia/Kolkata");
        addCustomCity("Trivandrum", "India", "Asia/Kolkata");
        addCustomCity("Thiruvananthapuram", "India", "Asia/Kolkata");
        addCustomCity("Goa", "India", "Asia/Kolkata");
        addCustomCity("Panaji", "India", "Asia/Kolkata");
        addCustomCity("Guwahati", "India", "Asia/Kolkata");
        addCustomCity("Bhubaneswar", "India", "Asia/Kolkata");
        addCustomCity("Chandigarh", "India", "Asia/Kolkata");
        addCustomCity("Srinagar", "India", "Asia/Kolkata");
        addCustomCity("Amritsar", "India", "Asia/Kolkata");
        addCustomCity("Dehradun", "India", "Asia/Kolkata");
        addCustomCity("Ranchi", "India", "Asia/Kolkata");
        addCustomCity("Raipur", "India", "Asia/Kolkata");
        addCustomCity("Shimla", "India", "Asia/Kolkata");
        addCustomCity("Visakhapatnam", "India", "Asia/Kolkata");
        addCustomCity("Coimbatore", "India", "Asia/Kolkata");
        addCustomCity("Madurai", "India", "Asia/Kolkata");
        addCustomCity("Seattle", "USA", "America/Los_Angeles");
        addCustomCity("San Francisco", "USA", "America/Los_Angeles");
        addCustomCity("San Jose", "USA", "America/Los_Angeles");
        addCustomCity("Boston", "USA", "America/New_York");
        addCustomCity("Washington DC", "USA", "America/New_York");
        addCustomCity("Chicago", "USA", "America/Chicago");
        addCustomCity("Houston", "USA", "America/Chicago");
        addCustomCity("Dallas", "USA", "America/Chicago");
        addCustomCity("Miami", "USA", "America/New_York");
        addCustomCity("Atlanta", "USA", "America/New_York");
        addCustomCity("Philadelphia", "USA", "America/New_York");
        addCustomCity("Los Angeles", "USA", "America/Los_Angeles");
        addCustomCity("New York", "USA", "America/New_York");
        addCustomCity("Beijing", "China", "Asia/Shanghai");
        addCustomCity("Shanghai", "China", "Asia/Shanghai");
        addCustomCity("Shenzhen", "China", "Asia/Shanghai");
        addCustomCity("Guangzhou", "China", "Asia/Shanghai");
        addCustomCity("Singapore", "Singapore", "Asia/Singapore");
        addCustomCity("Hong Kong", "Hong Kong", "Asia/Hong_Kong");
        addCustomCity("Tokyo", "Japan", "Asia/Tokyo");
        addCustomCity("Seoul", "South Korea", "Asia/Seoul");
        addCustomCity("Sydney", "Australia", "Australia/Sydney");
        addCustomCity("Melbourne", "Australia", "Australia/Melbourne");
        addCustomCity("Brisbane", "Australia", "Australia/Brisbane");
        addCustomCity("Toronto", "Canada", "America/Toronto");
        addCustomCity("Vancouver", "Canada", "America/Vancouver");
        addCustomCity("Montreal", "Canada", "America/Montreal");
        addCustomCity("London", "UK", "Europe/London");
        addCustomCity("Paris", "France", "Europe/Paris");
        addCustomCity("Berlin", "Germany", "Europe/Berlin");
        addCustomCity("Frankfurt", "Germany", "Europe/Berlin");
        addCustomCity("Munich", "Germany", "Europe/Berlin");
        addCustomCity("Rome", "Italy", "Europe/Rome");
        addCustomCity("Milan", "Italy", "Europe/Rome");
        addCustomCity("Madrid", "Spain", "Europe/Madrid");
        addCustomCity("Barcelona", "Spain", "Europe/Madrid");
        addCustomCity("Amsterdam", "Netherlands", "Europe/Amsterdam");
        addCustomCity("Brussels", "Belgium", "Europe/Brussels");
        addCustomCity("Geneva", "Switzerland", "Europe/Zurich");
        addCustomCity("Zurich", "Switzerland", "Europe/Zurich");
        addCustomCity("Vienna", "Austria", "Europe/Vienna");
        addCustomCity("Stockholm", "Sweden", "Europe/Stockholm");
        addCustomCity("Oslo", "Norway", "Europe/Oslo");
        addCustomCity("Copenhagen", "Denmark", "Europe/Copenhagen");
        addCustomCity("Dublin", "Ireland", "Europe/Dublin");
        addCustomCity("Moscow", "Russia", "Europe/Moscow");
        addCustomCity("Istanbul", "Turkey", "Europe/Istanbul");
        addCustomCity("Dubai", "UAE", "Asia/Dubai");
        addCustomCity("Cairo", "Egypt", "Africa/Cairo");
        addCustomCity("Johannesburg", "South Africa", "Africa/Johannesburg");
        addCustomCity("Cape Town", "South Africa", "Africa/Johannesburg");
        addCustomCity("Nairobi", "Kenya", "Africa/Nairobi");
        addCustomCity("Lagos", "Nigeria", "Africa/Lagos");
        addCustomCity("Riyadh", "Saudi Arabia", "Asia/Riyadh");
        addCustomCity("Bangkok", "Thailand", "Asia/Bangkok");
        addCustomCity("Jakarta", "Indonesia", "Asia/Jakarta");
        addCustomCity("Manila", "Philippines", "Asia/Manila");
        addCustomCity("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur");
        addCustomCity("Dhaka", "Bangladesh", "Asia/Dhaka");
        addCustomCity("Karachi", "Pakistan", "Asia/Karachi");
        addCustomCity("Lahore", "Pakistan", "Asia/Karachi");
        addCustomCity("Tehran", "Iran", "Asia/Tehran");
        addCustomCity("Tel Aviv", "Israel", "Asia/Jerusalem");
        addCustomCity("Reykjavik", "Iceland", "Atlantic/Reykjavik");
        addCustomCity("Honolulu", "Hawaii", "Pacific/Honolulu");
        addCustomCity("Anchorage", "Alaska", "America/Anchorage");

        // Parse default Java zone IDs and add them if not already added
        for (String id : ZoneId.getAvailableZoneIds()) {
            int slashIdx = id.lastIndexOf('/');
            if (slashIdx >= 0) {
                String cityName = id.substring(slashIdx + 1).replace('_', ' ');
                String region = id.substring(0, slashIdx).replace('_', ' ');
                
                boolean exists = false;
                for (CitySearchResult city : allCities) {
                    if (city.getZoneId().equals(id) && city.getDisplayName().toLowerCase().startsWith(cityName.toLowerCase())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allCities.add(new CitySearchResult(cityName + " (" + region + ")", id));
                }
            } else {
                allCities.add(new CitySearchResult(id, id));
            }
        }

        // Sort cities alphabetically
        allCities.sort((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));
    }

    private void addCustomCity(String cityName, String country, String zoneId) {
        allCities.add(new CitySearchResult(cityName + " (" + country + ")", zoneId));
    }

    private VBox createCenterPanel() {
        VBox center = new VBox(30);
        center.getStyleClass().add("center-panel");
        center.setPadding(new Insets(35, 40, 35, 40));
        center.setAlignment(Pos.TOP_CENTER);

        // Top Header Row: Logo Header on Left, Active Zone & Date on Right
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Application Logo Header
        VBox logoBox = new VBox(2);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        Label logoLabel = new Label("AURA CLOCK");
        logoLabel.getStyleClass().add("logo-title");
        Label logoSubtitle = new Label("GLOBAL TIME");
        logoSubtitle.getStyleClass().add("logo-subtitle");
        logoBox.getChildren().addAll(logoLabel, logoSubtitle);
        HBox.setHgrow(logoBox, Priority.ALWAYS);

        // Active Zone & Date Info
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        activeZoneLabel = new Label("PRIMARY TIME ZONE");
        activeZoneLabel.getStyleClass().add("primary-tz-title");
        dateLabel = new Label("August 05, 2026");
        dateLabel.getStyleClass().add("primary-date");
        infoBox.getChildren().addAll(activeZoneLabel, dateLabel);

        topRow.getChildren().addAll(logoBox, infoBox);

        // Main Clock Container (Analog Clock & Elegant Digital Clock below it)
        VBox clockContainer = new VBox(20);
        clockContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(clockContainer, Priority.ALWAYS);

        // Analog Clock Panel
        analogClock = new AnalogClockView();
        analogClock.setPrefSize(280, 280);
        analogClock.setMinSize(220, 220);
        analogClock.setMaxSize(360, 360);

        // Digital Clock Label (Minimal & Elegant)
        digitalClockLabel = new Label("00:00:00");
        digitalClockLabel.getStyleClass().add("digital-clock");

        clockContainer.getChildren().addAll(analogClock, digitalClockLabel);

        // Timezone Converter Panel (Slider)
        VBox converterBox = new VBox(12);
        converterBox.getStyleClass().add("glass-panel");
        converterBox.setPadding(new Insets(20));

        HBox converterHeader = new HBox();
        converterHeader.setAlignment(Pos.CENTER_LEFT);
        Label converterTitle = new Label("CONVERTER & MEETING PLANNER");
        converterTitle.getStyleClass().add("panel-header");
        HBox.setHgrow(converterTitle, Priority.ALWAYS);

        resetSliderBtn = new Button("Back to Live Time");
        resetSliderBtn.getStyleClass().add("live-reset-btn");
        resetSliderBtn.setVisible(false);
        resetSliderBtn.setOnAction(e -> resetToLiveTime());
        converterHeader.getChildren().addAll(converterTitle, resetSliderBtn);

        converterSlider = new Slider(-12, 12, 0);
        converterSlider.getStyleClass().add("glow-slider");
        converterSlider.setMajorTickUnit(2);
        converterSlider.setMinorTickCount(1);
        converterSlider.setSnapToTicks(true);
        converterSlider.setShowTickMarks(true);
        
        converterIndicatorBox = new HBox(8);
        converterIndicatorBox.setAlignment(Pos.CENTER);
        Label offsetText = new Label("Move slider to preview time changes across tracked zones.");
        offsetText.getStyleClass().add("converter-tip");
        converterIndicatorBox.getChildren().add(offsetText);

        converterIndicatorLabel = new Label();
        converterIndicatorLabel.getStyleClass().add("converter-status");

        converterSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int offsetVal = newVal.intValue();
            if (offsetVal != 0) {
                controller.currentModeProperty().set(ClockController.ClockMode.CONVERTER);
                controller.manualOffsetHoursProperty().set(offsetVal);
                resetSliderBtn.setVisible(true);
                
                String sign = offsetVal > 0 ? "+" : "";
                converterIndicatorLabel.setText("PREVIEWING TIME: " + sign + offsetVal + " Hours");
                if (converterIndicatorBox.getChildren().size() == 1) {
                    converterIndicatorBox.getChildren().clear();
                    converterIndicatorBox.getChildren().add(converterIndicatorLabel);
                }
            } else {
                resetToLiveTime();
            }
        });

        converterBox.getChildren().addAll(converterHeader, converterSlider, converterIndicatorBox);

        center.getChildren().addAll(topRow, clockContainer, converterBox);
        return center;
    }

    private VBox createRightPanel() {
        VBox right = new VBox(20);
        right.getStyleClass().add("right-panel");
        right.setPadding(new Insets(30, 24, 30, 24));
        right.setPrefWidth(400);
        right.setMinWidth(400);

        // Right Panel Title / Search Section
        Label rightTitle = new Label("TRACKED CITIES");
        rightTitle.getStyleClass().add("right-panel-header");

        // Search Autocomplete Layout
        VBox searchBox = new VBox(8);
        searchField = new TextField();
        searchField.setPromptText("Search & Add City (e.g. Delhi, London)...");
        searchField.getStyleClass().add("glass-textfield");

        // Filter Suggestion List
        suggestionList = new ListView<>();
        suggestionList.getStyleClass().add("suggestion-list");
        suggestionList.setPrefHeight(140);
        suggestionList.setVisible(false);
        suggestionList.setManaged(false);

        // Filter suggestion list items based on text input
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                suggestionList.setVisible(false);
                suggestionList.setManaged(false);
            } else {
                String query = newText.toLowerCase().trim();
                List<CitySearchResult> filtered = allCities.stream()
                        .filter(city -> city.getDisplayName().toLowerCase().contains(query))
                        .limit(8)
                        .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    suggestionList.setVisible(false);
                    suggestionList.setManaged(false);
                } else {
                    suggestionList.getItems().setAll(filtered);
                    suggestionList.setVisible(true);
                    suggestionList.setManaged(true);
                }
            }
        });

        // Add zone on selecting from suggestions
        suggestionList.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2 || (click.getClickCount() == 1 && suggestionList.getSelectionModel().getSelectedItem() != null)) {
                CitySearchResult selectedCity = suggestionList.getSelectionModel().getSelectedItem();
                if (selectedCity != null) {
                    String cityName = selectedCity.getDisplayName();
                    int parenIdx = cityName.indexOf('(');
                    if (parenIdx >= 0) {
                        cityName = cityName.substring(0, parenIdx).trim();
                    }
                    controller.addMonitoredZone(cityName + "|" + selectedCity.getZoneId());
                    searchField.clear();
                    suggestionList.setVisible(false);
                    suggestionList.setManaged(false);
                }
            }
        });

        searchBox.getChildren().addAll(searchField, suggestionList);

        // ScrollPane for Tracked timezone cards
        ScrollPane cardsScroll = new ScrollPane();
        cardsScroll.getStyleClass().add("glass-scroll-pane");
        cardsScroll.setFitToWidth(true);
        cardsScroll.setPrefHeight(200);
        VBox.setVgrow(cardsScroll, Priority.ALWAYS);

        cardsContainer = new VBox(12);
        cardsContainer.setAlignment(Pos.TOP_CENTER);
        cardsScroll.setContent(cardsContainer);

        // Alarms Section
        VBox alarmsSection = new VBox(10);
        HBox alarmHeader = new HBox();
        alarmHeader.setAlignment(Pos.CENTER_LEFT);
        Label alarmsTitle = new Label("ACTIVE ALARMS");
        alarmsTitle.getStyleClass().add("right-panel-header");
        HBox.setHgrow(alarmsTitle, Priority.ALWAYS);

        Button addAlarmBtn = new Button("+ Add");
        addAlarmBtn.getStyleClass().add("action-btn-small");
        addAlarmBtn.setOnAction(e -> showAddAlarmModal());
        alarmHeader.getChildren().addAll(alarmsTitle, addAlarmBtn);

        ScrollPane alarmScroll = new ScrollPane();
        alarmScroll.getStyleClass().add("glass-scroll-pane");
        alarmScroll.setFitToWidth(true);
        alarmScroll.setPrefHeight(130);
        alarmScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        alarmsContainer = new VBox(8);
        alarmsContainer.setAlignment(Pos.TOP_LEFT);
        alarmScroll.setContent(alarmsContainer);
        alarmsSection.getChildren().addAll(alarmHeader, alarmScroll);

        // Separator
        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-separator");

        // Preferences Box (Clean and Integrated)
        VBox preferencesBox = new VBox(12);
        Label preferencesTitle = new Label("PREFERENCES");
        preferencesTitle.getStyleClass().add("section-header");

        HBox togglesBox = new HBox(12);
        togglesBox.setAlignment(Pos.CENTER);
        
        ToggleButton formatToggle = new ToggleButton("24-Hour Format");
        formatToggle.getStyleClass().add("glass-toggle");
        HBox.setHgrow(formatToggle, Priority.ALWAYS);
        formatToggle.setMaxWidth(Double.MAX_VALUE);
        controller.use24HourProperty().bind(formatToggle.selectedProperty());

        ToggleButton secondsToggle = new ToggleButton("Show Seconds");
        secondsToggle.getStyleClass().add("glass-toggle");
        HBox.setHgrow(secondsToggle, Priority.ALWAYS);
        secondsToggle.setMaxWidth(Double.MAX_VALUE);
        secondsToggle.setSelected(true);
        controller.showSecondsProperty().bind(secondsToggle.selectedProperty());

        togglesBox.getChildren().addAll(formatToggle, secondsToggle);

        // Primary Timezone selection dropdown
        Label tzSelectLabel = new Label("Primary Clock Timezone");
        tzSelectLabel.getStyleClass().add("field-label");

        ComboBox<String> primaryTzCombo = new ComboBox<>();
        primaryTzCombo.getItems().addAll(sortedZoneIds);
        primaryTzCombo.setValue(controller.activeZoneProperty().get().getId());
        primaryTzCombo.setMaxWidth(Double.MAX_VALUE);
        primaryTzCombo.getStyleClass().add("glass-combobox");
        primaryTzCombo.setEditable(true);
        primaryTzCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String trimmedVal = newVal.trim();
                String targetZoneId = null;
                try {
                    ZoneId.of(trimmedVal);
                    targetZoneId = trimmedVal;
                } catch (Exception e) {
                    for (CitySearchResult city : allCities) {
                        String cleanCityName = city.getDisplayName();
                        int parenIdx = cleanCityName.indexOf('(');
                        if (parenIdx >= 0) {
                            cleanCityName = cleanCityName.substring(0, parenIdx).trim();
                        }
                        if (cleanCityName.equalsIgnoreCase(trimmedVal) || city.getDisplayName().toLowerCase().contains(trimmedVal.toLowerCase())) {
                            targetZoneId = city.getZoneId();
                            break;
                        }
                    }
                }
                if (targetZoneId != null) {
                    try {
                        controller.activeZoneProperty().set(ZoneId.of(targetZoneId));
                    } catch (Exception ex) {
                        // Safe catch for invalid input
                    }
                }
            }
        });

        controller.activeZoneProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.getId().equals(primaryTzCombo.getValue())) {
                primaryTzCombo.setValue(newVal.getId());
            }
        });

        preferencesBox.getChildren().addAll(preferencesTitle, togglesBox, tzSelectLabel, primaryTzCombo);

        right.getChildren().addAll(rightTitle, searchBox, cardsScroll, sep, alarmsSection, preferencesBox);
        return right;
    }

    private void setupListeners() {
        // Monitored cards layout sync
        controller.getMonitoredZones().addListener((javafx.collections.ListChangeListener<String>) change -> {
            syncMonitoredZoneCards();
        });

        // Initialize cards
        syncMonitoredZoneCards();

        // Alarms list sync
        controller.getAlarms().addListener((javafx.collections.ListChangeListener<Alarm>) change -> {
            syncAlarmsList();
        });
        syncAlarmsList();
    }

    private void syncMonitoredZoneCards() {
        cardsContainer.getChildren().clear();
        for (String zoneSpec : controller.getMonitoredZones()) {
            TimezoneCardView card = new TimezoneCardView(zoneSpec, deadCard -> {
                controller.removeMonitoredZone(deadCard.getZoneSpec());
            });
            cardsContainer.getChildren().add(card);
        }
    }

    private void syncAlarmsList() {
        alarmsContainer.getChildren().clear();
        for (Alarm alarm : controller.getAlarms()) {
            HBox alarmRow = new HBox(8);
            alarmRow.setAlignment(Pos.CENTER_LEFT);
            alarmRow.getStyleClass().add("alarm-item-row");

            VBox infoBox = new VBox(2);
            HBox.setHgrow(infoBox, Priority.ALWAYS);
            Label descLabel = new Label(alarm.getDescription());
            descLabel.getStyleClass().add("alarm-item-desc");
            
            String targetCity = alarm.getTimezone().getId();
            int slashIdx = targetCity.lastIndexOf('/');
            if (slashIdx >= 0) targetCity = targetCity.substring(slashIdx + 1).replace('_', ' ');

            Label timeLabel = new Label(alarm.getFormattedTime(controller.use24HourProperty().get()) + " (" + targetCity + ")");
            timeLabel.getStyleClass().add("alarm-item-time");
            infoBox.getChildren().addAll(descLabel, timeLabel);

            // Toggle active status
            CheckBox activeCheck = new CheckBox();
            activeCheck.setSelected(alarm.isActive());
            activeCheck.getStyleClass().add("glass-checkbox");
            activeCheck.setOnAction(e -> alarm.setActive(activeCheck.isSelected()));

            Button delBtn = new Button("×");
            delBtn.getStyleClass().add("alarm-delete-btn");
            delBtn.setOnAction(e -> controller.removeAlarm(alarm));

            alarmRow.getChildren().addAll(activeCheck, infoBox, delBtn);
            alarmsContainer.getChildren().add(alarmRow);
        }
    }

    private void resetToLiveTime() {
        controller.currentModeProperty().set(ClockController.ClockMode.LIVE);
        controller.manualOffsetHoursProperty().set(0);
        converterSlider.setValue(0);
        resetSliderBtn.setVisible(false);

        // Restore tip message
        converterIndicatorBox.getChildren().clear();
        Label offsetText = new Label("Move slider to preview time changes across tracked zones.");
        offsetText.getStyleClass().add("converter-tip");
        converterIndicatorBox.getChildren().add(offsetText);
    }

    /**
     * Executes every tick cycle (fluid 60fps AnimationTimer updates).
     */
    private void onTickFrame() {
        ZonedDateTime now = controller.getCurrentLiveTime();
        boolean use24h = controller.use24HourProperty().get();
        boolean showSec = controller.showSecondsProperty().get();
        int manualOffset = controller.manualOffsetHoursProperty().get();

        // 1. Digital Clock Face
        ZonedDateTime displayContext = now;
        if (manualOffset != 0) {
            displayContext = displayContext.plusHours(manualOffset);
        }

        String digitalPattern;
        if (use24h) {
            digitalPattern = showSec ? "HH:mm:ss" : "HH:mm";
        } else {
            digitalPattern = showSec ? "hh:mm:ss a" : "hh:mm a";
        }
        digitalClockLabel.setText(displayContext.format(DateTimeFormatter.ofPattern(digitalPattern)));

        // 2. Date Header
        dateLabel.setText(displayContext.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        
        // 3. Zone Label Header
        activeZoneLabel.setText(displayContext.getZone().getId().toUpperCase());

        // 4. Analog Clock Hands
        double hr = displayContext.getHour();
        double mn = displayContext.getMinute();
        double sc = displayContext.getSecond();
        if (controller.currentModeProperty().get() == ClockController.ClockMode.LIVE) {
            sc += (System.currentTimeMillis() % 1000) / 1000.0; // Sweet fluid sweep
        } else {
            sc = 0; // Lock sub-second movement
        }
        analogClock.updateTime(hr, mn, sc);

        // 5. Update Monitored Cards
        for (javafx.scene.Node node : cardsContainer.getChildren()) {
            if (node instanceof TimezoneCardView) {
                ((TimezoneCardView) node).update(now, use24h, showSec, manualOffset);
            }
        }
    }

    /**
     * Display adding alarms overlay modal.
     */
    private void showAddAlarmModal() {
        VBox dialogBox = new VBox(20);
        dialogBox.getStyleClass().add("modal-dialog");
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxSize(320, 320);

        Label title = new Label("ADD NEW ALARM");
        title.getStyleClass().add("modal-title");

        HBox timeSpinners = new HBox(10);
        timeSpinners.setAlignment(Pos.CENTER);
        
        Spinner<Integer> hrSpinner = new Spinner<>(0, 23, 12, 1);
        hrSpinner.getStyleClass().add("glass-spinner");
        hrSpinner.setPrefWidth(80);
        
        Label colon = new Label(":");
        colon.setTextFill(Color.web("#e2e8f0"));
        
        Spinner<Integer> minSpinner = new Spinner<>(0, 59, 0, 1);
        minSpinner.getStyleClass().add("glass-spinner");
        minSpinner.setPrefWidth(80);

        timeSpinners.getChildren().addAll(hrSpinner, colon, minSpinner);

        VBox inputs = new VBox(10);
        inputs.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label("Alarm Label");
        descLabel.getStyleClass().add("field-label");
        TextField descField = new TextField("Meeting");
        descField.getStyleClass().add("glass-textfield");

        Label tzLabel = new Label("Alarm Timezone");
        tzLabel.getStyleClass().add("field-label");
        ComboBox<String> tzCombo = new ComboBox<>();
        tzCombo.getItems().addAll(sortedZoneIds);
        tzCombo.setValue(controller.activeZoneProperty().get().getId());
        tzCombo.getStyleClass().add("glass-combobox");
        tzCombo.setMaxWidth(Double.MAX_VALUE);

        inputs.getChildren().addAll(descLabel, descField, tzLabel, tzCombo);

        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("glass-btn-secondary");
        cancelBtn.setOnAction(e -> hideModal());

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("glass-btn-primary");
        saveBtn.setOnAction(e -> {
            int hr = hrSpinner.getValue();
            int mn = minSpinner.getValue();
            ZoneId zone = ZoneId.of(tzCombo.getValue());
            String desc = descField.getText().trim();
            if (desc.isEmpty()) desc = "Alarm";

            Alarm newAlarm = new Alarm(hr, mn, zone, desc);
            controller.addAlarm(newAlarm);
            hideModal();
        });

        actionRow.getChildren().addAll(cancelBtn, saveBtn);
        dialogBox.getChildren().addAll(title, timeSpinners, inputs, actionRow);

        showModal(dialogBox);
    }

    /**
     * Triggers the visual full-screen dialog overlay when alarm goes off.
     */
    private void onAlarmTriggered(Alarm alarm) {
        SoundSynthesizer.playAlarmSound();

        VBox dialogBox = new VBox(25);
        dialogBox.getStyleClass().add("modal-dialog-alarm");
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxSize(380, 280);

        Label alarmHeader = new Label("ALARM TRIPPED");
        alarmHeader.getStyleClass().add("alarm-header-txt");

        Label descLabel = new Label(alarm.getDescription());
        descLabel.getStyleClass().add("alarm-desc-txt");

        Label timeLabel = new Label(alarm.getFormattedTime(controller.use24HourProperty().get()));
        timeLabel.getStyleClass().add("alarm-time-txt");

        Label tzLabel = new Label(alarm.getTimezone().getId());
        tzLabel.getStyleClass().add("alarm-tz-txt");

        HBox actionRow = new HBox(20);
        actionRow.setAlignment(Pos.CENTER);

        Button dismissBtn = new Button("DISMISS");
        dismissBtn.getStyleClass().add("alarm-dismiss-btn");
        dismissBtn.setOnAction(e -> {
            SoundSynthesizer.stopAlarmSound();
            hideModal();
        });

        Button snoozeBtn = new Button("SNOOZE");
        snoozeBtn.getStyleClass().add("alarm-snooze-btn");
        snoozeBtn.setOnAction(e -> {
            SoundSynthesizer.stopAlarmSound();
            hideModal();
            // Create a snooze alarm in +5 minutes
            ZonedDateTime alarmLocalTime = ZonedDateTime.now(alarm.getTimezone()).plusMinutes(5);
            Alarm snoozeAlarm = new Alarm(alarmLocalTime.getHour(), alarmLocalTime.getMinute(), alarm.getTimezone(), "Snooze: " + alarm.getDescription());
            controller.addAlarm(snoozeAlarm);
        });

        actionRow.getChildren().addAll(snoozeBtn, dismissBtn);
        dialogBox.getChildren().addAll(alarmHeader, descLabel, timeLabel, tzLabel, actionRow);

        showModal(dialogBox);
    }

    private void showModal(VBox content) {
        modalOverlay.getChildren().clear();
        modalOverlay.getChildren().add(content);
        modalOverlay.setVisible(true);
    }

    private void hideModal() {
        modalOverlay.setVisible(false);
        modalOverlay.getChildren().clear();
    }
}
