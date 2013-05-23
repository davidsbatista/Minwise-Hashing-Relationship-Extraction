package utils.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PMI {
	
	static LinkedList<String> patterns = new LinkedList<String>();
	static Map<String,Integer> number_sentences_per_class = new HashMap<String, Integer>();
	static Map<String,Integer> number_sentences_with_pattern = new HashMap<String, Integer>();	
	static Map<String,Integer> class_pattern_occurrences = new HashMap<String, Integer>(); //class,pattern -> occr	
	static int total_sentences = 0;
	
	public static void main(String args[]) throws IOException {		
		System.out.println("patterns file: " + args[0]);
		System.out.println("sentences file: " + args[1]);
		readPatterns(args[0]);
		System.out.println(patterns.size() + " patterns");
		readSentences(args[1]);				
		System.out.println(total_sentences + " sentences");
		System.out.println(number_sentences_per_class.size() + " types");
		System.out.println(number_sentences_with_pattern.size() + " patterns occurring at least in 1 phrase");
		System.out.println(class_pattern_occurrences.size() + " pairs (type,pattern) ");
		
		for (String key : class_pattern_occurrences.keySet()) {
			System.out.println(key);
		}
		
		System.out.println("====");
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
	
	public static void readSentences(String file) throws IOException {
		
		BufferedReader input = new BufferedReader( new FileReader(file) );
		String aux;
		String type = null;
		String sentence; 		
		while ((aux = input.readLine()) != null) {
			if (aux.startsWith("SENTENCE")) {
				sentence = aux.split(": ")[1];				
				sentence = sentence.replaceAll("&nbsp;", "").replaceAll("&mdash;", "—").replaceAll("&ndash", "–").replaceAll("&bull;", "•");
				sentence = sentence.replaceAll("\\[?URLTOKEN\\s?([A-Za-z0-9íÍÌìàÀáÁâÂâÂãÃçÇéÉêÊóÓõÕôÔúÚüÜ\\.\\s,\\+\\(\\)\\-]+)?\\]?", "");
				while (!aux.startsWith("*")) {
					aux = input.readLine();
					if (aux.startsWith("REL TYPE")) type = aux.split(": ")[1];								
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
						}String pair = type + "--" + p;
						
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
			aux = input.readLine();
		}
		input.close();
	}
}
		

