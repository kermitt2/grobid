CREATE DATABASE IF NOT EXISTS crossref DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_bin;

USE crossref;

CREATE TABLE IF NOT EXISTS AuthorTitle(
                Author  varchar(500) CHARACTER SET utf8,
                Title   varchar(500) CHARACTER SET utf8,
                Unixref TEXT CHARACTER SET utf8
);
CREATE INDEX Author_index ON AuthorTitle(Author);
CREATE INDEX Title_index ON AuthorTitle(Title);

CREATE TABLE IF NOT EXISTS AllSubFields(
                Request  varchar(1000) CHARACTER SET utf8,
                Unixref TEXT CHARACTER SET utf8
);
CREATE INDEX request_index ON AllSubFields(Request);

CREATE TABLE IF NOT EXISTS DOIRequest(
                DOI  varchar(1000) CHARACTER SET utf8,
                Unixref TEXT CHARACTER SET utf8
);
CREATE INDEX request_index ON DOIRequest(DOI);
