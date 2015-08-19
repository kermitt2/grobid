package org.grobid.core.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a tokenization of document section where tokens include layout attributes.
 * Once built, it is possible to iterate through the string tokens only ignoring the layout information or
 * through the layout token objects.
 *
 * @author Patrice Lopez
 */
public class LayoutTokenization {
	private List<LayoutToken> layoutTokenization = null;
	private List<String> tokenization = null; // this should ultimately be removed
	
	public LayoutTokenization(List<String> tokens, List<LayoutToken> layoutTokens) {
		layoutTokenization = layoutTokens;
		tokenization = tokens;
	}
	
	public List<LayoutToken> getLayoutTokens() {
		return layoutTokenization;
	}
	
	public List<String> getTokenization() {
		return tokenization;
	}
	
	public void addLayoutToken(LayoutToken token) {
		if (layoutTokenization == null) 
			layoutTokenization = new ArrayList<LayoutToken>();
		else 
			layoutTokenization.add(token);
	}
	
	public void setLayoutTokens(List<LayoutToken> layoutTokens) {
		this.layoutTokenization = layoutTokens;
	}
	
	public void addToken(String token) {
		if (tokenization == null) 
			tokenization = new ArrayList<String>();
		else 
			tokenization.add(token);
	}
	
	public void setTokenization(List<String> tokens) {
		tokenization = tokens;
	}
}