package drawingbot.javafx.controls;

import drawingbot.files.json.projects.ObservableProject;
import drawingbot.javafx.FXHelper;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.List;
import java.util.function.Supplier;

public class ContextMenuObservableProject extends ContextMenu {

    public ContextMenuObservableProject(Supplier<List<ObservableProject>> listSupplier, Supplier<ObservableProject> projectSupplier) {
        super();

        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(e -> FXHelper.openRenameDialog(() -> projectSupplier.get().name));
        getItems().add(rename);

        MenuItem duplicate = new MenuItem("Duplicate");
        duplicate.setOnAction(e -> listSupplier.get().add(new ObservableProject(projectSupplier.get())));
        getItems().add(duplicate);

    }
}
