package org.grobid.core.main;

import org.grobid.core.GrobidFactory;
import org.grobid.core.engines.Engine;

/**
 * The entrance point, to start grobid from command line
 *
 * @author Florian Zipser
 */
public class GrobidMain {
    static private Engine engine = GrobidFactory.instance.createEngine();


    /**
     * Starts grobid from command line using the following parameters:
     *
     * @param args arguments
     */

    public static void main(String[] args) throws Exception {


        System.out.println(engine.fullTextToTEI("/tmp/paper-08.pdf", false, false));
//        engine.processHeader("/Users/zholudev/Dropbox/problem.pdf", false, null);
//        String reference = "Altschul SF, Madden TL, Schäffer AA, Zhang J, Zhang Z, Miller W, Lipman DJ: Gapped BLAST and PSI-BLAST: a new generation of protein database  search programs.    Nucleic Acid Res 1997 25:3389-3402";
//        String reference2 = "Lipman DJ: Gapped BLAST and PSI-BLAST: a new generation of protein database  search programs.    Nucleic Acid Res 1997 25:3389-3402";
//        System.out.println(reference);
//        BiblioItem x = engine.processRawReference(reference, false);
//        System.out.println(x);
//
//        System.out.println("++++++++++++");
//        System.out.println(engine.processRawReference(reference2, false));
/*
        if (args.length != 1) {
            System.out.println("Please provide just one affiliation address string to process");
            System.exit(-1);
        }
        for (Affiliation a : engine.processAffiliation(args[0])) {
            System.out.println(a);
        }
*/
//        engine.close();

    }
}
