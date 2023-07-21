package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.preferences.DBPreferences;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

public class NotificationOverlays extends AbstractOverlay{

    public static final NotificationOverlays INSTANCE = new NotificationOverlays();

    private NotificationPane notificationPane;

    @Override
    public void init() {
        notificationPane = new NotificationPane();
        notificationPane.setManaged(false);
        notificationPane.setShowFromTop(false);
        notificationPane.setPickOnBounds(false);
        notificationPane.layoutXProperty().bind(DrawingBotV3.INSTANCE.controller.vBoxMain.layoutXProperty().add(DrawingBotV3.INSTANCE.controller.vBoxMain.widthProperty()).subtract(notificationPane.widthProperty()).subtract(30));
        notificationPane.layoutYProperty().bind(DrawingBotV3.INSTANCE.controller.vBoxMain.layoutYProperty().add(DrawingBotV3.INSTANCE.controller.vBoxMain.heightProperty()).subtract(notificationPane.heightProperty()).subtract(50));
        notificationPane.resize(600, 70);
        notificationPane.getStylesheets().add(NotificationOverlays.class.getResource("notifications.css").toExternalForm());
        DrawingBotV3.INSTANCE.controller.vBoxMain.getChildren().add(notificationPane);

        activeProperty().bindBidirectional(DBPreferences.INSTANCE.notificationsEnabled.asBooleanProperty());
    }

    @Override
    protected void activate() {
        notificationPane.setVisible(true);
    }

    @Override
    protected void deactivate() {
        notificationPane.setVisible(false);
        notificationPane.hide();
        notificationPane.setMouseTransparent(true);
    }

    @Override
    public void doRender() {
        super.doRender();

        if((displayMS == 0 && notificationPane.isShowing()) || System.currentTimeMillis() - startTime > displayMS){
            displayMS = 0;
            notificationPane.hide();
            notificationPane.setMouseTransparent(true);
        }
    }

    public long startTime;
    public int displayMS = 0;

    public void setDisplayTime(int displayS){
        displayMS = displayS*1000;
        startTime = System.currentTimeMillis();
    }

    public void showWithSubtitle(String text, String subtitle, final Action... actions){
        if(!isActive()){
            return;
        }

        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> showWithSubtitle(text, subtitle, actions));
            return;
        }

        TextFlow flow = new TextFlow();
        FXHelper.addText(flow, 14, "Bold", text + "\n");
        FXHelper.addText(flow, 12, "Normal", subtitle);

        notificationPane.setGraphic(null);
        notificationPane.getActions().clear();
        notificationPane.setMouseTransparent(false);
        notificationPane.show("", flow, actions);
        setDisplayTime(DBPreferences.INSTANCE.notificationsScreenTime.get());

        DrawingBotV3.logger.info("Notification: " + text + " : " + subtitle);
    }

    public void show(final String text) {
        if(!isActive()){
            return;
        }
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> show(text));
            return;
        }
        notificationPane.setGraphic(null);
        notificationPane.getActions().clear();
        notificationPane.setMouseTransparent(false);
        notificationPane.show(text);
        setDisplayTime(DBPreferences.INSTANCE.notificationsScreenTime.get());

        DrawingBotV3.logger.info("Notification: " + text);
    }

    public void show(final String text, final Node graphic) {
        if(!isActive()){
            return;
        }
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> show(text, graphic));
            return;
        }
        notificationPane.setGraphic(null);
        notificationPane.getActions().clear();
        notificationPane.setMouseTransparent(false);
        notificationPane.show(text, graphic);
        setDisplayTime(DBPreferences.INSTANCE.notificationsScreenTime.get());

        DrawingBotV3.logger.info("Notification: " + text);
    }

    public void show(final String text, final Node graphic, final Action... actions) {
        if(!isActive()){
            return;
        }
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> show(text, graphic, actions));
            return;
        }
        notificationPane.setGraphic(null);
        notificationPane.getActions().clear();
        notificationPane.setMouseTransparent(false);
        notificationPane.show(text, graphic, actions);
        setDisplayTime(DBPreferences.INSTANCE.notificationsScreenTime.get());

        DrawingBotV3.logger.info("Notification: " + text);
    }

    @Override
    public String getName() {
        return "Notifications";
    }
}
