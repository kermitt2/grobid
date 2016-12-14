package org.grobid.core.engines.label;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grobid.core.GrobidModel;

/**
 * Created by zholudev on 11/01/16.
 * Representing label that can be tagged
 */
public class TaggingLabelImpl implements TaggingLabel {
    //TODO: move them in the underlying modules

   /* // labels for quantities/measurements
    QUANTITY_VALUE_ATOMIC(GrobidModels.QUANTITIES, "<valueAtomic>"),
    QUANTITY_VALUE_LEAST(GrobidModels.QUANTITIES, "<valueLeast>"),
    QUANTITY_VALUE_MOST(GrobidModels.QUANTITIES, "<valueMost>"),
    QUANTITY_VALUE_LIST(GrobidModels.QUANTITIES, "<valueList>"),
    QUANTITY_UNIT_LEFT(GrobidModels.QUANTITIES, "<unitLeft>"),
    QUANTITY_UNIT_RIGHT(GrobidModels.QUANTITIES, "<unitRight>"),
    QUANTITY_VALUE_BASE(GrobidModels.QUANTITIES, "<valueBase>"),
    QUANTITY_VALUE_RANGE(GrobidModels.QUANTITIES, "<valueRange>"),
    QUANTITY_OTHER(GrobidModels.QUANTITIES, "<other>"),

    // unit of measurements
    UNIT_VALUE_BASE(GrobidModels.UNITS, "<base>"),
    UNIT_VALUE_POW(GrobidModels.UNITS, "<pow>"),
    UNIT_VALUE_PREFIX(GrobidModels.UNITS, "<prefix>"),
    UNIT_VALUE_OTHER(GrobidModels.UNITS, "<other>"),

    // labels for astronomical entity recognition
    ASTRO_OBJECT(GrobidModels.ASTRO, "<object>"),
    ASTRO_OTHER(GrobidModels.ASTRO, "<other>"),

    DICTIONARY_LEXICAL_ENTRIES_ENTRY(GrobidModels.LEXICAL_ENTRY, "<entry>"),
    DICTIONARY_LEXICAL_ENTRIES_ETYM(GrobidModels.LEXICAL_ENTRY, "<etym>"),
    DICTIONARY_LEXICAL_ENTRIES_METAMARK(GrobidModels.LEXICAL_ENTRY, "<metamark>"),
    DICTIONARY_LEXICAL_ENTRIES_FORM(GrobidModels.LEXICAL_ENTRY, "<form>"),
    DICTIONARY_LEXICAL_ENTRIES_RE(GrobidModels.LEXICAL_ENTRY, "<re>"),
    DICTIONARY_LEXICAL_ENTRIES_NOTE(GrobidModels.LEXICAL_ENTRY, "<note>"),
    DICTIONARY_LEXICAL_ENTRIES_SENSE(GrobidModels.LEXICAL_ENTRY, "<sense>"),

    DICTIONARY_SEGMENTATION_HEADNOTE(GrobidModels.DICTIONARY_SEGMENTATION, "<headnote>"),
    DICTIONARY_SEGMENTATION_BODY(GrobidModels.DICTIONARY_SEGMENTATION, "<body>"),
    DICTIONARY_SEGMENTATION_FOOTNOTE(GrobidModels.DICTIONARY_SEGMENTATION, "<footnote>");*/

    private final GrobidModel grobidModel;
    private final String label;

    TaggingLabelImpl(GrobidModel grobidModel, String label) {
        this.grobidModel = grobidModel;
        this.label = label;
    }

    public GrobidModel getGrobidModel() {
        return grobidModel;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TaggingLabelImpl)) return false;

        TaggingLabelImpl that = (TaggingLabelImpl) o;

        return new EqualsBuilder()
                .append(getGrobidModel(), that.getGrobidModel())
                .append(getLabel(), that.getLabel())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getGrobidModel())
                .append(getLabel())
                .toHashCode();
    }

    @Override
    public String getName() {
        final String tmp = getLabel().replaceAll("[<>]", "");
        return StringUtils.upperCase(getGrobidModel().getModelName() + "_" + tmp.replace("I-", ""));
    }
}
