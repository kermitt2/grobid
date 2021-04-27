# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Deutsch
      include Languages::Common

      module Abbreviation
        ABBREVIATIONS = Set.new(['Ä', 'ä', 'adj', 'adm', 'adv', 'art', 'asst', 'b.a', 'b.s', 'bart', 'bldg', 'brig', 'bros', 'bse', 'buchst', 'bzgl', 'bzw', 'c.-à-d', 'ca', 'capt', 'chr', 'cmdr', 'co', 'col', 'comdr', 'con', 'corp', 'cpl', 'd.h', 'd.j', 'dergl', 'dgl', 'dkr', 'dr ', 'ens', 'etc', 'ev ', 'evtl', 'ff', 'g.g.a', 'g.u', 'gen', 'ggf', 'gov', 'hon', 'hosp', 'i.f', 'i.h.v', 'ii', 'iii', 'insp', 'iv', 'ix', 'jun', 'k.o', 'kath ', 'lfd', 'lt', 'ltd', 'm.e', 'maj', 'med', 'messrs', 'mio', 'mlle', 'mm', 'mme', 'mr', 'mrd', 'mrs', 'ms', 'msgr', 'mwst', 'no', 'nos', 'nr', 'o.ä', 'op', 'ord', 'pfc', 'ph', 'pp', 'prof', 'pvt', 'rep', 'reps', 'res', 'rev', 'rt', 's.p.a', 'sa', 'sen', 'sens', 'sfc', 'sgt', 'sog', 'sogen', 'spp', 'sr', 'st', 'std', 'str  ', 'supt', 'surg', 'u.a  ', 'u.e', 'u.s.w', 'u.u', 'u.ä', 'usf', 'usw', 'v', 'vgl', 'vi', 'vii', 'viii', 'vs', 'x', 'xi', 'xii', 'xiii', 'xiv', 'xix', 'xv', 'xvi', 'xvii', 'xviii', 'xx', 'z.b', 'z.t', 'z.z', 'z.zt', 'zt', 'zzt', 'univ.-prof', 'o.univ.-prof', 'ao.univ.prof', 'ass.prof', 'hon.prof', 'univ.-doz', 'univ.ass', 'stud.ass', 'projektass', 'ass', 'di', 'dipl.-ing', 'mag']).freeze
        NUMBER_ABBREVIATIONS = Set.new(['art', 'ca', 'no', 'nos', 'nr', 'pp']).freeze
        PREPOSITIVE_ABBREVIATIONS = [].freeze
      end

      # Rubular: http://rubular.com/r/OdcXBsub0w
      BETWEEN_UNCONVENTIONAL_DOUBLE_QUOTE_DE_REGEX = /,,(?>[^“\\]+|\\{2}|\\.)*“/

      # Rubular: http://rubular.com/r/2UskIupGgP
      SPLIT_DOUBLE_QUOTES_DE_REGEX = /\A„(?>[^“\\]+|\\{2}|\\.)*“/

      # Rubular: http://rubular.com/r/TkZomF9tTM
      BETWEEN_DOUBLE_QUOTES_DE_REGEX = /„(?>[^“\\]+|\\{2}|\\.)*“/


      module Numbers
        # Rubular: http://rubular.com/r/hZxoyQwKT1
        NumberPeriodSpaceRule = Rule.new(/(?<=\s[0-9]|\s([1-9][0-9]))\.(?=\s)/, '∯')

        # Rubular: http://rubular.com/r/ityNMwdghj
        NegativeNumberPeriodSpaceRule = Rule.new(/(?<=-[0-9]|-([1-9][0-9]))\.(?=\s)/, '∯')

        All = [
          Common::Numbers::All,
          NumberPeriodSpaceRule,
          NegativeNumberPeriodSpaceRule
        ]
      end

      MONTHS = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'].freeze

      # Rubular: http://rubular.com/r/B4X33QKIL8
      SingleLowerCaseLetterRule = Rule.new(/(?<=\s[a-z])\.(?=\s)/, '∯')

      # Rubular: http://rubular.com/r/iUNSkCuso0
      SingleLowerCaseLetterAtStartOfLineRule = Rule.new(/(?<=^[a-z])\.(?=\s)/, '∯')

      class Processor < PragmaticSegmenter::Processor
        private

        def replace_numbers
          @text.apply Numbers::All

          replace_period_in_deutsch_dates
        end

        def replace_period_in_deutsch_dates
          MONTHS.each do |month|
            # Rubular: http://rubular.com/r/zlqgj7G5dA
            @text.gsub!(/(?<=\d)\.(?=\s*#{Regexp.escape(month)})/, '∯')
          end
        end
      end

      class AbbreviationReplacer < AbbreviationReplacer

        SENTENCE_STARTERS = %w(
          Am Auch Auf Bei Da Das Der Die Ein Eine Es Für Heute Ich Im In
          Ist Jetzt Mein Mit Nach So Und Warum Was Wenn Wer Wie Wir
        ).freeze

        def replace
          @text = text.apply(
            @language::PossessiveAbbreviationRule,
            @language::SingleLetterAbbreviationRules::All,
            SingleLowerCaseLetterRule,
            SingleLowerCaseLetterAtStartOfLineRule)

          @text = search_for_abbreviations_in_string(@text)
          @text = replace_multi_period_abbreviations(@text)
          @text.apply(Languages::Common::AmPmRules::All)
          replace_abbreviation_as_sentence_boundary(@text)
        end

        private

        def scan_for_replacements(txt, am, index, character_array)
          txt.gsub!(/(?<=#{am})\.(?=\s)/, '∯')
          txt
        end
      end

      class BetweenPunctuation < PragmaticSegmenter::BetweenPunctuation
        private

        def btwn_dbl_quote(txt)
          if txt.include?('„')
            btwn_dbl_quote = txt.scan(BETWEEN_DOUBLE_QUOTES_DE_REGEX)
            txt.scan(SPLIT_DOUBLE_QUOTES_DE_REGEX).each do |q|
              btwn_dbl_quote << q
            end
          elsif txt.include?(',,')
            btwn_dbl_quote = txt.scan(BETWEEN_UNCONVENTIONAL_DOUBLE_QUOTE_DE_REGEX)
          end
          btwn_dbl_quote
        end
      end
    end
  end
end
