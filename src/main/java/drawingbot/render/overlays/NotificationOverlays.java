package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.render.viewport.Viewport;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

//TODO MAKE INSTANCEABLE
public class NotificationOverlays extends ViewportOverlayBase {

    public static final NotificationOverlays INSTANCE = new NotificationOverlays();

    private NotificationOverlays(){}

    public NotificationPane notificationPane;

    @Override
    public void initOverlay() {
        notificationPane = new NotificationPane();
        notificationPane.setManaged(false);
        notificationPane.setShowFromTop(false);
        notificationPane.setPickOnBounds(false);
        notificationPane.resize(600, 70);
        notificationPane.getStylesheets().add(NotificationOverlays.class.getResource("/drawingbot/notifications.css").toExternalForm());


        JFXUtils.subscribeListener(target, (observable, oldValue, newValue) -> {
            if(oldValue != null){
                notificationPane.layoutXProperty().unbind();
                notificationPane.layoutYProperty().unbind();
            }
            if(newValue != null){
                notificationPane.layoutXProperty().bind(newValue.layoutXProperty().add(newValue.widthProperty()).subtract(notificationPane.widthProperty()).subtract(30));
                notificationPane.layoutYProperty().bind(newValue.layoutYProperty().add(newValue.heightProperty()).subtract(notificationPane.heightProperty()).subtract(50));
                newValue.getChildren().add(notificationPane);
            }
        });

    }

    @Override
    public void activateViewportOverlay(Viewport viewport) {
        super.activateViewportOverlay(viewport);
        notificationPane.setVisible(true);
    }

    @Override
    public void deactivateViewportOverlay(Viewport viewport) {
        super.deactivateViewportOverlay(viewport);
        notificationPane.setVisible(false);
        notificationPane.hide();
        notificationPane.setMouseTransparent(true);
    }

    @Override
    public void onRenderTick() {
        super.onRenderTick();

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

    public static Node getIcon(String name){
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        return fontAwesome.create(name).size(12);
    }

    public void showWithSubtitle(String icon, String text, String subtitle, final Action... actions) {
        showIconTextSubtitle(getIcon(icon), text, subtitle, actions);
    }

    public void showWithSubtitle(String text, String subtitle, final Action... actions) {
        showIconTextSubtitle(null, text, subtitle, actions);
    }

    public void showIconTextSubtitle(Node icon, String text, String subtitle, final Action... actions){
        if(!getEnabled()){
            return;
        }
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> showIconTextSubtitle(icon, text, subtitle, actions));
            return;
        }

        Parent parent = null;
        VBox vBox = new VBox();
        vBox.getStyleClass().add("notification-vbox");

        if(!text.isEmpty()){
            Label titleLabel = new Label(text);
            titleLabel.getStyleClass().add("notification-title");

            if(icon != null){
                HBox hBox = new HBox(icon, titleLabel);
                hBox.setAlignment(Pos.BASELINE_LEFT);
                hBox.setSpacing(4);
                vBox.getChildren().add(hBox);
            }else{
                vBox.getChildren().add(titleLabel);
            }
        }

        if(!subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setPadding(new Insets(0, 0, 0, icon != null ? 16 : 0));
            subtitleLabel.getStyleClass().add("notification-subtitle");
            vBox.getChildren().add(subtitleLabel);
        }

        parent = vBox;

        if(actions.length != 0){
            HBox hBox = new HBox();
            ButtonBar buttonBar = new ButtonBar();
            for(Action action : actions){
                Button button = new Button(action.getText());
                button.setOnAction(action);
                button.setStyle("-fx-font-size: 12px;");
                buttonBar.getButtons().add(button);
            }
            hBox.getChildren().addAll(vBox, buttonBar);
            HBox.setHgrow(vBox, Priority.NEVER);
            HBox.setHgrow(buttonBar, Priority.SOMETIMES);
            parent = hBox;
        }

        notificationPane.getActions().clear();
        notificationPane.setMouseTransparent(false);
        notificationPane.show("", parent);
        setDisplayTime(DBPreferences.INSTANCE.notificationsScreenTime.get());

        DrawingBotV3.logger.info("Notification: " + text + " : " + subtitle);
    }

    public void show(final String text) {
        if(!getEnabled()){
            return;
        }
        showWithSubtitle(text, "");
    }

    public void show(final String text, final Node graphic) {
        if(!getEnabled()){
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
        if(!getEnabled()){
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

    /////////////////////////////////////////

    public final ObjectProperty<Pane> target = new SimpleObjectProperty<>();

    public Pane getTarget() {
        return target.get();
    }

    public ObjectProperty<Pane> targetProperty() {
        return target;
    }

    public void setTarget(Pane target) {
        this.target.set(target);
    }

    /////////////////////////////////////////

    @Override
    public String getName() {
        return "Notifications";
    }
}
