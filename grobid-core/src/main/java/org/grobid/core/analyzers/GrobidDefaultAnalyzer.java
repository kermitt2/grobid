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

import org.apache.lucene.analysis.Analyzer;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
	
/**
 * Default tokenizer adequate for all Indo-European languages.
 *
 * @author Patrice Lopez
 */

public class GrobidDefaultAnalyzer {

    public static final String delimiters = " \n\r\t([,:;?.!/)-–\"“”‘’'`$]*\u2666\u2665\u2663\u2660\u00A0";

	public static List<String> tokenize(String text) {
		List<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(text, delimiters, true);
		while(st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}
	
	public static List<String> retokenize(List<String> chunks) {
		StringTokenizer st = null;
		List<String> result = new ArrayList<String>();
		for(String chunk : chunks) {
			st = new StringTokenizer(chunk, delimiters, true);
			while(st.hasMoreTokens()) {
				result.add(st.nextToken());
			}
		}
		return result;
	}
}