package org.grobid.core.main.batch;

/**
 * Class containing args of the batch {@link GrobidMain}
 * 
 * @author Damien
 * 
 */
public class GrobidMainArgs {

	private String path2grobidHome;

	private String path2grobidProperty;

	private String path2pdfs;

	private String path2Output;

	private String processMethodName;

	private String input;

	private boolean isPdf;

	/**
	 * @return the path2grobidHome
	 */
	public String getPath2grobidHome() {
		return path2grobidHome;
	}

	/**
	 * @param path2grobidHome
	 *            the path2grobidHome to set
	 */
	public void setPath2grobidHome(String path2grobidHome) {
		this.path2grobidHome = path2grobidHome;
	}

	/**
	 * @return the path2grobidProperty
	 */
	public String getPath2grobidProperty() {
		return path2grobidProperty;
	}

	/**
	 * @param path2grobidProperty
	 *            the path2grobidProperty to set
	 */
	public void setPath2grobidProperty(String path2grobidProperty) {
		this.path2grobidProperty = path2grobidProperty;
	}

	/**
	 * @return the path2pdfs
	 */
	public String getPath2pdfs() {
		return path2pdfs;
	}

	/**
	 * @param path2pdfs
	 *            the path2pdfs to set
	 */
	public void setPath2pdfs(String path2pdfs) {
		this.path2pdfs = path2pdfs;
	}

	/**
	 * @return the path2Output
	 */
	public String getPath2Output() {
		return path2Output;
	}

	/**
	 * @param path2Output
	 *            the path2Output to set
	 */
	public void setPath2Output(String path2Output) {
		this.path2Output = path2Output;
	}

	/**
	 * @return the processMethodName
	 */
	public String getProcessMethodName() {
		return processMethodName;
	}

	/**
	 * @param processMethodName
	 *            the processMethodName to set
	 */
	public void setProcessMethodName(String processMethodName) {
		this.processMethodName = processMethodName;
	}

	/**
	 * @return the input
	 */
	public String getInput() {
		return input;
	}

	/**
	 * @param input
	 *            the input to set
	 */
	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * @return the isPdf
	 */
	public boolean isPdf() {
		return isPdf;
	}

	/**
	 * @param isPdf
	 *            the isPdf to set
	 */
	public void setPdf(boolean isPdf) {
		this.isPdf = isPdf;
	}

}
