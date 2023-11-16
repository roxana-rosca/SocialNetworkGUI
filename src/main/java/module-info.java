module ro.ubbcluj.map.socialnetworkgui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;

    opens ro.ubbcluj.map.socialnetworkgui to javafx.fxml;
    opens ro.ubbcluj.map.socialnetworkgui.domain to javafx.base;
    exports ro.ubbcluj.map.socialnetworkgui;

}