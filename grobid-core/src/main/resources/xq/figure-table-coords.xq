declare default element namespace "http://www.tei-c.org/ns/1.0";

for $i in //*[local-name() eq "figure" or local-name() eq "table"][@coords] return
($i/@coords/string(), not(empty($i/table)))