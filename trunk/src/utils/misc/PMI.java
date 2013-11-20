package utils.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import datasets.WikiPT.Relations;

public class PMI {
	
	static LinkedList<String> patterns = new LinkedList<String>();
	static Map<String,Integer> number_sentences_per_class = new HashMap<String, Integer>();		//class 	-> frequ
	static Map<String,Integer> number_sentences_with_pattern = new HashMap<String, Integer>();	//pattern 	-> freq
	static Map<String,Integer> class_pattern_occurrences = new HashMap<String, Integer>(); 		//class,pattern -> freq	
	static int total_sentences = 0;
	
	public static Map<String, Set<String>> readScores(String file, int topk) throws IOException {
		
		Map<String,SortedSet<Pair<Float, String>>> class_patterns = new HashMap<String,SortedSet<Pair<Float, String>>>();
		BufferedReader input = new BufferedReader( new FileReader(file) );
		String type;
		String line;
		String pattern;
		float score;
		while ( ( line = input.readLine() ) != null ) {
			String[] data = line.split("\\t");
			type = data[0];
			pattern = data[1];
			score = Float.parseFloat(data[2]);
			Pair<Float, String> score_patttern = new Pair<Float, String>(score, pattern);			
			if (class_patterns.keySet().contains(type)) {
				class_patterns.get(type).add(score_patttern);
			}
			else {
				SortedSet<Pair<Float, String>> set = new TreeSet<Pair<Float, String>>(new ScorePatternComparator());
				set.add(score_patttern);
				class_patterns.put(type, set);
			}			
		}
		
		Map<String,Set<String>> top_patterns = new HashMap<String, Set<String>>();
		
		for (String t : class_patterns.keySet()) {
			
			//initialize structure
			Set<String> patterns = new HashSet<String>();
			top_patterns.put(t, patterns);						
			
			//get top-k
			Iterator<Pair<Float, String>> it = class_patterns.get(t).iterator();
			
			int i=0;
			while (i<topk && it.hasNext()) {
				Pair<Float,String> p = it.next();
				top_patterns.get(t).add(p.getSecond());
				i++;
			}			
		}
		
		return top_patterns;
	}
	
	
	public static void main(String[] args) throws IOException{
		System.out.println(args[0]);
		readScores(args[0],5);
		/*
		readPatterns(args[0]);
		System.out.println(patterns.size() + " patterns");
		readSentencesSemEval(args[1]);
		System.out.println(total_sentences + " sentences");
		System.out.println(number_sentences_per_class.size() + " types");
		System.out.println(number_sentences_with_pattern.size() + " patterns occurring at least in 1 phrase");
		System.out.println(class_pattern_occurrences.size() + " pairs (type,pattern) ");
		calculatePMI();
		*/
	}
	
	public static void calculateWikiPTPMI(String args[]) throws IOException {
		Relations.initialize();		
		readPatterns(args[0]);
		readSentencesWikiPT(args[1]);						
		calculatePMI();
	}
	
	public static void calculatePMI() {
		for (String type : number_sentences_per_class.keySet()) {
			for (String pattern : patterns) {				
				String type_pattern = type + "--" + pattern;				
				if (!class_pattern_occurrences.containsKey(type_pattern)) continue;
				else {
					double numerator = (double) class_pattern_occurrences.get(type_pattern) / (double) total_sentences;
					double denominator = (number_sentences_per_class.get(type) / (double) total_sentences) * ((double) number_sentences_with_pattern.get(pattern) / (double) total_sentences);				
					double pmi = Math.log(numerator/denominator);					 
					float normalizedPMI = (float) (pmi / Math.log(1/numerator));  
					System.out.println(type+'\t'+pattern+'\t'+String.valueOf(normalizedPMI));
				}
			}
		}
	}
	
	public static void readPatterns(String file) throws IOException {		
		BufferedReader input = new BufferedReader( new FileReader(file) );
		String aux;
		while ( ( aux = input.readLine() ) != null ) {
			patterns.add(aux);
		}
		input.close();
	}
	
	public static void readSentencesSemEval(String file) throws IOException {
		BufferedReader input = new BufferedReader( new FileReader(file) );
		String aux = null;
		String sentence = null;
		String type = null;
		while ( ( aux = input.readLine() ) != null ) {
			if ( aux.contains("\t\"") ) {
				sentence = aux.substring(aux.indexOf("\"")+1,aux.lastIndexOf("\""));				
				type = input.readLine().trim();
				total_sentences++;
				try {
					int count = number_sentences_per_class.get(type);
					number_sentences_per_class.put(type, count+1);
				} catch (Exception e) {
					number_sentences_per_class.put(type, 1);
				}
				
				for (String p : patterns) {					
					String pattern = p.replaceAll("_", " ").replaceAll("RVB", "");
					if (sentence.contains(pattern)) {						
						//update number of sentences where pattern occurs 
						try {
							int count = number_sentences_with_pattern.get(p);
							count++;
							number_sentences_with_pattern.put(p, count);
						} catch (Exception e) {
							number_sentences_with_pattern.put(p, 1);
						}						
						//update number of classes where pattern occurs						
						String type_pattern = type + "--" + p;						
						try {
							int count = class_pattern_occurrences.get(type_pattern);						
							count++;
							class_pattern_occurrences.put(type_pattern, count);
						} catch (Exception e) {
							class_pattern_occurrences.put(type_pattern, 1);
						}
					}
				}
		    }
		}		
		input.close();
	}

	public static void readSentencesWikiPT(String file) throws IOException {
		
		BufferedReader input = new BufferedReader( new FileReader(file) );
		String aux;
		String type = null;
		String type_norm = null;
		String sentence; 		
		while ((aux = input.readLine()) != null) {
			if (aux.startsWith("SENTENCE")) {
				sentence = aux.split(": ")[1];				
				sentence = sentence.replaceAll("&nbsp;", "").replaceAll("&mdash;", "—").replaceAll("&ndash", "–").replaceAll("&bull;", "•");
				sentence = sentence.replaceAll("\\[?URLTOKEN\\s?([A-Za-z0-9íÍÌìàÀáÁâÂâÂãÃçÇéÉêÊóÓõÕôÔúÚüÜ\\.\\s,\\+\\(\\)\\-]+)?\\]?", "");
				while (!aux.startsWith("*")) {
					aux = input.readLine();
					if (aux.startsWith("REL TYPE")) type = aux.split(": ")[1];					
					//transform relationship type into a top aggregated type
					if (!Arrays.asList(Relations.ignore).contains(type) &&  (!Arrays.asList(Relations.changeDirection).contains(type))) {							
							type_norm = Relations.aggregatedRelations.get(type);
						}
				}
				total_sentences++;
				
				try {
					int count = number_sentences_per_class.get(type);
					number_sentences_per_class.put(type, count+1);
				} catch (Exception e) {
					number_sentences_per_class.put(type, 1);
				}
				
				for (String p : patterns) {					
					String pattern = p.replaceAll("_", " ").replaceAll("RVB", "");
					if (sentence.contains(pattern)) {
						
						//update number of sentences where pattern occurs 
						try {
							int count = number_sentences_with_pattern.get(p);
							count++;
							number_sentences_with_pattern.put(p, count);
						} catch (Exception e) {
							number_sentences_with_pattern.put(p, 1);
						}
						
						//update number of classes where pattern occurs						
						String type_pattern = type_norm + "--" + p;						
						try {
							int count = class_pattern_occurrences.get(type_pattern);						
							count++;
							class_pattern_occurrences.put(type_pattern, count);
						} catch (Exception e) {
							class_pattern_occurrences.put(type_pattern, 1);
						}
					}
				}
				
			}
			aux = input.readLine();
		}
		input.close();
	}
}