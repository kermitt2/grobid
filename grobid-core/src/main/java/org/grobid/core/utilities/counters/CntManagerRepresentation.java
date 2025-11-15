package org.grobid.core.utilities.counters;

import java.io.IOException;

public interface CntManagerRepresentation {
    String getRepresentation(CntManager cntManager) throws IOException;
}
