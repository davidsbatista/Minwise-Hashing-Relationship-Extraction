package bin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import datasets.WikiPT.Relations;

public class Main {
	
	public static void main(String[] args) throws Exception {
		writeRelationsFile(args[0]);			
	}
	
	public static void writeRelationsFile(String inputFile) throws IOException{
		Relations.initialize();
		PrintWriter out = new PrintWriter(new FileWriter("data-wikipt.txt"));
		BufferedReader input = new BufferedReader(new FileReader(inputFile));
		String aux = null;
		String type = null;
		String e1 = null;
		String e2 = null;
		while ((aux = input.readLine()) != null) {			
			if (!aux.startsWith("REL TYPE")) out.write(aux + "\n");						
			
			else if (aux.startsWith("REL TYPE")) {				
				
				type = aux.split(": ")[1];				
				
				if (Arrays.asList(Relations.ignore).contains(type)) type="other";
				else {					
					if (!Arrays.asList(Relations.changeDirection).contains(type)) {
						//transform relationship type into a top aggregated type
						type  = Relations.aggregatedRelations.get(type);
					}
					
					else {
						//keyPerson, president, leaderName to keyPerson
						if (type.equals("keyPerson") || type.equals("president") || type.equals("leaderName")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "keyPerson";
						}
	
						//currentMember and pastMember to partOf  
						if (type.equals("currentMember") || type.equals("pastMember")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "partOf";
						}
							
						//capitalCountry to locatedinArea
						if (type.equals("capitalCountry") || type.equals("city") || type.equals("capital")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "locatedInArea";
						}
							
						//more examples of "influencedBy"
						if (type.equals("influenced") || type.equals("doctoralAdvisor")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "influencedBy";
						}
	
						//more examples of "successor"
						if (type.equals("predecessor") || type.equals("successor")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "successor";
						}
							
						//more examples of "parent"
						if (type.equals("child")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "parent";
						}						
							
						//more examples of "keyPerson"
						if (type.equals("foundedBy")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "keyPerson";
						}
							
						//largestCity to locatedInArea
						if (type.equals("largestCity")) {
							String tmp = e2;
							e2 = e1;
							e1 = tmp;
							type = "locatedInArea";							
						}
					}				
				}
				out.write("REL TYPE : " + type +'\n');
			}
		}
				
	}
}
