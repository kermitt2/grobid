# -*- encoding : utf-8 -*-
# frozen_string_literal: true

module PragmaticSegmenter
  # This class searches for a list within a string and adds
  # newlines before each list item.
  class List
    ROMAN_NUMERALS = %w(i ii iii iv v vi vii viii ix x xi xii xiii xiv x xi xii xiii xv xvi xvii xviii xix xx)
    LATIN_NUMERALS = ('a'..'z').to_a

    # Rubular: http://rubular.com/r/XcpaJKH0sz
    ALPHABETICAL_LIST_WITH_PERIODS =
      /(?<=^)[a-z](?=\.)|(?<=\A)[a-z](?=\.)|(?<=\s)[a-z](?=\.)/

    # Rubular: http://rubular.com/r/Gu5rQapywf
    ALPHABETICAL_LIST_WITH_PARENS =
      /(?<=\()[a-z]+(?=\))|(?<=^)[a-z]+(?=\))|(?<=\A)[a-z]+(?=\))|(?<=\s)[a-z]+(?=\))/i

    SubstituteListPeriodRule = Rule.new(/♨/, '∯')
    ListMarkerRule = Rule.new(/☝/, '')

    # Rubular: http://rubular.com/r/Wv4qLdoPx7
    SpaceBetweenListItemsFirstRule = Rule.new(/(?<=\S\S|^)\s(?=\S\s*\d{1,2}♨)/, "\r")

    # Rubular: http://rubular.com/r/AizHXC6HxK
    SpaceBetweenListItemsSecondRule = Rule.new(/(?<=\S\S|^)\s(?=\d{1,2}♨)/, "\r")

    # Rubular: http://rubular.com/r/GE5q6yID2j
    SpaceBetweenListItemsThirdRule = Rule.new(/(?<=\S\S|^)\s(?=\d{1,2}☝)/, "\r")

    NUMBERED_LIST_REGEX_1 =
      /\s\d{1,2}(?=\.\s)|^\d{1,2}(?=\.\s)|\s\d{1,2}(?=\.\))|^\d{1,2}(?=\.\))|(?<=\s\-)\d{1,2}(?=\.\s)|(?<=^\-)\d{1,2}(?=\.\s)|(?<=\s\⁃)\d{1,2}(?=\.\s)|(?<=^\⁃)\d{1,2}(?=\.\s)|(?<=s\-)\d{1,2}(?=\.\))|(?<=^\-)\d{1,2}(?=\.\))|(?<=\s\⁃)\d{1,2}(?=\.\))|(?<=^\⁃)\d{1,2}(?=\.\))/
    NUMBERED_LIST_REGEX_2 =
      /(?<=\s)\d{1,2}\.(?=\s)|^\d{1,2}\.(?=\s)|(?<=\s)\d{1,2}\.(?=\))|^\d{1,2}\.(?=\))|(?<=\s\-)\d{1,2}\.(?=\s)|(?<=^\-)\d{1,2}\.(?=\s)|(?<=\s\⁃)\d{1,2}\.(?=\s)|(?<=^\⁃)\d{1,2}\.(?=\s)|(?<=\s\-)\d{1,2}\.(?=\))|(?<=^\-)\d{1,2}\.(?=\))|(?<=\s\⁃)\d{1,2}\.(?=\))|(?<=^\⁃)\d{1,2}\.(?=\))/
    NUMBERED_LIST_PARENS_REGEX = /\d{1,2}(?=\)\s)/

    # Rubular: http://rubular.com/r/NsNFSqrNvJ
    EXTRACT_ALPHABETICAL_LIST_LETTERS_REGEX =
      /\([a-z]+(?=\))|(?<=^)[a-z]+(?=\))|(?<=\A)[a-z]+(?=\))|(?<=\s)[a-z]+(?=\))/i

    # Rubular: http://rubular.com/r/wMpnVedEIb
    ALPHABETICAL_LIST_LETTERS_AND_PERIODS_REGEX =
      /(?<=^)[a-z]\.|(?<=\A)[a-z]\.|(?<=\s)[a-z]\./i

    # Rubular: http://rubular.com/r/GcnmQt4a3I
    ROMAN_NUMERALS_IN_PARENTHESES =
        /\(((?=[mdclxvi])m*(c[md]|d?c*)(x[cl]|l?x*)(i[xv]|v?i*))\)(?=\s[A-Z])/

    attr_reader :text
    def initialize(text:)
      @text = Text.new(text)
    end

    def add_line_break
      format_alphabetical_lists
      format_roman_numeral_lists
      format_numbered_list_with_periods
      format_numbered_list_with_parens
    end

    def replace_parens
      text.gsub!(ROMAN_NUMERALS_IN_PARENTHESES, '&✂&\1&⌬&'.freeze)
      text
    end

    private

    def format_numbered_list_with_parens
      replace_parens_in_numbered_list
      add_line_breaks_for_numbered_list_with_parens
      @text.apply(ListMarkerRule)
    end

    def format_numbered_list_with_periods
      replace_periods_in_numbered_list
      add_line_breaks_for_numbered_list_with_periods
      @text.apply(SubstituteListPeriodRule)
    end

    def format_alphabetical_lists
      add_line_breaks_for_alphabetical_list_with_periods(roman_numeral: false)
      add_line_breaks_for_alphabetical_list_with_parens(roman_numeral: false)
    end

    def format_roman_numeral_lists
      add_line_breaks_for_alphabetical_list_with_periods(roman_numeral: true)
      add_line_breaks_for_alphabetical_list_with_parens(roman_numeral: true)
    end

    def replace_periods_in_numbered_list
      scan_lists(NUMBERED_LIST_REGEX_1, NUMBERED_LIST_REGEX_2, '♨', strip: true)
    end

    def add_line_breaks_for_numbered_list_with_periods
      if @text.include?('♨') && @text !~ /♨.+\n.+♨|♨.+\r.+♨/ && @text !~ /for\s\d{1,2}♨\s[a-z]/
        @text.apply(SpaceBetweenListItemsFirstRule, SpaceBetweenListItemsSecondRule)
      end
    end

    def replace_parens_in_numbered_list
      scan_lists(
        NUMBERED_LIST_PARENS_REGEX, NUMBERED_LIST_PARENS_REGEX, '☝')
      scan_lists(NUMBERED_LIST_PARENS_REGEX, NUMBERED_LIST_PARENS_REGEX, '☝')
    end

    def add_line_breaks_for_numbered_list_with_parens
      if @text.include?('☝') && @text !~ /☝.+\n.+☝|☝.+\r.+☝/
        @text.apply(SpaceBetweenListItemsThirdRule)
      end
    end

    def scan_lists(regex1, regex2, replacement, strip: false)
      list_array = @text.scan(regex1).map(&:to_i)
      list_array.each_with_index do |a, i|
        next unless (a + 1).eql?(list_array[i + 1]) ||
                    (a - 1).eql?(list_array[i - 1]) ||
                    (a.eql?(0) && list_array[i - 1].eql?(9)) ||
                    (a.eql?(9) && list_array[i + 1].eql?(0))
        substitute_found_list_items(regex2, a, strip, replacement)
      end
    end

    def substitute_found_list_items(regex, a, strip, replacement)
      @text.gsub!(regex).with_index do |m|
        if a.to_s.eql?(strip ? m.strip.chop : m)
          "#{Regexp.escape(a.to_s)}" + replacement
        else
          "#{m}"
        end
      end
    end

    def add_line_breaks_for_alphabetical_list_with_periods(roman_numeral: false)
      iterate_alphabet_array(ALPHABETICAL_LIST_WITH_PERIODS, roman_numeral: roman_numeral)
    end

    def add_line_breaks_for_alphabetical_list_with_parens(roman_numeral: false)
      iterate_alphabet_array(ALPHABETICAL_LIST_WITH_PARENS,
        parens: true,
        roman_numeral: roman_numeral)
    end

    def replace_alphabet_list(a)
      @text.gsub!(ALPHABETICAL_LIST_LETTERS_AND_PERIODS_REGEX).with_index do |m|
        a.eql?(m.chomp('.')) ? "\r#{Regexp.escape(a.to_s)}∯" : "#{m}"
      end
    end

    def replace_alphabet_list_parens(a)
      @text.gsub!(EXTRACT_ALPHABETICAL_LIST_LETTERS_REGEX).with_index do |m|
        if m.include?('(')
          a.eql?(Unicode::downcase(m.dup).gsub!(/\(/, '')) ? "\r&✂&#{Regexp.escape(m.gsub!(/\(/, ''))}" : "#{m}"
        else
          a.eql?(Unicode::downcase(m.dup)) ? "\r#{Regexp.escape(m)}" : "#{m}"
        end
      end
    end

    def replace_correct_alphabet_list(a, parens)
      if parens
        replace_alphabet_list_parens(a)
      else
        replace_alphabet_list(a)
      end
    end

    def last_array_item_replacement(a, i, alphabet, list_array, parens)
      return if alphabet & list_array == [] ||
        !alphabet.include?(list_array[i - 1]) ||
        !alphabet.include?(a)
      return if (alphabet.index(list_array[i - 1]) - alphabet.index(a)).abs != 1
      replace_correct_alphabet_list(a, parens)
    end

    def other_items_replacement(a, i, alphabet, list_array, parens)
      return if alphabet & list_array == [] ||
        !alphabet.include?(list_array[i - 1]) ||
        !alphabet.include?(a) ||
        !alphabet.include?(list_array[i + 1])
      return if alphabet.index(list_array[i + 1]) - alphabet.index(a) != 1 &&
                (alphabet.index(list_array[i - 1]) - alphabet.index(a)).abs != 1
      replace_correct_alphabet_list(a, parens)
    end

    def iterate_alphabet_array(regex, parens: false, roman_numeral: false)
      list_array = @text.scan(regex).map { |s| Unicode::downcase(s) }
      if roman_numeral
        alphabet = ROMAN_NUMERALS
      else
        alphabet = LATIN_NUMERALS
      end
      list_array.delete_if { |item| !alphabet.any? { |a| a.include?(item) } }
      list_array.each_with_index do |a, i|
        if i.eql?(list_array.length - 1)
          last_array_item_replacement(a, i, alphabet, list_array, parens)
        else
          other_items_replacement(a, i, alphabet, list_array, parens)
        end
      end
    end
  end
end
