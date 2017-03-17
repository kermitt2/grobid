package org.grobid.core.tokenization;

import org.grobid.core.engines.label.TaggingLabels;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 17/03/17.
 */
public class TaggingTokenClusterorTest {
    @Test
    public void testExclusion_notPresent_shouldReturnTrue() throws Exception {
        final TaggingTokenClusteror.LabelTypeExcludePredicate labelTypeExcludePredicate =
                new TaggingTokenClusteror.LabelTypeExcludePredicate(TaggingLabels.EQUATION, TaggingLabels.HEADER_KEYWORD);

        assertThat(labelTypeExcludePredicate.apply(new TaggingTokenCluster(TaggingLabels.FIGURE)),
                is(true));
    }

    @Test
    public void testExclusion_shouldReturnFalse() throws Exception {
        final TaggingTokenClusteror.LabelTypeExcludePredicate labelTypeExcludePredicate =
                new TaggingTokenClusteror.LabelTypeExcludePredicate(TaggingLabels.EQUATION, TaggingLabels.FIGURE);

        assertThat(labelTypeExcludePredicate.apply(new TaggingTokenCluster(TaggingLabels.FIGURE)),
                is(false));
    }


    @Test
    public void testInclusion_notPresent_shouldReturnFalse() throws Exception {
        final TaggingTokenClusteror.LabelTypePredicate labelTypePredicate =
                new TaggingTokenClusteror.LabelTypePredicate(TaggingLabels.HEADER_KEYWORD);

        assertThat(labelTypePredicate.apply(new TaggingTokenCluster(TaggingLabels.FIGURE)),
                is(false));
    }

    @Test
    public void testInclusion_present_shouldReturnTrue() throws Exception {
        final TaggingTokenClusteror.LabelTypePredicate labelTypePredicate =
                new TaggingTokenClusteror.LabelTypePredicate(TaggingLabels.FIGURE);

        assertThat(labelTypePredicate.apply(new TaggingTokenCluster(TaggingLabels.FIGURE)),
                is(true));
    }

}