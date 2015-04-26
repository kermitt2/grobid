/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grobid.core.analyzers;

import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.cn.smart.SentenceTokenizer;
import org.apache.lucene.analysis.cn.smart.WordTokenFilter;
import org.apache.lucene.util.Version;
import org.wipo.analyzers.ChineseFilterAndOr;

/**
 * <p>
 * This Chinese analyzer is the Wipo modified version of Lucene's SmartChineseAnalyzer
 * The only difference is that we do not use exactly the same filters (added 
 * "ChineseFilterAndOr" and "FilterReferencesToFigure"
 * 
 * The rest of the documentation is the same as SmartChineseAnalyzer .
 * 
 * @author Bruno Pouliquen
 *     
 * SmartChineseAnalyzer is an analyzer for Chinese or mixed Chinese-English text.
 * The analyzer uses probabilistic knowledge to find the optimal word segmentation for Simplified Chinese text.
 * The text is first broken into sentences, then each sentence is segmented into words.
 * </p>
 * <p>
 * Segmentation is based upon the <a href="http://en.wikipedia.org/wiki/Hidden_Markov_Model">Hidden Markov Model</a>. 
 * A large training corpus was used to calculate Chinese word frequency probability.
 * </p>
 * <p>
 * This analyzer requires a dictionary to provide statistical data. 
 * SmartChineseAnalyzer has an included dictionary out-of-box.
 * </p>
 * <p>
 * The included dictionary data is from <a href="http://www.ictclas.org">ICTCLAS1.0</a>.
 * Thanks to ICTCLAS for their hard work, and for contributing the data under the Apache 2 License!
 * </p>
 * <p><font color="#FF0000">
 * WARNING: The status of the analyzers/smartcn <b>analysis.cn.smart</b> package is experimental. 
 * The APIs and file formats introduced here might change in the future and will not be 
 * supported anymore in such a case.</font>
 * </p>
 */
public class GrobidChineseAnalyzer extends Analyzer {

	//static Pattern pattern = Pattern.compile("([a-zA-Z] *\\/ *[0-9]+)");
	
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
	    Tokenizer tok = new SentenceTokenizer(reader);
	    TokenStream result = new WordTokenFilter(tok);
	    // result = new LowerCaseFilter(result);
	    // LowerCaseFilter is not needed, as SegTokenFilter lowercases Basic Latin text.
	    // The porter stemming is too strict, this is not a bug, this is a feature:)
	    //    result = new PorterStemFilter(result);

	    //if (stopWords != null) {
	     // result = new StopFilter(StopFilter.getEnablePositionIncrementsVersionDefault(matchVersion),
	      //                        result, stopWords, false);
	    //}
	//    result = new ChineseFilterAndOr(result);
	//    result = new FilterReferencesToFigure(result);
	    
	 //   result = new org.apache.lucene.analysis.pattern.PatternCaptureGroupTokenFilter(result, false, pattern);
	    //result, false, false);
	    
	    return new TokenStreamComponents(tok, result);
	  }
  
  
}
