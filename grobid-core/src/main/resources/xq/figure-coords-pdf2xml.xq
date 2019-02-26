for $i in //Illustration
return string-join(($i/ancestor::Page/@number, $i/@HPOS, $i/@VPOS, $i/@WIDTH, $i/@HEIGHT), ',')