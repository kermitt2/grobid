package org.grobid.core.jni;

import java.io.File;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.grobid.core.utilities.GrobidProperties;


public class DeLFTModelTest {
    @Test
    public void testShouldBuildTrainCommand() {
        GrobidProperties.getInstance();
        File trainingData = new File("test/train.data");
        assertThat(
            DeLFTModel.getTrainCommand("model1", trainingData),
            contains(
                "python3", "grobidTagger.py", "model1", "train",
                "--input", trainingData.getAbsolutePath(),
                "--output", GrobidProperties.getModelPath().getAbsolutePath()
            )
        );
    }
}
