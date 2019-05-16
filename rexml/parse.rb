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
      GLOSSARY << "<!-- #{tei_file} -->\n" << text << "\n\n" if @filter&. === text

      type = elm.attributes['type']
      @type_count[type] += 1

      id = elm.attributes['xml:id']
      cnt = @id_count[id] += 1
      rause ArgumentError, "Duplicate xml:id: #{id} !" if cnt != 1
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

parser = TeiParser.new(filter:/[Pp][āa][ṇn][ḍd]ava/) #/[Bb]hagav[āa][nt]/
         .process_dir('translations')
         .write_reports STDOUT
