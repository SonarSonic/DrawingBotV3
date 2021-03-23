package drawingbot.pfm.modules;

import drawingbot.pfm.PFMModular;

/**
 * A PFM Module for use with PFM Modular
 */
public class ModularEncoder implements IPFMModule {

    protected PFMModular pfmModular;

    public void setPFMModular(PFMModular pfmModular){
        this.pfmModular = pfmModular;
    }
}
