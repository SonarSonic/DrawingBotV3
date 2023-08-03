package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.DBConstants;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class FXDocumentation extends AbstractFXController{

    public WebView webView;

    public Button buttonHome;
    public Button buttonBack;
    public Button buttonForward;

    @FXML
    public void initialize(){
        webView.getEngine().setOnAlert(e -> {
            DrawingBotV3.logger.severe("Web Error: " + e.toString());
        });

        VBox.setVgrow(webView, Priority.ALWAYS);
        HBox.setHgrow(webView, Priority.ALWAYS);

        WebHistory history = webView.getEngine().getHistory();

        buttonBack.setDisable(true);
        buttonBack.disableProperty().bind(Bindings.createBooleanBinding(() -> history.getEntries().isEmpty() || history.getCurrentIndex() == 0, history.currentIndexProperty(), history.getEntries()));

        buttonForward.setDisable(true);
        buttonForward.disableProperty().bind(Bindings.createBooleanBinding(() -> history.getEntries().isEmpty() || history.getCurrentIndex() == history.getEntries().size()-1, history.currentIndexProperty(), history.getEntries()));
    }

    public void back(){
        WebHistory history = webView.getEngine().getHistory();
        int index = history.getCurrentIndex() -1;
        if (index < 0 || index >= history.getEntries().size()) {
            return;
        }
        webView.getEngine().getHistory().go(-1);
    }

    public void forward(){
        WebHistory history = webView.getEngine().getHistory();
        int index = history.getCurrentIndex() +1;
        if (index < 0 || index >= history.getEntries().size()) {
            return;
        }
        webView.getEngine().getHistory().go(1);
    }

    public void home(){
        load(DBConstants.URL_READ_THE_DOCS_HOME);
    }

    public void openBrowser(){
        String location = webView.getEngine().getLocation();
        if(location != null){
            FXHelper.openURL(location);
        }
    }

    public void load(String url){
        webView.getEngine().load(url);
    }

    public static void openPFMHelp(PFMFactory<?> pfmFactory){
        String locationName = safeString(pfmFactory.getDisplayName().toLowerCase());
        navigate(DBConstants.URL_READ_THE_DOCS_PFMS + "#" + locationName);
    }

    public static void onPFMSettingSelected(GenericSetting<?, ?> pfmSetting){
        if(DrawingBotV3.INSTANCE.controller.documentationStage.isShowing() && getLocation() != null && getLocation().startsWith(DBConstants.URL_READ_THE_DOCS_PFMS)){
            openPFMSetting(pfmSetting);
        }
    }

    public static void openPFMSetting(GenericSetting<?, ?> pfmSetting){
        String docURL = DBConstants.URL_READ_THE_DOCS_PFMS;
        String docURLSuffix = "";

        if(pfmSetting instanceof CategorySetting){
            docURLSuffix = "#" + safeString(pfmSetting.getDisplayName().toLowerCase());
        }else{
            docURLSuffix = "#" + "term-" + safeString(pfmSetting.getDisplayName());
        }

        if(pfmSetting.getDocURL() != null){
            docURL = pfmSetting.getDocURL();
        }

        if(pfmSetting.getDocURLSuffix() != null){
            docURLSuffix = pfmSetting.getDocURLSuffix();
        }

        navigate( docURL + docURLSuffix);
    }

    public static void navigate(String url){
        DrawingBotV3.INSTANCE.controller.documentationController.load(url);
        DrawingBotV3.INSTANCE.controller.documentationStage.show();
    }

    public static String getLocation(){
        return DrawingBotV3.INSTANCE.controller.documentationController.webView.getEngine().getLocation();
    }

    /**
     * Removes illegal url characters, note doesn't make the result lower case, as some sphinx links will be case sensitive
     */
    public static String safeString(String string){
        string = string.replace(" ", "-");
        string = string.replace("(", "");
        string = string.replace(")", "");
        string = string.replace("%", "");
        return string;
    }

}
