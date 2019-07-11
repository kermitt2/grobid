package org.grobid.trainer;

import org.grobid.core.utilities.GrobidProperties;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class TrainerFactory {
    public static GenericTrainer getTrainer() {
        switch (GrobidProperties.getGrobidCRFEngine()) {
            case CRFPP:
                return new CRFPPGenericTrainer();
            case WAPITI:
                return new WapitiTrainer();
            case DELFT:
                return new DeLFTTrainer();
            case DUMMY:
                return new DummyTrainer();
            default:
                throw new IllegalStateException("Unsupported Grobid sequence labelling engine: " + GrobidProperties.getGrobidCRFEngine());
        }
    }
}
