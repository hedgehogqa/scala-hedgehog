package hedgehog.extra

import hedgehog.core.GenT

trait CharacterOps {

  private val genT = new hedgehog.GenTOps {}

  /** Generates an ASCII binit: '0' to '1' */
  def binit: GenT[Char] =
    genT.char('0', '1')

  /** Generates an ASCII octit: '0' to '7' */
  def octit: GenT[Char] =
    genT.char('0', '7')

  /** Generates an ASCII digit: '0' to '9' */
  def digit: GenT[Char] =
    genT.char('0', '9')

  /** Generates an ASCII hexit: '0' to '9', 'a' to 'f', 'A' to 'F' */
  def hexit: GenT[Char] =
    genT.choice1(
      genT.char('0', '9')
    , genT.char('a', 'f')
    , genT.char('A', 'F')
    )

  /** Generates an ASCII lowercase letter: 'a' to 'z' */
  def lower: GenT[Char] =
    genT.char('a', 'z')

  /** Generates an ASCII uppercase letter: 'A' to 'Z' */
  def upper: GenT[Char] =
    genT.char('A', 'Z')

  /** Generates an ASCII letter: 'a' to 'z', 'A' to 'Z' */
  def alpha: GenT[Char] =
    genT.choice1(lower, upper)

  /** Generates an ASCII letter or digit: 'a' to 'z', 'A' to 'Z', '0' to '9' */
  def alphaNum: GenT[Char] =
    genT.choice1(alpha, digit)

  /** Generates an ASCII character */
  def ascii: GenT[Char] =
    genT.char(0, 125)

  /** Generates an Latin-1 character */
  def latin1: GenT[Char] =
    genT.char(0, 255)

  /** Generates a Unicode character, excluding noncharacters and invalid standalone surrogates: */
  def unicode: GenT[Char] =
    unicodeAll
      .filter(!isSurrogate(_))
      .filter(!isNoncharacter(_))

  /** Generates a Unicode character, including noncharacters and invalid standalone surrogates */
  def unicodeAll: GenT[Char] =
    genT.char(0, Integer.MAX_VALUE.toChar)

  /** Check if a character is in the surrogate category. */
  def isSurrogate(x: Char): Boolean =
    x >= 55296 && x <= 57343

  /** Check if a character is one of the noncharacters. */
  def isNoncharacter(x: Char): Boolean =
    x == 65534 || x == 65535
}
