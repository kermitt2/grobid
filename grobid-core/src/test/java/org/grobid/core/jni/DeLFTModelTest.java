package org.grobid.core.jni;

import java.io.File;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.grobid.core.utilities.GrobidProperties;


public class DeLFTModelTest {
    @Test
    public void testShouldBuildTrainCommand() {
        File trainingData = new File("test/train.data");
        File outputModel = new File("test/output");
        assertThat(
            DeLFTModel.getTrainCommand("model1", trainingData, outputModel),
            contains(
                "python3", "grobidTagger.py", "model1", "train",
                "--input", trainingData.getAbsolutePath(),
                "--output", GrobidProperties.getModelPath().getAbsolutePath()
            )
        );
    }
}
