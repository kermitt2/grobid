# frozen_string_literal: true

module PragmaticSegmenter
  # This is an opinionated class that removes errant newlines,
  # xhtml, inline formatting, etc.
  class Cleaner
    module Rules
      # Rubular: http://rubular.com/r/V57WnM9Zut
      NewLineInMiddleOfWordRule = Rule.new(/\n(?=[a-zA-Z]{1,2}\n)/, '')

      # Rubular: http://rubular.com/r/dMxp5MixFS
      DoubleNewLineWithSpaceRule = Rule.new(/\n \n/, "\r")

      # Rubular: http://rubular.com/r/H6HOJeA8bq
      DoubleNewLineRule = Rule.new(/\n\n/, "\r")

      # Rubular: http://rubular.com/r/FseyMiiYFT
      NewLineFollowedByPeriodRule = Rule.new(/\n(?=\.(\s|\n))/, '')


      ReplaceNewlineWithCarriageReturnRule = Rule.new(/\n/, "\r")

      EscapedNewLineRule = Rule.new(/\\n/, "\n")
      EscapedCarriageReturnRule = Rule.new(/\\r/, "\r")

      TypoEscapedNewLineRule = Rule.new(/\\\ n/, "\n")

      TypoEscapedCarriageReturnRule = Rule.new(/\\\ r/, "\r")




      # Rubular: http://rubular.com/r/bAJrhyLNeZ
      InlineFormattingRule = Rule.new(/\{b\^&gt;\d*&lt;b\^\}|\{b\^>\d*<b\^\}/, '')

      # Rubular: http://rubular.com/r/8mc1ArOIGy
      TableOfContentsRule = Rule.new(/\.{5,}\s*\d+-*\d*/, "\r")

      # Rubular: http://rubular.com/r/DwNSuZrNtk
      ConsecutivePeriodsRule = Rule.new(/\.{5,}/, ' ')

      # Rubular: http://rubular.com/r/IQ4TPfsbd8
      ConsecutiveForwardSlashRule = Rule.new(/\/{3}/, '')


      # Rubular: http://rubular.com/r/6dt98uI76u
      NO_SPACE_BETWEEN_SENTENCES_REGEX = /(?<=[a-z])\.(?=[A-Z])/
      NoSpaceBetweenSentencesRule = Rule.new(NO_SPACE_BETWEEN_SENTENCES_REGEX, '. ')

      # Rubular: http://rubular.com/r/l6KN6rH5XE
      NO_SPACE_BETWEEN_SENTENCES_DIGIT_REGEX = /(?<=\d)\.(?=[A-Z])/
      NoSpaceBetweenSentencesDigitRule = Rule.new(NO_SPACE_BETWEEN_SENTENCES_DIGIT_REGEX, '. ')


      URL_EMAIL_KEYWORDS = ['@', 'http', '.com', 'net', 'www', '//']

      # Rubular: http://rubular.com/r/3GiRiP2IbD
      NEWLINE_IN_MIDDLE_OF_SENTENCE_REGEX = /(?<=\s)\n(?=([a-z]|\())/


      # Rubular: http://rubular.com/r/Gn18aAnLdZ
      NewLineFollowedByBulletRule = Rule.new(/\n(?=•)/, "\r")

      QuotationsFirstRule = Rule.new(/''/, '"')
      QuotationsSecondRule = Rule.new(/``/, '"')


      module HTML
        # Rubular: http://rubular.com/r/9d0OVOEJWj
        HTMLTagRule = Rule.new(/<\/?\w+((\s+\w+(\s*=\s*(?:".*?"|'.*?'|[\^'">\s]+))?)+\s*|\s*)\/?>/, '')

        # Rubular: http://rubular.com/r/XZVqMPJhea
        EscapedHTMLTagRule = Rule.new(/&lt;\/?[^gt;]*gt;/, '')

        All = [HTMLTagRule, EscapedHTMLTagRule]
      end

      module PDF
        # Rubular: http://rubular.com/r/UZAVcwqck8
        NewLineInMiddleOfSentenceRule = Rule.new(/(?<=[^\n]\s)\n(?=\S)/, '')

        # Rubular: http://rubular.com/r/eaNwGavmdo
        NewLineInMiddleOfSentenceNoSpacesRule = Rule.new(/\n(?=[a-z])/, ' ')
      end

    end
  end
end
