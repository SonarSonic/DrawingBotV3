package drawingbot.javafx;

import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import drawingbot.DrawingBotV3;
import drawingbot.javafx.editors.SettingEditors;
import drawingbot.javafx.preferences.ProgramSettings;
import drawingbot.javafx.settings.BooleanSetting;
import drawingbot.render.overlays.DrawingBorderOverlays;
import drawingbot.render.overlays.RulerOverlays;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Note this implementation is inspired by / borrows from ControlsFX, PreferencesFX and FXSampler
 */
public class FXPreferences {

    public TextField textFieldSearchBar = null;
    public TreeView<TreeNode> treeViewCategories = null;
    public ScrollPane scrollPaneContent = null;
    public TreeNode root = null;

    @FXML
    public void initialize(){
        DrawingBotV3.INSTANCE.controller.preferencesStage.setResizable(true);

        ProgramSettings settings = DrawingBotV3.INSTANCE.getProgramSettings();
        root = root(
                node("Export Settings",
                        grid("Path Optimisation", grid -> {
                                ObservableValue<Boolean> disablePathOptimisation = settings.pathOptimisationEnabled.asBooleanProperty().not();

                                setting(grid,"Enable Path Optimisation", settings.pathOptimisationEnabled);

                                ObservableValue<Boolean> disableSimplify = Bindings.createBooleanBinding(() -> !settings.pathOptimisationEnabled.get() || !settings.lineSimplifyEnabled.get(), settings.pathOptimisationEnabled, settings.lineSimplifyEnabled);

                                title(grid,"Line Simplifying", disablePathOptimisation);
                                subtitle(grid,"Simplifies lines using the Douglas Peucker Algorithm", disablePathOptimisation);
                                setting(grid,"Enabled", settings.lineSimplifyEnabled, disablePathOptimisation);
                                setting(grid,"Tolerance", settings.lineSimplifyTolerance, disableSimplify);
                                setting(grid,"Units", settings.lineSimplifyUnits, disableSimplify);

                                gap(grid,12);

                                ObservableValue<Boolean> disableMerging = Bindings.createBooleanBinding(() -> !settings.pathOptimisationEnabled.get() || !settings.lineMergingEnabled.get(), settings.pathOptimisationEnabled, settings.lineMergingEnabled);

                                title(grid,"Line Merging", disablePathOptimisation);
                                subtitle(grid,"Merges start/end points within the given tolerance", disablePathOptimisation);
                                setting(grid,"Enabled", settings.lineMergingEnabled, disablePathOptimisation);
                                setting(grid,"Tolerance", settings.lineMergingTolerance, disableMerging);
                                setting(grid,"Units", settings.lineMergingUnits, disableMerging);

                                gap(grid,12);

                                title(grid,"Line Filtering", disablePathOptimisation);
                                subtitle(grid,"Remove lines shorter than the tolerance", disablePathOptimisation);
                                setting(grid,"Enabled", settings.lineFilteringEnabled, disablePathOptimisation);
                                setting(grid,"Tolerance", settings.lineFilteringTolerance, disablePathOptimisation);
                                setting(grid,"Units", settings.lineFilteringUnits, disablePathOptimisation);

                                gap(grid,12);

                                title(grid,"Line Sorting", disablePathOptimisation);
                                subtitle(grid,"Sorts lines to minimise air time", disablePathOptimisation);
                                setting(grid,"Enabled", settings.lineSortingEnabled, disablePathOptimisation);
                                setting(grid,"Tolerance", settings.lineSortingTolerance, disablePathOptimisation);
                                setting(grid,"Units", settings.lineSortingUnits, disablePathOptimisation);

                                gap(grid,12);

                                title(grid,"Line Multipass", disablePathOptimisation);
                                subtitle(grid,"Draws over each geometry multiple times", disablePathOptimisation);
                                setting(grid,"Enabled", settings.multipassEnabled, disablePathOptimisation);
                                setting(grid,"Count", settings.multipassCount, disablePathOptimisation);
                            }
                        ),
                        node("SVG"),
                        node("HPGL"),
                        node("Image & Animation")
                ),
                grid("User Interface", grid -> {


                            title(grid,"Rulers");
                            /*
                            setting(grid, "Enabled", RulerOverlays.INSTANCE.activeProperty());

                            node("Drawing Borders",
                                    Setting.of("Enabled", DrawingBorderOverlays.INSTANCE.activeProperty()),
                                    Setting.of("Colour", DrawingBorderOverlays.borderColour)
                            );
                            node("Notifications",
                                    convertSetting("Enabled", settings.notificationsEnabled),
                                    convertSetting("Screen Time", settings.notificationsScreenTime),
                                    Setting.of(new Label("Set this value to 0 if you don't want notifications to disappear"))
                            );

                             */
                        }
                ),
                node("Plugins")
        );

        treeViewCategories.setShowRoot(false);
        treeViewCategories.setRoot(build(root));

        textFieldSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            //rebuild the tree when the search changes
            treeViewCategories.setRoot(build(root));
        });

        treeViewCategories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.getValue() instanceof PageNode){
                PageNode node = (PageNode) newValue.getValue();
                scrollPaneContent.setContent(node.getContent());
            }
        });
        treeViewCategories.getSelectionModel().select(0);
    }

    public TreeItem<TreeNode> build(TreeNode rootNode){
        TreeItem<TreeNode> root = new TreeItem<>(rootNode);
        root.setExpanded(true);

        for(TreeNode child : rootNode.getChildren()){
            root.getChildren().add(build(child));
        }

        String search = textFieldSearchBar.getText();
        if(search != null && !search.isEmpty()){
            prune(root, search);
        }

        sort(root, Comparator.comparing(o -> o.getValue().getName()));

        return root;
    }

    public boolean prune(TreeItem<TreeNode> treeItem, String search){
        if(treeItem.isLeaf()){
            return treeItem.getValue().getName().toLowerCase().contains(search.toLowerCase());
        }else{
            List<TreeItem<TreeNode>> toRemove = new ArrayList<>();

            for (TreeItem<TreeNode> child : treeItem.getChildren()) {
                boolean keep = prune(child, search);
                if (! keep) {
                    toRemove.add(child);
                }
            }
            treeItem.getChildren().removeAll(toRemove);

            return !treeItem.getChildren().isEmpty();
        }
    }

    private void sort(TreeItem<TreeNode> node, Comparator<TreeItem<TreeNode>> comparator) {
        node.getChildren().sort(comparator);
        for (TreeItem<TreeNode> child : node.getChildren()) {
            sort(child, comparator);
        }
    }

    public TreeNode root(TreeNode...children){
        return new TreeNode("root", children);
    }

    public TreeNode node(String name, TreeNode...children){
        return new TreeNode(name, children);
    }

    public PageNode page(String name, Supplier<Node> node){
        return new PageNode(name){

            @Override
            public Node buildContent() {
                return node.get();
            }
        };
    }

    public void title(GridPane gridPane, String title){
        title(gridPane, title, null);
    }

    public void title(GridPane gridPane, String title, ObservableValue<Boolean> disabled){
        Label label = new Label(title);
        label.setFont(new Font(18));
        if(disabled != null){
            label.disableProperty().bind(disabled);
        }
        node(gridPane, label);
    }

    public void subtitle(GridPane gridPane, String title){
         subtitle(gridPane, title, null);
    }

    public void subtitle(GridPane gridPane, String title, ObservableValue<Boolean> disabled){
        Label label = new Label(title);
        label.setFont(new Font(14));
        label.setPadding(new Insets(0, 0, 0, 12));
        if(disabled != null){
            label.disableProperty().bind(disabled);
        }
        node(gridPane, label);
    }

    public void gap(GridPane gridPane, int size){
        Label label = new Label(" ");
        label.setFont(new Font(size));
        node(gridPane, label);
    }

    public void setting(GridPane gridPane, GenericSetting<?, ?> setting){
        setting(gridPane, setting.getDisplayName(), setting, null);
    }

    public void setting(GridPane gridPane, String displayName, GenericSetting<?, ?> setting){
        setting(gridPane, displayName, setting, null);
    }

    public void setting(GridPane gridPane, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled){
        setting(gridPane, setting.getDisplayName(), setting, disabled);
    }

    public void setting(GridPane gridPane, String displayName, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled){
        setting(gridPane, displayName, setting, disabled, 30);
    }

    public void setting(GridPane gridPane, String displayName, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled, int inset){
        Label label = new Label(displayName);
        Node editor = createEditor(setting);
        gridPane.addRow(gridPane.getRowCount(), label, editor);
        label.setPadding(new Insets(6, 0, 6, inset));

        if(disabled != null){
            label.disableProperty().bind(disabled);
            editor.disableProperty().bind(disabled);
        }
    }

    public void node(GridPane gridPane, Node node){
        gridPane.add(node, 0, gridPane.getRowCount(), 2, 1);
    }

    public PageNode grid(String name, Consumer<GridPane> consumer){
        return page(name, () -> {

            GridPane gridPane = new GridPane();
            gridPane.setHgap(4.0);
            gridPane.setVgap(4.0);
            gridPane.setPadding(new Insets(12, 12, 64, 12));
            ColumnConstraints column1 = new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true);
            column1.setPercentWidth(30);
            ColumnConstraints column2 = new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true);
            column2.setPercentWidth(70);

            gridPane.getColumnConstraints().addAll(column1, column2);

            HBox.setHgrow(gridPane, Priority.ALWAYS);
            gridPane.setMaxWidth(Double.MAX_VALUE);

            consumer.accept(gridPane);
            return gridPane;
        });
    }

    public static Node createEditor(GenericSetting<?, ?> generic){
        /*
        if(generic instanceof BooleanSetting){
            BooleanSetting<?> setting = (BooleanSetting<?>) generic;
            ToggleSwitch toggleSwitch = new ToggleSwitch();
            toggleSwitch.setTranslateX(-20);
            toggleSwitch.selectedProperty().bindBidirectional(setting.valueProperty());
            return toggleSwitch;
        }
         */
        return SettingEditors.createEditor(generic);
    }



    public static DefaultPropertyEditorFactory defaultPropertyEditorFactory = new DefaultPropertyEditorFactory();

    public PropertyEditor<?> getPropertyEditor(PropertySheet.Item item){
        //TODO CUSTOM EDITORS
        if(item instanceof SettingProperty && ((SettingProperty) item).setting instanceof BooleanSetting){
            return createSwitchEditor(item);
        }
        return defaultPropertyEditorFactory.call(item);
    }

    public static PropertyEditor<?> createSwitchEditor( PropertySheet.Item property) {

        return new AbstractPropertyEditor<Boolean, ToggleSwitch>(property, new ToggleSwitch()) {

            @Override protected BooleanProperty getObservableValue() {
                return getEditor().selectedProperty();
            }

            @Override public void setValue(Boolean value) {
                getEditor().setSelected((Boolean)value);
            }
        };
    }

    public static class SettingProperty implements PropertySheet.Item {

        public GenericSetting<?, ?> setting;

        public SettingProperty(GenericSetting<?, ?> setting){
            this.setting = setting;
        }

        @Override
        public Class<?> getType() {
            return setting.type;
        }

        @Override
        public String getCategory() {
            return setting.getCategory();
        }

        @Override
        public String getName() {
            return setting.getDisplayName();
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public Object getValue() {
            return setting.getValue();
        }

        @Override
        public void setValue(Object value) {
            setting.setValue(value);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.of(setting.valueProperty());
        }
    }

    public static class TreeNode implements Observable {

        public TreeNode(String name, TreeNode...children){
            setName(name);
            getChildren().addAll(children);
        }

        ////////////

        public final ObservableList<TreeNode> children = FXCollections.observableArrayList();

        public ObservableList<TreeNode> getChildren() {
            return children;
        }

        ////////////

        public final StringProperty name = new SimpleStringProperty();

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            children.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            children.removeListener(listener);
        }

        ////////////

        @Override
        public String toString() {
            return getName();
        }
    }

    public static abstract class PageNode extends TreeNode {

        private Node content;

        public PageNode(String name) {
            super(name);
        }

        public PageNode(String name, TreeNode...children){
            super(name, children);
        }

        public Node getContent(){
            //if(content == null){
                content = buildContent();
            //}
            return content;
        }

        public abstract Node buildContent();

    }

}
