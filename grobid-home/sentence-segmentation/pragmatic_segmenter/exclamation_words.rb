# -*- encoding : utf-8 -*-
# frozen_string_literal: true

require 'pragmatic_segmenter/punctuation_replacer'

module PragmaticSegmenter
  # This class searches for exclamation points that
  # are part of words and not ending punctuation and replaces them.
  module ExclamationWords
    EXCLAMATION_WORDS = %w[!Xũ !Kung ǃʼOǃKung !Xuun !Kung-Ekoka ǃHu ǃKhung ǃKu ǃung ǃXo ǃXû ǃXung ǃXũ !Xun Yahoo! Y!J Yum!].freeze
    REGEXP            = Regexp.new(EXCLAMATION_WORDS.map { |string| Regexp.escape(string) }.join('|'))

    def self.apply_rules(text)
      PragmaticSegmenter::PunctuationReplacer.new(
        matches_array: text.scan(REGEXP),
        text: text
      ).replace
    end
  end
end
