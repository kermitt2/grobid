package org.grobid.core.engines.tagging;

import com.google.common.base.Joiner;
import org.grobid.core.GrobidModels;
import org.grobid.core.jni.WapitiModel;

import java.io.IOException;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class WapitiTagger implements GenericTagger {

    private final WapitiModel wapitiModel;

    public WapitiTagger(GrobidModels model) {
        wapitiModel = new WapitiModel(model);
    }

    @Override
    public String label(Iterable<String> data) {
        return label(Joiner.on('\n').join(data));
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
