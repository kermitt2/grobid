package org.grobid.core.engines.tagging;

import jdk.internal.joptsimple.internal.Strings;
import org.grobid.core.GrobidModel;
import org.grobid.core.jni.WapitiModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WapitiTagger implements GenericTagger {

    private final WapitiModel wapitiModel;

    public WapitiTagger(GrobidModel model) {
        wapitiModel = new WapitiModel(model);
    }

    @Override
    public String label(Iterable<String> data) {
        List<String> labelled = new ArrayList<>();
        data.forEach( d -> {
            labelled.add(label(d));   
        });
        return Strings.join(labelled, "\n\n");
    }

    @Override
    public String label(String data) {
        return wapitiModel.label(data);
    }

    @Override
    public void close() throws IOException {
        wapitiModel.close();
    }
}
