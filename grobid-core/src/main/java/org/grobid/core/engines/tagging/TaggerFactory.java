package org.grobid.core.engines.tagging;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.utilities.GrobidProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for a sequence labelling, aka a tagger, instance.
 * Supported implementations are CRF (CRFPP, Wapiti) and Deep Learning (DeLFT)  
 *
 */
public class TaggerFactory {
    private static Map<GrobidModel, GenericTagger> cache = new HashMap<GrobidModel, GenericTagger>();

    public static synchronized GenericTagger getTagger(GrobidModel model) {
        GenericTagger t = cache.get(model);
        if (t == null) {
            switch (GrobidProperties.getGrobidCRFEngine()) {
                case CRFPP:
                    t = new CRFPPTagger(model);
                    break;
                case WAPITI:
                    t = new WapitiTagger(model);
                    break;
                case DELFT:
                    // if model is fulltext or segmentation we use currently WAPITI as fallback because they
                    // are not covered by DeLFT for the moment
                    if (model.getModelName().equals("fulltext") || model.getModelName().equals("segmentation"))
                        t = new WapitiTagger(model);
                    else    
                        t = new DeLFTTagger(model);
                    break;
                default:
                    throw new IllegalStateException("Unsupported Grobid sequence labelling engine: " + GrobidProperties.getGrobidCRFEngine());
            }
            cache.put(model, t);
        }
        return t;
    }
}
