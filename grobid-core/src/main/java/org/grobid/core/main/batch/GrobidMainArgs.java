package org.grobid.core.main.batch;

/**
 * Class containing args of the batch {@link GrobidMain}.
 * 
 * @author Damien, Patrice
 * 
 */
public class GrobidMainArgs {

	private String path2grobidHome;

	private String path2grobidProperty;

	private String path2Input;

	private String path2Output;

	private String processMethodName;

	private String input;

	private boolean isPdf;
	
	private boolean recursive = false; 

	private boolean saveAssets = true;

	/**
	 * @return the path2grobidHome
	 */
	public final String getPath2grobidHome() {
		return path2grobidHome;
	}

	/**
	 * @param pPath2grobidHome
	 *            the path2grobidHome to set
	 */
	public final void setPath2grobidHome(final String pPath2grobidHome) {
		path2grobidHome = pPath2grobidHome;
	}

	/**
	 * @return the path2grobidProperty
	 */
	public final String getPath2grobidProperty() {
		return path2grobidProperty;
	}

	/**
	 * @param pPath2grobidProperty
	 *            the path2grobidProperty to set
	 */
	public final void setPath2grobidProperty(final String pPath2grobidProperty) {
		path2grobidProperty = pPath2grobidProperty;
	}

	/**
	 * @return the path2input
	 */
	public final String getPath2Input() {
		return path2Input;
	}

	/**
	 * @param pPath2input
	 *            the path2input to set
	 */
	public final void setPath2Input(final String pPath2input) {
		path2Input = pPath2input;
	}

	/**
	 * @return the path2Output
	 */
	public final String getPath2Output() {
		return path2Output;
	}

	/**
	 * @param pPath2Output
	 *            the path2Output to set
	 */
	public final void setPath2Output(final String pPath2Output) {
		path2Output = pPath2Output;
	}

	/**
	 * @return the processMethodName
	 */
	public final String getProcessMethodName() {
		return processMethodName;
	}

	/**
	 * @param pProcessMethodName
	 *            the processMethodName to set
	 */
	public final void setProcessMethodName(final String pProcessMethodName) {
		processMethodName = pProcessMethodName;
	}

	/**
	 * @return the input
	 */
	public final String getInput() {
		return input;
	}

	/**
	 * @param pInput
	 *            the input to set
	 */
	public final void setInput(final String pInput) {
		input = pInput;
	}

	/**
	 * @return the isPdf
	 */
	public final boolean isPdf() {
		return isPdf;
	}

	/**
	 * @param pIsPdf
	 *            the isPdf to set
	 */
	public final void setPdf(final boolean pIsPdf) {
		isPdf = pIsPdf;
	}

	/**
	 * @return true if recursive file processing
	 */
	public final boolean isRecursive() {
		return recursive;
	}

	/**
	 * @return true if the PDF assets (bitmaps, vector graphics) should be also extracted and saved
	 */
	public final boolean getSaveAssets() {
		return saveAssets;
	}

	/**
	 * @param pSaveAssets true if the PDF assets (bitmaps, vector graphics) should be also extracted and saved
	 */
	public final void setSaveAssets(boolean pSaveAssets) {
		saveAssets = pSaveAssets;
	}

	/**
	 * @param pRecursive
	 *            recursive file processing parameter to set
	 */
	public final void setRecursive(final boolean pRecursive) {
		recursive = pRecursive;
	}

}
