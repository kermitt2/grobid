package org.grobid.core;

import java.io.Serializable;

/**
 * Created by lfoppiano on 19/08/16.
 */
public interface GrobidModel extends Serializable {
    String getFolderName();

    String getModelPath();

    String getModelName();

    String getTemplateName();

    String toString();

}
