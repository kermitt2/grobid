require 'rake/clean'
require 'rake/extensiontask'
require 'rubygems/package_task'

CLEAN << 'pkg' << 'tmp' << 'lib/unicode'

UPSTREAM_URL = 'http://www.yoshidam.net/unicode-%s.tar.gz'

gem_spec = eval(File.read(File.expand_path('../unicode.gemspec', __FILE__)))

gem_task = Gem::PackageTask.new(gem_spec) {|pkg|}

Rake::ExtensionTask.new('unicode_native', gem_spec) do |ext|
  ext.cross_compile = true
  ext.cross_platform = ['x86-mingw32', 'x86-mswin32-60']
  ext.ext_dir = 'ext/unicode'
  ext.lib_dir = 'lib/unicode'
end

namespace :gem do

  desc 'Build all gem files'
  task all: %w[clean gem gem:java gem:windows]

  java_gem_spec = gem_spec.dup
  java_gem_spec.platform = 'java'
  java_gem_spec.extensions.clear
  java_gem_spec.files.delete_if { |f| f.start_with?('ext/') }

  directory java_gem_dir = gem_task.package_dir

  java_gem_file = File.basename(java_gem_spec.cache_file)
  java_gem_path = File.join(java_gem_dir, java_gem_file)

  desc "Build the gem file #{java_gem_file}"
  task java: java_gem_path

  file java_gem_path => [java_gem_dir] + java_gem_spec.files do
    lib_file = 'lib/unicode.rb'
    tmp_file = "#{lib_file}.tmp-#{$$}"
    jrb_file = lib_file.sub('.', '-java.')

    begin
      mv lib_file, tmp_file
      ln jrb_file, lib_file

      Gem::Package.build(java_gem_spec)

      mv java_gem_file, java_gem_dir
    ensure
      mv tmp_file, lib_file if File.exist?(tmp_file)
    end
  end

  desc 'Build native gems for Windows'
  task :windows do
    ENV['RUBY_CC_VERSION'] = '1.9.3:2.0.0:2.1.5:2.2.2:2.3.0'
    sh 'rake cross compile'
    sh 'rake cross native gem'
  end

end

desc 'Update from upstream'
task :update, [:version] do |t, args|
  require 'zlib'
  require 'open-uri'
  require 'archive/tar/minitar'

  unless version = args.version || ENV['UPSTREAM_VERSION']
    abort "Please specify UPSTREAM_VERSION. See #{gem_spec.homepage}."
  end

  io = begin
    open(url = UPSTREAM_URL % version)
  rescue OpenURI::HTTPError
    abort "Upstream version not found: #{url}. See #{gem_spec.homepage}."
  end

  Archive::Tar::Minitar.open(Zlib::GzipReader.new(io)) { |tar|
    basedir = File.expand_path('..', __FILE__)

    extract = lambda { |entry, name, dir|
      puts "Extracting `#{name}' to `#{dir || '.'}'..."
      tar.extract_entry(dir ? File.join(basedir, dir) : basedir, entry)
    }

    tar.each { |entry|
      entry.name.sub!(/\Aunicode\//, '')

      case name = entry.full_name
        when /\Atools\/|\.gemspec\z/, 'README'
          extract[entry, name, nil]
        when /\.(?:[ch]|map)\z/, 'extconf.rb'
          extract[entry, name, 'ext/unicode']
        when /\Atest/
          extract[entry, name, 'test']
        else
          puts "Skipping `#{name}'..."
      end
    }
  }
end
