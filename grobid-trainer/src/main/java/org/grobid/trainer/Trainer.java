package org.grobid.trainer;

import org.grobid.core.GrobidModels;

import java.io.File;

/**
 * @author Patrice Lopez
 */
public interface Trainer {

    public int createCRFPPData(File corpusPath, File outputFile);
    public void train();

    /**
     *
     * @return a report
     */
    public String evaluate();

    public GrobidModels getModel();
}