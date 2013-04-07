package datasets.WikiPT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import nlputils.PortugueseNLP;

public class GenerateSets {

	public static void generateWikiPT() throws Exception, IOException {
		System.out.println("Generating WikiPT data...");
		processWikiPT("Datasets/WikiPT/results-relation-extraction.txt",new PrintWriter(new FileWriter("train-data-wikipt.txt")));
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

	public static void processWikiPT(String file, PrintWriter out) throws Exception {
		BufferedReader input = new BufferedReader(new FileReader(file));
		String aux = null;
		String sentence = null;
		String type = null;
		String e1 = null;
		String e2 = null;
		String e1_type = null;
		String e2_type = null;
		String direction = null;
		boolean checked = false;
		String before = null;
		String between = null;
		String after = null;
		while ((aux = input.readLine()) != null) {
			if (aux.startsWith("SENTENCE")) {
				sentence = aux.split(": ")[1];
				aux = input.readLine();
				if (aux.equals("")) aux = input.readLine();
				if (aux.startsWith("MANUALLY CHECKED : TRUE")) checked = true; else checked = false;
				aux = input.readLine();
				while (!aux.startsWith("*")) {
					if (aux.startsWith("ENTITY1")) e1 = aux.split(": ")[1];
					if (aux.startsWith("ENTITY2")) e2 = aux.split(": ")[1];
					if (aux.startsWith("TYPE1")) e1_type = aux.split(": ")[1];
					if (aux.startsWith("TYPE2")) e2_type = aux.split(": ")[1];
					if (aux.startsWith("REL TYPE")) type = aux.split(": ")[1];
					aux = input.readLine();
				}
				
				int e1_count = countWords(e1,sentence);
				int e2_count = countWords(e2,sentence);
				
				if (checked && !e2.equals("Lima") && !e2.equals("Terceira") && (e1_count==1) && (e2_count==1)) {
						
					int e1_start = sentence.indexOf(e1);
					int e1_finish = sentence.indexOf(e1)+e1.length();
					
					int e2_start = sentence.indexOf(e2);
					int e2_finish = sentence.indexOf(e2)+e2.length();
					
					if (e1_start!=-1 && e2_start!=-1) {
						
						if (e1_finish < e2_start) direction = "(e1,e2)";
						else if (e1_start > e2_finish) direction = "(e2,e1)";

						/*
						System.out.println(sentence);
						System.out.println("e1: " + e1 + '\t' + e1_type + '\t' + "("+e1_start+","+e1_finish+")" + '\t' + e1_count);
						System.out.println("e2: " + e2 + '\t' + e2_type + '\t' + "("+e2_start+","+e2_finish+")" + '\t' + e2_count);
						System.out.println(type + direction);
						*/
						
						before = sentence.substring(0,Math.min(e1_finish, e2_finish));
						between = sentence.substring(Math.min(e1_finish, e2_finish),Math.max(e1_start,e2_start));
						after = sentence.substring(Math.max(e1_start, e2_start),sentence.length()-1);
							
						before = before + " " + between;
						after = between + " " + after;
						
						/*
						System.out.println();
						System.out.println("before: " + before);
						System.out.println("between: " + between);
						System.out.println("after: " + after);
						System.out.println();						
						System.out.println("\n========");
						*/
					}
					processExample(before,after,between,type,out); 
				}
			}
		}
		out.flush();
		input.close();
	}
	
	public static void processExample ( String before, String after, String between, String type, PrintWriter out ) {
	     out.print(type);
	     
	     if ( before.lastIndexOf(",") != -1 && before.lastIndexOf(",") < before.lastIndexOf(between) ) before = before.substring(before.lastIndexOf(",") + 1);
	     if ( after.indexOf(",") != -1 && after.indexOf(",") > between.length()) after = after.substring(0,after.lastIndexOf(","));
	     
	     int betweenLength = between.split(" +").length;     
	     int beforeLength = before.split(" +").length;
	     int afterLength = after.split(" +").length;
	     
	     if ( beforeLength >= Math.max(betweenLength, afterLength) ) out.print(" " + "LARGER_BEF"); 
	     if ( afterLength >= Math.max(betweenLength, beforeLength) ) out.print(" " + "LARGER_AFT"); 
	     if ( betweenLength >= Math.max(afterLength, beforeLength) ) out.print(" " + "LARGER_BET");
	     
	     if ( beforeLength == 0 ) out.print(" " + "EMPTY_BEF"); 
	     if ( afterLength == 0 ) out.print(" " + "EMPTY_AFT"); 
	     if ( betweenLength == 0 ) out.print(" " + "EMPTY_BET");
	     
	     ArrayList<String> someCollection = new ArrayList<String>();     
	     
	     for ( String aux : new String[]{ "BEF\t" + before, "BET\t" + between, "AFT\t" + after } ) someCollection.add(aux);
	     for ( String obj : someCollection ) {
	    			String suffix = obj.substring(0,obj.indexOf("\t"));
	    			String str = obj.substring(obj.indexOf("\t")+1);
	    			out.print(" " + PortugueseNLP.generateNGrams(str, suffix, 5, 1, 0));
	     }
	     out.println();
	}
}