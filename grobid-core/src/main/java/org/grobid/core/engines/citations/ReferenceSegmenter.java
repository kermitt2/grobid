package org.grobid.core.engines.citations;

import java.util.List;

/**
 * User: zholudev
 * Date: 4/15/14
 */
public interface ReferenceSegmenter {
    List<LabeledReferenceResult> extract(String referenceBlock);
}
