package org.grobid.core.utilities;

import java.util.stream.IntStream;

/**
 * Copyright (C) 2017 Simon Gwerder.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
public class FuzzySubstringSearch {

    private static final String EMPTY = "";

    /**
     * This method will perform a fuzzy substring search to find a portion of the given term that matches
     * with the given query (considering the given maxDistance). The algorithm is a slightly modified version of the
     * Levenshtein distance. E.g. Looking for 'abcd' in 'xyzabydxyz' and a maxDistance of 1 will return 'abyd'. In
     * case there are multiple matching substrings, the last matching is returned. For more info:
     *
     * <a href=
     * "http://ginstrom.com/scribbles/2007/12/01/fuzzy-substring-matching-with-levenshtein-distance-in-python/">Fuzzy
     * Search Substring Algorithm</a>.
     *
     * @param term a full term that should be matched against
     * @param query the query that will be matched against a term
     * @param maxDistance the maximum number of characters that are allowed to deviate between term and query
     * @return the fuzzy matched substring or empty string if none is found
     */
    public static CharSequence fuzzySubstringSearch(CharSequence term, CharSequence query, int maxDistance) {
        if (term == null || term.length() == 0 || query == null || query.length() == 0 || maxDistance < 0) { // check input and short circuit
            return EMPTY;
        }

        int[][] minDistances = minDistances(term, query);

        MinScore minScore = minScore(query, minDistances);

        if (minScore.score > maxDistance) { // short circuit
            return EMPTY;
        }

        return reconstructResult(term, query, minDistances, minScore.endIndex);
    }

    /**
     * Calculates the minimum distances between given query and term using dynamic programming.
     *
     * @param term the term
     * @param query the query
     * @return the minimum distances between query and term for each position in the strings
     */
    private static int[][] minDistances(CharSequence term, CharSequence query) {
        int[][] m = new int[query.length() + 1][term.length() + 1];
        for (int i = 1; i <= query.length(); i++) {
            for (int j = 0; j <= term.length(); j++) {
                if (j == 0) {
                    m[i][j] = i;  // initial conditions
                }
                else if (term.charAt(j - 1) == query.charAt(i - 1)) {
                    m[i][j] = m[i - 1][j - 1];
                }
                else {
                    m[i][j] = 1 + IntStream.of(m[i][j - 1], m[i - 1][j], m[i - 1][j - 1]).min().getAsInt();
                }
            }
        }
        return m;
    }

    /**
     * Calculates the minimum score between term and query.
     *
     * @param query the query
     * @param minDistances the minimum distances between term and query
     * @return the minimum score and endIndex indicating where the matched query ends in term
     */
    private static MinScore minScore(CharSequence query, int[][] minDistances) {
        int score = -1;
        int endIndex = -1;
        for (int i = 0; i < minDistances[query.length()].length; i++) {
            if (score < 0 || score >= minDistances[query.length()][i]) {
                score = minDistances[query.length()][i];
                endIndex = i;
            }
        }
        return new MinScore(score, endIndex);
    }

    /**
     * Reconstructs the fuzzy matching substring.
     *
     * @param term the term
     * @param query the query
     * @param minDistances the minimum distances between term and query
     * @param endIndex the endIndex indicating where the matched query ends in term
     * @return the fuzzy matched substring
     */
    private static CharSequence reconstructResult(CharSequence term, CharSequence query, int[][] minDistances, int endIndex) {
        int row = query.length();
        int col = endIndex;
        while (row > 0 && col > 0) {
            if (query.charAt(row - 1) == term.charAt(col - 1)) {
                row--;
                col--;
            }
            else {
                int min = IntStream.of(minDistances[row][col - 1], minDistances[row - 1][col], minDistances[row - 1][col - 1]).min().getAsInt();
                if (minDistances[row][col - 1] == min) {
                    col--;
                }
                else if (minDistances[row - 1][col] == min) {
                    row--;
                }
                else {
                    row--;
                    col--;
                }
            }
        }

        return term.subSequence(col, endIndex);
    }

    private static class MinScore {
        public final int score;
        public final int endIndex;

        public MinScore(int score, int endIndex) {
            this.score = score;
            this.endIndex = endIndex;
        }
    }

}