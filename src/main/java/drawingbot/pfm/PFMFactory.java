package drawingbot.pfm;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.utils.EnumReleaseState;
import drawingbot.utils.INamedSetting;
import drawingbot.api.IPFM;
import drawingbot.files.json.adapters.JsonAdapterPFMFactory;
import drawingbot.javafx.GenericFactory;
import drawingbot.utils.EnumDistributionType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@JsonAdapter(JsonAdapterPFMFactory.class)
public class PFMFactory<C extends IPFM> extends GenericFactory<C> implements INamedSetting {

    public EnumDistributionType distributionType;
    public boolean bypassOptimisation = false;
    public boolean transparentColourSeperation = true;
    public EnumReleaseState releaseState = EnumReleaseState.RELEASE;
    public boolean isGenerative = false;
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

    public boolean isGenerativePFM(){
        return isGenerative;
    }

    public PFMFactory<C> setIsGenerative(boolean isGenerative) {
        this.isGenerative = isGenerative;
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

    @Override
    public EnumReleaseState getReleaseState() {
        return releaseState;
    }

    public PFMFactory<C> setReleaseState(EnumReleaseState releaseState) {
        this.releaseState = releaseState;
        return this;
    }

    @Override
    public boolean isPremiumFeature() {
        return requiresPremium;
    }

    public PFMFactory<C> setPremium(boolean isPremium) {
        this.requiresPremium = isPremium;
        return this;
    }
}