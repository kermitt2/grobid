package org.grobid.core.analyzers;

import org.grobid.core.layout.LayoutToken;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
	
/**
 * Abstract analyzer for tokenizing/filtering text.
 *
 * @author Patrice Lopez
 */

public interface Analyzer {

	public List<String> tokenize(String text);

	public List<String> retokenize(List<String> chunks);

	public List<LayoutToken> tokenizeWithLayoutToken(String text);

	public String getName();
}