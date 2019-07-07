for $i in //Illustration
return string-join(($i/ancestor::PAGE/@number, $i/@HPOS, $i/@VPOS, $i/@WIDTH, $i/@HEIGHT), ',')