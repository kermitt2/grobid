package org.grobid.core.engines.tagging;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;

import java.io.IOException;

/**
 * This tagger just return one label <dummy>
 */
public class DummyTagger implements GenericTagger {

    public DummyTagger(GrobidModel model) {
        if(!model.equals(GrobidModels.DUMMY)) {
            throw new GrobidException("Cannot use a non-dummy model with the dummy tagger. All dummies or no dummies. ");
        }
    }

    @Override
    public String label(Iterable<String> data) {
        return "<dummy>";
    }

    @Override
    public String label(String data) {
        return "<dummy>";
    }

    @Override
    public void close() throws IOException {

    }
}
