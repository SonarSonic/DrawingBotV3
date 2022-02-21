package drawingbot.pfm;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.api.IPathFindingModule;
import drawingbot.files.json.adapters.JsonAdapterPFMFactory;
import drawingbot.javafx.GenericFactory;
import drawingbot.utils.EnumDistributionType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@JsonAdapter(JsonAdapterPFMFactory.class)
public class PFMFactory<C extends IPathFindingModule> extends GenericFactory<C> {

    public EnumDistributionType distributionType;
    public boolean bypassOptimisation = false;
    public boolean transparentColourSeperation = true;
    public boolean isBeta = false;
    public boolean isLayered = false;
    public boolean isComposite = false;
    public boolean hasSampledARGB = false;

    public boolean requiresPremium = false;

    public PFMFactory(Class<C> clazz, String name, Supplier<C> create, boolean isHidden) {
        super(clazz, name, create, isHidden);
        this.distributionType = EnumDistributionType.getRecommendedType(null, null);
    }

    /**
     * The PFMs distribution type preference
     */
    @Nullable
    public EnumDistributionType getDistributionType() {
        return distributionType;
    }

    public PFMFactory<C> setDistributionType(EnumDistributionType distributionType) {
        this.distributionType = distributionType;
        return this;
    }

    public boolean shouldBypassOptimisation() {
        return bypassOptimisation;
    }

    public PFMFactory<C> setBypassOptimisation(boolean bypassOptimisation) {
        this.bypassOptimisation = bypassOptimisation;
        return this;
    }

    public boolean getTransparentCMYK() {
        return transparentColourSeperation;
    }

    public PFMFactory<C> setTransparentCMYK(boolean transparentCMYK) {
        this.transparentColourSeperation = transparentCMYK;
        return this;
    }

    public boolean isLayeredPFM() {
        return isLayered;
    }

    public PFMFactory<C> setIsLayeredPFM(boolean isLayered) {
        this.isLayered = isLayered;
        return this;
    }

    /**
     * A composite PFM, will create sub PFMS while it's processing.
     */
    public boolean isCompositePFM(){
        return isComposite;
    }

    public PFMFactory<C> setIsComposite(boolean isComposite) {
        this.isComposite = isComposite;
        return this;
    }

    public boolean hasSampledARGB() {
        return hasSampledARGB;
    }

    public PFMFactory<C> hasSampledARGB(boolean hasSampledARGB) {
        this.hasSampledARGB = hasSampledARGB;
        return this;
    }

    public boolean isBeta() {
        return isBeta;
    }

    public PFMFactory<C> setIsBeta(boolean isBeta) {
        this.isBeta = isBeta;
        return this;
    }

    public boolean isPremium() {
        return requiresPremium;
    }

    public PFMFactory<C> setPremium(boolean isPremium) {
        this.requiresPremium = isPremium;
        return this;
    }
}