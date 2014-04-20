import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import edu.stanford.nlp.trees.EnglishGrammaticalRelations.PunctuationGRAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.pipeline.*;

import java.util.*;

public class Parser {
	HashSet<String> verbs = new HashSet<String>();
	HashSet<String> ner = new HashSet<String>();
	HashSet<String> sentenceList = new HashSet<String>();
	static String[] conjunctionList={"for","and","nor","but","or","yet","so","after","although","because,","before,","if","lest","once","since","than","that","though","till","unless","until","when","whenever","where","wherever","while","both","either","neither","whether","eventhough"}; 
	static HashSet<String> conjunction=new HashSet<String>();
	void parseDocument(File file) throws IOException{
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		Properties props = new Properties();
		//props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.put("annotators", "tokenize,cleanxml, ssplit, pos,lemma,ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// read some text in the text variable
		BufferedReader br=new BufferedReader(new FileReader(file));
		String line;
		StringBuilder textTemp=new StringBuilder();
		while((line=br.readLine())!=null)
			textTemp.append(line);
		br.close();
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
			StringBuilder temp=new StringBuilder();
			String prev="";
			for (int i=0;i< sentence.get(TokensAnnotation.class).size();i++) {
				//CoreLabel token
				CoreLabel token=sentence.get(TokensAnnotation.class).get(i);
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);
				if(!ne.equals("O")){
					if(!prev.equals(ne))
						temp.append(" "+ne);
					prev=ne;
				}else if(pos.startsWith("VB")||conjunction.contains(word.toLowerCase())){
					temp.append(" "+word);
				}else if(pos.equals(word)){
					temp.append(word);
				}
				else if(pos.startsWith("NN")){
					if(!prev.startsWith("NN"))
						temp.append(" NP");
					prev="NN";
				} 
				else
					prev="";
				
			}
			sentenceList.add(file.getName()+"\t"+temp.toString().trim()+"\t"+sentence.toString());
		}
	}

	public static void main(String[] args) throws IOException {
		for(String conj: conjunctionList){
			conjunction.add(conj);
		}
		
		
		Parser parser = new Parser();

		//parser.parseDocument(new File("temp.txt"));
		parser.parseDocumentList(new File("C:\\Users\\Sail\\Desktop\\LDC2014E20\\LDC2014E20_TAC_2014_KBP_Event_Argument_Extraction_Pilot_Source_Corpus_V1.1\\data"));
		//parser.readRules("event-rules.txt");
		
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("sentences.txt")));
		System.out.println("Printing all Scentences...");
		for(String scen:parser.sentenceList){
			bw.write(scen);
			bw.newLine();
		}
		bw.close();
	}

	
	public void parseDocumentList(final File folder) throws IOException {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				parseDocumentList(fileEntry);
			} else {
				parseDocument(fileEntry);
			}
		}
	}
}
