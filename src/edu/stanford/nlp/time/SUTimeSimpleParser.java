package edu.stanford.nlp.time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotations;
import edu.stanford.nlp.util.CoreMap;

/**
 * Simple wrapper around SUTime for parsing lots of strings outside of Annotation objects.
 * 
 * @author David McClosky
 */
public class SUTimeSimpleParser {
  /**
   * Indicates that any exception occurred inside the TimeAnnotator.  This should only be caused by bugs in SUTime.
   */
  public static class SUTimeParsingError extends Exception {
    private static final long serialVersionUID = 1L;
    public String timeExpression;

    public SUTimeParsingError(String timeExpression) {
      this.timeExpression = timeExpression;
    }
    
    public String getLocalizedMessage() {
      return "Error while parsing '" + timeExpression + "'";
    }

  }

  private static AnnotationPipeline pipeline;
  private static Map<String, Temporal> cache;
  public static int calls = 0;
  public static int misses = 0;

  static {
    pipeline = makeNumericPipeline();
    cache = new HashMap<String, Temporal>();
  }
  
  private static AnnotationPipeline makeNumericPipeline() {  
    AnnotationPipeline pipeline = new AnnotationPipeline();
    pipeline.addAnnotator(new PTBTokenizerAnnotator(false));
    pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
    pipeline.addAnnotator(new POSTaggerAnnotator(false));
    pipeline.addAnnotator(new TimeAnnotator());
    
    return pipeline;
  }
  
  /**
   * Parse a string with SUTime.
   * 
   * @throws SUTimeParsingError if anything goes wrong
   */
  public static Temporal parse(String str) throws SUTimeParsingError {
    try {
      Annotation doc = new Annotation(str);
      pipeline.annotate(doc);

      assert doc.get(SentencesAnnotation.class) != null;
      assert doc.get(SentencesAnnotation.class).size() > 0;
      List<CoreMap> timexAnnotations = doc.get(TimexAnnotations.class);
      if (timexAnnotations.size() > 1) {
        throw new RuntimeException("Too many timexes for '" + str + "'");
      }
      CoreMap timex = timexAnnotations.get(0);

      return timex.get(TimeExpression.Annotation.class).getTemporal();
    } catch (Exception e) {
      SUTimeSimpleParser.SUTimeParsingError parsingError = new SUTimeSimpleParser.SUTimeParsingError(str);
      parsingError.initCause(e);
      throw parsingError;
    }
  }
  
  /**
   * Cached wrapper of parse method.
   */
  public static Temporal parseUsingCache(String str) throws SUTimeParsingError {
    calls++;
    if (!cache.containsKey(str)) {
      misses++;
      cache.put(str, parse(str));
    }
    
    return cache.get(str);
  }
  
  public static void main(String[] args) throws SUTimeParsingError {
    for (String s : new String[] {"1972", "1972-07-05", "0712", "1972-04"}) {
      System.out.println("String: " + s);
      Temporal timeExpression = parse(s);
      System.out.println("Parsed: " + timeExpression);
      System.out.println();
    }
    
  }
}