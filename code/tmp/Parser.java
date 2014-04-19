import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.pipeline.*;

public class Parser {
	void parseDocument(String path) throws IOException{
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    props.put("annotators", "tokenize, ssplit, pos,lemma,ner");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // read some text in the text variable
	    
	    List<String> lines = Files.readAllLines(Paths.get(path),StandardCharsets.UTF_8);
	    StringBuilder textTemp=new StringBuilder();
	    for(String line:lines)
	    	textTemp.append(line);
	    String text = textTemp.toString();
	    
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	      // traversing the words in the current sentence
	      // a CoreLabel is a CoreMap with additional token-specific methods
	      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        // this is the text of the token
	        String word = token.get(TextAnnotation.class);
	        // this is the POS tag of the token
	        String pos = token.get(PartOfSpeechAnnotation.class);
	        // this is the NER label of the token
	       String ne = token.get(NamedEntityTagAnnotation.class);
	       if(pos.startsWith("VB"))
	        	System.out.println(word+" "+pos+" "+ne);
	      }
	    }
	}
	
	public static void main(String[] args) throws IOException {
		Parser parser=new Parser();
		parser.parseDocumentList(new File("C:\\Users\\Sail\\Desktop\\LDC2014E20\\LDC2014E20_TAC_2014_KBP_Event_Argument_Extraction_Pilot_Source_Corpus_V1.1\\data"));
	}
	
	public void parseDocumentList(final File folder) throws IOException {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	parseDocumentList(fileEntry);
	        } else {
	        	parseDocument(fileEntry.getAbsolutePath());
	        }
	    }
	}
}
