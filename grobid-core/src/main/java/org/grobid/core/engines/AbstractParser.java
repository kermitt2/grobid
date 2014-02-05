package org.grobid.core.engines;

import java.io.Closeable;
import java.io.IOException;
import java.util.StringTokenizer;

import org.chasen.crfpp.Model;
import org.chasen.crfpp.Tagger;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractParser implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class);
    private final Model model;

//    protected Tagger tagger;

    protected AbstractParser(GrobidModels model) {
        this.model = ModelMap.getModel(model);
//		tagger = createTagger(model);
    }

//	public Tagger getTagger() {
//		return tagger;
//	}

    // TODO: VZ: Switch to String iterables

    protected String getTaggerResult(StringTokenizer st) {
        return getTaggerResult(st, null);
    }

    protected String getTaggerResult(StringTokenizer st, String type) {
        Tagger tagger = null;
        StringBuilder res;
        try {
            tagger = feedTaggerAndParse(st);

            res = new StringBuilder();
            for (int i = 0; i < tagger.size(); i++) {
                for (int j = 0; j < tagger.xsize(); j++) {
                    res.append(tagger.x(i, j)).append("\t");
                }

                if (type != null) {
                    res.append(type).append("\t");
                }

                res.append(tagger.y2(i));
                res.append("\n");
            }
        } finally {
            if (tagger != null) {
                tagger.delete();
            }
        }

        return res.toString();
    }

    private Tagger feedTaggerAndParse(StringTokenizer st) {
        Tagger tagger = getNewTagger();
        feedTaggerAndParse(tagger, st);
        return tagger;
    }

//	protected void feedTagger(StringTokenizer st) {
//        Tagger tagger = getNewTagger();
//		feedTagger(tagger, st);
//        tagger.delete();
//	}

    public Tagger getNewTagger() {
        return model.createTagger();
    }


    public static void feedTaggerAndParse(Tagger tagger, StringTokenizer st) {
        tagger.clear();
        feedTagger(tagger, st);
        if (!tagger.parse()) {
            throw new GrobidException("CRF++ parsing failed.");
        }

        if (!tagger.what().isEmpty()) {
            LOGGER.warn("CRF++ Tagger Warnings: " + tagger.what());
        }
    }

    private static void feedTagger(Tagger tagger, StringTokenizer st) {
        while (st.hasMoreTokens()) {
            String piece = st.nextToken();
            if (piece.trim().isEmpty()) {
                continue;
            }
            if (!tagger.add(piece)) {
                LOGGER.warn("CRF++ Tagger Warnings: " + tagger.what());
                throw new GrobidException("Cannot add a feature row: " + piece
                        + "\n Reason: " + tagger.what());
            }
        }
    }

    public static Tagger createTagger(GrobidModels model) {
        return ModelMap.getTagger(model);
    }

    @Override
    public void close() throws IOException {
//		if (tagger != null) {
//			tagger.clear();
//			tagger.delete();
//		}
//		tagger = null;
    }
}
