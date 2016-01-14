package org.grobid.core.data;

import java.util.*;

/**
 * Structure for representing the different information for a citation and its different context of citation.
 *
 * @author Patrice Lopez
 */
public class BibDataSet {
    public enum Counters {
        CITATIONS_CNT,
        CITATIONS_WITH_CONTEXT_CNT,
        CITATIONS_WITHOUT_CONTEXT_CNT
    }

    private BiblioItem resBib = null; // identified parsed bibliographical item
    private List<String> sourceBib = null;
    // the context window (raw text) where the bibliographical item is cited
    private String refSymbol = null; // reference marker in the text body
    private String rawBib = null; // raw text of the bibliographical item
    private double confidence = 1.0; // confidence score of the extracted bibiliographical item
    private List<Integer> offsets = null; // list of offsets corresponding to the position of the reference

    //private List<grisp.nlp.Term> terms = null;
    // set of terms describing the reference obtained in the citation context

    public BibDataSet() {
    }

    public void setResBib(BiblioItem res) {
        resBib = res;
    }

    public void addSourceBib(String sentence) {
        if (sourceBib == null)
            sourceBib = new ArrayList<String>();
        //sourceBib.add(org.grobid.core.utilities.TextUtilities.HTMLEncode(sentence));
        sourceBib.add(sentence);
    }

    public void setRawBib(String s) {
        //rawBib = org.grobid.core.utilities.TextUtilities.HTMLEncode(s);
        rawBib = s;
    }

    public void setRefSymbol(String s) {
        refSymbol = s;
    }

    //public void setTerms(List<grisp.nlp.Term> a) { terms = a; }
    public void setConfidence(double c) {
        confidence = c;
    }

    public BiblioItem getResBib() {
        return resBib;
    }

    public String getRawBib() {
        return rawBib;
    }

    public String getRefSymbol() {
        return refSymbol;
    }

    public List<String> getSourceBib() {
        return sourceBib;
    }
    //public List<grisp.nlp.Term> getTerms() { return terms; }

    public double getConfidence() {
        return confidence;
    }

    public void addOffset(int begin) {
        if (offsets == null) {
            offsets = new ArrayList<Integer>();
        }
        offsets.add(begin);
    }

    public void addOffset(Integer begin) {
        if (offsets == null) {
            offsets = new ArrayList<Integer>();
        }
        offsets.add(begin);
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

	@Override
	public String toString() {
		return "BibDataSet [resBib=" + resBib.toString() + ", sourceBib=" + sourceBib
				+ ", refSymbol=" + refSymbol + ", rawBib=" + rawBib
				+ ", confidence=" + confidence + ", offsets=" + offsets + "]";
	}
    
	public String toTEI() {
		if (resBib != null) {
            return resBib.toTEI(-1);
        } else {
            return "";
        }
	}
    
	public String toTEI(int p) {
		if (resBib != null) {
            return resBib.toTEI(p);
        } else {
            return "";
        }
	}
}
