package org.grobid.core.utilities.crossref;

import java.util.Iterator;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Person;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Convert a JSON Work model (from a crossref response) to a BiblioItem (understandable by Grobid)
 *
 * @author Vincent Kaestle
 */
public class WorkDeserializer extends CrossrefDeserializer<BiblioItem> {

	@Override
	protected BiblioItem deserializeOneItem(JsonNode item) {
		BiblioItem biblio = null;
		
		if (item.isObject()) {
			biblio = new BiblioItem();
			
			biblio.setDOI(item.get("DOI").asText());
			
			JsonNode titlesNode = item.get("title");
			if (titlesNode != null && titlesNode.isArray() && (((ArrayNode)titlesNode).size() > 0))
				biblio.setTitle(((ArrayNode)titlesNode).get(0).asText());
			
			JsonNode authorsNode = (item.get("author") != null) ? item.get("author") : item.get("editor");
			if (authorsNode != null && authorsNode.isArray() && (((ArrayNode)authorsNode).size() > 0)) {
				Iterator<JsonNode> authorIt = ((ArrayNode)authorsNode).elements();
				while (authorIt.hasNext()) {
					JsonNode authorNode = authorIt.next();
					
					Person person = new Person();
					person.setFirstName(authorNode.get("given").asText());
					person.setLastName(authorNode.get("family").asText());
					biblio.addFullAuthor(person);
				}
			}
		}
		
		return biblio;
	}
}
