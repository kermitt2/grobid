package org.grobid.core.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Index to compute a gorn index while parsing a xml document.
 * 
 * @author Damien
 * 
 */
public class GornIndex implements Cloneable {

	/**
	 * Separator between each gorn digit.
	 */
	private static final String SEPARATOR = ".";

	/**
	 * Current depth of the index.
	 */
	private int depth;

	/**
	 * List containing the indexes.
	 */
	private List<Integer> index;

	/**
	 * Constructor of the Gorn index.
	 */
	public GornIndex() {
		depth = 0;
		index = new ArrayList<Integer>();
	}

	/**
	 * Constructor of the Gorn index.
	 */
	public GornIndex(final int pDepth, final List<Integer> pIndex) {
		depth = pDepth;
		index = new ArrayList<Integer>(pIndex);
	}

	/**
	 * Decrement the index. This method has to be called when a start tag is
	 * found.
	 */
	public void incrementIndex() {
		depth++;
		int sizeOfIdx = index.size();
		if (depth == sizeOfIdx) {
			increment();
		} else if (depth > sizeOfIdx) {
			addDigit();
		}
	}

	/**
	 * Decrement the index. This method has to be called when a end tag is
	 * found.
	 */
	public void decrementIndex() {
		int sizeOfIdx = index.size();
		if (depth < sizeOfIdx) {
			removeLast();
		}
		depth--;
	}

	/**
	 * Return the current value of the Gorn index.
	 * 
	 * @return String corresponding to the Gorn index.
	 */
	public String getCurrentGornIndex() {
		StringBuffer gorn = new StringBuffer();
		for (Integer currIdx : index) {
			gorn.append(currIdx);
			gorn.append(SEPARATOR);
		}
		return gorn.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GornIndex clone() throws CloneNotSupportedException {
		return new GornIndex(depth, index);
	}

	/**
	 * Increment by 1 the element of index located at depth-1.
	 */
	protected void increment() {
		int currIdx = index.get(depth - 1);
		index.set(depth - 1, ++currIdx);
	}

	/**
	 * Add the number 1 as last element of index.
	 */
	protected void addDigit() {
		index.add(1);
	}

	/**
	 * Remove the last element of index.
	 */
	protected void removeLast() {
		index.remove(index.size() - 1);
	}

}
