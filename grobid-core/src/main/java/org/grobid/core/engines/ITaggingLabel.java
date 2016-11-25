package org.grobid.core.engines;

import org.grobid.core.GrobidModel;

/**
 * Created by lfoppiano on 25/11/16.
 */
public interface ITaggingLabel extends Countable{

    GrobidModel getGrobidModel();

    String getLabel();
}
