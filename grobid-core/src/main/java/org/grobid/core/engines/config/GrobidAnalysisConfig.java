package org.grobid.core.engines.config;

import java.io.File;

/**
 * Created by zholudev on 25/08/15.
 * A class representing configuration values needed in the analysis chain
 * TODO: clean up the docs
 * consolidateHeader    - the consolidation option allows GROBID to exploit Crossref
 *                             web services for improving header information
 * consolidateCitations - the consolidation option allows GROBID to exploit Crossref
 *                             web services for improving citations information
 * assetPath if not null, the PDF assets (embedded images) will be extracted and
 * saved under the indicated repository path
 * startPage give the starting page to consider in case of segmentation of the
 * PDF, -1 for the first page (default)
 * endPage give the end page to consider in case of segmentation of the
 * PDF, -1 for the last page (default)
 * generateIDs if true, generate random attribute id on the textual elements of
 * the resulting TEI

 */
public class GrobidAnalysisConfig {
    private GrobidAnalysisConfig() {
    }

    // give the starting page to consider in case of segmentation of the
    // PDF, -1 for the first page (default)
    private int startPage = -1;

    // give the end page to consider in case of segmentation of the
    // PDF, -1 for the last page (default)
    private int endPage = -1;

    // if consolidate citations
    private boolean consolidateCitations = false;

    // if consolidate header
    private boolean consolidateHeader = false;


    /// === TEI-specific settings ==

    // if true, generate random attribute id on the textual elements of
    // the resulting TEI
    private boolean generateTeiIds = false;

    // if true, generates the coordinates in the PDF corresponding
    // to the TEI full text substructures (e.g. reference markers)
    private boolean generateTeiCoordinates = true;

    // if true, include image references into TEI
    private boolean generateImageReferences = false;

    private boolean withXslStylesheet = false;

    // if not null, the PDF assets (embedded images) will be extracted
    // and saved under the indicated repository path
    private File pdfAssetPath = null;

    // transform images to PNGs
    private boolean preprocessImages = true;

    private boolean processVectorGraphics = false;

    // BUILDER

    public static class GrobidAnalysisConfigBuilder {
        GrobidAnalysisConfig config = new GrobidAnalysisConfig();

        public GrobidAnalysisConfigBuilder consolidateHeader(boolean consolidate) {
            config.consolidateHeader = consolidate;
            return this;
        }

        public GrobidAnalysisConfigBuilder consolidateCitations(boolean consolidate) {
            config.consolidateCitations = consolidate;
            return this;
        }

        public GrobidAnalysisConfigBuilder startPage(int p) {
            config.startPage = p;
            return this;
        }

        public GrobidAnalysisConfigBuilder endPage(int p) {
            config.endPage = p;
            return this;
        }

        public GrobidAnalysisConfigBuilder generateTeiIds(boolean b) {
            config.generateTeiIds = b;
            return this;
        }

        public GrobidAnalysisConfigBuilder pdfAssetPath(File path) {
            config.pdfAssetPath = path;
            return this;
        }

        public GrobidAnalysisConfigBuilder generateTeiCoordinates(boolean b) {
            config.generateTeiCoordinates = b;
            return this;
        }

        public GrobidAnalysisConfigBuilder withXslStylesheet(boolean b) {
            config.withXslStylesheet = b;
            return this;
        }

        public GrobidAnalysisConfigBuilder withPreprocessImages(boolean b) {
            config.preprocessImages = b;
            return this;
        }

        public GrobidAnalysisConfigBuilder withProcessVectorGraphics(boolean b) {
            config.processVectorGraphics = b;
            return this;
        }

        public GrobidAnalysisConfig build() {
            postProcessAndValidate();
            return config;
        }

        private void postProcessAndValidate() {
            if (config.getPdfAssetPath() != null) {
                config.generateImageReferences = true;
            }

            if (config.generateImageReferences && config.getPdfAssetPath() == null) {
                throw new InvalidGrobidAnalysisConfig("Generating image references is switched on, but no pdf asset path is provided");
            }
        }

    }

    public static GrobidAnalysisConfigBuilder builder() {
        return new GrobidAnalysisConfigBuilder();
    }

    public static GrobidAnalysisConfig defaultInstance() {
        return new GrobidAnalysisConfig();
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public boolean isConsolidateCitations() {
        return consolidateCitations;
    }

    public boolean isConsolidateHeader() {
        return consolidateHeader;
    }

    public boolean isGenerateTeiIds() {
        return generateTeiIds;
    }

    public boolean isGenerateTeiCoordinates() {
        return generateTeiCoordinates;
    }

    public File getPdfAssetPath() {
        return pdfAssetPath;
    }

    public boolean isWithXslStylesheet() {
        return withXslStylesheet;
    }

    public boolean isGenerateImageReferences() {
        return generateImageReferences;
    }

    public boolean isPreprocessImages() {
        return preprocessImages;
    }

    public boolean isProcessVectorGraphics() {
        return processVectorGraphics;
    }
}
