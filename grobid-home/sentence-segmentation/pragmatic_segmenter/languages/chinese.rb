# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Chinese
      include Languages::Common

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = [].freeze
      end

      class BetweenPunctuation < PragmaticSegmenter::BetweenPunctuation
        BETWEEN_DOUBLE_ANGLE_QUOTATION_MARK_REGEX = /《(?>[^》\\]+|\\{2}|\\.)*》/
        BETWEEN_L_BRACKET_REGEX = /「(?>[^」\\]+|\\{2}|\\.)*」/
        private

        def sub_punctuation_between_quotes_and_parens(txt)
          super
          sub_punctuation_between_double_angled_quotation_marks(txt)
          sub_punctuation_between_l_bracket(txt)
        end
        
        def sub_punctuation_between_double_angled_quotation_marks(txt)
          PunctuationReplacer.new(
            matches_array: txt.scan(BETWEEN_DOUBLE_ANGLE_QUOTATION_MARK_REGEX),
            text: txt
          ).replace
        end
        
        def sub_punctuation_between_l_bracket(txt)
          PunctuationReplacer.new(
            matches_array: txt.scan(BETWEEN_L_BRACKET_REGEX),
            text: txt
          ).replace
        end
      end
    end
  end
end
