package org.grobid.core;

import java.io.Serializable;

public interface GrobidModel extends Serializable {
    String getFolderName();

    String getModelPath();

    String getModelName();

    String getTemplateName();

    String toString();

}
