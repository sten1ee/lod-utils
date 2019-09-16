#!/usr/bin/env ruby

require 'rexml/document'

# A class to process all *.tei files in a dir
class TeiParser
  include REXML

  OUT = STDOUT

  def initialize(filter:nil)
    @filter = filter
  end

  def process_dir(dir:, entity:)
    parse_method = method("parse_" + entity.to_s)
    report_method = method("report_" + entity.to_s)

    Dir.glob('*.xml', base: dir) do |tei_file|
      parse_method.call("#{dir}/#{tei_file}")
    end

    report_method.call
  end

  def parse_gloss(tei_file)
    doc = Document.new File.new tei_file
    doc.elements.each('//gloss') do |elm|
      text = elm.to_s
      if @filter === text
        OUT << "<!-- #{tei_file} -->\n" << text << "\n\n"
      end

      @type_count ||= Hash.new(0)
      @id_count ||= Hash.new(0)
  
      type = elm.attributes['type']
      @type_count[type] += 1

      id = elm.attributes['xml:id']
      cnt = @id_count[id] += 1
      raise(ArgumentError, "Duplicate xml:id: #{id} !") if cnt != 1
    end
  end

  def report_gloss
    @type_count = @type_count.sort_by { |_type, count| -count }.to_h
    OUT << @type_count << "\n"
  end

  def parse_ref(tei_file)
    doc = Document.new File.new tei_file
    doc.elements.each('//ref') do |elm|
      text = elm.to_s
      if @filter === text
        OUT << "<!-- #{tei_file} -->\n" << text << "\n\n"
      end

      @ref_attr ||= Hash.new { |h, k| h[k] = Set.new }
      elm.attributes.each { |attr, val| @ref_attr[attr] << val }

      if (cref = elm.attributes['cRef'])
        @cref_map ||= Hash.new { |h, k| h[k] = Array.new }
        @cref_map[cref] << tei_file
      end
    end
  end

  def report_ref
    <<~COMMENT
    @cref_map
      .filter { |cref, occurances| occurances.size > 1 }
      .sort_by { |cref, occurances| cref }
      .each do |cref, occurances|
        OUT << cref << " =>\n"
        occurances.each do |tei_file|
          OUT << "\t" << tei_file.to_s.split('/').last << "\n"
        end
        OUT << "\n"
      end
    COMMENT

    @ref_attr.each do |attr, values|
      next if attr == 'cRef' || attr == 'key'
      OUT << attr << " =>\n"
      values.each do |value|
        OUT << "\t" << value << "\n"
      end
      OUT << "\n"
    end
  end
end

# doc = XmlSimple.xml_in('061-013_toh193,739_prophecy_of_shri_mahadevi.xml')

if __FILE__ == $PROGRAM_NAME
  puts "ruby #{RUBY_VERSION}p#{RUBY_PATCHLEVEL} running"
  puts "command line: #{$PROGRAM_NAME} #{ARGV.join}"
  raise ArgumentError, 'Too many command line args!' if ARGV.size > 1
  if ARGV.size < 1 || ARGV[0].empty?
    STDERR << 'Missing command line arg for report filter! Assuming match all.'
    filter = '.*'
  else
    filter = ARGV[0]
    filter = '.*' if (filter.empty? || filter == '*')
  end

  parser = TeiParser
             .new(filter: Regexp.new(filter, Regexp::IGNORECASE)) #/[Bb]hagav[āa][nt]/  #/[Pp][āa][ṇn][ḍd]ava/
             .process_dir(dir: 'translations', entity: :ref)
end
