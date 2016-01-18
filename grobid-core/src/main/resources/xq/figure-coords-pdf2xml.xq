for $i in //IMAGE
return string-join(($i/ancestor::PAGE/@number, $i/@x, $i/@y, $i/@width, $i/@height), ',')