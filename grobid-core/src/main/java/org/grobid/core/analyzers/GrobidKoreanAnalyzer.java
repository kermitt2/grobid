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
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.wipo.analyzers.WipoFilterTwoBytesLatinChars;
import org.wipo.analyzers.wipokr.KoreanFilter;
import org.wipo.analyzers.wipokr.KoreanTokenizer;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}, using a list of English stop words.
 *
 * @version $Id: KoreanAnalyzer.java 53 2013-10-28 15:54:24Z uschindler $
 */
public class GrobidKoreanAnalyzer extends Analyzer {
	
	private boolean bigrammable = true;
	  
	private boolean hasOrigin = true;
	  
	public static final String DIC_ENCODING = "UTF-8";


  	/** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

  	@Override
  	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
    	Tokenizer tok = new KoreanTokenizer( reader );
    	TokenStream result = new KoreanFilter(tok, bigrammable, hasOrigin);
 //   result = new LowerCaseFilter(Version.LUCENE_35,result);
//    result = new StopFilter(result, stopSet);
    	result = new WipoFilterTwoBytesLatinChars(result);
    
    	return new TokenStreamComponents(tok, result);
  	}

	/**
	 * determine whether the bigram index term is returned or not if a input word is failed to analysis
	 * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
	 * @param is
	 */
	public void setBigrammable(boolean is) {
		bigrammable = is;
	}
	
	/**
	 * determin whether the original term is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setHasOrigin(boolean has) {
		hasOrigin = has;
	}
}
