package org.grobid.core.utilities.fatcat;

import java.util.Iterator;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Person;
import org.grobid.core.data.Date;
import org.grobid.core.utilities.crossref.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Convert a JSON fatcat release model - from a glutton response (or maybe
 * direct fatcat API response?)- to a BiblioItem
 *
 * @author Bryan Newbold, Vincent Kaestle, Patrice
 */
public class FatcatReleaseDeserializer extends CrossrefDeserializer<BiblioItem> {

	@Override
	protected BiblioItem deserializeOneItem(JsonNode item) {
		BiblioItem biblio = null;
		String type = null; // the fatcat release_type of the item, which is mostly CSL type, see fatcat docs

		if (item.isObject()) {
			biblio = new BiblioItem();
			//System.out.println(item.toString());			
		
            biblio.setFatcatIdent("release_" + item.get("ident").asText());

			JsonNode extIdsNode = item.get("ext_ids");

			JsonNode doiNode = extIdsNode.get("doi");
            if (doiNode != null && (!doiNode.isMissingNode()) ) {
                String doi = doiNode.asText();
                biblio.setDOI(doi);
            }

			JsonNode pmidNode = extIdsNode.get("pmid");
            if (pmidNode != null && (!pmidNode.isMissingNode()) ) {
                String pmid = pmidNode.asText();
                biblio.setPMID(pmid);
            }

            JsonNode pmcidNode = extIdsNode.get("pmcid");
            if (pmcidNode != null && (!pmcidNode.isMissingNode()) ) {
                String pmcid = pmcidNode.asText();
                biblio.setPMCID(pmcid);
            }

            // NOTE: not currently part of fatcat schema
            JsonNode piiNode = extIdsNode.get("pii");
            if (piiNode != null && (!piiNode.isMissingNode()) ) {
                String pii = piiNode.asText();
                biblio.setPII(pii);
            }

            JsonNode arkNode = extIdsNode.get("ark");
            if (arkNode != null && (!arkNode.isMissingNode()) ) {
                String ark = arkNode.asText();
                biblio.setArk(ark);
            }

            // NOTE: not currently part of fatcat schema
            JsonNode istexNode = extIdsNode.get("istex");
            if (istexNode != null && (!istexNode.isMissingNode()) ) {
                String istexId = istexNode.asText();
                biblio.setIstexId(istexId);
            }

            JsonNode arxivNode = extIdsNode.get("arxiv");
            if (arxivNode != null && (!arxivNode.isMissingNode()) ) {
                String arxivId = arxivNode.asText();
                biblio.setArXivId(arxivId);
            }

            JsonNode wikidataQidNode = extIdsNode.get("wikidata_qid");
            if (wikidataQidNode != null && (!wikidataQidNode.isMissingNode()) ) {
                String wikidataQid = wikidataQidNode.asText();
                biblio.setWikidataQid(wikidataQid);
            }

            // NOTE: not currently part of regular fatcat schema
            // the open access url - if available, from the glorious UnpayWall dataset provided
            // by biblio-glutton
            JsonNode oaLinkNode = item.get("oaLink");
            if (oaLinkNode != null && (!oaLinkNode.isMissingNode()) ) {
                String oaLink = oaLinkNode.asText();
                biblio.setOAURL(oaLink);
            }

			JsonNode typeNode = item.get("release_type");
			if (typeNode != null && (!typeNode.isMissingNode()) ) {
                // used below
				type = typeNode.asText();
			}
			
            JsonNode titleNode = item.get("title");
            if (titleNode != null && (!titleNode.isMissingNode()) ) {
                String title = titleNode.asText();
                biblio.setTitle(title);
            }
			
			JsonNode authorsNode = item.get("contribs");
			if (authorsNode != null && (!authorsNode.isMissingNode()) && 
				authorsNode.isArray() && (((ArrayNode)authorsNode).size() > 0)) {
				Iterator<JsonNode> authorIt = ((ArrayNode)authorsNode).elements();
				while (authorIt.hasNext()) {
					JsonNode authorNode = authorIt.next();
					
					Person person = new Person();
                    // try release-specific names first
					if (authorNode.get("surname") != null && !authorNode.get("surname").isMissingNode()) {
						person.setLastName(authorNode.get("family").asText());
                        if (authorNode.get("given_name") != null && !authorNode.get("given_name").isMissingNode()) {
                            person.setFirstName(authorNode.get("given_name").asText());
                            person.normalizeCrossRefFirstName();
                        }
                    } else if (authorNode.get("creator") != null && !authorNode.get("creator").isMissingNode()) {
                        // then try generic (creator entity) full name or display name
                        JsonNode creatorNode = authorNode.get("creator");
                        if (creatorNode.get("surname") != null && !creatorNode.get("surname").isMissingNode()) {
                            person.setLastName(creatorNode.get("family").asText());
                            if (creatorNode.get("given_name") != null &&
                                    !creatorNode.get("given_name").isMissingNode()) {
                                person.setFirstName(creatorNode.get("given_name").asText());
                                person.normalizeCrossRefFirstName();
                            }
                        // generic display name as rawName (does not fall through to paper raw_name)
                        } else if (creatorNode.get("display_name") != null &&
                                   !creatorNode.get("display_name").isMissingNode()) {
                            person.setRawName(creatorNode.get("display_name").asText());
                        }
                    } else if (authorNode.get("raw_name") != null && !authorNode.get("raw_name").isMissingNode()) {
                        // last try paper-specific raw name
                        person.setRawName(authorNode.get("raw_name").asText());
                    }
    	   			// for cases like JM Smith and for case normalisation
    	   			person.normalizeName();
					biblio.addFullAuthor(person);
				}
			}

			JsonNode publisherNode = item.get("publisher");
			if (publisherNode != null && (!publisherNode.isMissingNode()))
				biblio.setPublisher(publisherNode.asText());

			JsonNode pageNode = item.get("pages");
			if (pageNode != null && (!pageNode.isMissingNode()) )
				biblio.setPageRange(pageNode.asText());

			JsonNode volumeNode = item.get("volume");
			if (volumeNode != null && (!volumeNode.isMissingNode()))
				biblio.setVolumeBlock(volumeNode.asText(), false);

			JsonNode issueNode = item.get("issue");
			if (issueNode != null && (!issueNode.isMissingNode()))
				biblio.setIssue(issueNode.asText());

			JsonNode containerNode = item.get("container");
			if (containerNode != null && (!containerNode.isMissingNode())) {
                JsonNode containerTitleNode = containerNode.get("name");
                if (containerTitleNode != null && (!containerTitleNode.isMissingNode())) {
                    // container title depends on the type of object
                    // if journal
                    if ( (type != null) && (type.equals("article-journal")) )
                        biblio.setJournal(containerTitleNode.asText());

                    // if book chapter or proceedings article
                    if ( (type != null) && (type.equals("paper-conference") || type.equals("chapter")) )
                        biblio.setBookTitle(containerTitleNode.asText());
                }

                JsonNode containerAbbrevNode = containerNode.get("abbrev");
                if (containerAbbrevNode != null && (!containerAbbrevNode.isMissingNode())) {
                    // container title depends on the type of object
                    // if journal
                    if ( (type != null) && (type.equals("article-journal")) )
                        biblio.setJournalAbbrev(containerAbbrevNode.asText());
                } 
                JsonNode containerIssnlNode = containerNode.get("issnl");
                if (containerIssnlNode != null && (!containerIssnlNode.isMissingNode())) {
                    // NOTE: just setting the ISSN-L as ISSN, instead of
                    // electronic/print
                    biblio.setISSN(containerIssnlNode.asText());
                }
			}

			JsonNode yearNode = item.get("release_year");
			JsonNode dateNode = item.get("release_date");
            if (yearNode != null && (!yearNode.isMissingNode())) {

                Date date = new Date();
                date.setYear(yearNode.asInt());
                
                // TODO: parse dateNode (ISO datetime string), and populate rest of date from that
                // date.setMonth(monthInt);
                // date.setDay(dayInt);
                biblio.setNormalizedPublicationDate(date);

            }
            //System.out.println(biblio.toTEI(0));
		}
		return biblio;
	}
}
