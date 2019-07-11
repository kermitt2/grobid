package org.grobid.trainer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.grobid.core.GrobidModels;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;


/** This test is creating temp files - some cannot be removed **/
public class AbstractTrainerIntegrationTest {

    private AbstractTrainer target;

    @BeforeClass
    public static void beforeClass() throws Exception {
//        LibraryLoader.load();
    }

    @Before
    public void setUp() throws Exception {
        target = new AbstractTrainer(GrobidModels.DUMMY) {

            @Override
            public int createCRFPPData(File corpusPath, File outputTrainingFile, File outputEvalFile, double splitRatio) {
                // the file for writing the training data
//                if (outputTrainingFile != null) {
//                    try (OutputStream os = new FileOutputStream(outputTrainingFile)) {
//                        try (Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
//
//                            for (int i = 0; i < 100; i++) {
//                                double random = Math.random();
//                                writer.write("blablabla" + random);
//                                writer.write("\n");
//                                if (i % 10 == 0) {
//                                    writer.write("\n");
//                                }
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                return 100;
            }
        };
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testSplitNFold_n2_shouldWork() throws Exception {
        List<String> dummyTrainingData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double random = Math.random();
            dummyTrainingData.add("blablabla" + random);
        }

        List<ImmutablePair<String, String>> splitMapping = target.splitNFold(dummyTrainingData, 2);
        assertThat(splitMapping, hasSize(2));

        assertThat(splitMapping.get(0).getLeft(), endsWith("train"));
        assertThat(splitMapping.get(0).getRight(), endsWith("test"));

        // Cleanup
        splitMapping.stream().forEach(f -> {
            try {
                Files.delete(Paths.get(f.getRight()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        splitMapping.stream().forEach(f -> {
            try {
                Files.delete(Paths.get(f.getLeft()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testSplitNFold_n10_shouldWork() throws Exception {
        List<String> dummyTrainingData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double random = Math.random();
            dummyTrainingData.add("blablabla" + random);
        }

        List<ImmutablePair<String, String>> splitMapping = target.splitNFold(dummyTrainingData, 10);
        assertThat(splitMapping, hasSize(10));

        assertThat(splitMapping.get(0).getLeft(), endsWith("train"));
        assertThat(splitMapping.get(0).getRight(), endsWith("test"));

        // Cleanup
        splitMapping.stream().forEach(f -> {
            try {
                Files.delete(Paths.get(f.getRight()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        splitMapping.stream().forEach(f -> {
            try {
                Files.delete(Paths.get(f.getLeft()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    @Test
    public void testLoadAndShuffle_shouldWork() throws Exception {
        Path path = Paths.get("src/test/resources/sample.wapiti.output.date.txt");
        List<String> orderedTrainingData = new ArrayList<>();
        try (Stream<String> stream = Files.lines(path)) {

            ListIterator<String> iterator = stream.collect(Collectors.toList()).listIterator();
            List<String> instance = new ArrayList<>();
            while (iterator.hasNext()) {
                String current = iterator.next();
                if (StringUtils.isBlank(current)) {
                    if (CollectionUtils.isNotEmpty(instance)) {
                        orderedTrainingData.add(String.join("\n", instance));
                    }
                    instance = new ArrayList<>();
                } else {
                    instance.add(current);
                }
            }
        }
        List<String> shuffledTrainingData = target.loadAndShuffle(path);

        assertThat(shuffledTrainingData, hasSize(orderedTrainingData.size()));

        assertThat(shuffledTrainingData.get(0), is(not(orderedTrainingData.get(0))));
        assertThat(shuffledTrainingData.get(0).split("\n").length, is(4));
        assertThat(shuffledTrainingData.get(1), is(not(orderedTrainingData.get(1))));
    }

}