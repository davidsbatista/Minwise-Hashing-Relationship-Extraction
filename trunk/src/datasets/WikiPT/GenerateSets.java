package datasets.WikiPT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import datasets.TestClassification;

import utils.nlp.PortuguesePOSTagger;

public class GenerateSets {

	static Map<String,Integer> sentences = new HashMap<String, Integer>();
	static Set<String> sentences_ignored = new HashSet<String>();	
	static Multimap<String, Instance> instances_per_class = LinkedListMultimap.create();
	
	public static void generateWikiPT() throws Exception, IOException {				
		PortuguesePOSTagger.initialize();		
		Relations.initialize();
		PrintWriter outTrain = new PrintWriter(new FileWriter("train-data-wikipt.txt"));
		PrintWriter outTest = new PrintWriter(new FileWriter("test-data-wikipt.txt"));		
		System.out.println("Generating WikiPT data...");
		processWikiPT("Datasets/WikiPT/results-relation-extraction.txt",outTrain,outTest);
	}
	
	public static int countWords(String entity, String sentence) {
		int count = 0;
		int result = sentence.indexOf(entity);
		while (result != -1) {
			result = sentence.indexOf(entity, result + 1);
			count++;
		}
		return count;
	}
	
public static String generateNGrams(String source, String prefix, int betweenLenght, int casing, int window) {
		
		String[] sourcePOS = PortuguesePOSTagger.posTags(source);
		String[] sourceTokens = PortuguesePOSTagger.tokenize(source);

		/* - quadgrams de caracteres
		 * - quadgrams de caracteres + verbos 
		 * - quadgrams de caracteres + verbos + preposições 
		 * - quadgrams de caracteres + verbos + preposições + padrão_reverb
		 */
		
		Set<String> set = new HashSet<String>();
		
		if ((sourcePOS!=null && sourceTokens!=null) && sourceTokens.length == sourcePOS.length) {
			for ( int i = 0 ; i < sourceTokens.length; i++ ) {				
				if ( prefix.startsWith("BEF") && sourceTokens.length - i > betweenLenght + window ) continue;
				if ( prefix.startsWith("AFT") && i > betweenLenght + window ) continue;
				
				if ( sourcePOS[i].startsWith("verb") || sourcePOS[i].startsWith("pp") ) {
				  set.add(sourceTokens[i].toLowerCase() + "_" + ( i < sourceTokens.length - 1 ? sourceTokens[i+1].toLowerCase() + "_" : "" ) +  prefix);
				  set.add(sourceTokens[i].toLowerCase() + "_" + prefix);
				  if ( sourcePOS[i].startsWith("pp")  ) set.add(sourceTokens[i].toLowerCase() + "_PP_" + prefix);
				  
				  //ReVerb inspired: um verbo, seguido de vários nomes, adjectivos ou adverbios, terminando numa preposição.
				  if (i < sourceTokens.length - 2) {
		  			String pattern = sourceTokens[i].toLowerCase();
		  			int j = i+1;				
					while ( ((j < sourceTokens.length - 2)) && ((sourcePOS[j].startsWith("adverb") || sourcePOS[j].startsWith("adjective")) || sourcePOS[j].startsWith("noun"))) {	  				
						pattern += "_" + sourceTokens[j].toLowerCase();
						j++;				
					}
					if (sourcePOS[j].startsWith("preposition")) {
							pattern += "_" + sourceTokens[j].toLowerCase();
							set.add(pattern + "_RVB_" + prefix);
					}
		  		  }		  	
				} else if (sourcePOS[i].startsWith("preposition")) set.add(sourceTokens[i].toLowerCase() + "_PREP_" + prefix);
			}
			// character quadgrams
			for (int j = 0; j < source.length() + 3; j++) {
					String tok = "";
					for (int i = -3; i <= 0; i++) {
						char ch = (j + i) < 0 || (j + i) >= source.length() ? '_' : source.charAt(j + i);
						tok += ch == ' ' ? '_' : ch;
					}
					set.add(tok + "_" + prefix + "_");
				}
		} 
		String result = "";
		for (String tok : set)
			result += " " + tok;
		return result.trim();
	}
	
	public static void processRelations(String sentence, String e1, String e2, String type, boolean checked, PrintWriter outTrain, PrintWriter outTest) {
		
		String before = null;
		String between = null;
		String after = null;
		String direction = null;
		
		int e1_count = countWords(e1,sentence);
		int e2_count = countWords(e2,sentence);
				
		if ( (!(e1.contains(e2) || e2.contains(e1))) && (!e2.equals(e1)) && (e1_count==1) && (e2_count==1)) {
			
			int e1_start = sentence.indexOf(e1);
			int e1_finish = sentence.indexOf(e1)+e1.length();
			
			int e2_start = sentence.indexOf(e2);
			int e2_finish = sentence.indexOf(e2)+e2.length();
			
			if (e1_start!=-1 && e2_start!=-1) {

				/*
				///test if relationships as a direction
				if (!(type.equals("other") || type.equals("partner"))) {
					
					if (e1_finish < e2_start) direction = "(e1,e2)";
					else if (e1_start > e2_finish) direction = "(e2,e1)";					
					type = type + direction;
					
					if (direction==null) {
						System.out.println("direction null:" + sentence);					
						System.out.println("e1 start: " + e1_start);
						System.out.println("e1 finish: " + e1_finish);					
						System.out.println("e2 start: " + e2_start);
						System.out.println("e2 finish: " + e2_finish);						
					}	
				}
				*/				
				
				try {
					before = sentence.substring(0,Math.min(e1_finish, e2_finish));
					between = sentence.substring(Math.min(e1_finish, e2_finish),Math.max(e1_start,e2_start));
					after = sentence.substring(Math.max(e1_start, e2_start),sentence.length()-1);
					before = before + " " + between;
					after = between + " " + after;
					
				} catch (Exception e) {
					System.out.println(sentence);
					System.out.println("e1: " + e1 + '\t' + '\t' + "("+e1_start+","+e1_finish+")" + '\t' + e1_count);
					System.out.println("e2: " + e2 + '\t' + '\t' + "("+e2_start+","+e2_finish+")" + '\t' + e2_count);
					System.out.println(type + direction);
					System.out.println();
					System.out.println("before: " + before);
					System.out.println("between: " + between);
					System.out.println("after: " + after);
					System.out.println();						
					System.out.println("\n========");
					e.printStackTrace();
					System.exit(0);
				}
			}
					
			/*
			if (!checked) processExample(before,after,between,type,outTrain);
			else processExample(before,after,between,type,outTest);
			*/
			
			Instance i = new Instance(type, before, after, between);
			instances_per_class.put(type, i);
			
			/*
			try {
				int count = sentences.get(sentence); 
				sentences.put(sentence,count+1);
			} catch (Exception e) {
				sentences.put(sentence,1);
			}
			*/
		}
	}

	public static void processWikiPT(String file, PrintWriter outTrain, PrintWriter outTest) throws Exception {		
		BufferedReader input = new BufferedReader(new FileReader(file));
		String aux = null;
		String sentence = null;
		String type = null;
		String e1 = null;
		String e2 = null;
		boolean checked = false;
		
		Map<String,Integer> relations_train = new HashMap<String, Integer>();
		Map<String,Integer> relations_test  = new HashMap<String, Integer>();
		
		int sentences_train = 0;
		int words_train = 0;		
		
		int sentences_test = 0;
		int words_test = 0;
		
		Vector<Integer> sentences_train_size = new Vector<Integer>();
		Vector<Integer> sentences_test_size = new Vector<Integer>();
		
		while ((aux = input.readLine()) != null) {
			if (aux.startsWith("SENTENCE")) {
				sentence = aux.split(": ")[1];				
				sentence = sentence.replaceAll("&nbsp;", "").replaceAll("&mdash;", "—").replaceAll("&ndash", "–").replaceAll("&bull;", "•");
				sentence = sentence.replaceAll("\\[?URLTOKEN\\s?([A-Za-z0-9íÍÌìàÀáÁâÂâÂãÃçÇéÉêÊóÓõÕôÔúÚüÜ\\.\\s,\\+\\(\\)\\-]+)?\\]?", "");
				
				//eliminar alguns pontos devido ao erro to SentenceDetector
				sentence = sentence.replaceAll(" ca\\. "," ca ").replaceAll(" etc\\. ", " etc ").replaceAll("\\(c\\. ", "(c ").replaceAll(" c\\. ", " c ");				
				sentence = sentence.replaceAll(" Mrs\\. "," Mrs  ").replaceAll("Ph\\.D\\.", "PhD").replaceAll("LL\\.D\\.","LLD").replaceAll("Sc\\.D\\.","ScD").replaceAll(" Mr\\. "," Mr ");				
				sentence = sentence.replaceAll("([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.([0-9]+)","$1 $2 $3 $4");
				sentence = sentence.replaceAll("([0-9]+)\\.([0-9]+)\\.([0-9]+)","$1 $2 $3");
				sentence = sentence.replaceAll("([0-9]+)\\.([0-9]+)","$1 $2");
				sentence = sentence.replaceAll(" Lei nº\\. "," Lei nº ").replaceAll(" n°\\. ", " nº ").replaceAll(" nº\\. ", "  nº ").replaceAll("\\(n. ", "(nº ");
				sentence = sentence.replaceAll(" S\\.A\\. "," SA ").replaceAll("Inc\\.","Inc");
								
				aux = input.readLine();
				if (aux.equals("")) aux = input.readLine();
				if (aux.startsWith("MANUALLY CHECKED : TRUE")) checked = true; else checked = false;
				if (aux.startsWith("MANUALLY CHECKED : IGNORE")) {
					sentences_ignored.add(sentence); 
					continue;
				}
				aux = input.readLine();
				while (!aux.startsWith("*")) {
					if (aux.startsWith("ENTITY1")) e1 = aux.split(": ")[1].trim();
					if (aux.startsWith("ENTITY2")) e2 = aux.split(": ")[1].trim();
					if (aux.startsWith("REL TYPE")) type = aux.split(": ")[1];
					aux = input.readLine();
				}
				
				if (!Arrays.asList(Relations.ignore).contains(type)) {
										
					if (!Arrays.asList(Relations.changeDirection).contains(type)) {
						
						//transform relationship type into a top aggregated type
						type = Relations.aggregatedRelations.get(type);												
						processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
					}
					
					else {
						
						//keyPerson, president, leaderName to keyPerson
						if (type.equals("keyPerson") || type.equals("president") || type.equals("leaderName")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "keyPerson";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}

						//currentMember and pastMember to partOf  
						if (type.equals("currentMember") || type.equals("pastMember")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "partOf";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}
						
						//capitalCountry to locatedinArea
						if (type.equals("capitalCountry")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "locatedInArea";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}
						
						//more examples of "influencedBy"
						if (type.equals("influenced") || type.equals("doctoralAdvisor")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "influencedBy";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}

						//more examples of "successor"
						if (type.equals("predecessor") || type.equals("successor")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "successor";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}
						
						//more examples of "parent"
						if (type.equals("child")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "parent";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}						
						
						//more examples of "keyPerson"
						if (type.equals("foundedBy")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "keyPerson";
							processRelations(sentence,e1,e2,type,checked,outTrain,outTest);
						}
						
					}
					
					/*
					if (checked) {
						sentences_test++;
						sentences_test_size.add(sentence.split("\\s+").length);
						words_test += sentence.split("\\s+").length;
						try {							
							int count = relations_test.get(type);
							count++;							
							relations_test.put(type, count);							
						} catch (Exception e) {
							relations_test.put(type, 1);
						}		
						
					} else if (!checked) {
						sentences_train++;
						sentences_train_size.add(sentence.split("\\s+").length);
						words_train += sentence.split("\\s+").length;
						try {
							int count = relations_train.get(type);
							count++;
							relations_train.put(type, count);
						} catch (Exception e) {
							relations_train.put(type, 1);
						}	
					}
					*/
				}				
			}
		}
		
		//precorrer instances_per_classe, para cada classe gerar:  75% treino, restantes 25% para teste
		for (String class_type : instances_per_class.keySet()) {
			Collection<Instance> instances = instances_per_class.get(class_type);
			int total = instances.size();						
			int num_train_examples = 0;
			int num_test_examples = 0;
			Iterator<Instance> instIter = instances.iterator();
			while (instIter.hasNext()) {	
				Instance i = instIter.next();
				if (num_train_examples<Math.round((total * 0.75))) {						
					//treino
					processExample(i.before,i.after,i.between,i.rel_type,outTrain);
					num_train_examples++;
				}				
				else {
					//teste
					processExample(i.before,i.after,i.between,i.rel_type,outTest);
					num_test_examples++;
				}
			}
			
			System.out.println("Class: " + class_type);
			System.out.println("Train: " + num_train_examples);
			System.out.println("Test:  " + num_test_examples);
		}
		
		outTrain.flush();
		outTrain.close();
		outTest.flush();
		outTest.close();
		input.close();
		
		/*
		System.out.println("Sentences TRAIN: " + sentences_train);
		System.out.println("Sentences TEST: " + sentences_test);
		System.out.println();
		System.out.println("Words TRAIN: " + words_train);
		System.out.println("Words TEST: " + words_test);
		System.out.println();
		
		/* class instance statistics */ 
		/*
		System.out.println("Statistics relations TRAIN");
		classStatistics(relations_train);
		System.out.println();		
		System.out.println("Statistics relations TEST");
		classStatistics(relations_test);
		System.out.println();
		Map<String,Integer> relations_total = new HashMap<String, Integer>();		
		System.out.println("Statistics relations TOTAL");
		for (String r : relations_test.keySet()) {
			int count_train = relations_test.get(r);
			int count_test  = relations_train.get(r);
			relations_total.put(r, count_test+count_train);			
		}
		classStatistics(relations_total);
		System.out.println();
		
		/* sentence length statistics */	
		/*
		System.out.println("Statistics sentences TRAIN");
		sentenceStatistics(sentences_train_size);
		System.out.println();
		System.out.println("Statistics sentences TEST");
		sentenceStatistics(sentences_test_size);
		System.out.println();
		System.out.println("Statistics sentences TOTAL");
		Vector<Integer> total_sentences = new Vector<Integer>();
		total_sentences.addAll(sentences_test_size);
		total_sentences.addAll(sentences_train_size);
		sentenceStatistics(total_sentences);
		*/
	}

	static void sentenceStatistics(Vector<Integer> sentences_size) {
		int total = 0;
		for (Integer s : sentences_size) { total += s;}
			double average = (double) total / (double) sentences_size.size();   
			System.out.println("Avg. sentence Length: " + average);   
			List<Double> distance_to_average = new Vector<Double>();      
			for (Integer s : sentences_size) { 
	           double difference = (double) s - (average);
	           distance_to_average.add(Math.pow(difference, 2));   
			}
	   double stdvt = 0.0;
	   for (Double d: distance_to_average) {stdvt += d;}   
	   System.out.println("StDev. sentence Length: " +  Math.sqrt(stdvt / (double) distance_to_average.size()));
	}

	static void classStatistics(Map<String, Integer> relations) { int total = 0;
		   for (String c : relations.keySet()) { total += relations.get(c);}
		   double average = (double) total / (double) relations.keySet().size();
		   System.out.println("Avg. class instances: " + average);
		   
		   Vector<Double> distance_to_average = new Vector<Double>();
		   for (String c : relations.keySet()) { 
		           double difference = (double) relations.get(c) - (average);
		           distance_to_average.add(Math.pow(difference, 2));
		   }   
		   double stdvt = 0.0;
		   for (Double d: distance_to_average) {stdvt += d;}   
		   System.out.println("StDev. class instances: " +  Math.sqrt(stdvt / (double) distance_to_average.size()));
	}

	static void statsRelations(String type, boolean checked,Map<String, Integer> relations_train, Map<String, Integer> relations_test, int sentences_train, int sentences_test, String sentence, int words_train, int words_test, Vector<Integer> sentences_train_size, Vector<Integer> sentences_test_size) {
		if (checked) {
			sentences_test++;
			sentences_test_size.add(sentence.split("\\s+").length);
			words_test += sentence.split("\\s+").length;
			try {
				int count = relations_test.get(type);
				relations_test.put(type, count++);
			} catch (Exception e) {
				relations_test.put(type, 1);
			}		
			
		} else if (!checked) {
			sentences_train++;
			sentences_train_size.add(sentence.split("\\s+").length);
			words_train += sentence.split("\\s+").length;
			try {
				int count = relations_train.get(type);
				relations_train.put(type, count++);
			} catch (Exception e) {
				relations_train.put(type, 1);
			}	
		}
	}
	
	public static void processExample(String before, String after,String between, String type, PrintWriter out) {
		
		int window = 3;
		int casing = 1;
		
		out.print(type);
		if (before.lastIndexOf(",") != -1 && before.lastIndexOf(",") < before.lastIndexOf(between)) before = before.substring(before.lastIndexOf(",") + 1);		
		if (after.indexOf(",") != -1 && after.indexOf(",") > between.length()) after = after.substring(0, after.lastIndexOf(","));				
		
		int betweenLength = PortuguesePOSTagger.tokenize(between).length;
		int beforeLength = PortuguesePOSTagger.tokenize(before).length;
		int afterLength = PortuguesePOSTagger.tokenize(after).length;		
		
		if (beforeLength >= Math.max(betweenLength, afterLength)) out.print(" " + "LARGER_BEF");
		if (afterLength >= Math.max(betweenLength, beforeLength)) out.print(" " + "LARGER_AFT");
		if (betweenLength >= Math.max(afterLength, beforeLength)) out.print(" " + "LARGER_BET");
		
		if (beforeLength == 0) out.print(" " + "EMPTY_BEF");
		if (afterLength == 0) out.print(" " + "EMPTY_AFT");
		if (betweenLength == 0) out.print(" " + "EMPTY_BET");
		ArrayList<String> someCollection = new ArrayList<String>();
		for (String aux : new String[] { "BEF\t" + before, "BET\t" + between, "AFT\t" + after })
			someCollection.add(aux);
		for (String obj : someCollection) {
			String prefix = obj.substring(0, obj.indexOf("\t"));
			String str = obj.substring(obj.indexOf("\t") + 1);
			out.print(" " + generateNGrams(str, prefix, betweenLength, casing, window));
		}
		out.println();
	}
}