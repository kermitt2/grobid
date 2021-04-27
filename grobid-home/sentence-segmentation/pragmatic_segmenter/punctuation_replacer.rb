# -*- encoding : utf-8 -*-
# frozen_string_literal: true

module PragmaticSegmenter
  # This class replaces punctuation that is typically a sentence boundary
  # but in this case is not a sentence boundary.
  class PunctuationReplacer
    module Rules
      module EscapeRegexReservedCharacters
        LeftParen = Rule.new('(', '\\(')
        RightParen = Rule.new(')', '\\)')
        LeftBracket = Rule.new('[', '\\[')
        RightBracket = Rule.new(']', '\\]')
        Dash = Rule.new('-', '\\-')

        All = [ LeftParen, RightParen,
                LeftBracket, RightBracket, Dash ]
      end

      module SubEscapedRegexReservedCharacters
        SubLeftParen = Rule.new('\\(', '(')
        SubRightParen = Rule.new('\\)', ')')
        SubLeftBracket = Rule.new('\\[', '[')
        SubRightBracket = Rule.new('\\]', ']')
        SubDash = Rule.new('\\-', '-')

        All = [ SubLeftParen, SubRightParen,
                SubLeftBracket, SubRightBracket, SubDash ]
      end

    end

    attr_reader :matches_array, :text, :match_type
    def initialize(text:, matches_array:, match_type: nil)
      @text = text
      @matches_array = matches_array
      @match_type = match_type
    end

    def replace
      replace_punctuation(matches_array)
    end

    private

    def replace_punctuation(array)
      return if !array || array.empty?
      @text.apply(Rules::EscapeRegexReservedCharacters::All)
      array.each do |a|
        a.apply(Rules::EscapeRegexReservedCharacters::All)
        sub = sub_characters(a, '.', '∯')
        sub_1 = sub_characters(sub, '。', '&ᓰ&')
        sub_2 = sub_characters(sub_1, '．', '&ᓱ&')
        sub_3 = sub_characters(sub_2, '！', '&ᓳ&')
        sub_4 = sub_characters(sub_3, '!', '&ᓴ&')
        sub_5 = sub_characters(sub_4, '?', '&ᓷ&')
        sub_6 = sub_characters(sub_5, '？', '&ᓸ&')
        unless match_type.eql?('single')
          sub_7 = sub_characters(sub_6, "'", '&⎋&')
        end
      end
      @text.apply(Rules::SubEscapedRegexReservedCharacters::All)
    end

    def sub_characters(string, char_a, char_b)
      sub = string.gsub(char_a, char_b)
      @text.gsub!(/#{Regexp.escape(string)}/, sub)
      sub
    end
  end
end
