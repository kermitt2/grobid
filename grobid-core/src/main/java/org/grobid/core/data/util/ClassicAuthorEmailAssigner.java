package org.grobid.core.data.util;

import org.grobid.core.data.Person;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * User: zholudev
 * Date: 10/8/14
 */
public class ClassicAuthorEmailAssigner implements  AuthorEmailAssigner{

    @Override
    public void assign(List<Person> fullAuthors, List<String> emailles) {
        List<Integer> winners = new ArrayList<Integer>();

        for (String maille : emailles) {
            maille = maille.replace("{", "");
            maille = maille.replace("}", "");
            maille = maille.replace("(", "");
            maille = maille.replace(")", "").trim();
            maille = maille.replaceAll("(E|e)lectronic(\\s)(A|a)ddress(\\:)?", "");
            maille = maille.replaceAll("^(e|E)?(\\-)?mail(\\:)?(\\s)(A|a)ddress(\\:)?", "");
            maille = maille.replaceAll("^(e|E)?(\\-)?mail(\\:)?(\\s)?", "");
            // case: Peter Pan -peter.pan@email.org with asterisks and spaces
            maille = maille.replaceAll("^[A-Z][a-z]+\\s+[A-Z][a-z]+(\\*)?(\\s)*-(\\s)*", "");
            StringTokenizer st = new StringTokenizer(maille, ", ");
            ArrayList<String> emails = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.length() > 2) {
                    emails.add(token);
                }
            }

            int i = 0;
            for (String token : emails) {
                if (!token.contains("@")) {
                    // the domain information is missing, we are taking the first one of the next tokens
                    String newToken = null;
                    int j = 0;
                    for (String token2 : emails) {
                        if (j <= i) {
                            j++;
                        } else {
                            int ind = token2.indexOf("@");
                            if (ind != -1) {
                                newToken = token + token2.substring(ind, token2.length());
                                break;
                            }
                            j++;
                        }
                    }
                    if (newToken != null) {
                        emails.set(i, newToken);
                    }
                }
                i++;
            }


            // if 1 email and 1 author, not too hard...
            if (fullAuthors != null) {
                if ((emails.size() == 1) && (fullAuthors.size() == 1)) {
                    fullAuthors.get(0).setEmail(emails.get(0));
                } else {
                    // we asociate emails to the authors based on string proximity
                    for (String mail : emails) {
                        int maxDist = 1000;
                        int best = -1;
                        int ind = mail.indexOf("@");
                        if (ind != -1) {
                            String nam = mail.substring(0, ind).toLowerCase();
                            int k = 0;
                            for (Person aut : fullAuthors) {
                                Integer kk = k;
                                if (!winners.contains(kk)) {
                                    List<String> emailVariants = TextUtilities.generateEmailVariants(aut.getFirstName(), aut.getLastName());

                                    for (String variant: emailVariants) {
                                        variant = variant.toLowerCase();

                                        int dist = TextUtilities.getLevenshteinDistance(nam, variant);
                                        if (dist < maxDist) {
                                            best = k;
                                            maxDist = dist;
                                        }
                                    }
                                }
                                k++;
                            }

                            // make sure that the best candidate found is not too far
                            if (best != -1 && maxDist<nam.length()/2) {
                                Person winner = fullAuthors.get(best);
                                winner.setEmail(mail);
                                winners.add(best);
                            }
                        }
                    }
                }
            }
        }

    }
}
