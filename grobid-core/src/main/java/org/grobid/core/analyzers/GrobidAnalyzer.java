package org.grobid.core.analyzers;

import org.grobid.core.lang.Language;

import org.wipo.nlp.textboundaries.ReTokenizer;
import org.wipo.nlp.textboundaries.ReTokenizerFactory;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class dispatching text to be tokenized to the adequate analyzer given a specified language.
 *
 * The language might be preliminary set by the language recognizer or manually if it is already 
 * known by the context of usage of the text. 
 *
 * @author Patrice Lopez
 */
public class GrobidAnalyzer {
	private static final Logger LOGGER = LoggerFactory.getLogger(GrobidAnalyzer.class);
	
	private static volatile GrobidAnalyzer instance;
	
	private ReTokenizer jaAnalyzer = null;
	private ReTokenizer krAnalyzer = null;
	private ReTokenizer zhAnalyzer = null;
	
    public static GrobidAnalyzer getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
                if (instance == null)
					getNewInstance();
            // }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
	private static synchronized void getNewInstance() {
		LOGGER.debug("Get new instance of GrobidAnalyzer");
		instance = new GrobidAnalyzer();
	}

    /**
     * Hidden constructor
     */
    private GrobidAnalyzer() {
	}
	
	/**
	 * Tokenizer entry point
	 */
	public List<String> tokenize(String text) throws Exception {
		return tokenize(null, text);
	}
	
	public List<String> tokenize(Language lang, String text) throws Exception {
		List<String> result = null;
		if ( (text == null) || (text.length() == 0) ) {
			return new ArrayList<String>();
		}
		if ( (lang == null) || (lang.getLangId() == null) ) {
			// default Indo-European languages
			result = GrobidDefaultAnalyzer.tokenize(text);
		}
		else if (lang.getLangId().equals("ja")) {
			// Japanese analyser
			if (jaAnalyzer == null)
				jaAnalyzer = ReTokenizerFactory.create("ja_g");
			result = jaAnalyzer.tokensAsList(text);
		}
		else if (lang.getLangId().equals("zh") || lang.getLangId().equals("zh-cn")) {
			// Chinese analyser
			if (zhAnalyzer == null)
				zhAnalyzer = ReTokenizerFactory.create("zh_g");
			result = zhAnalyzer.tokensAsList(text);
		}
		else if (lang.getLangId().equals("kr")) {
			// Korean analyser
			if (krAnalyzer == null)
				krAnalyzer = ReTokenizerFactory.create("kr_g");
			result = krAnalyzer.tokensAsList(text);
		}
		else if (lang.getLangId().equals("ar")) {
			// Arabic analyser
			result = GrobidDefaultAnalyzer.tokenize(text);
			int p = 0;
			for(String token : result) {
				// string being immutable in Java, I think we can't do better that this:
				StringBuilder newToken = new StringBuilder();
				for(int i=0; i<token.length(); i++) {
					newToken.append(ArabicChars.arabicCharacters(token.charAt(i)));
				}
				result.set(p, newToken.toString());
				p++;
			}
		}
		else {
			// default Indo-European languages
			result = GrobidDefaultAnalyzer.tokenize(text);
		}
		return result;
	}
	
	/**
	 * Re-tokenizer entry point to be applied to text already tokenized in the PDF representation
	 */
	public List<String> retokenize(List<String> textTokenized) throws Exception {
		return retokenize(null, textTokenized);
	}
	
	public List<String> retokenize(Language lang, List<String> textTokenized) throws Exception {
		List<String> result = null;
		if ( (textTokenized == null) || (textTokenized.size() == 0) ) {
			return new ArrayList<String>();
		}
		if ( (lang == null) || (lang.getLangId() == null) ) {
			// default Indo-European languages
			result = GrobidDefaultAnalyzer.retokenize(textTokenized);
		}
		else if (lang.getLangId().equals("ja")) {
			// Japanese analyser
		}
		else if (lang.getLangId().equals("zh")) {
			// Chinese analyser
		}
		else if (lang.getLangId().equals("kr")) {
			// Korean analyser
		}
		else if (lang.getLangId().equals("ar")) {
			// Arabic analyser
		}
		else {
			// default Indo-European languages
			result = GrobidDefaultAnalyzer.retokenize(textTokenized);
		}
		return result;
	}
}