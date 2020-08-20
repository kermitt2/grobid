module Unicode

  extend self

  VERSION = '0.4.3'

  Normalizer = Java::JavaText::Normalizer

  def strcmp(str1, str2)
    decompose(str1).to_java.compare_to(decompose(str2))
  end

  def strcmp_compat(str1, str2)
    decompose_compat(str1).to_java.compare_to(decompose_compat(str2))
  end

  def decompose(str)
    Normalizer.normalize(str, Normalizer::Form::NFD)
  end

  alias_method :normalize_D, :decompose
  alias_method :nfd,         :decompose

  # Decompose Unicode string with a non-standard mapping.
  #
  # It does not decompose the characters in CompositionExclusions.txt.
  def decompose_safe(str)
    raise NotImplementedError
  end

  alias_method :normalize_D_safe, :decompose_safe
  alias_method :nfd_safe,         :decompose_safe

  def decompose_compat(str)
    Normalizer.normalize(str, Normalizer::Form::NFKD)
  end

  alias_method :normalize_KD, :decompose_compat
  alias_method :nfkd,         :decompose_compat

  # Compose Unicode string. Before composing, the trailing
  # characters are sorted in canonical order.
  #
  # The parameter must be decomposed.
  #
  # The composition is based on the reverse of the
  # character decomposition mapping in UnicodeData.txt,
  # CompositionExclusions.txt and the Hangul composition
  # algorithm.
  def compose(str)
    raise NotImplementedError
  end

  def normalize_C(str)
    Normalizer.normalize(str, Normalizer::Form::NFC)
  end

  alias_method :nfc, :normalize_C

  def normalize_KC(str)
    Normalizer.normalize(str, Normalizer::Form::NFKC)
  end

  alias_method :nfkc, :normalize_KC

  def normalize_C_safe(str)
    compose(decompose_safe(str))
  end

  alias_method :nfc_safe, :normalize_C_safe

  def upcase(str)
    str.to_java.to_upper_case
  end

  def downcase(str)
    str.to_java.to_lower_case
  end

  def capitalize(str)
    downcase(str).tap { |s| s[0] = upcase(s[0]) }
  end

  # Get an array of general category names of the string.
  #
  # Can be called with a block.
  def categories(str)
    raise NotImplementedError
  end

  # Get an array of abbreviated category names of the string.
  #
  # Can be called with a block.
  def abbr_categories(str)
    raise NotImplementedError
  end

  # Get an array of text elements.
  #
  # A text element is a unit that is displayed as a single character.
  #
  # Can be called with a block.
  def text_elements(str)
    raise NotImplementedError
  end

  # Estimate the display width on the fixed pitch text terminal.
  #
  # It based on Markus Kuhn's mk_wcwidth.
  #
  # If the optional argument 'cjk' is true, East Asian
  # Ambiguous characters are treated as wide characters.
  def width(str, cjk = false)
    raise NotImplementedError
  end

end
