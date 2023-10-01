package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.proxy.SimpleProxyHandler;
import drawingbot.javafx.FXHelper;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.DBConstants;
import drawingbot.utils.DBTask;
import drawingbot.utils.Utils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.controlsfx.control.action.Action;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    public static UpdateChecker INSTANCE = new UpdateChecker();

    //Internal
    public SimpleObjectProperty<UpdateStatus> updateStatus = new SimpleObjectProperty<>(UpdateStatus.UNKNOWN);
    public SimpleStringProperty latestVersion = new SimpleStringProperty("");

    public void requestLatestUpdate(){
       if(updateStatus.get() == UpdateStatus.UNKNOWN){
           updateStatus.set(UpdateStatus.CHECKING);
           UpdateCheckerTask task = new UpdateCheckerTask(DrawingBotV3.context());
           DrawingBotV3.INSTANCE.backgroundService.submit(task);
           task.valueProperty().addListener((observable, oldValue, newValue) -> {
               if(newValue != null){
                   latestVersion.set(newValue.latestVersion);
                   updateStatus.set(newValue.updateStatus);
                   updateStatus.get().sendNotification(latestVersion.get());
               }
           });
       }else{
           //Re-send the update status.
           updateStatus.get().sendNotification(latestVersion.get());
       }
    }

    public static class UpdateCheckerTask extends DBTask<UpdateCheckerResult> {

        public UpdateCheckerTask(DBTaskContext context) {
            super(context);
        }

        @Override
        protected UpdateCheckerResult call() throws Exception {

            // Download the CHANGELOG.MD, to check for changes
            URL url = new URI(DBConstants.CHANGELOG_URL).toURL();
            String changelog = "";
            URLConnection connection = url.openConnection(SimpleProxyHandler.getDefaultProxy());
            try (InputStream in = connection.getInputStream()){
                byte[] bytes = in.readAllBytes();
                changelog = new String(bytes, StandardCharsets.UTF_8);
            }catch (Exception e){
                DrawingBotV3.logger.log(Level.SEVERE, "TASK FAILED", e);
                return new UpdateCheckerResult(UpdateStatus.UNAVAILABLE, "");
            }

            // RegEx extracts all version names and also the raw version name
            Pattern pattern = Pattern.compile("\\[v(.*?)-.*?\\]");
            Matcher matcher = pattern.matcher(changelog);
            if(matcher.find()){
                String fullVersion = matcher.group(0).replace("[", "").replace("]", "");
                String rawVersion = matcher.group(1);
                int result = Utils.compareVersion(FXApplication.getSoftware().getRawVersion(), rawVersion, 3);
                return new UpdateCheckerResult(result == -1 ? UpdateStatus.OUTDATED : UpdateStatus.LATEST, fullVersion);
            }
            return new UpdateCheckerResult(UpdateStatus.UNAVAILABLE, "");
        }

    }

    public static class UpdateCheckerResult{

        public UpdateStatus updateStatus;
        public String latestVersion;

        public UpdateCheckerResult(UpdateStatus updateStatus, String latestVersion) {
            this.updateStatus = updateStatus;
            this.latestVersion = latestVersion;
        }
    }


    public enum UpdateStatus{
        UNKNOWN, //Updates haven't been checked yet.
        CHECKING, //Waiting for server response.
        LATEST, //The latest version is already installed.
        OUTDATED, //The installed version is outdated.
        UNAVAILABLE; //No server response.

        public void sendNotification(String latestVersion){
            switch (this){
                case LATEST -> {
                    NotificationOverlays.INSTANCE.showWithSubtitle("CHECK", "Latest Version: " + FXApplication.getSoftware().getDisplayVersion(), "You are running the latest version");
                }
                case OUTDATED -> {
                    NotificationOverlays.INSTANCE.showWithSubtitle("ARROW_UP","Update Available: " + latestVersion, "Update to the latest version now!", new Action("Update", e -> FXHelper.openURL(FXApplication.getSoftware().getUpdateLink())));
                }
                case UNAVAILABLE -> {
                    NotificationOverlays.INSTANCE.showWithSubtitle("WARNING", "Update Check Failed", "This may be caused by connection Issues");
                }
            }
        }

        @Override
        public String toString() {
            return Utils.capitalize(name());
        }
    }

}
