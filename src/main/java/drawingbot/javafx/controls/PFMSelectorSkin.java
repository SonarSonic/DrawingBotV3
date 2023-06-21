package drawingbot.javafx.controls;

import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class PFMSelectorSkin extends SkinBase<PFMSelectorControl> {

    protected PFMSelectorSkin(PFMSelectorControl control) {
        super(control);
        GridPane gridPane = new GridPane();
        ObservableList<PFMFactory<?>> pfmFactories = MasterRegistry.INSTANCE.getObservablePFMLoaderList();
        List<String> categories = new ArrayList<>();
        for(PFMFactory<?> factory : pfmFactories){
            if(!categories.contains(factory.category)){
                categories.add(factory.category);
            }
        }
        for(String category : categories){
            ObservableList<PFMFactory<?>> pfmPerCategory = FXCollections.observableArrayList();
            List<Node> nodes = new ArrayList<>();
            Label label = new Label(category);
            label.setStyle("");
            nodes.add(label);

            for(PFMFactory<?> factory : pfmFactories){
                if(!factory.category.equals(category)){
                    continue;
                }
                Label node = new Label();
                node.setText(factory.getDisplayName());
                node.setOnMouseClicked(e -> {
                    control.factory.set(factory);
                });
                nodes.add(node);
                if(factory.category.equals(category)){
                    pfmPerCategory.add(factory);
                }
            }
            int columnIndex = gridPane.getColumnCount();
            gridPane.getColumnConstraints().add(new ColumnConstraints(100));
            gridPane.addColumn(columnIndex, nodes.toArray(new Node[0]));
        }
        getChildren().add(gridPane);
    }



}
