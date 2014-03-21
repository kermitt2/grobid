package org.grobid.trainer;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class TrainerFactory {
    public static GenericTrainer getTrainer() {
        return new WapitiTrainer();
//        return new CRFPPGenericTrainer();
    }
}
