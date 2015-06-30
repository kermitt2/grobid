package org.grobid.trainer;

import com.google.common.base.Joiner;
import org.allenai.ml.sequences.crf.conll.ConllFormat;
import org.allenai.ml.sequences.crf.conll.Trainer;
import org.grobid.core.GrobidModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static org.allenai.ml.util.IOUtils.linesFromPath;

public class AI2CRFGenericTrainer implements GenericTrainer {
    public static final Logger LOGGER = LoggerFactory.getLogger(AI2CRFGenericTrainer.class);
    public static final String CRF = "ai2-ml";
    private final org.allenai.ml.sequences.crf.conll.Trainer trainer;

    // default training parameters (not exploited by CRFPP so far, it requires to extend the JNI)
    private double epsilon = 0.00001; // default size of the interval for stopping criterion
    private int window = 20; // default similar to CRF++

    public AI2CRFGenericTrainer() {
        trainer = new Trainer();
    }

    private boolean isCorrectStateTransitions(List<ConllFormat.Row> rows) {
        if (!rows.get(0).getLabel().get().equals("<s>")) return false;
        if (!rows.get(rows.size()-1).getLabel().get().equals("</s>")) return false;
        String currentLabel = null;
        for (int idx = 1; idx + 1 < rows.size(); idx++) {
            String label = rows.get(idx).getLabel().get();
            if (label.startsWith("I-")) {
                currentLabel = label.substring(2);
            } else if (!label.equals(currentLabel)) {
                return false;
            }
        }
        return true;
    }

    private List<ConllFormat.Row> pruneLongRuns(List<ConllFormat.Row> rows, int maxLength) {
        List<Integer> fieldStarts = new ArrayList<Integer>();
        for (int idx = 0; idx < rows.size(); idx++) {
            String label = rows.get(idx).getLabel().get();
            if (label.startsWith("I-")) {
                fieldStarts.add(idx);
            }
        }
        fieldStarts.add(rows.size()-1);
        List<ConllFormat.Row> prunedRows = new ArrayList<ConllFormat.Row>();
        prunedRows.add(rows.get(0));
        List<String> prunedLabels = new ArrayList<String>();
        for (int i = 0; i + 1 < fieldStarts.size(); i++) {
            int start = fieldStarts.get(i);
            int stop = fieldStarts.get(i + 1);
            if (stop - start > maxLength) {
                prunedRows.addAll(rows.subList(start, start + maxLength/2));
                prunedRows.addAll(rows.subList(stop - maxLength/2, stop));
            } else {
                prunedRows.addAll(rows.subList(start, stop));
            }
        }
        prunedRows.add(rows.get(rows.size()-1));
        for (ConllFormat.Row prunedRow : prunedRows) {
            prunedLabels.add(prunedRow.getLabel().get());
        }
        return prunedRows;
    }

    @Override
    public void train(File template, File trainingData, File outputModel, int numThreads, GrobidModels model) {

        List<List<ConllFormat.Row>> labeledData =
            ConllFormat.readData(linesFromPath(trainingData.getAbsolutePath()), true);

        String trainDataPath;
        // NOTE(aria42): There were some alignment issues with the header training data and some long fields
        // which worsened learning of boundary detection. This is probably helpful for
        // all models but only verified for the header model.
        if (model.getModelName().equalsIgnoreCase("header")) {
            List<List<ConllFormat.Row>> nonBrokenLabeledData = new ArrayList<List<ConllFormat.Row>>();
            int numDroppedTokens = 0;
            for (List<ConllFormat.Row> rows : labeledData) {
                if (isCorrectStateTransitions(rows)) {
                    List<ConllFormat.Row> fixedRows = pruneLongRuns(rows, 10);
                    numDroppedTokens += rows.size() - fixedRows.size();
                    nonBrokenLabeledData.add(fixedRows);
                }
            }
            try {
                File fixedTrainData = File.createTempFile("fixed-train", "train");
                BufferedWriter writer = new BufferedWriter(new FileWriter(fixedTrainData.getAbsoluteFile()));
                for (List<ConllFormat.Row> rows : nonBrokenLabeledData) {
                    for (ConllFormat.Row row : rows) {
                        String feats = Joiner.on("\t").join(row.features);
                        writer.write(feats);
                        writer.write("\t" + row.getLabel().get());
                        writer.write("\n");
                    }
                    writer.write("\n");
                }
                writer.close();
                fixedTrainData.deleteOnExit();
                trainDataPath = fixedTrainData.getAbsolutePath();
                System.out.printf("Original data size: %d, fixed data size: %d\n",
                    labeledData.size(), nonBrokenLabeledData.size());
                System.out.printf("Num dropped tokens: %d\n", numDroppedTokens);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            trainDataPath = trainingData.getAbsolutePath();
        }
        Trainer.Opts opts = new Trainer.Opts();
        opts.templateFile = template.getAbsolutePath();
        opts.trainPath = trainDataPath;
        opts.modelPath = outputModel.getAbsolutePath();
        opts.numThreads = numThreads;
        opts.featureKeepProb = 0.1;
        opts.sigmaSquared = 1.0;
        opts.maxIterations = 300;

        trainer.trainAndSaveModel(opts);
    }

    @Override
    public String getName() {
        return CRF;
    }

    @Override
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public void setWindow(int window) {
        this.window = window;
    }

    @Override
    public double getEpsilon() {
        return epsilon;
    }

    @Override
    public int getWindow() {
        return window;
    }
}
