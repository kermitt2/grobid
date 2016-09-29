package org.grobid.core.engines;

import org.grobid.core.GrobidModel;

/**
 * Created by lfoppiano on 29/09/16.
 */
public interface TaggingLabel {

    GrobidModel getGrobidModel();

    String getLabel();
}
