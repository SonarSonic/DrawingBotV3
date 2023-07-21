package drawingbot.pfm;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.api.IPFM;
import drawingbot.files.json.adapters.JsonAdapterPFMFactory;
import drawingbot.javafx.GenericFactory;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.EnumReleaseState;
import drawingbot.utils.INamedSetting;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@JsonAdapter(JsonAdapterPFMFactory.class)
public class PFMFactory<C extends IPFM> extends GenericFactory<C> implements INamedSetting {

    public EnumDistributionType distributionType;
    public EnumReleaseState releaseState = EnumReleaseState.RELEASE;
    public FlagStates flags = new FlagStates(Flags.PFM_FACTORY_FLAGS);

    public String category = "";
    public String displayName;

    public PFMFactory(Class<C> clazz, String name, String category, Supplier<C> create) {
        super(clazz, name, create, false);
        this.distributionType = EnumDistributionType.getRecommendedType(null, null);
        this.category = category;
        this.displayName = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public PFMFactory<C> setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public boolean isHidden() {
        return releaseState.isExperimental();
    }

    public FlagStates getFlags(){
        return flags;
    }

    public final <V> PFMFactory<C> setFlags(V value, Flags.Flag<V> ...flagArray) {
        for(Flags.Flag<V> flag : flagArray){
            flags.setFlag(flag, value);
        }
        return this;
    }

    public boolean shouldLineOptimise() {
        return flags.getFlag(Flags.PFM_LINE_OPTIMISING);
    }

    public PFMFactory<C> setLineOptimisation(boolean bypassOptimisation) {
        flags.setFlag(Flags.PFM_LINE_OPTIMISING, bypassOptimisation);
        return this;
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

    public boolean getTransparentCMYK() {
        return flags.getFlag(Flags.PFM_TRANSPARENT_CMYK);
    }

    public PFMFactory<C> setTransparentCMYK(boolean transparentCMYK) {
        flags.setFlag(Flags.PFM_TRANSPARENT_CMYK, transparentCMYK);
        return this;
    }

    public boolean isLayeredPFM() {
        return flags.getFlag(Flags.PFM_LAYERED);
    }

    public PFMFactory<C> setIsLayeredPFM(boolean isLayered) {
        flags.setFlag(Flags.PFM_LAYERED, isLayered);
        return this;
    }

    public boolean isGenerativePFM(){
        return flags.getFlag(Flags.PFM_GENERATIVE);
    }

    public PFMFactory<C> setIsGenerative(boolean isGenerative) {
        flags.setFlag(Flags.PFM_GENERATIVE, isGenerative);
        return this;
    }

    public boolean supportsSoftClip() {
        return flags.getFlag(Flags.PFM_SUPPORTS_SOFT_CLIP);
    }

    public PFMFactory<C> setSupportsSoftClip(boolean supportsSoftClip) {
        flags.setFlag(Flags.PFM_SUPPORTS_SOFT_CLIP, supportsSoftClip);
        return this;
    }

    /**
     * A composite PFM, will create sub PFMS while it's processing.
     */
    public boolean isCompositePFM(){
        return flags.getFlag(Flags.PFM_COMPOSITE);
    }

    public PFMFactory<C> setIsComposite(boolean isComposite) {
        flags.setFlag(Flags.PFM_COMPOSITE, isComposite);
        return this;
    }

    public boolean hasSampledARGB() {
        return flags.getFlag(Flags.PFM_HAS_SAMPLED_ARGB);
    }

    public PFMFactory<C> hasSampledARGB(boolean hasSampledARGB) {
        flags.setFlag(Flags.PFM_HAS_SAMPLED_ARGB, hasSampledARGB);
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
    public boolean isNewFeature() {
        return flags.getFlag(Flags.PFM_NEW_FEATURE);
    }

    public PFMFactory<C> setNewFeature(boolean newFeature) {
        flags.setFlag(Flags.PFM_NEW_FEATURE, newFeature);
        return this;
    }

    @Override
    public boolean isPremiumFeature() {
        return flags.getFlag(Flags.PFM_REQUIRES_PREMIUM);
    }

    public PFMFactory<C> setPremium(boolean isPremium) {
        flags.setFlag(Flags.PFM_REQUIRES_PREMIUM, isPremium);
        return this;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}