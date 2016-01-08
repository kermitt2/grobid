declare default element namespace "http://www.tei-c.org/ns/1.0";
declare namespace functx = "http://www.functx.com";

declare function functx:follows-not-descendant
( $a as node()? ,
        $b as node()? )  as xs:boolean {

    $a >> $b and empty($b intersect $a/ancestor::node())
} ;

for $i in (//ref[@type = "bibr"])
return
    let $after := string-length(string-join (//*[functx:follows-not-descendant(., $i)]/text(), ''))
    let $all := string-length(string-join (//*/text(), ''))
    let $rel as xs:double  := xs:double(($all - $after) div $all)
    let $sn := $i/ancestor::div[./head][last()]/head/string(.)
    let $coords := $i/@coords/string()
    return
        (concat(string-join($i/preceding-sibling::node()/string(), ''),
                '<ref>', $i, '</ref>', string-join($i/following-sibling::node()/string(), '') ),

        (:  citation id, should not be empty due to the above query :)
        let $s := string($i/@target) return
            if (starts-with($s, '#')) then substring($s, 2) else $s,

        (: section name :)
        if (not($sn)) then "" else $sn,

        (: double position in the document :)
        $rel,

        (: coordinates :)
        if (not($coords)) then "" else $coords
)








