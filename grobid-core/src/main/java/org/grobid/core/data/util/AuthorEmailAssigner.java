package org.grobid.core.data.util;

import org.grobid.core.data.Person;

import java.util.List;

/**
 * User: zholudev
 * Date: 10/8/14
 */
public interface AuthorEmailAssigner {
    //embeds emails into authors
    //emails should be sanitized before
    public void assign(List<Person> authors, List<String> emails);
}
