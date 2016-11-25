package org.grobid.core.engines.counters;

import org.grobid.core.engines.Countable;

/**
 * Created by lfoppiano on 25/11/16.
 */
public class FigureCounters {
    public static final Countable TOO_MANY_FIGURES_PER_PAGE = new Countable() {
        @Override
        public String getName() {
            return "TOO_MANY_FIGURES_PER_PAGE";
        }
    };

    public static final Countable SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS = new Countable() {
        @Override
        public String getName() {
            return "SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS";
        }
    };
}
