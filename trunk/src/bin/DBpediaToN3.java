package bin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import datasets.WikiPT.Relations;

public class DBpediaToN3 {
	
	static String base_url = "http://dbpedia.org/resource/";
	
	public static void printRelations(String e1, String e2, String type, String type1, String type2, String original) {
		if (semanticsValid(type, type1, type2)) {
			System.out.print("<"+base_url+e1.replaceAll("\\s", "_")+">");
			System.out.print(" ");
			System.out.print("<"+type+">");
			System.out.print(" ");
			System.out.print("<"+base_url+e2.replaceAll("\\s", "_")+">");
			System.out.print(".\t\t # "+ original + "\n");
		}		
	}
	
	public static boolean semanticsValid(String relation, String type1, String type2) {		
		if (relation.equals("deathOrBurialPlace")) if ( type1.equals("PERSON") && type2.equals("LOCATION")) return true;
		if (relation.equals("keyPerson")) if (type1.equals("PERSON") && (type2.equals("LOCATION ") || type2.equals("ORGANIZATION"))) return true;		
		if (relation.equals("locatedInArea")) if ((type1.equals("ORGANIZATION") ||  type1.equals("LOCATION")) && type2.equals("LOCATION")) return true;  		
		if (relation.equals("origin")) if ( (type1.equals("PERSON") || type1.equals("ORGANIZATION")) && type2.equals("LOCATION")) return true; 		
		if (relation.equals("parent")) if ( type1.equals("PERSON") && type2.equals("PERSON")) return true;
		if (relation.equals("successor")) if ( type1.equals("PERSON") && type2.equals("PERSON")) return true;
		if (relation.equals("partner")) if ( type1.equals("PERSON") && type2.equals("PERSON")) return true;		
		if (relation.equals("influencedBy")) if ( (type1.equals("PERSON") || type1.equals("ORGANIZATION")) && (type2.equals("PERSON") || type2.equals("ORGANIZATION")) ) return true;		
		if (relation.equals("partOf")) if ( type1.equals("ORGANIZATION") && type2.equals("ORGANIZATION") || type1.equals("PERSON") && type1.equals("ORGANIZATION")) return true;
		return false;		
	}

	public static void main(String[] args) throws IOException {
		Relations.initialize();
		BufferedReader input = new BufferedReader(new FileReader(args[0]));
		String aux = null;
		String relation = null;
		String rel_normalized = null;
		String e1 = null;
		String e2 = null;
		String type1 = null;
		String type2 = null;
		
		while ((aux = input.readLine()) != null) {			
				if (aux.startsWith("ENTITY1")) e1 = aux.split(": ")[1].trim();
				if (aux.startsWith("ENTITY2")) e2 = aux.split(": ")[1].trim();				
				if (aux.startsWith("TYPE1")) type1 = aux.split(": ")[1].trim();
				if (aux.startsWith("TYPE2")) type2 = aux.split(": ")[1].trim();
				if (aux.startsWith("REL TYPE")) {
					relation = aux.split(": ")[1];
					if (!Arrays.asList(Relations.ignore).contains(relation) && !Arrays.asList(Relations.other).contains(relation)) {
						// transform relationship type into a top aggregated type
						if (!Arrays.asList(Relations.changeDirection).contains(relation)) {							
							rel_normalized = Relations.aggregatedRelations.get(relation);
							printRelations(e1,e2,rel_normalized,type1,type2,relation);					
						}						
						//change directions
						else {					
							if (relation.equals("keyPerson") || relation.equals("president") || relation.equals("leaderName")) printRelations(e2,e1,"keyPerson",type1,type2,relation);
							
							// currentMember and pastMember to partOf
							if (relation.equals("currentMember") || relation.equals("pastMember")) printRelations(e2,e1,"partOf",type1,type2,relation);
							
							// capitalCountry to locatedinArea
							if (relation.equals("capitalCountry") || relation.equals("city") || relation.equals("capital")) printRelations(e2,e1,"locatedInArea",type1,type2,relation);
	
							// more examples of "influencedBy"
							if (relation.equals("influenced") || relation.equals("doctoralAdvisor")) printRelations(e2,e1,"influencedBy",type1,type2,relation);
	
							// more examples of "successor"
							if (relation.equals("predecessor") || relation.equals("successor")) printRelations(e2,e1,"successor",type1,type2,relation);
	
							// more examples of "parent"
							if (relation.equals("child")) printRelations(e2,e1,"parent",type1,type2,relation);
	
							// more examples of "keyPerson"
							if (relation.equals("foundedBy")) printRelations(e2,e1,"keyPerson",type1,type2,relation);
						}
					}
				}
			}		
		}
	}