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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer.Mode;
import org.apache.lucene.analysis.ja.dict.UserDictionary;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;
import org.wipo.analyzers.FilterDeleteSpaceBetweenDigits;
import org.wipo.analyzers.FilterDeleteSpaceBetweenSameAlphabet;
import org.wipo.analyzers.WipoFilterTwoBytesLatinChars;

/**
 * Analyzer for Japanese that uses morphological analysis.
 * @see JapaneseTokenizer
 *
 * @author Bruno Pouliquen
 *
 */

public class GrobidJapaneseAnalyzer extends StopwordAnalyzerBase {
  	private final Mode mode;

  	private UserDictionary userDict;
  
  	public GrobidJapaneseAnalyzer() { // Provide a default constructor
		this(Version.LUCENE_45,null, Mode.NORMAL);
  	}

	public GrobidJapaneseAnalyzer(Version matchVersion) {
    	this(matchVersion, null,  Mode.NORMAL);
  	}
  
  	public GrobidJapaneseAnalyzer(Version matchVersion, UserDictionary userDict, Mode mode) {
    	super(matchVersion);
    	this.userDict = userDict;
    	this.mode = mode;
  	}

  	@Override
  	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer tokenizer = new JapaneseTokenizer(reader, userDict, false, mode);
    	TokenStream stream = tokenizer;

 //   stream = new JapaneseBaseFormFilter(tokenizer);
 //   stream = new JapanesePartOfSpeechStopFilter(true, stream, stoptags);
  //  stream = new CJKWidthFilter(stream);
 //   stream = new StopFilter(matchVersion, stream, stopwords);
//    stream = new JapaneseKatakanaStemFilter(stream);

 //   stream = new LowerCaseFilter(matchVersion, stream);
    	stream = new WipoFilterTwoBytesLatinChars(stream);
 //   stream = new GrobidFilterDeleteSpaceBetweenSameAlphabet(stream); // replaces "r e c e p t o r" by "receptor"
    	stream = new FilterDeleteSpaceBetweenDigits(stream);
    
 //   stream = new FilterSeparateHyphen(stream); // replaces "alpha-receptor" by "alpha - receptor"
//    stream = new FilterSeparateHyphen(stream); // replaces "alpha-receptor" by "alpha - receptor"
    
  //  stream = new MyJapaneseFilterOrdinals(stream);
 //  stream = new MyJapaneseFilterReferencesToFigure(stream); // replaces " ( 32 , 33 ) " by "(32,33)"
    	return new TokenStreamComponents(tokenizer, stream);
  	}
  
}
