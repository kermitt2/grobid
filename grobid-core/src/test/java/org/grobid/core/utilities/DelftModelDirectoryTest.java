/*
 * Copyright 2008-2026 GROBID contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grobid.core.utilities;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class DelftModelDirectoryTest {

    @BeforeClass
    public static void setInitialContext() throws Exception {
        GrobidProperties.getInstance();
    }

    private static GrobidConfig.ModelParameters delftModel(String name, String embeddingsName) {
        GrobidConfig.ModelParameters modelParameters = new GrobidConfig.ModelParameters();
        modelParameters.name = name;
        modelParameters.engine = "delft";
        modelParameters.delft = new GrobidConfig.DelftModelParameters();
        modelParameters.delft.architecture = "BidLSTM_CRF_FEATURES";
        modelParameters.delft.embeddings_name = embeddingsName;
        return modelParameters;
    }

    @Test
    public void testEmbeddingsSuffix_default_shouldBeGlove() {
        assertThat(GrobidProperties.getDelftEmbeddingsSuffix("header"), is("glove_840B"));
    }

    @Test
    public void testEmbeddingsSuffix_flavor_shouldInheritFromBase() {
        assertThat(GrobidProperties.getDelftEmbeddingsSuffix("header-article-light"), is("glove_840B"));
    }

    @Test
    public void testEmbeddingsSuffix_noEmbeddings() {
        GrobidProperties.addModel(delftModel("suffix-test-null", null));
        GrobidProperties.addModel(delftModel("suffix-test-empty", ""));

        assertThat(GrobidProperties.getDelftEmbeddingsSuffix("suffix-test-null"), is("no_embeddings"));
        assertThat(GrobidProperties.getDelftEmbeddingsSuffix("suffix-test-empty"), is("no_embeddings"));
    }

    @Test
    public void testModelDirectory_shippedModel_shouldUseSuffixedDirectory() {
        File modelDir = GrobidProperties.getDelftModelDirectory("header", "BidLSTM_CRF_FEATURES");

        assertThat(modelDir, is(notNullValue()));
        assertThat(modelDir.getName(), is("header-BidLSTM_CRF_FEATURES-glove_840B"));
    }

    @Test
    public void testModelDirectory_missing_shouldBeNull() {
        assertThat(GrobidProperties.getDelftModelDirectory("header", "BidLSTM_ChainCRF_FEATURES"), is(nullValue()));
        assertThat(GrobidProperties.getDelftModelDirectory("header-article-light", "BidLSTM_CRF_FEATURES"), is(nullValue()));
    }

    @Test
    public void testModelDirectory_legacyDirectory_shouldBeUsedWhenSuffixedIsMissing() throws Exception {
        GrobidProperties.addModel(delftModel("legacy-dir-test", "glove-840B"));
        File legacyDir = new File(GrobidProperties.getModelPath(), "legacy-dir-test-BidLSTM_CRF_FEATURES");
        File suffixedDir = new File(GrobidProperties.getModelPath(), "legacy-dir-test-BidLSTM_CRF_FEATURES-glove_840B");
        try {
            assertThat(legacyDir.mkdir(), is(true));
            assertThat(GrobidProperties.getDelftModelDirectory("legacy-dir-test", "BidLSTM_CRF_FEATURES"), is(legacyDir));

            assertThat(suffixedDir.mkdir(), is(true));
            assertThat(GrobidProperties.getDelftModelDirectory("legacy-dir-test", "BidLSTM_CRF_FEATURES"), is(suffixedDir));
        } finally {
            FileUtils.deleteQuietly(legacyDir);
            FileUtils.deleteQuietly(suffixedDir);
        }
    }
}
