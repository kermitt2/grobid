
for $g  in //GROUP return

  let $x1 := min(($g//*/@x, $g//*/@x1, $g//*/@x2, $g//*/@x3))
  let $y1 := min(($g/*/@y, $g//*/@y1, $g//*/@y2, $g//*/@y3))
  let $x2 := max(($g/*/@x, $g//*/@x1, $g//*/@x2, $g//*/@x3))
  let $y2 := max(($g/*/@y, $g//*/@y1, $g//*/@y2, $g//*/@y3))
  return concat($x1, ",", $y1, ",", $x2 - $x1, ",", $y2 - $y1)