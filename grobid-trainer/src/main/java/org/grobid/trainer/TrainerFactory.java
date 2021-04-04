package org.grobid.trainer;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.GrobidModel;
import org.grobid.core.engines.tagging.GrobidCRFEngine;

public class TrainerFactory {
    public static GenericTrainer getTrainer(GrobidModel model) {
        switch (GrobidProperties.getGrobidCRFEngine(model)) {
            case CRFPP:
                return new CRFPPGenericTrainer();
            case WAPITI:
                return new WapitiTrainer();
            case DELFT:
                return new DeLFTTrainer();
            case DUMMY:
                return new DummyTrainer();
            default:
                throw new IllegalStateException("Unsupported GROBID sequence labelling engine: " + GrobidProperties.getGrobidCRFEngine(model));
        }
    }
}
