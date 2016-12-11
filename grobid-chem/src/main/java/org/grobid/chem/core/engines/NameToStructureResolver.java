package org.grobid.core.engines;

import org.grobid.core.data.ChemicalEntity;
//import uk.ac.cam.ch.wwmm.oscar.opsin.OpsinDictionary;
//import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;

import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureConfig;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.NameToInchi;

import java.util.Set;
import java.util.Iterator;

/**
 * Chemical name-to-structure processing based on external Open Source libraries.
 *
 * @author Patrice Lopez
 */

public class NameToStructureResolver {

	//private OpsinDictionary opsin = null;
	//private ChemNameDictRegistry dict = null;
	
	private static volatile NameToStructureResolver instance;
	
	public static NameToStructureResolver getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
                if (instance == null)
					getNewInstance();
            // }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
	private static synchronized void getNewInstance() {
		//LOGGER.debug("Get new instance of Lexicon");
		//GrobidProperties.getInstance();
		instance = new NameToStructureResolver();
	}
	
	private NameToStructureResolver() {
		//opsin = new OpsinDictionary();
		//dict = new ChemNameDictRegistry();
	}

    public ChemicalEntity process(String name) {
        ChemicalEntity result = new ChemicalEntity(name);

		NameToStructure n2s = NameToStructure.getInstance();
		NameToStructureConfig n2sconfig = new NameToStructureConfig();
		
		OpsinResult res = n2s.parseChemicalName(name, n2sconfig);

		if (res.getStatus() != OpsinResult.OPSIN_RESULT_STATUS.FAILURE) {

			//Element cml = res.getCml();
			String smiles = res.getSmiles();
			if (smiles != null)	
				result.setSmiles(smiles);
			String inchi = NameToInchi.convertResultToInChI(res);
			if (inchi != null) {
				result.setInchi(inchi);
			}
			
		}
		
		/*java.util.Set<String> smiles = opsin.getAllSmiles(name);
		if (!smiles.isEmpty()) {
			Iterator<String> it = smiles.iterator();
			if (it.hasNext()) {
		        result.setSmiles(it.next());
		    }
		}
		java.util.Set<String> inchis = opsin.getInchis(name);
		if (!inchis.isEmpty()) {
			Iterator<String> it = inchis.iterator();
			if (it.hasNext()) {
        		result.setInchi(it.next());
			}
		}
		
		if (dict.hasName(name)) {
			
		}
		*/
        return result;
    }

    public static void depict(ChemicalEntity structure, String path) {
		
    }


}