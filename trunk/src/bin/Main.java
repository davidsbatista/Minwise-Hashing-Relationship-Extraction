package bin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import datasets.GenerateSetsEN;
import datasets.GenerateSetsPT;
import datasets.TestClassification;
import datasets.WikiPT.Relations;

public class Main {
		
	public static void main(String[] args) throws Exception {
	
		if (args.length < 5) {
		  System.out.println("usage is: dataset true|false signature bands knn [train_file] [test file]");
		  System.out.println("dataset: semeval wiki aimed drugbank wikipt publico");
		  System.out.println("generate examples: 	true|false");
		  System.out.println("signature:  			number of hash signatures");
		  System.out.println("bands:  				number of hash bands");
		  System.out.println("knn: 					number of closest neighbors to consider");
	      System.exit(0);
	  }
		else {
			
			TestClassification.signature = Integer.parseInt(args[2]);
			TestClassification.bands = Integer.parseInt(args[3]);
			TestClassification.knn = Integer.parseInt(args[4]);
			  
			  System.out.println("signature: " + TestClassification.signature);
			  System.out.println("bands: " + TestClassification.bands);
			  System.out.println("knn: " + TestClassification.knn);
			  
			  if (args[0].equalsIgnoreCase("semeval") && args[1].equalsIgnoreCase("false")) TestClassification.testSemEval();
			  else if (args[0].equalsIgnoreCase("semeval") && args[1].equalsIgnoreCase("true")) {				  
				  GenerateSetsEN.generateDataSemEval(args[5],args[6]);
				  TestClassification.testSemEval();
			  }
			  
			  if (args[0].equalsIgnoreCase("aimed") && args[1].equalsIgnoreCase("false")) TestClassification.testAIMED();
			  else if (args[0].equalsIgnoreCase("aimed") && args[1].equalsIgnoreCase("true")) {
				  GenerateSetsEN.generateDataAIMED();
				  TestClassification.testAIMED();			  		  			  
			  }
			  
			  if (args[0].equalsIgnoreCase("wiki") && args[1].equalsIgnoreCase("false")) TestClassification.testWikiEN();		  
			  else if (args[0].equalsIgnoreCase("wiki") && args[1].equalsIgnoreCase("true")) {
				  GenerateSetsEN.generateDataWikiEn();
				  TestClassification.testWikiEN();
			  }
			  
			  if (args[0].equals("publico")) {
				  GenerateSetsPT.generatePublico();
				  TestClassification.classifyPublico();
			  }			  
			  
			  if ((args[0].equals("drugbank")) && args[1].equalsIgnoreCase("false")) TestClassification.testDrugBank();
			  else if (args[0].equals("drugbank") && (args[1].equals("true"))) {
				  GenerateSetsEN.generateDataDrugBank();
				  TestClassification.testDrugBank();				  
			  }
			  
			  if (args[0].equals("wikipt") && args[1].equalsIgnoreCase("false")) TestClassification.testWikiPT(args[5],args[6]);
			  else if (args[0].equals("wikipt") && args[1].equalsIgnoreCase("true")) {				  
				  GenerateSetsPT.generateWikiPT();
				  TestClassification.testWikiPT(args[5],args[6]);
			  }			  
		}
		System.exit(0);
	}

	
	/*
	 * code used to generate file with the aggregated relations from the Portuguese DBpedia
	 * available at: http://dmir.inesc-id.pt/project/DBpediaRelations-PT_01_in_English	 
	 */	
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
