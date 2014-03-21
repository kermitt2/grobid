package org.grobid.core.jni;

import fr.limsi.wapiti.SWIGTYPE_p_mdl_t;
import fr.limsi.wapiti.Wapiti;

import java.io.File;

/**
 * User: zholudev
 * Date: 3/17/14
 */
public class WapitiWrapper {
    public static String label(SWIGTYPE_p_mdl_t model, String data) {
        return Wapiti.labelFromModel(model, data);
    }

    public static SWIGTYPE_p_mdl_t getModel(File model) {
        return getModel(model, false);
    }

    public static SWIGTYPE_p_mdl_t getModel(File model, boolean checkLabels) {
        return Wapiti.loadModel("label " + (checkLabels ? "--check" : "")  + " -m " + model.getAbsolutePath());
    }

}
