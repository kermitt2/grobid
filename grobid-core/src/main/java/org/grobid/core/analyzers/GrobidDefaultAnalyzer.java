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

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.lang.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
	
/**
 * Default tokenizer adequate for all Indo-European languages.
 *
 * @author Patrice Lopez
 */
public class GrobidDefaultAnalyzer implements Analyzer {

	private static volatile GrobidDefaultAnalyzer instance;

	public static GrobidDefaultAnalyzer getInstance() {
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
		instance = new GrobidDefaultAnalyzer();
	}

    /**
     * Hidden constructor
     */
    private GrobidDefaultAnalyzer() {
	}

    public static final String delimiters = TextUtilities.delimiters;
    //" \n\r\t([,:;?.!/)-–−\"“”‘’'`$]*\u2666\u2665\u2663\u2660\u00A0";

    public String getName() {
		return "DefaultGrobidAnalyzer";
	} 

	public List<String> tokenize(String text) {
		// as a default analyzer, language is not considered
		return tokenize(text, null);
	}

	public List<String> tokenize(String text, Language lang) {
		List<String> result = new ArrayList<String>();
		text = UnicodeUtil.normaliseText(text);
		StringTokenizer st = new StringTokenizer(text, delimiters, true);
		while(st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}
	
	public List<String> retokenize(List<String> chunks) {
		StringTokenizer st = null;
		List<String> result = new ArrayList<String>();
		for(String chunk : chunks) {
			chunk = UnicodeUtil.normaliseText(chunk);
			st = new StringTokenizer(chunk, delimiters, true);
			while(st.hasMoreTokens()) {
				result.add(st.nextToken());
			}
		}
		return result;
	}

	public List<LayoutToken> tokenizeWithLayoutToken(String text) {
        List<LayoutToken> result = new ArrayList<>();
        text = UnicodeUtil.normaliseText(text);
        List<String> tokens = tokenize(text);
        int pos = 0;
        for(String tok : tokens) {
        	LayoutToken layoutToken = new LayoutToken();
            layoutToken.setText(tok);
            layoutToken.setOffset(pos);
            result.add(layoutToken);
            pos += tok.length();
        }
        return result;
    }
}