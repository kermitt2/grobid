#! /usr/local/bin/ruby -KU
# -*- coding: utf-8 -*-

require 'unicode'

## dump Unicode string
class String
  def udump
    ustr = self.unpack("U*")
    ret = []
    ustr.each do |e|
      if e.is_a?(Integer)
        ret << "U+%04X" % e
      else
        ret << e
      end
    end
    ret
  end
end


print "Canonical decomposition vs compatibility decomposition\n"
p Unicode::decompose("⑽ o\xef\xac\x83ce").udump
p Unicode::decompose_compat("⑽ o\xef\xac\x83ce")

print "Canonical equivalent vs Compatibility equivalent\n"
p Unicode::strcmp("ｶﾞ", "ガ")
p Unicode::strcmp("ガ", "ｶﾞ")
p Unicode::strcmp_compat("ｶﾞ", "ガ")

print "Decomposition/composition\n"
p Unicode::normalize_D([0x63, 0x301, 0x327].pack("U*")).udump
p Unicode::normalize_D([0x63, 0x327, 0x301].pack("U*")).udump
p Unicode::normalize_D([0x107, 0x327].pack("U*")).udump
p Unicode::normalize_D([0xe7, 0x301].pack("U*")).udump
p Unicode::normalize_C([0x63, 0x301, 0x327].pack("U*")).udump
p Unicode::normalize_C([0x63, 0x327, 0x301].pack("U*")).udump
p Unicode::normalize_C([0x107, 0x327].pack("U*")).udump
p Unicode::normalize_C([0xe7, 0x301].pack("U*")).udump

print "Kana Normalization\n"
p Unicode::normalize_D("ｶﾞガ").udump
p Unicode::normalize_C("ｶﾞガ").udump
p Unicode::normalize_KD("ｶﾞガ").udump
p Unicode::normalize_KC("ｶﾞガ").udump

print "Hangul\n"
p "요시담".udump
p Unicode::normalize_D("요시담").udump
p Unicode::normalize_C("요시담").udump

print "Composition Exclusion\n"
print "   ANGSTROM SIGN [U+212B]\n"
p Unicode::normalize_D([0x212b].pack("U")).udump
p Unicode::normalize_C([0x212b].pack("U")).udump
print "   LATIN CAPITAL LETTER A WITH RING ABOVE [U+00C5]\n"
p Unicode::normalize_D([0x00c5].pack("U")).udump
p Unicode::normalize_C([0x00c5].pack("U")).udump

print "Case conversion\n"
p Unicode::normalize_C(Unicode::upcase([0x63, 0x301, 0x327, 0xff41].pack("U*"))).udump
p Unicode::normalize_C(Unicode::downcase([0x43, 0x301, 0x327, 0xff21].pack("U*"))).udump
p Unicode::capitalize([0x1f1, 0x41, 0x61, 0xff21].pack("U*")).udump


## Local variables:
## coding: utf-8
## End:
