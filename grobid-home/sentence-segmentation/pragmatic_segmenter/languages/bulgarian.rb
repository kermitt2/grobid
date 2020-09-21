# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Bulgarian
      include Languages::Common

      module Abbreviation
        ABBREVIATIONS = Set.new(["p.s", "акад", "ал", "б.р", "б.ред", "бел.а", "бел.пр", "бр", "бул", "в", "вж", "вкл", "вм", "вр", "г", "ген", "гр", "дж", "дм", "доц", "др", "ем", "заб", "зам", "инж", "к.с", "кв", "кв.м", "кг", "км", "кор", "куб", "куб.м", "л", "лв", "м", "м.г", "мин", "млн", "млрд", "мм", "н.с", "напр", "пл", "полк", "проф", "р", "рис", "с", "св", "сек", "см", "сп", "срв", "ст", "стр", "т", "т.г", "т.е", "т.н", "т.нар", "табл", "тел", "у", "ул", "фиг", "ха", "хил", "ч", "чл", "щ.д"]).freeze
        NUMBER_ABBREVIATIONS = [].freeze
        PREPOSITIVE_ABBREVIATIONS = [].freeze
      end

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = [].freeze

        private
        def replace_period_of_abbr(txt, abbr)
          txt.gsub!(/(?<=\s#{abbr.strip})\.|(?<=^#{abbr.strip})\./, '∯')
          txt
        end
      end
    end
  end
end
