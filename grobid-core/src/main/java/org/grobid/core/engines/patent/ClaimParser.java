package org.grobid.core.engines.patent;

import org.grobid.core.features.*;

/**
 * Structure extraction and content analysis of patent claims.
 *
 * @author Patrice Lopez
 */
public class ClaimParser {

    private FeatureFactory featureFactory = null;

    public ClaimParser() {
        featureFactory = FeatureFactory.getInstance();
    }


}