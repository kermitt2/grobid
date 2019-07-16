package org.grobid.trainer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.grobid.trainer.evaluation.ModelStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Zholudev, Lopez
 */
public abstract class AbstractTrainer implements Trainer {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrainer.class);
    public static final String OLD_MODEL_EXT = ".old";
    public static final String NEW_MODEL_EXT = ".new";

    // default training parameters (only exploited by Wapiti)
    protected double epsilon = 0.0; // size of the interval for stopping criterion
    protected int window = 0; // similar to CRF++
    protected int nbMaxIterations = 0; // maximum number of iterations in training

    protected GrobidModel model;
    private File trainDataPath;
    private File evalDataPath;
    private GenericTagger tagger;

    public AbstractTrainer(final GrobidModel model) {
        GrobidFactory.getInstance().createEngine();
        this.model = model;
        if (model.equals(GrobidModels.DUMMY)) {
            // In case of dummy model we do not initialise (and create) temporary files
            return;
        }
        this.trainDataPath = getTempTrainingDataPath();
        this.evalDataPath = getTempEvaluationDataPath();
    }

    public void setParams(double epsilon, int window, int nbMaxIterations) {
        this.epsilon = epsilon;
        this.window = window;
        this.nbMaxIterations = nbMaxIterations;
    }

    @Override
    public int createCRFPPData(final File corpusDir, final File trainingOutputPath) {
        return createCRFPPData(corpusDir, trainingOutputPath, null, 1.0);
    }

    @Override
    public void train() {
        final File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath);
        GenericTrainer trainer = TrainerFactory.getTrainer();

        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        File dirModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath()).getParentFile();
        if (!dirModelPath.exists()) {
            LOGGER.warn("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.getModelName() + ". Creating it.");
            dirModelPath.mkdir();
            //throw new GrobidException("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.toString());
        }
        final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidProperties.getModelPath(model);
        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidProperties.getNBThreads(), model);
        // if we are here, that means that training succeeded
        // rename model for CRF sequence labellers (not with DeLFT deep learning models)
        if (GrobidProperties.getGrobidCRFEngine() != GrobidCRFEngine.DELFT)
            renameModels(oldModelPath, tempModelPath);
    }

    protected void renameModels(final File oldModelPath, final File tempModelPath) {
        if (oldModelPath.exists()) {
            if (!oldModelPath.renameTo(new File(oldModelPath.getAbsolutePath() + OLD_MODEL_EXT))) {
                LOGGER.warn("Unable to rename old model file: " + oldModelPath.getAbsolutePath());
                return;
            }
        }

        if (!tempModelPath.renameTo(oldModelPath)) {
            LOGGER.warn("Unable to rename new model file: " + tempModelPath);
        }
    }

    @Override
    public String evaluate() {
        createCRFPPData(getEvalCorpusPath(), evalDataPath);
        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger()).toString();
    }

    @Override
    public String splitTrainEvaluate(Double split) {
        final File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath, evalDataPath, split);
        GenericTrainer trainer = TrainerFactory.getTrainer();

        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        File dirModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath()).getParentFile();
        if (!dirModelPath.exists()) {
            LOGGER.warn("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.getModelName() + ". Creating it.");
            dirModelPath.mkdir();
            //throw new GrobidException("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.toString());
        }

        final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidProperties.getModelPath(model);

        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidProperties.getNBThreads(), model);

        // if we are here, that means that training succeeded
        renameModels(oldModelPath, tempModelPath);

        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger()).toString();
    }

    @Override
    public String nFoldEvaluate(int numFolds) {
        final File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath);
        GenericTrainer trainer = TrainerFactory.getTrainer();

        // Load in memory and Shuffle
        Path dataPath2 = Paths.get(dataPath.getAbsolutePath());
        List<String> trainingData = loadAndShuffle(dataPath2);

        // Split into folds
        List<ImmutablePair<String, String>> foldMap = splitNFold(trainingData, numFolds);

        // Train and evaluation
        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        //We dump the model in the tmp directory
        File tmpDirectory = new File(GrobidProperties.getTempPath().getAbsolutePath());
        if (!tmpDirectory.exists()) {
            LOGGER.warn("Cannot find the destination directory " + tmpDirectory);
        }

        // Output
        StringBuilder sb = new StringBuilder();
        sb.append("Recap results for each fold:").append("\n\n");


        AtomicInteger counter = new AtomicInteger(0);
        List<ModelStats> evaluationResults = foldMap.stream().map(fold -> {
            final File tempModelPath = new File(tmpDirectory + File.separator + getModel().getModelName()
                + "_nfold_" + counter.getAndIncrement() + ".wapiti");
            System.out.println("Saving model in " + tempModelPath);

            System.out.println("Training input data: " + fold.getLeft());
            trainer.train(getTemplatePath(), new File(fold.getLeft()), tempModelPath, GrobidProperties.getNBThreads(), model);
            System.out.println("Evaluation input data: " + fold.getRight());
            ModelStats modelStats = EvaluationUtilities.evaluateStandard(fold.getRight(), getTagger());
            System.out.println(modelStats.toString());

            sb.append(" ====================== Fold " + counter.get() + " ====================== ").append("\n");
            sb.append(modelStats.toString()).append("\n");

            return modelStats;
        }).collect(Collectors.toList());

        sb.append("\n").append("Summary results: ").append("\n");

        Comparator<ModelStats> f1ScoreComparator = (o1, o2) -> {
            if (o1.getFieldStats().getMacroAverageF1() > o2.getFieldStats().getMacroAverageF1()) {
                return 1;
            } else if (o1.getFieldStats().getMacroAverageF1() < o2.getFieldStats().getMacroAverageF1()) {
                return -1;
            } else {
                return 0;
            }
        };

        Optional<ModelStats> worstModel = evaluationResults.stream().min(f1ScoreComparator);
        sb.append("Worst Model").append("\n");
        ModelStats worstModelStats = worstModel.orElseGet(() -> {
            throw new GrobidException("Something wrong when computing evaluations " +
                "- worst model metrics not found. ");
        });
        sb.append(worstModelStats.toString()).append("\n");

        sb.append("Best model:").append("\n");
        Optional<ModelStats> bestModel = evaluationResults.stream().max(f1ScoreComparator);
        ModelStats bestModelStats = bestModel.orElseGet(() -> {
            throw new GrobidException("Something wrong when computing evaluations " +
                "- best model metrics not found. ");
        });
        sb.append(bestModelStats.toString()).append("\n");

        // Averages
        OptionalDouble averageF1 = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAverageF1()).average();
        OptionalDouble averagePrecision = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAveragePrecision()).average();
        OptionalDouble averageRecall = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAverageRecall()).average();

        sb.append("average over " + numFolds + " folds: ").append("\n");

        double avgF1 = averageF1.orElseGet(() -> {
            throw new GrobidException("Missing average F1. Something went wrong. Please check. ");
        });
        sb.append("\tmacro f1 = " + TextUtilities.formatTwoDecimals(avgF1 * 100)).append("\n");

        double avgPrecision = averagePrecision.orElseGet(() -> {
            throw new GrobidException("Missing average precision. Something went wrong. Please check. ");
        });
        sb.append("\tmacro precision = " + TextUtilities.formatTwoDecimals(avgPrecision * 100)).append("\n");

        double avgRecall = averageRecall.orElseGet(() -> {
            throw new GrobidException("Missing average recall. Something went wrong. Please check. ");
        });
        sb.append("\tmacro recall = " + TextUtilities.formatTwoDecimals(avgRecall * 100)).append("\n");


        return sb.toString();
    }

    /**
     * Partition the corpus in n folds, dump them in n files and return the pairs of (trainingPath, evaluationPath)
     */
    protected List<ImmutablePair<String, String>> splitNFold(List<String> trainingData, int numberFolds) {
        int trainingSize = CollectionUtils.size(trainingData);
        int foldSize = Math.floorDiv(trainingSize, numberFolds);
        if (foldSize == 0) {
            throw new IllegalArgumentException("There aren't enough training data for n-fold evaluation with fold of size " + numberFolds);
        }

        return IntStream.range(0, numberFolds).mapToObj(foldIndex -> {
            int foldStart = foldSize * foldIndex;
            int foldEnd = foldStart + foldSize;

            if (foldIndex == numberFolds - 1) {
                foldEnd = trainingSize;
            }

            List<String> foldEvaluation = trainingData.subList(foldStart, foldEnd);
            List<String> foldTraining0 = trainingData.subList(0, foldStart);
            List<String> foldTraining1 = trainingData.subList(foldEnd, trainingSize);
            List<String> foldTraining = new ArrayList<>();
            foldTraining.addAll(foldTraining0);
            foldTraining.addAll(foldTraining1);

            //Dump Evaluation
            String tempEvaluationDataPath = getTempEvaluationDataPath().getAbsolutePath();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(tempEvaluationDataPath))) {
                writer.write(String.join("\n\n", foldEvaluation));
                writer.write("\n");
            } catch (IOException e) {
                throw new GrobidException("Error when dumping n-fold evaluation data into files. ", e);
            }

            //Dump Training
            String tempTrainingDataPath = getTempTrainingDataPath().getAbsolutePath();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(tempTrainingDataPath))) {
                writer.write(String.join("\n\n", foldTraining));
                writer.write("\n");
            } catch (IOException e) {
                throw new GrobidException("Error when dumping n-fold training data into files. ", e);
            }

            return new ImmutablePair<>(tempTrainingDataPath, tempEvaluationDataPath);
        }).collect(Collectors.toList());
    }

    /**
     * Load the dataset in memory and shuffle it.
     */
    protected List<String> loadAndShuffle(Path dataPath) {
        List<String> trainingData = load(dataPath);

        Collections.shuffle(trainingData, new Random(839374947498L));

        return trainingData;
    }

    /**
     * Read the Wapiti training files in list of String.
     * Assuming that each empty line is a delimiter between instances.
     * Each list element corresponds to one instance.
     * Empty line are filtered out from the output.
     */
    public List<String> load(Path dataPath) {
        List<String> trainingData = new ArrayList<>();
        try (Stream<String> stream = Files.lines(dataPath)) {
            List<String> instance = new ArrayList<>();
            ListIterator<String> iterator = stream.collect(Collectors.toList()).listIterator();
            while (iterator.hasNext()) {
                String current = iterator.next();

                if (StringUtils.isBlank(current)) {
                    if (CollectionUtils.isNotEmpty(instance)) {
                        trainingData.add(String.join("\n", instance));
                    }
                    instance = new ArrayList<>();
                } else {
                    instance.add(current);
                }
            }
            if (CollectionUtils.isNotEmpty(instance)) {
                trainingData.add(String.join("\n", instance));
            }

        } catch (IOException e) {
            throw new GrobidException("Error in n-fold, when loading training data. Failing. ", e);
        }

        return trainingData;
    }

    protected final File getTempTrainingDataPath() {
        try {
            return File.createTempFile(model.getModelName(), ".train", GrobidProperties.getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary training file for model: " + model);
        }
    }

    protected final File getTempEvaluationDataPath() {
        try {
            return File.createTempFile(model.getModelName(), ".test", GrobidProperties.getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary evaluation file for model: " + model);
        }
    }

    protected GenericTagger getTagger() {
        if (tagger == null) {
            tagger = TaggerFactory.getTagger(model);
        }

        return tagger;
    }

    protected static File getFilePath2Resources() {
        File theFile = new File(GrobidProperties.get_GROBID_HOME_PATH().getAbsoluteFile() + File.separator + ".." + File.separator
            + "grobid-trainer" + File.separator + "resources");
        if (!theFile.exists()) {
            theFile = new File("resources");
        }
        return theFile;
    }

    protected File getCorpusPath() {
        return GrobidProperties.getCorpusPath(getFilePath2Resources(), model);
    }

    protected File getTemplatePath() {
        return getTemplatePath(model);
    }

    protected File getTemplatePath(final GrobidModel model) {
        return GrobidProperties.getTemplatePath(getFilePath2Resources(), model);
    }

    protected File getEvalCorpusPath() {
        return GrobidProperties.getEvalCorpusPath(getFilePath2Resources(), model);
    }

    public static File getEvalCorpusBasePath() {
        final String path2Evelutation = getFilePath2Resources().getAbsolutePath() + File.separator + "dataset" + File.separator + "patent"
            + File.separator + "evaluation";
        return new File(path2Evelutation);
    }

    @Override
    public GrobidModel getModel() {
        return model;
    }

    public static void runTraining(final Trainer trainer) {
        long start = System.currentTimeMillis();
        trainer.train();
        long end = System.currentTimeMillis();

        System.out.println("Model for " + trainer.getModel() + " created in " + (end - start) + " ms");
    }

    public File getEvalDataPath() {
        return evalDataPath;
    }

    public static String runEvaluation(final Trainer trainer) {
        long start = System.currentTimeMillis();
        String report = "";
        try {
            report = trainer.evaluate();
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nEvaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";

        return report;
    }

    public static String runSplitTrainingEvaluation(final Trainer trainer, Double split) {
        long start = System.currentTimeMillis();
        String report = "";
        try {
            report = trainer.splitTrainEvaluate(split);

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nSplit, training and evaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";

        return report;
    }

    public static void runNFoldEvaluation(final Trainer trainer, int numFolds, Path outputFile) {

        String report = runNFoldEvaluation(trainer, numFolds);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write(report);
            writer.write("\n");
        } catch (IOException e) {
            throw new GrobidException("Error when dumping n-fold training data into files. ", e);
        }

    }

    public static String runNFoldEvaluation(final Trainer trainer, int numFolds) {
        long start = System.currentTimeMillis();
        String report = "";
        try {
            report = trainer.nFoldEvaluate(numFolds);

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nN-Fold evaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";

        return report;
    }

    /**
     * Dispatch the example to the training or test data, based on the split ration and the drawing of
     * a random number
     */
    public Writer dispatchExample(Writer writerTraining, Writer writerEvaluation, double splitRatio) {
        Writer writer = null;
        if ((writerTraining == null) && (writerEvaluation != null)) {
            writer = writerEvaluation;
        } else if ((writerTraining != null) && (writerEvaluation == null)) {
            writer = writerTraining;
        } else {
            if (Math.random() <= splitRatio)
                writer = writerTraining;
            else
                writer = writerEvaluation;
        }
        return writer;
    }


}
