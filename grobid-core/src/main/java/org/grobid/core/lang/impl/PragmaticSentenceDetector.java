package org.grobid.core.lang.impl;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.LocalContextScope;

import org.grobid.core.lang.SentenceDetector;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.GrobidProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * Implementation of sentence segmentation via the Pragmatic Segmenter
 * 
 */
public class PragmaticSentenceDetector implements SentenceDetector {
    private static final Logger LOGGER  = LoggerFactory.getLogger(PragmaticSentenceDetector.class);

    private ScriptingContainer instance = null;

    public PragmaticSentenceDetector() {
        String segmenterRbFile = GrobidProperties.getGrobidHomePath() + 
            File.separator + "lexicon" + File.separator + "pragmatic_segmenter"+ File.separator + "segmenter.rb";
        String segmenterLoadPath = GrobidProperties.getGrobidHomePath() + File.separator + "lexicon";  
        String unicodeLoadPath = GrobidProperties.getGrobidHomePath() + File.separator + "lexicon" + 
            File.separator + "pragmatic_segmenter" + File.separator + "gem" + File.separator + "gems" +
            File.separator + "unicode-0.4.4.4-java" + File.separator + "lib";    
//System.out.println(vendorLoadPath);

        List<String> loadPaths = new ArrayList();
        loadPaths.add(segmenterLoadPath);
        loadPaths.add(unicodeLoadPath);

        instance = new ScriptingContainer(LocalContextScope.THREADSAFE);
        instance.setClassLoader(instance.getClass().getClassLoader());
        instance.setLoadPaths(loadPaths);
        instance.runScriptlet(PathType.ABSOLUTE, segmenterRbFile);
    }

    @Override
    public List<OffsetPosition> detect(String text) {
        instance.put("text", text);
        String script = "ps = PragmaticSegmenter::Segmenter.new(text: text)\nps.segment";
        Object ret = instance.runScriptlet(script);
        //System.out.println(ret.toString());

        // build offset positions from the string chunks
        List<OffsetPosition> result = new ArrayList<>();
        int pos = 0;
        for(String chunk : (List<String>) ret) {
            int start = text.indexOf(chunk, pos);
            result.add(new OffsetPosition(start, start+chunk.length()));
            pos = start+chunk.length();
        }

        return result;
    }
}
