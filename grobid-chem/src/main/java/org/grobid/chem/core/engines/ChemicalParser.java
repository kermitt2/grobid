package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.ChemicalEntity;
import org.grobid.core.engines.AbstractParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorChemicalEntity;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Chemical entities extraction.
 *
 * @author Patrice Lopez
 */
public class ChemicalParser extends AbstractParser {

//    private FeatureFactory featureFactory = null;

	private NameToStructureResolver nameToStructure = NameToStructureResolver.getInstance();

    public ChemicalParser() {
        super(GrobidModels.ENTITIES_CHEMISTRY);
//        featureFactory = FeatureFactory.getInstance();
    }

    /**
     * Extract all reference from a simple piece of text.
     */
    public List<ChemicalEntity> extractChemicalEntities(String text) throws Exception {
//        int nbRes = 0;
        if (text == null)
            return null;
        if (text.length() == 0)
            return null;
        List<ChemicalEntity> entities;
        try {
            text = text.replace("\n", " ");
            StringTokenizer st = new StringTokenizer(text, TextUtilities.fullPunctuations, true);

            if (st.countTokens() == 0)
                return null;

            ArrayList<String> textBlocks = new ArrayList<String>();
            ArrayList<String> tokenizations = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                tokenizations.add(tok);
                if (!tok.equals(" ")) {
                    textBlocks.add(tok + "\t<chemical>");
                }
            }
            String ress = "";
            int posit = 0;
            for (String block : textBlocks) {
                //System.out.println(block);
                ress += FeaturesVectorChemicalEntity
                        .addFeaturesChemicalEntities(block, textBlocks.size(), posit, false, false)
                        .printVector()+"\n";
                posit++;
            }
            ress += "\n";
            String res = label(ress);
            entities = resultExtraction(text, res, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return entities;
    }
    

    /**
     * Extract the chemical entities from a labelled text.
     */
    public List<ChemicalEntity> resultExtraction(String text, 
												String result,
                                                List<String> tokenizations) {

		List<ChemicalEntity> entities = new ArrayList<ChemicalEntity>();
        StringTokenizer stt = new StringTokenizer(result, "\n");
		String label = null; // label
        String actual = null; // token
		int offset = 0;
		int addedOffset = 0;
		int p = 0; // iterator for the tokenizations for restauring the original tokenization with
        // respect to spaces
		ChemicalEntity currentEntity = null;
		while (stt.hasMoreTokens()) {
            String line = stt.nextToken();
            if (line.trim().length() == 0) {
                continue;
            }

			StringTokenizer st2 = new StringTokenizer(line, "\t");
            boolean start = true;
			//String separator = "";
            label = null;
            actual = null;
            while (st2.hasMoreTokens()) {
                if (start) {
                    actual = st2.nextToken().trim();
                    start = false;

                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p);
						addedOffset += tokOriginal.length();
						if (tokOriginal.equals(actual)) {
                            strop = true;
                        }
                        p++;
                    }
                } else {
                    label = st2.nextToken().trim();
                }
            }

            if (label == null) {
				offset += addedOffset;
				addedOffset = 0;
                continue;
            }

			if (actual != null) {
				if (label.startsWith("I-")) {      
					if (currentEntity != null) {
						int localPos = currentEntity.getOffsetEnd();
						if (label.length() > 1) {  
							String subtag = label.substring(2,label.length()).toLowerCase();
							if (currentEntity.getRawName().equals(subtag) && 
							   ( (localPos == offset) ) ) {
								currentEntity.setOffsetEnd(offset+addedOffset);
								offset += addedOffset;
								addedOffset = 0;	
								continue;
							}														
							//if (!currentEntity.getRawName().equals(subtag))
							entities.add(currentEntity);
						}
					}
					if (label.length() > 1) {  
						String subtag = label.substring(2,label.length()).toLowerCase();
						currentEntity = new ChemicalEntity(subtag);   
						//if (tokenizations.get(offset) == " ") {
						if ( text.charAt(offset) == ' ') {	
							currentEntity.setOffsetStart(offset+1);
						}
						else
							currentEntity.setOffsetStart(offset);
						currentEntity.setOffsetEnd(offset+addedOffset);
					}  
				}
				else if (!label.equals("<other>")) {  
					if (label.length() > 1) {  
						String subtag = label;

					    if ( (currentEntity != null) && (currentEntity.getRawName().equals(subtag)) ) {
							currentEntity.setOffsetEnd(offset+addedOffset);		
						}
						else {  
							if (currentEntity != null) 
								entities.add(currentEntity);
							currentEntity = new ChemicalEntity(subtag);   
							currentEntity.setOffsetStart(offset);
							currentEntity.setOffsetEnd(offset+addedOffset);
						}
				   	}
				}

				offset += addedOffset;
				addedOffset = 0;
			}			
		}

		if (currentEntity != null) {
			entities.add(currentEntity);
		}
		
		for (ChemicalEntity entity : entities) {
			String ent = text.substring(entity.getOffsetStart(), entity.getOffsetEnd()).replace("\n", " ").trim();
			entity.setRawString(ent);
            ChemicalEntity structure = nameToStructure.process(ent);
			entity.setInchi(structure.getInchi());
			entity.setSmiles(structure.getSmiles());
        }

		return entities;
	}


    public List<ChemicalEntity> resultExtraction2(String result,
                                                 ArrayList<String> tokenizations) {
        List<ChemicalEntity> entities = new ArrayList<ChemicalEntity>();

        StringTokenizer stt = new StringTokenizer(result, "\n");

        ArrayList<String> nameEntities = new ArrayList<String>();
        ArrayList<Integer> offsets_entities = new ArrayList<Integer>();
        String entity = null;
        int offset = 0;
        int currentOffset = 0;
        String label; // label
        String actual; // token
        String lastTag = null; // previous label
        int p = 0; // iterator for the tokenizations for restauring the original tokenization with
        // respect to spaces
        while (stt.hasMoreTokens()) {
            String line = stt.nextToken();
            if (line.trim().length() == 0) {
                continue;
            }

            StringTokenizer st2 = new StringTokenizer(line, "\t");
            boolean start = true;
            boolean addSpace = false;
            label = null;
            actual = null;
            int offset_addition = 0;
            while (st2.hasMoreTokens()) {
                if (start) {
                    actual = st2.nextToken();
                    start = false;
                    actual = actual.trim();
                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p);
                        offset_addition += tokOriginal.length();
                        if (tokOriginal.equals(" ")) {
                            addSpace = true;
                        } else if (tokOriginal.equals(actual)) {
                            strop = true;
                        }
                        p++;
                    }
                } else {
                    label = st2.nextToken().trim();
                }
            }

            if (label == null) {
                continue;
            }

            if (actual != null) {
                if (label.endsWith("<chemName>")) {
                    if (entity == null) {
                        entity = actual;
                        currentOffset = offset;
                    } else {
                        if (label.equals("I-<chemName>")) {
                            if (entity != null) {
                                nameEntities.add(entity);
                                offsets_entities.add(currentOffset);
                            }
                            entity = actual;
                            currentOffset = offset;
                        } else {
                            if (addSpace) {
                                entity += " " + actual;
                            } else {
                                entity += actual;
                            }
                        }
                    }
                } else if (label.equals("<other>")) {
                    if (entity != null) {
                        nameEntities.add(entity);
                        offsets_entities.add(currentOffset);
                    }
                    entity = null;
                }
            }
            offset += offset_addition;
            lastTag = label;
        }

        // call the name-to-structure processing
        int j = 0;
        if (nameEntities.size() == 0) {
            return null;
        }
        for (String name : nameEntities) {
			
            ChemicalEntity structure = nameToStructure.process(name);
            //ChemicalEntity structure = new ChemicalEntity();
            //structure.setRawName(name);
            structure.setOffsetStart(offsets_entities.get(j));
            structure.setOffsetEnd(offsets_entities.get(j) + name.length());
            entities.add(structure);
            j++;
        }

        return entities;
    }

}