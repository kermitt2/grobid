package org.grobid.core.jni;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;


public class DeLFTModelTest {
    @Before
    public void setUp() {
        GrobidProperties.getInstance();
        GrobidProperties.getProps().put(GrobidPropertyKeys.PROP_GROBID_DELFT_ELMO, "false");
        GrobidProperties.getProps().remove(GrobidPropertyKeys.PROP_GROBID_DELFT_TRAIN_MODULE);
        GrobidProperties.getProps().remove(GrobidPropertyKeys.PROP_GROBID_DELFT_TRAIN_ARGS);
    }

    @Test
    public void testShouldBuildTrainCommand() {
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

    @Test
    public void testShouldAddUseELMO() {
        GrobidProperties.getProps().put(GrobidPropertyKeys.PROP_GROBID_DELFT_ELMO, "true");
        File trainingData = new File("test/train.data");
        assertThat(
            DeLFTModel.getTrainCommand("model1", trainingData),
            contains(
                "python3", "grobidTagger.py", "model1", "train",
                "--input", trainingData.getAbsolutePath(),
                "--output", GrobidProperties.getModelPath().getAbsolutePath(),
                "--use-ELMo"
            )
        );
    }

    @Test
    public void testShouldUseCustomTrainModule() {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_DELFT_TRAIN_MODULE, "module1.py"
        );
        File trainingData = new File("test/train.data");
        assertThat(
            DeLFTModel.getTrainCommand("model1", trainingData),
            contains(
                "python3", "module1.py", "model1", "train",
                "--input", trainingData.getAbsolutePath(),
                "--output", GrobidProperties.getModelPath().getAbsolutePath()
            )
        );
    }

    @Test
    public void testShouldAddSingleCustomTrainArg() {
        GrobidProperties.getProps().put(GrobidPropertyKeys.PROP_GROBID_DELFT_TRAIN_ARGS, "arg1");
        File trainingData = new File("test/train.data");
        assertThat(
            DeLFTModel.getTrainCommand("model1", trainingData),
            contains(
                "python3", "grobidTagger.py", "model1", "train",
                "--input", trainingData.getAbsolutePath(),
                "--output", GrobidProperties.getModelPath().getAbsolutePath(),
                "arg1"
            )
        );
    }

    @Test
    public void testShouldAddMultipleCustomTrainArg() {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_DELFT_TRAIN_ARGS, "arg1 arg2"
        );
        File trainingData = new File("test/train.data");
        assertThat(
            DeLFTModel.getTrainCommand("model1", trainingData),
            contains(
                "python3", "grobidTagger.py", "model1", "train",
                "--input", trainingData.getAbsolutePath(),
                "--output", GrobidProperties.getModelPath().getAbsolutePath(),
                "arg1",
                "arg2"
            )
        );
    }
}
