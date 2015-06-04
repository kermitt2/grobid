package org.grobid.core.engines.tagging;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.allenai.ml.sequences.SequenceTagger;
import org.allenai.ml.sequences.crf.conll.ConllFormat;
import org.grobid.core.GrobidModels;
import org.grobid.core.jni.WapitiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class AI2CRFTagger implements GenericTagger {
    public static final Logger LOGGER = LoggerFactory.getLogger(AI2CRFTagger.class);
    private final SequenceTagger<String, ConllFormat.Row> tagger;

    public AI2CRFTagger(GrobidModels model) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(model.getModelPath()));
            this.tagger = ConllFormat.loadModel(dis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String label(Iterable<String> data) {
        Iterator<String> iter = data.iterator();
        List<String> lines = new ArrayList<String>();
        while (iter.hasNext()) {
            String line = iter.next();
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        List<ConllFormat.Row> rows = ConllFormat.readDatum(lines, false);
        List<String> labels = tagger.bestGuess(rows);
        if (labels.size() != lines.size()) {
            throw new RuntimeException("Bad tagging");
        }
        List<String> result = new ArrayList<String>(rows.size());
        try {
            for (int idx = 0; idx < lines.size(); idx++) {
                String token = rows.get(idx + 1).features.get(0);
                String label = labels.get(idx);
                result.add(token + "\t" + label);
            }
        } catch (Exception e) {
            throw new RuntimeException("Bad");
        }

        return Joiner.on("\n").join(result);
    }


    @Override
    public String label(String data) {
        return label(Splitter.on("\n").split(data));
    }

    @Override
    public void close() throws IOException {

    }
}
