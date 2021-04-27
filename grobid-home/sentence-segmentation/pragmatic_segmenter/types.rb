# frozen_string_literal: true

module PragmaticSegmenter
  Rule = Struct.new(:pattern, :replacement)

  class Text < String
    def apply(*rules)
      rules.flatten.each do |rule|
        self.gsub!(rule.pattern, rule.replacement)
      end
      self
    end
  end
end
