package drawingbot.pfm;

import drawingbot.api.IPathFindingModule;
import drawingbot.javafx.GenericFactory;
import drawingbot.pfm.modules.IPFMModule;
import drawingbot.utils.EnumDistributionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PFMFactory<C extends IPathFindingModule> extends GenericFactory<C> {

    public EnumDistributionType distributionType;
    public List<Class<? extends IPFMModule>> encoders;
    public boolean bypassOptimisation = false;

    public PFMFactory(Class<C> clazz, String name, Supplier<C> create, boolean isHidden) {
        super(clazz, name, create, isHidden);
        this.distributionType = EnumDistributionType.EVEN_WEIGHTED;
        this.encoders = new ArrayList<>();
    }

    public EnumDistributionType getDistributionType() {
        return distributionType;
    }

    public PFMFactory<C> setDistributionType(EnumDistributionType distributionType) {
        this.distributionType = distributionType;
        return this;
    }

    public List<Class<? extends IPFMModule>> getEncoders() {
        return encoders;
    }

    public PFMFactory<C> setEncoders(List<Class<? extends IPFMModule>> encoders) {
        this.encoders = encoders;
        return this;
    }

    public boolean shouldBypassOptimisation() {
        return bypassOptimisation;
    }

    public PFMFactory<C> setBypassOptimisation(boolean bypassOptimisation) {
        this.bypassOptimisation = bypassOptimisation;
        return this;
    }
}