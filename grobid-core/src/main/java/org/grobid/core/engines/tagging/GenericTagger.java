package org.grobid.core.engines.tagging;

import java.io.Closeable;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public interface GenericTagger extends Closeable {
    String label(Iterable<String> data);
    String label(String data);
}
