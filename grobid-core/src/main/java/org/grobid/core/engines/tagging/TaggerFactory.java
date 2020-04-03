package org.grobid.core.engines.tagging;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.main.LibraryLoader;

import java.util.HashMap;
import java.util.Map;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for a sequence labelling, aka a tagger, instance.
 * Supported implementations are CRF (CRFPP, Wapiti) and Deep Learning (DeLFT)  
 *
 */
public class TaggerFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(TaggerFactory.class);

    private static Map<GrobidModel, GenericTagger> cache = new HashMap<>();

    private TaggerFactory() {}

    public static synchronized GenericTagger getTagger(GrobidModel model) {
        return getTagger(model, GrobidProperties.getGrobidCRFEngine());
    }

    public static synchronized GenericTagger getTagger(GrobidModel model, GrobidCRFEngine engine) {
        GenericTagger t = cache.get(model);
        if (t == null) {
            if(model.equals(GrobidModels.DUMMY)) {
                return new DummyTagger(model);
            }

            if(engine != null) {
                switch (engine) {
                    case CRFPP:
                        t = new CRFPPTagger(model);
                        break;
                    case WAPITI:
                        t = new WapitiTagger(model);
                        break;
                    case DELFT:
                        // be sure the native JEP lib can be loaded
//                        try {
//                            String libraryFolder = LibraryLoader.getLibraryFolder();
//                            System.out.println(libraryFolder);
//                            LibraryLoader.addLibraryPath(libraryFolder);
//                        } catch (Exception e) {
//                            LOGGER.info("Loading JEP native library for DeLFT failed", e);
//                        }

                        // if model is fulltext or segmentation we use currently WAPITI as fallback because they
                        // are not covered by DeLFT for the moment
                        if (model.equals(GrobidModels.FULLTEXT) || model.equals(GrobidModels.SEGMENTATION))
                            t = new WapitiTagger(model);
                        else
                            t = new DeLFTTagger(model);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported Grobid sequence labelling engine: " + GrobidProperties.getGrobidCRFEngine());
                }
                cache.put(model, t);
            } else {
                throw new IllegalStateException("Unsupported or null Grobid sequence labelling engine: " + GrobidProperties.getGrobidCRFEngine());
            }
        }
        return t;
    }
}
