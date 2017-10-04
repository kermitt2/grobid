package org.grobid.core.lang;

import org.grobid.core.exceptions.GrobidException;

/**
 * Date: 11/24/11
 * Time: 11:39 AM
 *
 * @author Vyacheslav Zholudev
 */
public final class Language {
    //common language constants
    public static final String EN = "en";
    public static final String DE = "de";
    public static final String FR = "fr";
    public static final String IT = "it";
    public static final String ES = "es";

    private String lang;
    private double conf;

	// default construction for jackson mapping
	public Language() {}

    public Language(String langId) {
        if (langId == null) {
            throw new GrobidException("Language id cannot be null");
        }

        if ((langId.length() != 3 && langId.length() != 2 && (!langId.equals("sorb")) && 
            (!langId.equals("zh-cn")) && (!langId.equals("zh-tw"))) || !(Character.isLetter(langId.charAt(0))
            && Character.isLetter(langId.charAt(1)))) {
            throw new GrobidException("Language id should consist of two or three letters, but was: " + langId);
        }

        this.lang = langId;
        this.conf = 1.0;
    }

    public Language(String langId, double confidence) {
        if (langId == null) {
            throw new GrobidException("Language id cannot be null");
        }

        if ((langId.length() != 3 && langId.length() != 2 && (!langId.equals("sorb")) && 
            (!langId.equals("zh-cn")) && (!langId.equals("zh-tw"))) || !(Character.isLetter(langId.charAt(0))
            && Character.isLetter(langId.charAt(1)))) {
            throw new GrobidException("Language id should consist of two or three letters, but was: " + langId);
        }

        this.lang = langId;
        this.conf = confidence;
    }

    public boolean isChinese() {
        return "zh".equals(lang) || "zh-cn".equals(lang) || "zh-tw".equals(lang);
    }

    public boolean isJapaneses() {
        return "ja".equals(lang);
    }

    public boolean isKorean() {
        return "kr".equals(lang);
    }

    public boolean isArabic() {
        return "ar".equals(lang);
    }

    public String getLang() {
        return lang;
    }
	
    public void setLang(String lang) {
        this.lang = lang;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public double getConf() {
        return conf;
    }

	public void setConf(double conf) {
		this.conf = conf;
	}

    @Override
    public String toString() {
        return lang + ";" + conf;
    }

    public String toJSON() {
        return "{\"lang\":\""+lang+"\", \"conf\": "+conf+"}";
    }
}
