# -*- encoding : utf-8 -*-
# frozen_string_literal: true

require_relative 'cleaner/rules'

module PragmaticSegmenter
  # This is an opinionated class that removes errant newlines,
  # xhtml, inline formatting, etc.
  class Cleaner
    include Rules

    attr_reader :text, :doc_type
    def initialize(text:, doc_type: nil, language: Languages::Common)
      @text = Text.new(text)
      @doc_type = doc_type
      @language = language
    end

    # Clean text of unwanted formatting
    #
    # Example:
    #   >> text = "This is a sentence\ncut off in the middle because pdf."
    #   >> PragmaticSegmenter::Cleaner(text: text).clean
    #   => "This is a sentence cut off in the middle because pdf."
    #
    # Arguments:
    #    text:       (String)  *required
    #    language:   (String)  *optional
    #                (two character ISO 639-1 code e.g. 'en')
    #    doc_type:   (String)  *optional
    #                (e.g. 'pdf')

    def clean
      return unless text
      remove_all_newlines
      replace_double_newlines
      replace_newlines
      replace_escaped_newlines

      @text.apply(HTML::All)

      replace_punctuation_in_brackets
      @text.apply(InlineFormattingRule)
      clean_quotations
      clean_table_of_contents
      check_for_no_space_in_between_sentences
      clean_consecutive_characters
    end

    private

    def abbreviations
      @language::Abbreviation::ABBREVIATIONS
    end

    def check_for_no_space_in_between_sentences
      words = @text.split(' ')
      words.each do |word|
        search_for_connected_sentences(word, @text, NO_SPACE_BETWEEN_SENTENCES_REGEX, NoSpaceBetweenSentencesRule)
        search_for_connected_sentences(word, @text, NO_SPACE_BETWEEN_SENTENCES_DIGIT_REGEX, NoSpaceBetweenSentencesDigitRule)
      end
      @text
    end

    def replace_punctuation_in_brackets
      @text.dup.gsub!(/\[(?:[^\]])*\]/) do |match|
        @text.gsub!(/#{Regexp.escape(match)}/, match.dup.gsub!(/\?/, '&ᓷ&')) if match.include?('?')
      end
    end

    def search_for_connected_sentences(word, txt, regex, rule)
      if word =~ regex
        unless URL_EMAIL_KEYWORDS.any? { |web| word =~ /#{web}/ }
          unless abbreviations.any? { |abbr| word =~ /#{abbr}/i }
            new_word = word.dup.apply(rule)
            txt.gsub!(/#{Regexp.escape(word)}/, new_word)
          end
        end
      end
    end

    def remove_all_newlines
      remove_newline_in_middle_of_sentence
      remove_newline_in_middle_of_word
    end

    def remove_newline_in_middle_of_sentence
      @text.gsub!(/(?:[^\.])*/) do |match|
        match.gsub(NEWLINE_IN_MIDDLE_OF_SENTENCE_REGEX, '')
      end
      @text
    end

    def remove_newline_in_middle_of_word
      @text.apply NewLineInMiddleOfWordRule
    end

    def replace_escaped_newlines
      @text.apply EscapedNewLineRule, EscapedCarriageReturnRule,
        TypoEscapedNewLineRule, TypoEscapedCarriageReturnRule
    end

    def replace_double_newlines
      @text.apply DoubleNewLineWithSpaceRule, DoubleNewLineRule
    end

    def replace_newlines
      if doc_type.eql?('pdf')
        remove_pdf_line_breaks
      else
        @text.apply NewLineFollowedByPeriodRule,
          ReplaceNewlineWithCarriageReturnRule
      end
    end

    def remove_pdf_line_breaks
      @text.apply NewLineFollowedByBulletRule,

        PDF::NewLineInMiddleOfSentenceRule,
        PDF::NewLineInMiddleOfSentenceNoSpacesRule
    end

    def clean_quotations
      @text.apply QuotationsFirstRule, QuotationsSecondRule
    end

    def clean_table_of_contents
      @text.apply TableOfContentsRule, ConsecutivePeriodsRule,
        ConsecutiveForwardSlashRule
    end

    def clean_consecutive_characters
      @text.apply ConsecutivePeriodsRule, ConsecutiveForwardSlashRule
    end
  end
end
