package org.grobid.core.engines.tagging;

import org.grobid.core.GrobidModels;

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
            t = new WapitiTagger(model);
            cache.put(model, t);
        }
        return t;
    }
}
