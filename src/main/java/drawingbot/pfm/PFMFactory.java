package drawingbot.pfm;

import drawingbot.api.IPathFindingModule;
import drawingbot.javafx.GenericFactory;
import drawingbot.utils.EnumDistributionType;

import java.util.function.Supplier;

public class PFMFactory<C extends IPathFindingModule> extends GenericFactory<C> {

    public EnumDistributionType distributionType;
    public boolean bypassOptimisation = false;
    public boolean transparentColourSeperation = true;
    public boolean isBeta = false;
    public boolean isLayered = false;
    public boolean requiresPremium = false;

    public PFMFactory(Class<C> clazz, String name, Supplier<C> create, boolean isHidden) {
        super(clazz, name, create, isHidden);
        this.distributionType = EnumDistributionType.EVEN_WEIGHTED;
    }

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