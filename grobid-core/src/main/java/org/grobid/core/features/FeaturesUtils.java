package org.grobid.core.features;

public class FeaturesUtils {
    private FeaturesUtils() {
    }

    /**
     * All the characters which are treated as space in C++: http://en.cppreference.com/w/cpp/string/byte/isspace
     * @param token to be sanitized
     * @return sanitized string legible as Wapiti input
     */
    public static String sanitizeTokenForWapiti(String token) {
        if (token == null)
            return null;
        return token.replaceAll("[\f\n\r\t ]", "");
    }

    public static boolean isEmptyAfterSanitization(String token) {
        if (token == null) {
            return true;
        }
        return token.replaceAll("[\f\n\r\t ]", "").isEmpty();
    }
}
