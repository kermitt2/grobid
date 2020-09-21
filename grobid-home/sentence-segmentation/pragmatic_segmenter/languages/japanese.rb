# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Japanese
      include Languages::Common

      class Cleaner < PragmaticSegmenter::Cleaner
        # Rubular: http://rubular.com/r/N4kPuJgle7
        NewLineInMiddleOfWordRule = Rule.new(/(?<=の)\n(?=\S)/, '')

        def clean
          super
          remove_newline_in_middle_of_word
        end

        private

        def remove_newline_in_middle_of_word
          @text.apply NewLineInMiddleOfWordRule
        end
      end

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = [].freeze
      end

      class BetweenPunctuation < PragmaticSegmenter::BetweenPunctuation
        # Rubular: http://rubular.com/r/GnjOmry5Z2
        BETWEEN_QUOTE_JA_REGEX = /\u{300c}(?>[^\u{300c}\u{300d}\\]+|\\{2}|\\.)*\u{300d}/

        # Rubular: http://rubular.com/r/EjHcZn5ZSG
        BETWEEN_PARENS_JA_REGEX = /\u{ff08}(?>[^\u{ff08}\u{ff09}\\]+|\\{2}|\\.)*\u{ff09}/
        private

        def sub_punctuation_between_quotes_and_parens(txt)
          super
          sub_punctuation_between_parens_ja(txt)
          sub_punctuation_between_quotes_ja(txt)
        end

        def sub_punctuation_between_quotes_ja(txt)
          PunctuationReplacer.new(
            matches_array: txt.scan(BETWEEN_QUOTE_JA_REGEX),
            text: txt
          ).replace
        end

        def sub_punctuation_between_parens_ja(txt)
          PunctuationReplacer.new(
            matches_array: txt.scan(BETWEEN_PARENS_JA_REGEX),
            text: txt
          ).replace
        end
      end
    end
  end
end
