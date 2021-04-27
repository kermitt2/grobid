# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Arabic
      include Languages::Common

      Punctuations = ['?', '!', ':', '.', '؟', '،'].freeze
      SENTENCE_BOUNDARY_REGEX = /.*?[:\.!\?؟،]|.*?\z|.*?$/

      module Abbreviation
        ABBREVIATIONS = Set.new(['ا', 'ا. د', 'ا.د', 'ا.ش.ا', 'ا.ش.ا', 'إلخ', 'ت.ب', 'ت.ب', 'ج.ب', 'جم', 'ج.ب', 'ج.م.ع', 'ج.م.ع', 'س.ت', 'س.ت', 'سم', 'ص.ب.', 'ص.ب', 'كج.', 'كلم.', 'م', 'م.ب', 'م.ب', 'ه', 'د‪']).freeze
        PREPOSITIVE_ABBREVIATIONS = [].freeze
        NUMBER_ABBREVIATIONS = [].freeze
      end

      # Rubular: http://rubular.com/r/RX5HpdDIyv
      ReplaceColonBetweenNumbersRule = Rule.new(/(?<=\d):(?=\d)/, '♭')

      # Rubular: http://rubular.com/r/kPRgApNHUg
      ReplaceNonSentenceBoundaryCommaRule = Rule.new(/،(?=\s\S+،)/, '♬')

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = [].freeze
        private

        def scan_for_replacements(txt, am, index, character_array)
          txt.gsub!(/(?<=#{am})\./, '∯')
          txt
        end
      end
    end
  end
end
