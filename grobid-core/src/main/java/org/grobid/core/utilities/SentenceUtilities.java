package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.SentenceDetectorFactory;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for using sentence segmentation (singleton). The actual sentence segmentation implementation
 * is specified in the Grobid configuration. See org.grobid.core.lang.impl.* for the available 
 * implementations.
 *
 */
public class SentenceUtilities {
    public static final Logger LOGGER = LoggerFactory.getLogger(SentenceUtilities.class);

    private static volatile SentenceUtilities instance = null;

    private SentenceDetectorFactory sdf = null;

    public static SentenceUtilities getInstance() {
        if (instance == null) {
            synchronized (SentenceUtilities.class) {
                if (instance == null) {
                    LOGGER.debug("synchronized getNewInstance");
                    instance = new SentenceUtilities();
                }
            }
        }
        return instance;
    }

    private SentenceUtilities() {
        String className = GrobidProperties.getSentenceDetectorFactory();
        try {
            sdf = (SentenceDetectorFactory) Class.forName(className).newInstance();
        } catch (ClassCastException e) {
            throw new GrobidException("Class " + className
                    + " must implement "
                    + SentenceDetectorFactory.class.getName(), e);
        } catch (ClassNotFoundException e) {
            throw new GrobidException(
                    "Class "
                            + className
                            + " were not found in the classpath. "
                            + "Make sure that it is provided correctly is in the classpath", e);
        } catch (InstantiationException e) {
            throw new GrobidException("Class " + className
                    + " should have a default constructor", e);
        } catch (IllegalAccessException e) {
            throw new GrobidException(e);
        }
    }

    /**
     * Basic run for sentence identification, return the offset positions of the 
     * identified sentences
     *
     * @param text
     *            text to segment into sentences
     * @return list of offset positions for the identified sentence, relative to the input text
     */
    public List<OffsetPosition> runSentenceDetection(String text) {
        if (text == null)
            return null;
        try {
            return sdf.getInstance().detect(text);
        } catch (Exception e) {
            LOGGER.warn("Cannot detect sentences. ", e);
            return null;
        }
    }

    /**
     * Basic run for sentence identification with a specified language to be considered when segmenting, 
     * return the offset positions of the identified sentences
     *
     * @param text
     *            text to segment into sentences
     * @param lang 
     *            specified language to be used when segmenting text  
     * @return list of offset positions for the identified sentence, relative to the input text
     */
    public List<OffsetPosition> runSentenceDetection(String text, Language lang) {
        if (text == null)
            return null;
        try {
            return sdf.getInstance().detect(text, lang);
        } catch (Exception e) {
            LOGGER.warn("Cannot detect sentences. ", e);
            return null;
        }
    }

    /**
     * Run for sentence identification with some forbidden span constraints, return the offset positions of the 
     * identified sentences without sentence boundaries within a forbidden span (typically a reference marker
     * and we don't want a sentence end/start in the middle of that).
     *
     * @param text
     *            text to segment into sentences
     * @param forbidden
     *            list of offset positions where sentence boundaries are forbidden
     * @return list of offset positions for the identified sentence, relative to the input text
     */
    public List<OffsetPosition> runSentenceDetection(String text, List<OffsetPosition> forbidden) {
        return runSentenceDetection(text, forbidden, null, null);
    }

    /**
     * Run for sentence identification with some forbidden span constraints, return the offset positions of the 
     * identified sentences without sentence boundaries within a forbidden span (typically a reference marker
     * and we don't want a sentence end/start in the middle of that). The original LayoutToken objects are
     * provided, which allows to apply additional heuristics based on document layout and font features. 
     *
     * @param text
     *            text to segment into sentences
     * @param forbidden
     *            list of offset positions where sentence boundaries are forbidden
     * @param textLayoutTokens
     *            list of LayoutToken objects from which the text has been created, if this list is null
     *            we consider that we have a pure textual input (e.g. text is not from a PDF)
     * @param lang 
     *            specified language to be used when segmenting text  
     * @return list of offset positions for the identified sentence, relative to the input text
     */
    public List<OffsetPosition> runSentenceDetection(String text, List<OffsetPosition> forbidden, List<LayoutToken> textLayoutTokens, Language lang) {
        
        //String text2 = LayoutTokensUtil.toText(textLayoutTokens);
        
        if (text == null)
            return null;
        try {
            List<OffsetPosition> sentencePositions = sdf.getInstance().detect(text, lang);

            // to be sure, we sort the forbidden positions
            if (forbidden == null)
                return sentencePositions;
            Collections.sort(forbidden);

            // cancel sentence boundaries within the forbidden spans
            List<OffsetPosition> finalSentencePositions = correctSentencePositions(sentencePositions, forbidden);

            // as a heuristics for all implementations, because they clearly all fail for this case, we 
            // attached to the right sentence the numerical bibliographical references markers expressed 
            // in superscript just *after* the final sentence comma, e.g.
            // "Laboratory tests at the time of injury were not predictive of outcome. 32"
            // or
            // "CSF-1 has been linked to tumor growth and progression in breast cancer, 5,6 and has been 
            // shown to effectively reduce the number of tumor-associated macrophages in different tumor 
            // types. 4,5"
            // or 
            // "Even if the symmetry is s- like, it does not necessarily indicate that the
            // superconductivity is not exotic, because the s- like symmetry or the fully gapped state
            // may be realized by the pairing mediated by the interband excitations of the electrons. 23) "

            if (finalSentencePositions.size() == 0) {
                // this should normally not happen, but it happens (depending on sentence splitter, usually the text 
                // is just a punctuation)
                // in this case we consider the current text as a unique sentence as fall back
                finalSentencePositions.add(new OffsetPosition(0, text.length()));
            }

            if (textLayoutTokens == null || textLayoutTokens.size() == 0)
                return finalSentencePositions;

            int pos = 0;

            // init sentence index
            int currentSentenceIndex = 0;
            String sentenceChunk = text.substring(finalSentencePositions.get(currentSentenceIndex).start, 
                finalSentencePositions.get(currentSentenceIndex).end);
            boolean moved = false;

            StringBuilder accumulator = new StringBuilder();

            // iterate on layout tokens in sync with sentences
            for(int i=0; i<textLayoutTokens.size(); i++) {
                LayoutToken token = textLayoutTokens.get(i);
                accumulator.append(token);
                if (token.getText() == null || token.getText().length() == 0) 
                    continue;

                if (toSkipToken(token.getText()))
                    continue;
                //Checking whether the text contains the entire first chunk/sentence
                int newPos = sentenceChunk.indexOf(token.getText(), pos);

                if (newPos != -1) {
                    pos = newPos;
                    moved = true;
                } else {
                    // before moving to the next sentence, we check if a ref marker in superscript just follow
                    int pushedEnd = 0;
                    int buffer = 0;
                    int j = i;
                    for(; j<textLayoutTokens.size(); j++) {
                        LayoutToken nextToken = textLayoutTokens.get(j);
                        if (nextToken.getText() == null || nextToken.getText().length() == 0) 
                            continue;

                        // we don't look beyond an end of line (to prevent from numbered list/notes) 
                        if (nextToken.getText().equals("\n"))
                            break;

                        // we don't look beyond the text length
                        if (finalSentencePositions.get(currentSentenceIndex).end + nextToken.getText().length() + buffer >= text.length())
                            break;

                        if (toSkipTokenNoHyphen(nextToken.getText())) {
                            buffer += nextToken.getText().length();
                            continue;
                        }

                        if (isValidSuperScriptNumericalReferenceMarker(nextToken)
                            && isNextTokenFallingIntoAForbiddenInterval(accumulator.length() + j, forbidden)) {
                            pushedEnd += buffer + nextToken.getText().length();
                            buffer = 0;
                        } else 
                            break;
                    }

                    if (pushedEnd > 0) {

                        OffsetPosition newPosition = finalSentencePositions.get(currentSentenceIndex);
                        newPosition.end += pushedEnd+1;
                        finalSentencePositions.set(currentSentenceIndex, newPosition);
                        // push also the beginning of the next sentence
                        if (currentSentenceIndex+1 < finalSentencePositions.size()) {
                            OffsetPosition newNextPosition = finalSentencePositions.get(currentSentenceIndex+1);

                            // it could  be that the extra added ref marker was entirely the next sentence, which should be then removed
                            if (newNextPosition.start + pushedEnd + buffer >= newNextPosition.end) {
                                finalSentencePositions.remove(currentSentenceIndex+1);
                            } else {
                                newNextPosition.start += pushedEnd + buffer;
                                finalSentencePositions.set(currentSentenceIndex+1, newNextPosition);
                            }
                        }
                        pushedEnd = 0;
                        buffer = 0;
                        i = j-1;
                    }

                    if (moved) {
                        currentSentenceIndex++;
                        if (currentSentenceIndex >= finalSentencePositions.size())
                            break;
                        sentenceChunk = text.substring(finalSentencePositions.get(currentSentenceIndex).start, 
                            finalSentencePositions.get(currentSentenceIndex).end);
                        moved = false;
                    }
                    pos = 0;
                }
                
                if (currentSentenceIndex >= finalSentencePositions.size())
                    break;
            }

            // other heuristics/post-corrections based on layout/style features of the tokens could be added
            // here, for instance non-breakable italic or bold chunks, or adding sentence split based on 
            // spacing/indent

            return finalSentencePositions;
        } catch (Exception e) {
            LOGGER.warn("Cannot detect sentences. ", e);
            return null;
        }
    }

  
    private static boolean isNextTokenFallingIntoAForbiddenInterval(int currentOffset, List<OffsetPosition> forbidden) {
        return forbidden
            .stream().anyMatch(o -> currentOffset >= o.start && currentOffset < o.end);
    }
  
  
    public static List<OffsetPosition> correctSentencePositions(List<OffsetPosition> sentencePositions, List<OffsetPosition> forbiddenPositions) {
        List<OffsetPosition> finalSentencePositions = new ArrayList<>();
        int forbiddenIndex = 0;
        for(int j = 0; j < sentencePositions.size(); j++) {
            OffsetPosition position = new OffsetPosition(sentencePositions.get(j).start, sentencePositions.get(j).end);
            for(int i = forbiddenIndex; i < forbiddenPositions.size(); i++) {
                OffsetPosition forbiddenPos = forbiddenPositions.get(i);
                if (forbiddenPos.end < position.end)
                    continue;
                if (forbiddenPos.start > position.end)
                    break;
                while ( (forbiddenPos.start < position.end && position.end < forbiddenPos.end) ) {
                    if (j+1 < sentencePositions.size()) {
                        position.end = sentencePositions.get(j+1).end;
                        j++;
                        forbiddenIndex = i;
                    } else
                        break;
                }
            }
            finalSentencePositions.add(position);
        }
        return finalSentencePositions;
    }

    /**
     * Return true if the token should be skipped when considering sentence content. 
     */
    public static boolean toSkipToken(String tok) {
        // the hyphen is considered to be skipped to cover the case of word hyphenation
        if (tok.equals("-") || tok.equals(" ") || tok.equals("\n") || tok.equals("\t"))
            return true;
        else
            return false;
    }

    static boolean toSkipTokenNoHyphen(String tok) {
        if (tok.equals(" ") || tok.equals("\n") || tok.equals("\t"))
            return true;
        else
            return false;
    }


    /**
     * Return true if the token is a valid numerical reference markers ([0-9,())\-\]\[) in superscript.
     */
    private static boolean isValidSuperScriptNumericalReferenceMarker(LayoutToken token) {

        String tok = token.getText();
        if (tok == null) {
            // should never be the case, but we can just skip the token
            return true;
        }
        if (token.isSuperscript() && token.getText().matches("[0-9,\\-\\(\\)\\[\\]]+")) {
//System.out.println("isValidSuperScriptNumericalReferenceMarker: " + token.getText() + " -> true");            
            return true;
        } else {
//System.out.println("isValidSuperScriptNumericalReferenceMarker: " + token.getText() + " -> false");                        
            return false;
        }
    }
}