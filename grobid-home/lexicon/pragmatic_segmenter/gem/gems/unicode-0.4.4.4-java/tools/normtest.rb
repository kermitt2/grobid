#! /usr/local/bin/ruby -KU

## Conformance test with NormaliztionTest.txt
## Copyrigth 2010 yoshidam

require 'unicode'

TESTFILE = "NormalizationTest.txt"

def from_hex(str)
  ret = ""
  chars = str.split(" ")
  chars.each do |c|
    ret << [c.hex].pack("U")
  end
  return ret
end

def to_hex(str)
  ret = ""
  str = str.unpack('U*')
  str.each do |c|
    ret += sprintf("%04X ", c)
  end
  ret
end

open(TESTFILE) do |f|
  while l = f.gets
    next if l =~ /^#/
    l.chomp
    if l =~ /^@/
      puts l
      next
    end
    c1, c2, c3, c4, c5 = l.split(';')
    code = c1
    c1 = from_hex(c1)
    c2 = from_hex(c2)
    c3 = from_hex(c3)
    c4 = from_hex(c4)
    c5 = from_hex(c5)
    ## NFC TEST
    if c2 == Unicode.nfc(c1) && c2 == Unicode.nfc(c2) &&
        c2 == Unicode.nfc(c3) &&
        c4 == Unicode.nfc(c4) && c4 == Unicode.nfc(c4)
      ##puts "NFC OK: " + code
    else
      puts "NFC NG: " + to_hex(c1)
      printf("  c2=%s NFC(c1)=%s NFC(c2)=%s NFC(c3)=%s\n",
             to_hex(c2),
             to_hex(Unicode.nfc(c1)),
             to_hex(Unicode.nfc(c2)),
             to_hex(Unicode.nfc(c3)))
      printf("  c4=%s NFC(c4)=%s NFC(c5)=%s\n",
             to_hex(c4),
             to_hex(Unicode.nfc(c4)),
             to_hex(Unicode.nfc(c5)))
    end

    ## NFD TEST
    if c3 == Unicode.nfd(c1) && c3 == Unicode.nfd(c2) &&
        c3 == Unicode.nfd(c3) &&
        c5 == Unicode.nfd(c4) && c5 == Unicode.nfd(c5)
      ##puts "NFD OK: " + code
    else
      puts "NFD NG: " + to_hex(c1)
      printf("  c3=%s NFD(c1)=%s NFD(c2)=%s NFD(c3)=%s\n",
             to_hex(c3),
             to_hex(Unicode.nfd(c1)),
             to_hex(Unicode.nfd(c2)),
             to_hex(Unicode.nfd(c3)))
      printf("  c5=%s NFD(c4)=%s NFD(c5)=%s\n",
             to_hex(c5),
             to_hex(Unicode.nfd(c4)),
             to_hex(Unicode.nfd(c5)))
    end

    ## NFKC TEST
    if c4 == Unicode.nfkc(c1) && c4 == Unicode.nfkc(c2) &&
        c4 == Unicode.nfkc(c3) &&
        c4 == Unicode.nfkc(c4) && c4 == Unicode.nfkc(c5)
      ##puts "NFKC OK: " + code
    else
      puts "NFKC NG: " + to_hex(c1)
      printf("  c4=%s NFKC(c1)=%s NFKC(c2)=%s NFKC(c3)=%s NFKC(c4)=%s NFKC(c5)=%s\n",
             to_hex(c4),
             to_hex(Unicode.nfkc(c1)),
             to_hex(Unicode.nfkc(c2)),
             to_hex(Unicode.nfkc(c3)),
             to_hex(Unicode.nfkc(c4)),
             to_hex(Unicode.nfkc(c5)))
    end

    ## NFKD TEST
    if c5 == Unicode.nfkd(c1) && c5 == Unicode.nfkd(c2) &&
        c5 == Unicode.nfkd(c3) &&
        c5 == Unicode.nfkd(c4) && c5 == Unicode.nfkd(c5)
      ##puts "NFKD OK: " + code
    else
      puts "NFKD NG: " + to_hex(c1)
      printf("  c5=%s NFKD(c1)=%s NFKD(c2)=%s NFKD(c3)=%s NFKD(c4)=%s NFKD(c5)=%s\n",
             to_hex(c5),
             to_hex(Unicode.nfkd(c1)),
             to_hex(Unicode.nfkd(c2)),
             to_hex(Unicode.nfkd(c3)),
             to_hex(Unicode.nfkd(c4)),
             to_hex(Unicode.nfkd(c5)))
    end
  end
end
