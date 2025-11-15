# frozen_string_literal: true

module PragmaticSegmenter
  module Languages
    module Danish
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

      module Abbreviation
        ABBREVIATIONS = Set.new(['adm', 'adr', 'afd', 'afs', 'al', 'alm', 'alm', 'ang', 'ank', 'anm', 'ann', 'ansvh', 'apr', 'arr', 'ass', 'att', 'aud', 'aug', 'aut', 'bd', 'bdt', 'bet', 'bhk', 'bio', 'biol', 'bk', 'bl.a', 'bot', 'br', 'bto', 'ca', 'cal', 'cirk', 'cit', 'co', 'cpr-nr', 'cvr-nr', 'd.d', 'd.e', 'd.m', 'd.s', 'd.s.s', 'd.y', 'd.å', 'd.æ', 'da', 'dav', 'dec', 'def', 'del', 'dep', 'diam', 'din', 'dir', 'disp', 'distr', 'do', 'dobb', 'dr', 'ds', 'dvs', 'e.b', 'e.kr', 'e.l', 'e.o', 'e.v.t', 'eftf', 'eftm', 'egl', 'eks', 'eksam', 'ekskl', 'eksp', 'ekspl', 'el', 'emer', 'endv', 'eng', 'enk', 'etc', 'eur', 'evt', 'exam', 'f', 'f', 'f.eks', 'f.kr', 'f.m', 'f.n', 'f.o', 'f.o.m', 'f.s.v', 'f.t', 'f.v.t', 'f.å', 'fa', 'fakt', 'feb', 'fec', 'ff', 'fg', 'fg', 'fhv', 'fig', 'fl', 'flg', 'fm', 'fm', 'fmd', 'forb', 'foreg', 'foren', 'forf', 'forh', 'fork', 'form', 'forr', 'fors', 'forsk', 'forts', 'fp', 'fr', 'frk', 'fuldm', 'fuldm', 'fung', 'fung', 'fys', 'fær', 'g', 'g.d', 'g.m', 'gd', 'gdr', 'gg', 'gh', 'gl', 'gn', 'gns', 'gr', 'grdl', 'gross', 'h.a', 'h.c', 'hdl', 'henh', 'henv', 'hf', 'hft', 'hhv', 'hort', 'hosp', 'hpl', 'hr', 'hrs', 'hum', 'i', 'i.e', 'ib', 'ibid', 'if', 'ifm', 'ill', 'indb', 'indreg', 'ing', 'inkl', 'insp', 'instr', 'isl', 'istf', 'jan', 'jf', 'jfr', 'jnr', 'jr', 'jul', 'jun', 'jur', 'jvf', 'kal', 'kap', 'kat', 'kbh', 'kem', 'kgl', 'kin', 'kl', 'kld', 'km/t', 'knsp', 'komm', 'kons', 'korr', 'kp', 'kr', 'kr', 'kst', 'kt', 'ktr', 'kv', 'kvt', 'l', 'l.c', 'lab', 'lat', 'lb', 'lb.', 'lb.nr', 'lejl', 'lgd', 'lic', 'lign', 'lin', 'ling.merc', 'litt', 'lok', 'lrs', 'ltr', 'lø', 'm', 'm.a.o', 'm.fl.st', 'm.m', 'm/', 'ma', 'mag', 'maks', 'mar', 'mat', 'matr.nr', 'md', 'mdl', 'mdr', 'mdtl', 'med', 'medd', 'medflg', 'medl', 'merc', 'mezz', 'mf', 'mfl', 'mgl', 'mhp', 'mht', 'mi', 'mia', 'mio', 'ml', 'mods', 'modsv', 'modt', 'mr', 'mrk', 'mrs', 'ms', 'mul', 'mv', 'mvh', 'n', 'n.br', 'n.f', 'nat', 'ned', 'nedenn', 'nedenst', 'nederl', 'nkr', 'nl', 'no', 'nord', 'nov', 'nr', 'nr', 'nto', 'nuv', 'o', 'o.a', 'o.fl.st', 'o.g', 'o.h', 'o.m.a', 'obj', 'obl', 'obs', 'odont', 'oecon', 'off', 'ofl', 'okt', 'omg', 'omr', 'omtr', 'on', 'op.cit', 'opg', 'opl', 'opr', 'org', 'orig', 'osfr', 'osv', 'ovenn', 'ovenst', 'overs', 'ovf', 'oz', 'p', 'p.a', 'p.b.v', 'p.c', 'p.m.v', 'p.p', 'p.s', 'p.t', 'p.v.a', 'p.v.c', 'par', 'partc', 'pass', 'pct', 'pd', 'pens', 'perf', 'pers', 'pg', 'pga', 'pgl', 'ph', 'ph.d', 'pharm', 'phil', 'pinx', 'pk', 'pkt', 'pl', 'pluskv', 'polit', 'polyt', 'port', 'pos', 'pp', 'pr', 'prc', 'priv', 'prod', 'prof', 'pron', 'præd', 'præf', 'præp', 'præs', 'præt', 'psych', 'pt', 'pæd', 'q.e.d', 'rad', 'red', 'ref', 'reg', 'regn', 'rel', 'rep', 'repr', 'rest', 'rk', 'russ', 's', 's.br', 's.d', 's.e', 's.f', 's.m.b.a', 's.u', 's.å', 's/', 'sa', 'sb', 'sc', 'scient', 'sek', 'sek', 'sekr', 'sem', 'sen', 'sep', 'sept', 'sg', 'sign', 'sj', 'skr', 'skt', 'slutn', 'sml', 'smp', 'sms', 'smst', 'soc', 'soc', 'sort', 'sp', 'spec', 'spm', 'spr', 'spsk', 'st', 'stk', 'str', 'stud', 'subj', 'subst', 'suff', 'sup', 'suppl', 'sv', 'såk', 'sædv', 'sø', 't', 't.h', 't.o.m', 't.v', 'tab', 'td', 'tdl', 'tdr', 'techn', 'tekn', 'temp', 'th', 'ti', 'tidl', 'tilf', 'tilh', 'till', 'tilsv', 'tjg', 'tlf', 'tlgr', 'to', 'tr', 'trp', 'tv', 'ty', 'u', 'u.p', 'u.st', 'u.å', 'uafh', 'ubf', 'ubøj', 'udb', 'udbet', 'udd', 'udg', 'uds', 'ugtl', 'ulin', 'ult', 'undt', 'univ', 'v.f', 'var', 'vb', 'vbsb', 'vedk', 'vedl', 'vedr', 'vejl', 'vh', 'vol', 'vs', 'vsa', 'vær', 'zool', 'årg', 'årh', 'årl', 'ø.f', 'øv', 'øvr']).freeze
        NUMBER_ABBREVIATIONS = Set.new(['nr', 's']).freeze
        PREPOSITIVE_ABBREVIATIONS = Set.new(['adm', 'skt', 'dr', 'hr', 'fru', 'st']).freeze
      end

      # This handles the case where a dot is used to denote and ordinal (5. Juni)
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

      MONTHS = ['Januar', 'Februar', 'Marts', 'April', 'Maj', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'December'].freeze

      class AbbreviationReplacer < AbbreviationReplacer
        SENTENCE_STARTERS = %w(
          At De Dem Den Der Det Du En Et For Få Gjorde Han Hun Hvad Hvem Hvilke
          Hvor Hvordan Hvorfor Hvorledes Hvornår I Jeg Mange Vi Være
        ).freeze

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

          @language::AbbreviationReplacer::SENTENCE_STARTERS.each do |word|
            escaped = Regexp.escape(word)
            txt.gsub!(/U∯S∯\s#{escaped}\s/, "U∯S\.\s#{escaped}\s")
            txt.gsub!(/U\.S∯\s#{escaped}\s/, "U\.S\.\s#{escaped}\s")
            txt.gsub!(/U∯K∯\s#{escaped}\s/, "U∯K\.\s#{escaped}\s")
            txt.gsub!(/U\.K∯\s#{escaped}\s/, "U\.K\.\s#{escaped}\s")
            txt.gsub!(/E∯U∯\s#{escaped}\s/, "E∯U\.\s#{escaped}\s")
            txt.gsub!(/E\.U∯\s#{escaped}\s/, "E\.U\.\s#{escaped}\s")
            txt.gsub!(/U∯S∯A∯\s#{escaped}\s/, "U∯S∯A\.\s#{escaped}\s")
            txt.gsub!(/U\.S\.A∯\s#{escaped}\s/, "U\.S\.A\.\s#{escaped}\s")
            txt.gsub!(/I∯\s#{escaped}\s/, "I\.\s#{escaped}\s")
            txt.gsub!(/s.u∯\s#{escaped}\s/, "s\.u\.\s#{escaped}\s")
            txt.gsub!(/S.U∯\s#{escaped}\s/, "S\.U\.\s#{escaped}\s")
          end
          txt
        end
      end
    end
  end
end
