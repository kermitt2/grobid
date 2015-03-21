package org.grobid.core.engines.citations;

import java.util.List;
import org.grobid.core.document.Document;

/**
 * User: zholudev, patrice
 * Date: 4/15/14
 */
public interface ReferenceSegmenter {
    //List<LabeledReferenceResult> extract(String referenceBlock);
	List<LabeledReferenceResult> extract(Document document);
}
