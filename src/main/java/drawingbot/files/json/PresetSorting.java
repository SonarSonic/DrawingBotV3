package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PresetSorting {

    public interface IPresetSorter {
        void sortPresets(IPresetLoader<?> loader, @Nullable List<GenericPreset<?>> displayedList);
    }
    
    public enum SortOperations{
        NATURAL_ORDER("Apply Default Order", PresetSorting::sortPresetsDefaultOrder),
        ALPHABETICAL("Apply Alphabetical Ordering", PresetSorting::sortPresetsAlphabetical);

        public String displayName;
        public IPresetSorter sorter;

        SortOperations(String displayName, IPresetSorter sorter) {
            this.displayName = displayName;
            this.sorter = sorter;
        }

        public void sortPresets(IPresetLoader<?> loader, @Nullable List<GenericPreset<?>> displayedList){
            this.sorter.sortPresets(loader, displayedList);
        }
    }

    public static void sortPresetsDefaultOrder(IPresetLoader<?> loader, @Nullable List<GenericPreset<?>> displayedList) {
        loader.restoreDefaultOrder(displayedList);
    }

    public static void sortPresetsAlphabetical(IPresetLoader<?> loader, @Nullable List<GenericPreset<?>> displayedList) {
        loader.sortPresets((a, b) -> {
            if(!a.getPresetSubType().equals(b.getPresetSubType())){
                int indexA = loader.getPresetSubTypes().indexOf(a.getPresetSubType());
                int indexB = loader.getPresetSubTypes().indexOf(b.getPresetSubType());
                if(indexA != indexB){
                    return indexA < indexB ? -1 : 1;
                }
            }
            return String.CASE_INSENSITIVE_ORDER.compare(a.getPresetName(), b.getPresetName());
        }, displayedList);
    }

}
