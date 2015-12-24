package org.grobid.core.engines;

import org.grobid.core.engines.entities.ChemicalParser;
import org.grobid.core.engines.patent.ReferenceExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Slava
 * Date: 4/15/14
 */
public class EngineParsers implements Closeable {
    public static final Logger LOGGER = LoggerFactory.getLogger(EngineParsers.class);

    private AuthorParser authorParser = null;
    private AffiliationAddressParser affiliationAddressParser = null;
    private HeaderParser headerParser = null;
    private DateParser dateParser = null;
    private CitationParser citationParser = null;
    private FullTextParser fullTextParser = null;
    private ReferenceExtractor referenceExtractor = null;
    private ChemicalParser chemicalParser = null;
    private Segmentation segmentationParser = null;
    private ReferenceSegmenterParser referenceSegmenterParser = null;
    private FigureParser figureParser = null;
    private TableParser tableParser = null;

    public AffiliationAddressParser getAffiliationAddressParser() {
        if (affiliationAddressParser == null) {
            synchronized (this) {
                if (affiliationAddressParser == null) {
                    affiliationAddressParser = new AffiliationAddressParser();
                }
            }
        }
        return affiliationAddressParser;
    }

    public AuthorParser getAuthorParser() {
        if (authorParser == null) {
            synchronized (this) {
                if (authorParser == null) {
                    authorParser = new AuthorParser();
                }
            }
        }
        return authorParser;
    }

    public HeaderParser getHeaderParser() {
        if (headerParser == null) {
            synchronized (this) {
                if (headerParser == null) {
                    headerParser = new HeaderParser(this);
                }
            }
        }
        return headerParser;
    }

    public DateParser getDateParser() {
        if (dateParser == null) {
            synchronized (this) {
                if (dateParser == null) {
                    dateParser = new DateParser();
                }
            }
        }
        return dateParser;
    }

    public CitationParser getCitationParser() {
        if (citationParser == null) {
            synchronized (this) {
                if (citationParser == null) {
                    citationParser = new CitationParser(this);
                }
            }
        }

        return citationParser;
    }

    public FullTextParser getFullTextParser() {
        if (fullTextParser == null) {
            synchronized (this) {
                if (fullTextParser == null) {
                    fullTextParser = new FullTextParser(this);
                }
            }
        }

        return fullTextParser;
    }

    public Segmentation getSegmentationParser() {
        if (segmentationParser == null) {
            synchronized (this) {
                if (segmentationParser == null) {
                    segmentationParser = new Segmentation();
                }
            }
        }
        return segmentationParser;
    }

    public ReferenceExtractor getReferenceExtractor() {
        if (referenceExtractor == null) {
            synchronized (this) {
                if (referenceExtractor == null) {
                    referenceExtractor = new ReferenceExtractor(this);
                }
            }
        }
        return referenceExtractor;
    }

    public ReferenceSegmenterParser getReferenceSegmenterParser() {
        if (referenceSegmenterParser == null) {
            synchronized (this) {
                if (referenceSegmenterParser == null) {
                    referenceSegmenterParser = new ReferenceSegmenterParser();
                }
            }
        }
        return referenceSegmenterParser;
    }

    public ChemicalParser getChemicalParser() {
        if (chemicalParser == null) {
            synchronized (this) {
                if (chemicalParser == null) {
                    chemicalParser = new ChemicalParser();
                }
            }
        }
        return chemicalParser;
    }

    public FigureParser getFigureParser() {
        if (figureParser == null) {
            synchronized (this) {
                if (figureParser == null) {
                    figureParser = new FigureParser();
                }
            }
        }
        return figureParser;
    }

    public TableParser getTableParser() {
        if (tableParser == null) {
            synchronized (this) {
                if (tableParser == null) {
                    tableParser = new TableParser();
                }
            }
        }
        return tableParser;
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("==> Closing all resources...");
        if (authorParser != null) {
            authorParser.close();
            authorParser = null;
            LOGGER.debug("CLOSING authorParser");
        }
        if (affiliationAddressParser != null) {
            affiliationAddressParser.close();
            affiliationAddressParser = null;
            LOGGER.debug("CLOSING affiliationAddressParser");
        }

        if (headerParser != null) {
            headerParser.close();
            headerParser = null;
            LOGGER.debug("CLOSING headerParser");
        }

        if (dateParser != null) {
            dateParser.close();
            dateParser = null;
            LOGGER.debug("CLOSING dateParser");
        }

        if (citationParser != null) {
            citationParser.close();
            citationParser = null;
            LOGGER.debug("CLOSING citationParser");
        }

        if (fullTextParser != null) {
            fullTextParser.close();
            fullTextParser = null;
            LOGGER.debug("CLOSING fullTextParser");
        }

        if (referenceExtractor != null) {
            referenceExtractor.close();
            referenceExtractor = null;
            LOGGER.debug("CLOSING referenceExtractor");
        }

        if (chemicalParser != null) {
            chemicalParser.close();
            chemicalParser = null;
            LOGGER.debug("CLOSING chemicalParser");
        }

        if (figureParser != null) {
            figureParser.close();
            figureParser = null;
            LOGGER.debug("CLOSING figureParser");
        }

        if (tableParser != null) {
            tableParser.close();
            tableParser = null;
            LOGGER.debug("CLOSING tableParser");
        }

        LOGGER.debug("==>All resources closed");

    }
}
