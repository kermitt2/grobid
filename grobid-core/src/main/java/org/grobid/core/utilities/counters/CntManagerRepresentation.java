package org.grobid.core.utilities.counters;


import java.io.IOException;

/**
 * Date: 7/3/12
 * Time: 10:30 AM
 *
 * @author Vyacheslav Zholudev
 */
public interface CntManagerRepresentation {
    String getRepresentation(CntManager cntManager) throws IOException;
}
