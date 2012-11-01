package org.grobid.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.junit.Test;

public class GrobidPropertyTest {

	@Test
	public void testinferTypeBooleanUnknown() {
		assertEquals("Returned type not the one expected",
				GrobidProperty.TYPE.UNKNOWN,
				GrobidProperty.inferType(StringUtils.EMPTY, ""));
	}

	@Test
	public void testinferTypeBooleanTrue() {
		assertEquals("Returned type not the one expected",
				GrobidProperty.TYPE.BOOLEAN,
				GrobidProperty.inferType(StringUtils.EMPTY, " TruE "));
	}

	@Test
	public void testinferTypeBooleanFalse() {
		assertEquals("Returned type not the one expected",
				GrobidProperty.TYPE.BOOLEAN,
				GrobidProperty.inferType(StringUtils.EMPTY, " False "));
	}

	@Test
	public void testinferTypeInteger() {
		assertEquals("Returned type not the one expected",
				GrobidProperty.TYPE.INTEGER,
				GrobidProperty.inferType(StringUtils.EMPTY, " 88 "));
	}

	@Test
	public void testinferTypeFile() throws IOException {
		File file = File.createTempFile("file", "test");
		assertEquals(
				"Returned type not the one expected",
				GrobidProperty.TYPE.FILE,
				GrobidProperty.inferType(StringUtils.EMPTY,
						file.getCanonicalPath()));
	}

	@Test
	public void testinferTypeString() throws IOException {
		assertEquals("Returned type not the one expected",
				GrobidProperty.TYPE.STRING,
				GrobidProperty.inferType(StringUtils.EMPTY, " string test "));
	}

	@Test
	public void testinferTypeAdminPassword() throws IOException {
		assertEquals(
				"Returned type not the one expected",
				GrobidProperty.TYPE.PASSWORD,
				GrobidProperty
						.inferType(
								GrobidPropertyKeys.PROP_GROBID_SERVICE_ADMIN_PW,
								"pass"));
	}

	@Test
	public void testhashCode() throws IOException {
		GrobidProperty property = new GrobidProperty(
				GrobidPropertyKeys.PROP_GROBID_HOME, "HOME");
		GrobidProperty property2 = new GrobidProperty(
				GrobidPropertyKeys.PROP_GROBID_MAX_CONNECTIONS, "10");
		GrobidProperty property3 = new GrobidProperty(
				GrobidPropertyKeys.PROP_GROBID_HOME, "HOME");

		assertTrue("Hash code not the one expected",
				property.hashCode() != property2.hashCode());
		assertTrue("Hash code not the one expected",
				property.hashCode() == property3.hashCode());
	}

	@Test
	public void testequals() throws IOException {
		GrobidProperty property = new GrobidProperty(
				GrobidPropertyKeys.PROP_GROBID_HOME, "HOME");
		property.setType(GrobidProperty.TYPE.UNKNOWN);
		GrobidProperty property2 = new GrobidProperty(
				GrobidPropertyKeys.PROP_GROBID_HOME, "HOME");
		property2.setType(GrobidProperty.TYPE.UNKNOWN);

		assertTrue("Hash code not the one expected", property.equals(property2));
	}

	@Test
	public void testgetsetKey() {
		GrobidProperty prop = new GrobidProperty(StringUtils.EMPTY,
				StringUtils.EMPTY);
		prop.setKey("KEY");
		assertEquals("Returned type not the one expected", "KEY", prop.getKey());
	}

	@Test
	public void testgetsetKeyNull() {
		GrobidProperty prop = new GrobidProperty(StringUtils.EMPTY,
				StringUtils.EMPTY);
		prop.setKey(null);
		assertEquals("Returned type not the one expected", StringUtils.EMPTY,
				prop.getKey());
	}

	@Test
	public void testgetsetValue() {
		GrobidProperty prop = new GrobidProperty(StringUtils.EMPTY,
				StringUtils.EMPTY);
		prop.setValue("VALUE");
		assertEquals("Returned type not the one expected", "VALUE",
				prop.getValue());
	}

	@Test
	public void testgetsetValueNull() {
		GrobidProperty prop = new GrobidProperty(StringUtils.EMPTY,
				StringUtils.EMPTY);
		prop.setValue(null);
		assertEquals("Returned type not the one expected", StringUtils.EMPTY,
				prop.getValue());
	}

	@Test
	public void testgetsetType() {
		GrobidProperty prop = new GrobidProperty(StringUtils.EMPTY,
				StringUtils.EMPTY);
		prop.setType(GrobidProperty.TYPE.INTEGER);
		assertEquals("Returned type not the one expected",
				GrobidProperty.TYPE.INTEGER, prop.getType());
	}

}
