# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module English
      include Languages::Common

      class Cleaner < Cleaner
        def clean
          super
          clean_quotations
        end

        private

        def clean_quotations
          @text.gsub(/`/, "'")
        end

        def abbreviations
          [].freeze
        end
      end

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = %w(
          A Being Did For He How However I In It Millions More She That The
          There They We What When Where Who Why
        ).freeze
      end
    end
  end
end
