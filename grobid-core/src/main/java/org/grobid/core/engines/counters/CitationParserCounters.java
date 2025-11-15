package org.grobid.core.engines.counters;

public class CitationParserCounters {
    public static final Countable SEGMENTED_REFERENCES = new Countable() {
        @Override
        public String getName() {
            return "SEGMENTED_REFERENCES";
        }
    };
    public static final Countable NULL_SEGMENTED_REFERENCES_LIST = new Countable() {
        @Override
        public String getName() {
            return "NULL_SEGMENTED_REFERENCES_LIST";
        }
    };
    public static final Countable EMPTY_REFERENCES_BLOCKS = new Countable() {
        @Override
        public String getName() {
            return "EMPTY_REFERENCES_BLOCKS";
        }
    };
    public static final Countable NOT_EMPTY_REFERENCES_BLOCKS = new Countable() {
        @Override
        public String getName() {
            return "NOT_EMPTY_REFERENCES_BLOCKS";
        }
    };
}
