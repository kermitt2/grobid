# frozen_string_literal: true

module PragmaticSegmenter
  class Rule < Struct.new(:pattern, :replacement)
    class << self
      def apply(str, *rules)
        rules.flatten.each do |rule|
          str.gsub!(rule.pattern, rule.replacement)
        end
        str
      end
    end
  end
end
