module com.auraclock {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires java.desktop; // For audio synthesis (javax.sound)

    exports com.auraclock;
}
