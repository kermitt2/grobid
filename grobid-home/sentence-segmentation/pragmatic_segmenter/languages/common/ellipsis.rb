# -*- encoding : utf-8 -*-
# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Common
      # This class searches for ellipses within a string and
      # replaces the periods.

      # http://www.dailywritingtips.com/in-search-of-a-4-dot-ellipsis/
      # http://www.thepunctuationguide.com/ellipses.html

      module EllipsisRules
        # Rubular: http://rubular.com/r/i60hCK81fz
        ThreeConsecutiveRule = Rule.new(/\.\.\.(?=\s+[A-Z])/, '☏.')

        # Rubular: http://rubular.com/r/Hdqpd90owl
        FourConsecutiveRule = Rule.new(/(?<=\S)\.{3}(?=\.\s[A-Z])/, 'ƪ')

        # Rubular: http://rubular.com/r/YBG1dIHTRu
        ThreeSpaceRule = Rule.new(/(\s\.){3}\s/, '♟')

        # Rubular: http://rubular.com/r/2VvZ8wRbd8
        FourSpaceRule = Rule.new(/(?<=[a-z])(\.\s){3}\.(\z|$|\n)/, '♝')

        OtherThreePeriodRule = Rule.new(/\.\.\./, 'ƪ')

        All = [
          ThreeSpaceRule,
          FourSpaceRule,
          FourConsecutiveRule,
          ThreeConsecutiveRule,
          OtherThreePeriodRule
        ]
      end
    end
  end
end
