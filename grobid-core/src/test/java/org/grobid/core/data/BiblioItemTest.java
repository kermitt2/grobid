package org.grobid.core.data;

import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class BiblioItemTest {

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
    }

    @Test
    public void injectDOI() {
    }

    @Test
    public void correct_empty_shouldNotFail() {
        BiblioItem.correct(new BiblioItem(), new BiblioItem());
    }


    @Test
    public void correct_1author_shouldWork() {
        BiblioItem biblio1 = new BiblioItem();
        biblio1.setFullAuthors(Arrays.asList(createPerson("John", "Doe")));

        BiblioItem biblio2 = new BiblioItem();
        biblio2.setFullAuthors(Arrays.asList(createPerson("John1", "Doe")));

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));

    }

    @Test
    public void correct_2authors_shouldMatchFullName_sholdUpdateAffiliation() {
        BiblioItem biblio1 = new BiblioItem();
        biblio1.setFullAuthors(Arrays.asList(
            createPerson("John", "Doe"),
            createPerson("Jane", "Will")
        ));

        BiblioItem biblio2 = new BiblioItem();
        biblio2.setFullAuthors(Arrays.asList(
            createPerson("John", "Doe", "UCLA"),
            createPerson("Jane", "Will","Harward")
        ));

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        assertThat(biblio2.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is("UCLA"));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio2.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is("Harward"));
    }

    @Test
    public void correct_2authors_shouldMatchFullName_sholdKeepAffiliation() {
        BiblioItem biblio1 = new BiblioItem();
        biblio1.setFullAuthors(Arrays.asList(
            createPerson("John", "Doe", "Stanford"),
            createPerson("Jane", "Will", "Cambridge")
        ));

        BiblioItem biblio2 = new BiblioItem();
        biblio2.setFullAuthors(Arrays.asList(
            createPerson("John", "Doe" ),
            createPerson("Jane", "Will")
        ));

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        assertThat(biblio2.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is("Stanford"));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio2.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is("Cambridge"));
    }

    @Test
    public void correct_2authors_initial_2_shouldUpdateAuthor() {
        BiblioItem biblio1 = new BiblioItem();
        biblio1.setFullAuthors(Arrays.asList(
            createPerson("John", "Doe", "ULCA"),
            createPerson("J", "Will", "Harward")
        ));

        BiblioItem biblio2 = new BiblioItem();
        biblio2.setFullAuthors(Arrays.asList(
            createPerson("John1", "Doe", "Stanford"),
            createPerson("Jane", "Will", "Berkley")
        ));

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is(biblio2.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString()));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is(biblio2.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString()));
    }

    @Test
    @Ignore("This test is failing ")
    public void correct_2authors_initial_shouldUpdateAuthor() {
        BiblioItem biblio1 = new BiblioItem();
        biblio1.setFullAuthors(Arrays.asList(
            createPerson("John", "Doe", "ULCA"),
            createPerson("Jane", "Will", "Harward")
        ));

        BiblioItem biblio2 = new BiblioItem();
        biblio2.setFullAuthors(Arrays.asList(
            createPerson("John1", "Doe", "Stanford"),
            createPerson("J", "Will", "Berkley")
        ));

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is("UCLA"));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is("Berkley"));
    }

    private Person createPerson(String firstName, String secondName) {
        final Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(secondName);
        return person;
    }

    private Person createPerson(String firstName, String secondName, String affiliation) {
        final Person person = createPerson(firstName, secondName);
        final Affiliation affiliation1 = new Affiliation();
        affiliation1.setAffiliationString(affiliation);
        person.setAffiliations(Arrays.asList(affiliation1));
        return person;
    }
}