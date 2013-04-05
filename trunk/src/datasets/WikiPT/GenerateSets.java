package datasets.WikiPT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GenerateSets {

	public static void generateWikiPT() throws Exception, IOException {
		System.out.println("Generating WikiPT data...");
		processWikiPT("Datasets/WikiPT/results-relation-extraction.txt",new PrintWriter(new FileWriter("train-data-wikipt.txt")));
	}
	
	public static int countWords(String sentence, String entity) {
		int count = 0;
		String s = sentence;
		String w = entity;
		int result = s.indexOf(w);
		while (result != -1) {
			result = s.indexOf(w, result + 1);
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
				if (checked) {
					
					int e1_count = countWords(e1,sentence);
					int e2_count = countWords(e2,sentence);
					
					int e1_start = sentence.indexOf(e1);
					int e1_finish = sentence.indexOf(e1)+e1.length();
					
					int e2_start = sentence.indexOf(e2);
					int e2_finish = sentence.indexOf(e2)+e2.length();
					
					if (e1_finish < e2_start) direction = "(e1,e2)";
					else if (e1_start > e2_finish) direction = "(e2,e1)";

					System.out.println(sentence);
					System.out.println("e1: " + e1 + '\t' + e1_type + '\t' + "("+e1_start+","+e1_finish+")" + '\t' + e1_count);
					System.out.println("e2: " + e2 + '\t' + e2_type + '\t' + "("+e2_start+","+e2_finish+")" + '\t' + e2_count);
					System.out.println(type + direction);
					
					System.out.println("\n========");
				}
			}
		}
		out.flush();
		input.close();
	}
}
