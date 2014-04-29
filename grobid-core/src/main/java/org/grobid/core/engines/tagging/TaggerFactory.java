package org.grobid.core.engines.tagging;

import org.grobid.core.GrobidModels;
import org.grobid.core.utilities.GrobidProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class TaggerFactory {
    private static Map<GrobidModels, GenericTagger> cache = new HashMap<GrobidModels, GenericTagger>();

    public static synchronized GenericTagger getTagger(GrobidModels model) {
        GenericTagger t = cache.get(model);
        if (t == null) {
            switch (GrobidProperties.getGrobidCRFEngine()) {
                case CRFPP:
                    t = new CRFPPTagger(model);
                    break;
                case WAPITI:
                    t = new WapitiTagger(model);
                    break;
                default:
                    throw new IllegalStateException("Unsupported Grobid CRF engine: " + GrobidProperties.getGrobidCRFEngine());
            }
            cache.put(model, t);
        }
        return t;
    }
}
