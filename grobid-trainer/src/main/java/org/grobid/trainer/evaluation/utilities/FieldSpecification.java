package org.grobid.trainer.evaluation.utilities;

import java.util.*;

/**
 * Specification of field XML paths in different result documents for evaluation.
 *
 * @author Patrice Lopez
 */
public class FieldSpecification {

	public String fieldName = null;
	
	public List<String> nlmPath = new ArrayList<String>();
	public List<String> grobidPath = new ArrayList<String>();
	public List<String> pdfxPath = new ArrayList<String>();
	public List<String> cerminePath = new ArrayList<String>();
	
	public boolean isTextual = false;
//	public boolean hasMultipleValue = false;
}