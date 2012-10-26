package org.grobid.core.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.factory.GrobidFactory;
import org.junit.Test;

/**
 *  @author Patrice Lopez
 */
public class TestCitationParser extends EngineTest {
	
	@Test
	public void testCitationParser() throws Exception {
        GrobidFactory.getInstance().createEngine();
		String citation1 = "A. Cau, R. Kuiper, and W.-P. de Roever. Formalising Dijkstra's development "+
		"strategy within Stark's formalism. In C. B. Jones, R. C. Shaw, and " +
		"T. Denvir, editors, Proc. 5th. BCS-FACS Refinement Workshop, London, UK, 1992.";			
		BiblioItem resCitation = engine.processRawReference(citation1, false);
		assertNotNull(resCitation);	
		if (resCitation != null) {
			assertThat(resCitation.getTitle(), 
				is("Formalising Dijkstra's development strategy within Stark's formalism"));
			assertNotNull(resCitation.getFullAuthors());
		}
		
		String citation2 = "Sanda M. Harabagiu, Steven J. Maiorano and Marius A. Pasca. Open-Domain Textual "+
		"Question Answering Techniques. Natural Language Engineering 9 (3):1-38, 2003.";
		resCitation = engine.processRawReference(citation2, false);
		assertNotNull(resCitation);	
		if (resCitation != null) {
			assertThat(resCitation.getTitle(), 
				is("Open-Domain Textual Question Answering Techniques"));
			assertNotNull(resCitation.getFullAuthors());
		}
		
		String citation3 = "Graff, Expert. Opin. Ther. Targets (2002) 6(1): 103-113";
		resCitation = engine.processRawReference(citation3, true);
		assertNotNull(resCitation);	
		if (resCitation != null) {
			assertNotNull(resCitation.getNormalizedPublicationDate());	
			assertThat(resCitation.getNormalizedPublicationDate().getYear(), 
				is(2002));
		}
		
		String citation4 = "Zholudev Vyacheslav, Kohlhase Michael, Rabe Florian. A [insert XML Format] " + 
			"Database for [insert cool application] (extended version); Technical Report , Jacobs " + 
			"University Bremen 2010.";
		resCitation = engine.processRawReference(citation4, true);
		assertNotNull(resCitation);	
		if (resCitation != null) {
			assertNotNull(resCitation.getNormalizedPublicationDate());	
			assertThat(resCitation.getNormalizedPublicationDate().getYear(), 
				is(2010));
			assertNotNull(resCitation.getFullAuthors());
		}
		
		String citation5 = "Altschul SF, Madden TL, Schäffer AA, Zhang J, Zhang Z, Miller W, Lipman DJ: Gapped BLAST and PSI-BLAST: a new generation of protein database search programs. Nucleic Acid Res 1997 25:3389-3402";
		resCitation = engine.processRawReference(citation5, false);
		assertNotNull(resCitation);	
		if (resCitation != null) {
			assertNotNull(resCitation.getNormalizedPublicationDate());	
			assertThat(resCitation.getNormalizedPublicationDate().getYear(), 
				is(1997));
			assertNotNull(resCitation.getFullAuthors());
		}
				
	}
}