package org.grobid.core.engines.label;

import java.io.Serializable;

import org.grobid.core.GrobidModel;
import org.grobid.core.engines.counters.Countable;

public interface TaggingLabel extends Countable, Serializable {

    GrobidModel getGrobidModel();

    String getLabel();
}
