# -*- encoding : utf-8 -*-
# frozen_string_literal: true

require 'unicode'

module PragmaticSegmenter
  # This class searches for periods within an abbreviation and
  # replaces the periods.
  class AbbreviationReplacer

    attr_reader :text
    def initialize(text:, language: )
      @text = Text.new(text)
      @language = language
    end

    def replace
      @text.apply(@language::PossessiveAbbreviationRule,
        @language::KommanditgesellschaftRule,
        @language::SingleLetterAbbreviationRules::All)

      @text = search_for_abbreviations_in_string(@text)
      @text = replace_multi_period_abbreviations(@text)
      @text.apply(@language::AmPmRules::All)
      replace_abbreviation_as_sentence_boundary(@text)
    end

    private

    def search_for_abbreviations_in_string(txt)
      original = txt.dup
      downcased = Unicode::downcase(txt)
      @language::Abbreviation::ABBREVIATIONS.each do |abbreviation|
        stripped = abbreviation.strip
        next unless downcased.include?(stripped)
        abbrev_match = original.scan(/(?:^|\s|\r|\n)#{Regexp.escape(stripped)}/i)
        next if abbrev_match.empty?
        next_word_start = /(?<=#{Regexp.escape(stripped)} ).{1}/
        character_array = @text.scan(next_word_start)
        abbrev_match.each_with_index do |am, index|
          txt = scan_for_replacements(txt, am, index, character_array)
        end
      end
      txt
    end

    def scan_for_replacements(txt, am, index, character_array)
      character = character_array[index]
      prepositive = @language::Abbreviation::PREPOSITIVE_ABBREVIATIONS
      number_abbr = @language::Abbreviation::NUMBER_ABBREVIATIONS
      upper = /[[:upper:]]/.match(character.to_s)
      if upper.nil? || prepositive.include?(Unicode::downcase(am.strip))
        if prepositive.include?(Unicode::downcase(am.strip))
          txt = replace_prepositive_abbr(txt, am)
        elsif number_abbr.include?(Unicode::downcase(am.strip))
          txt = replace_pre_number_abbr(txt, am)
        else
          txt = replace_period_of_abbr(txt, am)
        end
      end
      txt
    end

    def replace_abbreviation_as_sentence_boundary(txt)
      # As we are being conservative and keeping ambiguous
      # sentence boundaries as one sentence instead of
      # splitting into two, we can split at words that
      # we know for certain never follow these abbreviations.
      # Some might say that the set of words that follow an
      # abbreviation such as U.S. (i.e. U.S. Government) is smaller than
      # the set of words that could start a sentence and
      # never follow U.S. However, we are being conservative
      # and not splitting by default, so we need to look for places
      # where we definitely can split. Obviously SENTENCE_STARTERS
      # will never cover all cases, but as the gem is named
      # 'Pragmatic Segmenter' we need to be pragmatic
      # and try to cover the words that most often start a
      # sentence but could never follow one of the abbreviations below.

      # Rubular: http://rubular.com/r/PkBQ3PVBS8
      @language::AbbreviationReplacer::SENTENCE_STARTERS.each do |word|
        escaped = Regexp.escape(word)
        regex   = /(U∯S|U\.S|U∯K|E∯U|E\.U|U∯S∯A|U\.S\.A|I|i.v|I.V)∯(?=\s#{escaped}\s)/
        txt.gsub!(regex, '\1.')
      end
      txt
    end

    def replace_multi_period_abbreviations(txt)
      mpa = txt.scan(@language::MULTI_PERIOD_ABBREVIATION_REGEX)
      return txt if mpa.empty?
      mpa.each do |r|
        txt.gsub!(/#{Regexp.escape(r)}/, "#{r.gsub!('.', '∯')}")
      end
      txt
    end

    def replace_pre_number_abbr(txt, abbr)
      txt.gsub!(/(?<=\s#{abbr.strip})\.(?=\s\d)|(?<=^#{abbr.strip})\.(?=\s\d)/, '∯')
      txt.gsub!(/(?<=\s#{abbr.strip})\.(?=\s+\()|(?<=^#{abbr.strip})\.(?=\s+\()/, '∯')
      txt
    end

    def replace_prepositive_abbr(txt, abbr)
      txt.gsub!(/(?<=\s#{abbr.strip})\.(?=\s)|(?<=^#{abbr.strip})\.(?=\s)/, '∯')
      txt.gsub!(/(?<=\s#{abbr.strip})\.(?=:\d+)|(?<=^#{abbr.strip})\.(?=:\d+)/, '∯')
      txt
    end

    def replace_period_of_abbr(txt, abbr)
      txt.gsub!(/(?<=\s#{abbr.strip})\.(?=((\.|\:|-|\?)|(\s([a-z]|I\s|I'm|I'll|\d|\())))|(?<=^#{abbr.strip})\.(?=((\.|\:|\?)|(\s([a-z]|I\s|I'm|I'll|\d))))/, '∯')
      txt.gsub!(/(?<=\s#{abbr.strip})\.(?=,)|(?<=^#{abbr.strip})\.(?=,)/, '∯')
      txt
    end

    def replace_possessive_abbreviations(txt)
      txt.gsub!(@language::POSSESSIVE_ABBREVIATION_REGEX, '∯')
      txt
    end
  end
end
