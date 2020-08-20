# -*- encoding : utf-8 -*-
# frozen_string_literal: true

module PragmaticSegmenter
  # This class searches for punctuation between quotes or parenthesis
  # and replaces it
  class BetweenPunctuation
    # Rubular: http://rubular.com/r/2YFrKWQUYi
    BETWEEN_SINGLE_QUOTES_REGEX = /(?<=\s)'(?:[^']|'[a-zA-Z])*'/

    BETWEEN_SINGLE_QUOTE_SLANTED_REGEX = /(?<=\s)‘(?:[^’]|’[a-zA-Z])*’/

    # Rubular: http://rubular.com/r/3Pw1QlXOjd
    BETWEEN_DOUBLE_QUOTES_REGEX = /"(?>[^"\\]+|\\{2}|\\.)*"/

    # Rubular: http://rubular.com/r/x6s4PZK8jc
    BETWEEN_QUOTE_ARROW_REGEX = /«(?>[^»\\]+|\\{2}|\\.)*»/

    # Rubular: http://rubular.com/r/JbAIpKdlSq
    BETWEEN_QUOTE_SLANTED_REGEX = /“(?>[^”\\]+|\\{2}|\\.)*”/

    # Rubular: http://rubular.com/r/WX4AvnZvlX
    BETWEEN_SQUARE_BRACKETS_REGEX = /\[(?>[^\]\\]+|\\{2}|\\.)*\]/

    # Rubular: http://rubular.com/r/6tTityPflI
    BETWEEN_PARENS_REGEX = /\((?>[^\(\)\\]+|\\{2}|\\.)*\)/

    # Rubular: http://rubular.com/r/mXf8cW025o
    WORD_WITH_LEADING_APOSTROPHE = /(?<=\s)'(?:[^']|'[a-zA-Z])*'\S/

    # Rubular: http://rubular.com/r/jTtDKfjxzr
    BETWEEN_EM_DASHES_REGEX = /\-\-(?>[^\-\-])*\-\-/

    attr_reader :text
    def initialize(text:)
      @text = text
    end

    def replace
      sub_punctuation_between_quotes_and_parens(text)
    end

    private

    def sub_punctuation_between_quotes_and_parens(txt)
      sub_punctuation_between_single_quotes(txt)
      sub_punctuation_between_single_quote_slanted(txt)
      sub_punctuation_between_double_quotes(txt)
      sub_punctuation_between_square_brackets(txt)
      sub_punctuation_between_parens(txt)
      sub_punctuation_between_quotes_arrow(txt)
      sub_punctuation_between_em_dashes(txt)
      sub_punctuation_between_quotes_slanted(txt)
    end

    def sub_punctuation_between_parens(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: txt.scan(BETWEEN_PARENS_REGEX),
        text: txt
      ).replace
    end

    def sub_punctuation_between_square_brackets(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: txt.scan(BETWEEN_SQUARE_BRACKETS_REGEX),
        text: txt
      ).replace
    end

    def sub_punctuation_between_single_quotes(txt)
      unless !(txt !~ WORD_WITH_LEADING_APOSTROPHE) && txt !~ /'\s/
        PragmaticSegmenter::PunctuationReplacer.new(
          matches_array: txt.scan(BETWEEN_SINGLE_QUOTES_REGEX),
          text: txt,
          match_type: 'single'
        ).replace
      end
    end

    def sub_punctuation_between_single_quote_slanted(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: txt.scan(BETWEEN_SINGLE_QUOTE_SLANTED_REGEX),
        text: txt
      ).replace
    end

    def sub_punctuation_between_double_quotes(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: btwn_dbl_quote(txt),
        text: txt
      ).replace
    end

    def btwn_dbl_quote(txt)
      txt.scan(BETWEEN_DOUBLE_QUOTES_REGEX)
    end

    def sub_punctuation_between_quotes_arrow(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: txt.scan(BETWEEN_QUOTE_ARROW_REGEX),
        text: txt
      ).replace
    end

    def sub_punctuation_between_em_dashes(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: txt.scan(BETWEEN_EM_DASHES_REGEX),
        text: txt
      ).replace
    end

    def sub_punctuation_between_quotes_slanted(txt)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: txt.scan(BETWEEN_QUOTE_SLANTED_REGEX),
        text: txt
      ).replace
    end
  end
end
