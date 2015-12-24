package org.grobid.core.utilities.matching;

/**
 * User: zholudev
 * Date: 3/4/14
 */
public class EntityMatcherException extends Exception {
    private static final long serialVersionUID = 6080563488720903757L;

    public EntityMatcherException() {
        super();
    }

    public EntityMatcherException(String message) {
        super(message);
    }

    public EntityMatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityMatcherException(Throwable cause) {
        super(cause);
    }
}
