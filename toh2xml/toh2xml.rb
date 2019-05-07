#!/usr/bin/env ruby
class Toh2xml      
    attr_reader :input_file_name, :lines_in, :output_file_name, :lines_out

  def self.convert(csv_file_name)
    new.convert(csv_file_name)
  end

  def initialize
    @map = Hash.new
  end  

  def convert(csv_file_name)
    process_csv_file(csv_file_name)
    @map = @map.sort_by { |key, val| key.to_i }.to_h
    write_xml_file(csv_file_name.sub(/.csv$/, '.xml'))
    self
  end

  private
  
  def process_csv_file(csv_file_name)
    @input_file_name = csv_file_name
    @lines_in = 0

    File.open(csv_file_name).each_line do |line|
      process_csv_line line
    end
    self
  end

  def write_xml_file(xml_file_name)
    @output_file_name = xml_file_name

    File.open(xml_file_name, 'w') do |out|
      out << <<~XML
        <?xml version='1.0' encoding='utf-8'?>
        <lod>
      XML
      @lines_out = 3;
      @map.each do |key, val|
        out << "    <text key='toh#{key}'>\n" \
               "        <idno type='bdrc' uri='#{val}'/>\n" \
               "    </text>\n"
        @lines_out += 3
      end
      out << "</lod>\n"
      @lines_out += 1
    end
    self
  end

  def process_csv_line(line)
    @lines_in += 1

    vals = line.strip!.split(',')
    raise ArgumentError, "line #{@lines_in}: unexpected line format: '#{line}'" if vals.size != 2
    
    key, val = vals.map { |val| val.gsub(/^\s*"|"\s*$/, '') }

    raise ArgumentError, "line #{@lines_in}: duplicate key: #{line}" if @map.has_key? key
    raise ArgumentError, "line #{@lines_in}: duplicate value: #{line}" if @map.has_value? val

    @map[key] = val if [key, val] != ['toh', 'id']
  end 
end


if __FILE__ == $PROGRAM_NAME
  puts "ruby #{RUBY_VERSION}p#{RUBY_PATCHLEVEL} running"
  puts "command line: #{$PROGRAM_NAME} #{ARGV.join}"
  conversion = Toh2xml.convert ARGV[0]
  puts "#{conversion.lines_in} lines read from file #{conversion.input_file_name}"
  puts "#{conversion.lines_out} lines written to file #{conversion.output_file_name}"
end
