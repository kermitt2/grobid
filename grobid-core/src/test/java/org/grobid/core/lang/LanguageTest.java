package org.grobid.core.lang;

import org.junit.Test;

public class LanguageTest {

    @Test
    public void testLanguagesAvailableInLangdetect() {
        String[] langList = new String[] {
            "af",
            "ar",
            "bg",
            "bn",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "et",
            "fa",
            "fi",
            "fr",
            "gu",
            "he",
            "hi",
            "hr",
            "hu",
            "id",
            "it",
            "ja",
            "kn",
            "ko",
            "lt",
            "lv",
            "mk",
            "ml",
            "mr",
            "ne",
            "nl",
            "no",
            "pa",
            "pl",
            "pt",
            "ro",
            "ru",
            "sk",
            "sl",
            "so",
            "sq",
            "sv",
            "sw",
            "ta",
            "te",
            "th",
            "tl",
            "tr",
            "uk",
            "ur",
            "vi",
            "zh-cn",
            "zh-tw"
        };
        // Should not throw an exception
        for (String lang : langList) {
            new Language(lang, 1d);
        }
    }
}
