package org.grobid.service.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Damien
 * 
 */
public class Template extends TagSupport {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the page.
	 */
	private String pageName;

	/**
	 * @param pageName
	 *            the pageName to set
	 */
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doStartTag() throws JspException {
		StringBuffer html = new StringBuffer();
		html.append(getHeader());
		html.append(getFullBody());
		html.append(getFooter());

		try {
			pageContext.getOut().println(html);
		} catch (IOException e) {
			throw new JspException("I/O Error:", e);
		}

		return SKIP_BODY;
	}

	/**
	 * Generates the header of the template.
	 * 
	 * @return the header.
	 */
	protected String getHeader() {
		StringBuffer header = new StringBuffer();
		header.append("<%@include file='/WEB-INF/grobid/navigation/header.html'%>");
		return header.toString();
	}

	/**
	 * Generates the side bar of the template.
	 * 
	 * @return the header.
	 */
	protected String getSideBar() {
		StringBuffer sideBar = new StringBuffer();
		sideBar.append("<%@include file='/WEB-INF/grobid/navigation/side-bar.html%'>");
		return sideBar.toString();
	}

	/**
	 * Generates the body of the template.
	 * 
	 * @return the header.
	 */
	protected String getFullBody() {
		StringBuffer body = new StringBuffer();
		body.append("<body>");
		body.append("<table>");
		body.append("<tr>");
		body.append("<td>" + getSideBar() + "</td>");
		body.append("<td><%@include file='" + pageName + "'%></td>");
		body.append("</tr>");
		body.append("</table>");
		body.append("</body>");
		return body.toString();
	}

	/**
	 * Generates the footer of the template.
	 * 
	 * @return the header.
	 */
	protected String getFooter() {
		StringBuffer footer = new StringBuffer();
		footer.append("<%@include file='/WEB-INF/grobid/navigation/footer.html'%>");
		return footer.toString();
	}

}
