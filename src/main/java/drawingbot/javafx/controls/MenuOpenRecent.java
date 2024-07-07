package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.files.RecentProjectHandler;
import drawingbot.javafx.FXHelper;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MenuOpenRecent extends Menu {

    public MenuOpenRecent(String text) {
        super(text);

        RecentProjectHandler.recentProjects.addListener((ListChangeListener<File>) c -> {
            while (c.next()){
                if(c.wasRemoved()){
                    List<? extends File> removed = c.getRemoved();
                    getItems().removeIf(m -> m instanceof RecentProjectMenuItem recent && removed.contains(recent.file));
                }
                if(c.wasAdded()) {
                    List<RecentProjectMenuItem> nodes = c.getAddedSubList().stream().map(RecentProjectMenuItem::new).toList();
                    getItems().addAll(c.getFrom(), nodes);
                }
                if(c.wasPermutated()){
                    List<MenuItem> items = new ArrayList<>(c.getTo() - c.getFrom());
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        int newIndex = c.getPermutation(i);
                        items.set(newIndex, getItems().get(i));
                    }
                    getItems().remove(c.getFrom(), c.getTo());
                    getItems().addAll(c.getFrom(), items);
                }
            }

        });
        RecentProjectHandler.recentProjects.forEach(f -> getItems().add(new RecentProjectMenuItem(f)));

    }

    public static class RecentProjectMenuItem extends CustomMenuItem {

        public final File file;

        public RecentProjectMenuItem(File file) {
            super();
            this.file = file;

            TextFlow textFlow = new TextFlow();
            FXHelper.addText(textFlow, 12, "normal", file.getName());
            FXHelper.addText(textFlow,  "-fx-font-size: 10px; -fx-font-style: italic;", "\n" + file.getParentFile().getPath());
            setContent(textFlow);
            this.setOnAction(e -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, false));

        }
    }


}
