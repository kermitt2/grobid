package org.grobid.core;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.grobid.core.engines.EngineParsers.LOGGER;

/**
 * This enum class acts as a registry for all Grobid models.
 *
 * @author Patrice Lopez
 */
public enum GrobidModels implements GrobidModel {
    AFFIILIATON_ADDRESS("affiliation-address"),
    SEGMENTATION("segmentation"),
    CITATION("citation"),
    REFERENCE_SEGMENTER("reference-segmenter"),
    DATE("date"),
    DICTIONARIES_LEXICAL_ENTRIES("dictionaries-lexical-entries"),
    DICTIONARIES_SENSE("dictionaries-sense"),
    EBOOK("ebook"),
    ENTITIES_CHEMISTRY("entities/chemistry"),
    //	ENTITIES_CHEMISTRY("chemistry"),
    FULLTEXT("fulltext"),
    SHORTTEXT("shorttext"),
    FIGURE("figure"),
    TABLE("table"),
    HEADER("header"),
    NAMES_CITATION("name/citation"),
    NAMES_HEADER("name/header"),
    PATENT_PATENT("patent/patent"),
    PATENT_NPL("patent/npl"),
    PATENT_ALL("patent/all"),
    PATENT_STRUCTURE("patent/structure"),
    PATENT_EDIT("patent/edit"),
    ENTITIES_NER("ner"),
    ENTITIES_NERFR("nerfr"),
    ENTITIES_NERSense("nersense"),
    QUANTITIES("quantities"),
    UNITS("units"),
    VALUE("value"),
    //	ENTITIES_BIOTECH("entities/biotech"),
    ENTITIES_BIOTECH("bio"),
    ASTRO("astro"),
    LEXICAL_ENTRY("lexical-entry"),
    DICTIONARY_BODY_SEGMENTATION("dictionary-body-segmentation"),
    DICTIONARY_SEGMENTATION("dictionary-segmentation");

    /**
     * Absolute path to the model.
     */
    private String modelPath;

    private String folderName;

    private static final ConcurrentMap<String, GrobidModel> models = new ConcurrentHashMap<>();

    GrobidModels(String folderName) {
        this.folderName = folderName;
        File path = GrobidProperties.getModelPath(this);
        if (!path.exists()) {
            // to be reviewed
            /*System.err.println("Warning: The file path to the "
                    + this.name() + " CRF model is invalid: "
					+ path.getAbsolutePath());*/
        }
        modelPath = path.getAbsolutePath();
    }

    public String getFolderName() {
        return folderName;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getModelName() {
        return folderName.replaceAll("/", "-");
    }

    public String getTemplateName() {
        return StringUtils.substringBefore(folderName, "/") + ".template";
    }

    @Override
    public String toString() {
        return folderName;
    }

    public static GrobidModel modelFor(final String name) {
        if (models.isEmpty()) {
            for (GrobidModel model : values())
                models.putIfAbsent(model.getFolderName(), model);
        }

        models.putIfAbsent(name.toString(/* null-check */), new GrobidModel() {
            @Override
            public String getFolderName() {
                return name;
            }

            @Override
            public String getModelPath() {
                File path = GrobidProperties.getModelPath(this);
                if (!path.exists()) {
                    LOGGER.warn("Warning: The file path to the "
                            + name + " model is invalid: "
                            + path.getAbsolutePath());
                }
                return path.getAbsolutePath();
            }

            @Override
            public String getModelName() {
                return getFolderName().replaceAll("/", "-");
            }

            @Override
            public String getTemplateName() {
                return StringUtils.substringBefore(getFolderName(), "/") + ".template";
            }
        });
        return models.get(name);
    }

    public String getName() {
        return name();
    }
}
