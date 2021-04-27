# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Burmese
      include Languages::Common

      SENTENCE_BOUNDARY_REGEX = /.*?[။၏!\?]|.*?$/
      Punctuations = ['။', '၏', '?', '!'].freeze

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = [].freeze
      end
    end
  end
end
