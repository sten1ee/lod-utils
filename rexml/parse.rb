#!/usr/bin/env ruby

require 'rexml/document'

# A class to process all *.tei files in a dir
class TeiParser
  include REXML

  GLOSSARY = STDOUT

  def initialize(filter:nil)
    @filter = filter
    @type_count = Hash.new(0)
    @id_count = Hash.new(0)
  end

  def process_dir(dir = '.')
    Dir.glob('*.xml', base: dir) do |tei_file|
      process_file "#{dir}/#{tei_file}"
    end
    self
  end

  def process_file(tei_file)
    doc = Document.new File.new tei_file
    doc.elements.each('//gloss') do |elm|
      text = elm.to_s
      GLOSSARY << "<!-- #{tei_file} -->\n" << text << "\n\n" if @filter === text

      type = elm.attributes['type']
      @type_count[type] += 1

      id = elm.attributes['xml:id']
      cnt = @id_count[id] += 1
      raise(ArgumentError, "Duplicate xml:id: #{id} !") if cnt != 1
    end
    self
  end

  def write_reports(out)
    @type_count = @type_count.sort_by { |_type, count| -count }.to_h
    out << @type_count << "\n"
  end
  self
end

# doc = XmlSimple.xml_in('061-013_toh193,739_prophecy_of_shri_mahadevi.xml')

if __FILE__ == $PROGRAM_NAME
  puts "ruby #{RUBY_VERSION}p#{RUBY_PATCHLEVEL} running"
  puts "command line: #{$PROGRAM_NAME} #{ARGV.join}"
  raise ArgumentError, 'Too many command line args!' if ARGV.size > 1
  raise ArgumentError, 'Missing command line arg for report filter! Supply "*" to match all.' if ARGV.size < 1
  filter = ARGV[0]
  filter = '.*' if filter.empty? || filter == '*'
  #raise ArgumentError, "Not a .csv file name specified - '#{ARGV[0]}'" if not ARGV[0].end_with? '.csv'

  parser = TeiParser.new(filter: Regexp.new(filter, Regexp::IGNORECASE)) #/[Bb]hagav[āa][nt]/  #/[Pp][āa][ṇn][ḍd]ava/
               .process_dir('translations')
               .write_reports STDOUT
  parser
end
