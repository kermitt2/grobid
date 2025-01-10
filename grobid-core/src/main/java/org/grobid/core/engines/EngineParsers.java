package org.grobid.core.engines;

import org.grobid.core.engines.entities.ChemicalParser;
import org.grobid.core.engines.patent.ReferenceExtractor;
import org.grobid.core.GrobidModels.Flavor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class EngineParsers implements Closeable {
    public static final Logger LOGGER = LoggerFactory.getLogger(EngineParsers.class);

    private AuthorParser authorParser = null;
    private AffiliationAddressParser affiliationAddressParser = null;
    private HeaderParser headerParser = null;
    private Map<Flavor,HeaderParser> headerParsers = null;
    private DateParser dateParser = null;
    private CitationParser citationParser = null;
    private FullTextParser fullTextParser = null;
    private ReferenceExtractor referenceExtractor = null;
    private ChemicalParser chemicalParser = null;
    private Segmentation segmentationParser = null;
    private Map<Flavor,Segmentation> segmentationParsers = null;
    private ReferenceSegmenterParser referenceSegmenterParser = null;
    private FigureParser figureParser = null;
    private TableParser tableParser = null;
    private MonographParser monographParser = null;
    private FundingAcknowledgementParser fundingAcknowledgementParser = null;

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
        return getHeaderParser(null);
    }

    public HeaderParser getHeaderParser(Flavor flavor) {
        if (flavor == null) {
            if (headerParser == null) {
                synchronized (this) {
                    if (headerParser == null) {
                        headerParser = new HeaderParser(this);
                    }
                }
            }
            return headerParser;
        } else {
            synchronized (this) {
                if (headerParsers == null || headerParsers.get(flavor) == null) {
                    HeaderParser localHeaderParser = new HeaderParser(this, flavor);
                    if (headerParsers == null)
                        headerParsers = new EnumMap<>(Flavor.class);
                    headerParsers.put(flavor, localHeaderParser);
                }
            }
            return headerParsers.get(flavor);
        }
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
        return getSegmentationParser(null);
    }

    public Segmentation getSegmentationParser(Flavor flavor) {
        if (flavor == null) {
            if (segmentationParser == null) {
                synchronized (this) {
                    if (segmentationParser == null) {
                        segmentationParser = new Segmentation();
                    }
                }
            }
            return segmentationParser;
        } {
            synchronized (this) {
                if (segmentationParsers == null || segmentationParsers.get(flavor) == null) {
                    Segmentation localSegmentationParser = new Segmentation(flavor);
                    if (segmentationParsers == null)
                        segmentationParsers = new EnumMap<>(Flavor.class);
                    segmentationParsers.put(flavor, localSegmentationParser);
                }
            }
            return segmentationParsers.get(flavor);
        }
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

    public MonographParser getMonographParser() {
        if (monographParser == null) {
            synchronized (this) {
                if (monographParser == null) {
                    monographParser = new MonographParser();
                }
            }
        }
        return monographParser;
    }

    public FundingAcknowledgementParser getFundingAcknowledgementParser() {
        if (fundingAcknowledgementParser == null) {
            synchronized (this) {
                if (fundingAcknowledgementParser == null) {
                    fundingAcknowledgementParser = new FundingAcknowledgementParser();
                }
            }
        }
        return fundingAcknowledgementParser;
    } 

    /**
     * Init all model, this will also load the model into memory
     */
    public void initAll() {
        affiliationAddressParser = getAffiliationAddressParser();
        authorParser = getAuthorParser();
        headerParser = getHeaderParser();
        dateParser = getDateParser();
        citationParser = getCitationParser();
        fullTextParser = getFullTextParser();
        //referenceExtractor = getReferenceExtractor();
        segmentationParser = getSegmentationParser();
        referenceSegmenterParser = getReferenceSegmenterParser();
        figureParser = getFigureParser();
        tableParser = getTableParser();
        //MonographParser monographParser = getMonographParser();
        fundingAcknowledgementParser = getFundingAcknowledgementParser();
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

        if (segmentationParser != null) {
            segmentationParser.close();
            segmentationParser = null;
            LOGGER.debug("CLOSING segmentationParser");
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

        if (referenceSegmenterParser != null) {
            referenceSegmenterParser.close();
            referenceSegmenterParser = null;
            LOGGER.debug("CLOSING referenceSegmenterParser");
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

        if (monographParser != null) {
            monographParser.close();
            monographParser = null;
            LOGGER.debug("CLOSING monographParser");
        }

        if (fundingAcknowledgementParser != null) {
            fundingAcknowledgementParser.close();
            fundingAcknowledgementParser = null;
            LOGGER.debug("CLOSING fundingAcknowledgementParser");
        }

        LOGGER.debug("==> All resources closed");
    }
}
