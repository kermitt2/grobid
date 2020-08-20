#! /usr/local/bin/ruby -KU

#if $KCODE != 'UTF8'
#  raise "$KCODE must be UTF8"
#end

HEAD=<<EOS
/*
 * UnicodeData
 * Copyright 1999, 2004, 2010, 2012 by yoshidam
 *
 */

#ifndef _UNIDATA_MAP
#define _UNIDATA_MAP

EOS

HEAD1=<<EOS

enum GeneralCategory {
  /* Letter */
  c_Lu = 1, c_Ll, c_Lt, c_LC, c_Lm, c_Lo,
  /* Mark */
  c_Mn, c_Mc, c_Me,
  /* Number */
  c_Nd, c_Nl, c_No,
  /* Punctuation */
  c_Pc, c_Pd, c_Ps, c_Pe, c_Pi, c_Pf, c_Po,
  /* Symbol */
  c_Sm, c_Sc, c_Sk, c_So,
  /* Separator */
  c_Zs, c_Zl, c_Zp,
  /* Other */
  c_Cc, c_Cf, c_Cs, c_Co, c_Cn
};

const char* const gencat_abbr[] = {
  "", /* 0 */
  /* Letter */
  "Lu", "Ll", "Lt", "LC", "Lm", "Lo",
  /* Mark */
  "Mn", "Mc", "Me",
  /* Number */
  "Nd", "Nl", "No",
  /* Punctuation */
  "Pc", "Pd", "Ps", "Pe", "Pi", "Pf", "Po",
  /* Symbol */
  "Sm", "Sc", "Sk", "So",
  /* Separator */
  "Zs", "Zl", "Zp",
  /* Other */
  "Cc", "Cf", "Cs", "Co", "Cn"
};

const char* const gencat_long[] = {
  "",
  "Uppercase_Letter",
  "Lowercase_Letter",
  "Titlecase_Letter",
  "Cased_Letter",
  "Modifier_Letter",
  "Other_Letter",
  "Nonspacing_Mark",
  "Spacing_Mark",
  "Enclosing_Mark",
  "Decimal_Number",
  "Letter_Number",
  "Other_Number",
  "Connector_Punctuation",
  "Dash_Punctuation",
  "Open_Punctuation",
  "Close_Punctuation",
  "Initial_Punctuation",
  "Final_Punctuation",
  "Other_Punctuation",
  "Math_Symbol",
  "Currency_Symbol",
  "Modifier_Symbol",
  "Other_Symbol",
  "Space_Separator",
  "Line_Separator",
  "Paragraph_Separator",
  "Control",
  "Format",
  "Surrogate",
  "Private_Use",
  "Unassigned"
};

enum EastAsianWidth {
  w_N = 1, w_A, w_H, w_W, w_F, w_Na
};

struct unicode_data {
  const int code;
  const char* const canon;
  const char* const compat;
  const char* const uppercase;
  const char* const lowercase;
  const char* const titlecase;
  const unsigned char combining_class;
  const unsigned char exclusion;
  const unsigned char general_category;
  const unsigned char east_asian_width;
};

static const struct unicode_data unidata[] = {
EOS

TAIL=<<EOS
};

#endif
EOS

def hex2str(hex)
  if hex.nil? || hex == ''
    return [nil, nil]
  end
  canon = ""
  compat = ""
  chars = hex.split(" ")
  if chars[0] =~ /^[0-9A-F]{4,6}$/
    chars.each do |c|
      canon << [c.hex].pack("U")
    end
    compat = canon
  elsif chars[0] =~ /^<.+>$/
    chars.shift
    chars.each do |c|
      compat << [c.hex].pack("U")
    end
    canon = nil
  else
    raise "unknown value: " + hex
  end
  [canon, compat]
end

def hex_or_nil(str)
  return nil if str.nil? || str == ''
  ret = ""
  chars = str.split(" ")
  chars.each do |c|
    ret << [c.hex].pack("U")
  end
  return ret
end

def printstr(str)
  return "NULL" if !str
  ret = ""
  str.each_byte do |c|
    if c >= 32 && c < 127 && c != 34 && c != 92
      ret << c
    else
      ret << format("\\%03o", c)
    end
  end
  return '"' + ret + '"'
end

if ARGV.length != 4
  puts "Usage: #{$0} <UnicodeData.txt> <DerivedNormalizationProps.txt> <SpecialCasing.txt> <EastAsianWidth.txt>"
  exit 0
end

## scan Composition Exclusions
exclusion = {}
open(ARGV[1]) do |f|
  while l = f.gets
    next if l =~ /^\#/ || l =~ /^$/
    next if l !~ /Full_Composition_Exclusion/
    code, = l.split(/\s/)
    if code =~ /^[0-9A-F]+$/
      code = code.hex
      exclusion[code] = true
    elsif code =~ /^([0-9A-F]+)\.\.([0-9A-F]+)$/
#      p [$1, $2]
      scode = $1.hex
      ecode = $2.hex
      for code in scode..ecode
        exclusion[code] = true
      end
    end
  end
end

## scan Special Casing
casing = {}
open(ARGV[2]) do |f|
  while l = f.gets
    l.chomp!
    next if l =~ /^\#/ || l =~ /^$/
    l =~ /^(.*)#\s*(.*)$/
    l = $1
    comment = $2
    code,lower,title,upper,cond = l.split(/;\s/)
    next if cond
    lower = nil if code == lower
    title = nil if code == title
    upper = nil if code == upper
    code = code.hex
    casing[code] = [hex_or_nil(lower), hex_or_nil(title), hex_or_nil(upper)]
  end
end

## scan UnicodeData
udata = {}
range_data = []
open(ARGV[0]) do |f|
  while l = f.gets
    l.chomp!
    code, charname, gencat, ccclass, bidicat,decomp,
      dec, digit, num, mirror, uni1_0, comment, upcase,
      lowcase, titlecase = l.split(";", 15);
    code = code.hex
    ccclass = ccclass.to_i
    canon, compat = hex2str(decomp)
    upcase = hex_or_nil(upcase)
    lowcase = hex_or_nil(lowcase)
    titlecase = hex_or_nil(titlecase)
    udata[code] = [ccclass, canon, compat, upcase, lowcase, titlecase, gencat]
    if charname =~ /^<(.*, (First|Last))>$/
      charname = $1.upcase.gsub(/,? /, '_')
      range_data << [charname, code]
    end
  end
end

## scan EastAsianWidth
ea_width = {}
open(ARGV[3]) do |f|
  while l = f.gets
    l.chomp!
    next if l =~ /^\#/ || l =~ /^$/
    l =~ /^(.*)\s+#\s*(.*)$/
    l = $1
    comment = $2
    code,width = l.split(/;/)
    if code =~ /\.\./
      start_code, end_code = code.split('..')
      start_code = start_code.hex
      end_code = end_code.hex
      (start_code..end_code).each do |code|
        ea_width[code] = width
      end
      next
    end
    code = code.hex
    ea_width[code] = width
  end
end

print HEAD
range_data.each do |charname, code|
  printf("#define %s\t(0x%04x)\n", charname, code)
end

print HEAD1
udata.sort.each do |code, data|
  ccclass, canon, compat, upcase, lowcase, titlecase, gencat = data
  ## Exclusions
  ex = 0
  if exclusion[code]  ## Script-specifics or Post Composition Version
    ex = 1
  elsif canon =~ /^.$/ ## Singltons
    ex = 2
  elsif !canon.nil?
    starter = canon.unpack("U*")[0]
    if udata[starter][0] != 0 ## Non-stater decompositions
      ex = 3
    end
  end
  ## Special Casing
  if casing[code]
    lowcase = casing[code][0] if casing[code][0]
    titlecase = casing[code][1] if casing[code][1]
    upcase = casing[code][2] if casing[code][2]
  end
  width = 'N'
  if ea_width[code]
    width = ea_width[code]
  end

  printf("  { 0x%04x, %s, %s, %s, %s, %s, %d, %d, c_%s, w_%s }, \n",
         code, printstr(canon),
         printstr(compat), printstr(upcase), printstr(lowcase),
         printstr(titlecase), ccclass, ex, gencat, width)
end
printf("  { -1, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0 }\n")
print TAIL
