# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{unicode}
  s.version = "0.4.4.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = [%q{Yoshida Masato}]
  s.date = %q{2013-02-07}
  s.email = %q{yoshidam@yoshidam.net}
  s.licenses = %w[Ruby]
  s.extensions = %w[ext/unicode/extconf.rb]
  s.extra_rdoc_files = [%q{README}]
  s.files = %w[
    README Rakefile unicode.gemspec lib/unicode.rb
    test/test.rb tools/README tools/mkunidata.rb tools/normtest.rb
    ext/unicode/extconf.rb ext/unicode/unicode.c ext/unicode/unidata.map
    ext/unicode/ustring.c ext/unicode/ustring.h ext/unicode/wstring.c ext/unicode/wstring.h
  ]
  s.homepage = %q{http://www.yoshidam.net/Ruby.html#unicode}
  s.require_paths = [%q{lib}]
  s.rubygems_version = %q{1.8.6}
  s.summary = %q{Unicode normalization library.}
  s.description = %q{Unicode normalization library.}

  if s.respond_to? :specification_version then
    s.specification_version = 3
  end
end

