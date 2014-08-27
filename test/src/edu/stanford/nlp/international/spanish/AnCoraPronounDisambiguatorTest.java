package edu.stanford.nlp.international.spanish;

import edu.stanford.nlp.international.spanish.process.AnCoraPronounDisambiguator;
import junit.framework.TestCase;

public class AnCoraPronounDisambiguatorTest extends TestCase {

  private final SpanishVerbStripper verbStripper = new SpanishVerbStripper();

  private void runTest(AnCoraPronounDisambiguator.PersonalPronounType expected, String verb,
                       int i) {
    assertEquals(expected,
      AnCoraPronounDisambiguator.disambiguatePersonalPronoun(verbStripper
                                                               .separatePronouns(verb), i, ""));
  }

  public void testDisambiguation() {
    runTest(AnCoraPronounDisambiguator.PersonalPronounType.REFLEXIVE, "enterarme", 0);
  }

}