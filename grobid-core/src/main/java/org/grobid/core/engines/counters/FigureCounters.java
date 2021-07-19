package org.grobid.core.engines.counters;

public class FigureCounters {
    public static final Countable TOO_MANY_FIGURES_PER_PAGE = new Countable() {
        @Override
        public String getName() {
            return "TOO_MANY_FIGURES_PER_PAGE";
        }
    };
    public static final Countable STANDALONE_FIGURES = new Countable() {
        @Override
        public String getName() {
            return "STANDALONE_FIGURES";
        }
    };

    public static final Countable SKIPPED_BAD_STANDALONE_FIGURES = new Countable() {
        @Override
        public String getName() {
            return "SKIPPED_BAD_STANDALONE_FIGURES";
        }
    };

    public static final Countable SKIPPED_SMALL_STANDALONE_FIGURES = new Countable() {
        @Override
        public String getName() {
            return "SKIPPED_SMALL_STANDALONE_FIGURES";
        }
    };
    public static final Countable SKIPPED_BIG_STANDALONE_FIGURES = new Countable() {
        @Override
        public String getName() {
            return "SKIPPED_BIG_STANDALONE_FIGURES";
        }
    };

    public static final Countable ASSIGNED_GRAPHICS_TO_FIGURES = new Countable() {
        @Override
        public String getName() {
            return "ASSIGNED_GRAPHICS_TO_FIGURES";
        }
    };

    public static final Countable SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS = new Countable() {
        @Override
        public String getName() {
            return "SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS";
        }
    };
}
