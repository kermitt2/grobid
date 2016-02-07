package org.grobid.core.lexicon;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;

import java.util.*;
import java.io.*;

/**
 * Class for fast matching of word sequences over text stream.
 *
 * @author Patrice Lopez
 */
public final class FastMatcher {
    private Map terms = null;

    public FastMatcher() {
      if (terms == null) {
            terms = new HashMap();
        }
    }

    public FastMatcher(File file) {
        if (!file.exists()) {
            throw new GrobidResourceException("Cannot add term to matcher, because file '" +
                    file.getAbsolutePath() + "' does not exist.");
        }
        if (!file.canRead()) {
            throw new GrobidResourceException("Cannot add terms to matcher, because cannot read file '" +
                    file.getAbsolutePath() + "'.");
        }
        try {
            loadTerms(file);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid FastMatcher.", e);
        }
    }

	/**
	 * Load a set of terms to the fast matcher from a file listing terms one per line
	 */
    public int loadTerms(File file) throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        FileInputStream fileIn = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(fileIn, "UTF-8");
        BufferedReader bufReader = new BufferedReader(reader);
        String line;
        if (terms == null) {
            terms = new HashMap();
        }
        //Map t = terms;
        int nbTerms = 0;
		    //String token = null;
        while ((line = bufReader.readLine()) != null) {
          if (line.length() == 0) continue;
			    line = line.toLowerCase();
			    nbTerms += loadTerm(line);
        }
        bufReader.close();
        reader.close();

        return nbTerms;
    }

  	/**
  	 * Load a term to the fast matcher
  	 */
  	public int loadTerm(String term) throws IOException, ClassNotFoundException,
              InstantiationException, IllegalAccessException {
  		int nbTerms = 0;
  		if ( (term == null) || (term.length() == 0) )
  			return 0;
  		String token = null;
  		Map t = terms;	
  		StringTokenizer st = new StringTokenizer(term, " \n\t" + TextUtilities.fullPunctuations, false);
      while (st.hasMoreTokens()) {
  			token = st.nextToken();
      	if (token.length() == 0) {
          	continue;
       	}
      	Map t2 = (Map) t.get(token);
       	if (t2 == null) {
         	t2 = new HashMap();
          	t.put(token, t2);
       	}
       	t = t2;
      }
      // end of the term
      if (t != terms) {
      	Map t2 = (Map) t.get("#");
       	if (t2 == null) {
          	t2 = new HashMap();
           	t.put("#", t2);
      	}
       	nbTerms++;
       	t = terms;
      }
  		return nbTerms;
  	}

    private static String delimiters = " \n\t" + TextUtilities.fullPunctuations;

    /**
     * Identify terms in a piece of text and gives corresponding token positions.
     * All the matches are returned.
     *
     * @param text: the text to be processed
     * @return the list of offset positions of the matches, an empty list if no match have been found
     */
    public List<OffsetPosition> matcher(String text) {
        List<OffsetPosition> results = new ArrayList<OffsetPosition>();
        List<Integer> startPos = new ArrayList<Integer>();
        List<Integer> lastNonSeparatorPos = new ArrayList<Integer>();
        List<Map> t = new ArrayList<Map>();
        int currentPos = 0;
        StringTokenizer st = new StringTokenizer(text, delimiters, true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(" ")) {
                continue;
            }
            if (delimiters.indexOf(token) != -1) {
                currentPos++;
                continue;
            }
            if ((token.charAt(0) == '<') && (token.charAt(token.length() - 1) == '>')) {
                currentPos++;
                continue;
            }
            token = token.toLowerCase();

            // we try to complete opened matching
            int i = 0;
            List<Map> new_t = new ArrayList<Map>();
            List<Integer> new_startPos = new ArrayList<Integer>();
            List<Integer> new_lastNonSeparatorPos = new ArrayList<Integer>();
            // continuation of current opened matching
            for (Map tt : t) {
                Map t2 = (Map) tt.get(token);
                if (t2 != null) {
                    new_t.add(t2);
                    new_startPos.add(startPos.get(i));
                    new_lastNonSeparatorPos.add(currentPos);
                }
                //else
                {
                    t2 = (Map) tt.get("#");
                    if (t2 != null) {
                        // end of the current term, matching sucesssful
                        OffsetPosition ofp = new OffsetPosition();
                        ofp.start = startPos.get(i).intValue();
                        ofp.end = lastNonSeparatorPos.get(i).intValue();
                        results.add(ofp);
                    }
                }
                i++;
            }

            // we start new matching starting at the current token
            Map t2 = (Map) terms.get(token);
            if (t2 != null) {
                new_t.add(t2);
                new_startPos.add(new Integer(currentPos));
                new_lastNonSeparatorPos.add(currentPos);
            }

            t = new_t;
            startPos = new_startPos;
            lastNonSeparatorPos = new_lastNonSeparatorPos;
            currentPos++;
        }

        // test if the end of the string correspond to the end of a term
        int i = 0;
        if (t != null) {
            for (Map tt : t) {
                Map t2 = (Map) tt.get("#");
                if (t2 != null) {
                    // end of the current term, matching sucesssful
                    OffsetPosition ofp = new OffsetPosition();
                    ofp.start = startPos.get(i).intValue();
                    ofp.end = lastNonSeparatorPos.get(i).intValue();
                    results.add(ofp);
                }
                i++;
            }
        }

        return results;
    }

    /**
     * Identify terms in a piece of text and gives corresponding token positions.
     * All the matches are returned. Here the input text is already tokenized.
     *
     * @param tokens: the text to be processed
     * @return the list of offset positions of the matches, an empty list if no match have been found
     */
    public List<OffsetPosition> matcher(List<String> tokens) {
        StringBuilder text = new StringBuilder();
        for (String token : tokens) {
            if (!token.trim().equals("@newline")) {
        				int ind = token.indexOf(" ");
        				if (ind == -1)
        			      ind = token.indexOf("\t");
        				if (ind == -1)
                    text.append(" ").append(token);
        				else
        					  text .append(" ").append(token.substring(0, ind));
            }
        }
        return matcher(text.toString());
    }


    /**
     * Identify terms in a piece of text and gives corresponding token positions.
     * All the matches are returned. This case correspond to text from a trainer,
     * where the text is already tokenized with some labeled that can be ignored.
     *
     * @param tokens: the text to be processed
     * @return the list of offset positions of the matches, an empty list if no match have been found
     */
    public List<OffsetPosition> matcherPairs(List<Pair<String,String>> tokens) {
        StringBuilder text = new StringBuilder();
        for (Pair<String,String> tokenP : tokens) {
            String token = tokenP.getA();
            if (!token.trim().equals("@newline")) {
                int ind = token.indexOf(" ");
                if (ind == -1)
                    ind = token.indexOf("\t");
                if (ind == -1)
                    text.append(" ").append(token);
                else
                    text .append(" ").append(token.substring(0, ind));
            }
        }
        return matcher(text.toString());
    }
}

