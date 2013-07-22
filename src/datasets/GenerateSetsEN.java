package datasets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.nlp.EnglishNLP;
import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.ICU4JBreakIteratorSentenceSplitter;

public class GenerateSetsEN {

	 public static Map<String,Integer> entropyMap;
	 public static Map<String,Integer> frequencyMap;
	 public static int minFreqThreshold = 2;
 
	 public static void processSemEval ( String file, PrintWriter out ) throws Exception {
	   BufferedReader input = new BufferedReader( new FileReader(file) );
	   String aux = null;
	   String sentence = null;
	   String type = null;
	   while ( ( aux = input.readLine() ) != null ) {
	     if ( aux.contains("\t\"") ) {
	       sentence = aux.substring(aux.indexOf("\"")+1,aux.lastIndexOf("\""));
		   String before = sentence.substring(0,Math.min(sentence.indexOf("</e1>"),sentence.indexOf("</e2>"))).trim();
		   String after = sentence.substring(Math.max(sentence.indexOf("<e2>")+4,sentence.indexOf("<e1>")+4)).trim();  	   
		   String between = sentence.substring(Math.min(sentence.indexOf("</e1>")+5,sentence.indexOf("</e2>")+5),Math.max(sentence.indexOf("<e2>"),sentence.indexOf("<e1>"))).trim();  
		   between = between.replaceAll("</?e[12] *>","");
		   before = before.replaceAll("</?e[12] *>","") + " " + between;
		   after = between + " " + after.replaceAll("</?e[12] *>","");
		   type = input.readLine().trim();
		   if (!TestClassification.SemEvalAsymmetrical) type = type.split("\\(")[0];
		   processExample(before,after,between,type,out); 
	     }
	   }
	   out.flush();
	   input.close();
	 }
 
 	 public static void processDrugBank( Map<String,String> instance, PrintWriter out ) throws Exception {
		 String sentence = instance.get("sentence");
		 String type = instance.get("type");
		 String before = sentence.substring(0,Math.min(sentence.indexOf("</e0>"),sentence.indexOf("</e1>"))).trim();
		 String after = sentence.substring(Math.max(sentence.indexOf("<e0>") + 4,sentence.indexOf("<e0>") + 4)).trim();
		 String between = sentence.substring(Math.min(sentence.indexOf("</e0>") + 5,sentence.indexOf("</e1>") + 5),Math.max(sentence.indexOf("<e1>"),sentence.indexOf("<e0>"))).trim();	
		 between = between.replaceAll("</?e[01] *>", "");
		 before = before.replaceAll("</?e[01] *>", "") + " " + between;
		 after = between + " " + after.replaceAll("</?e[01] *>", "");	
		 processExample(before,after,between,type,out);
	 }

	 public static void createTestTrain( String file, float test_portion ) throws Exception {
		 	List<Map<String,String>> instances = new LinkedList<Map<String,String>>();
		   	BufferedReader input = new BufferedReader( new FileReader(file) );
		   	String aux = null;
		   	String sentence = null;
		   	String type = null;	   	
			while ( ( aux = input.readLine() ) != null ) {
				if (aux.contains("\t")) {
					String[] data = aux.split("\t");
					sentence = data[1];				
					String tmp = input.readLine();
					type = tmp.split("Comment:")[0];
					Map<String,String> instance = new HashMap<String, String>();
					instance.put("type", type);
					instance.put("sentence",sentence);
					instances.add(instance);
				}
			}
			input.close();
			
			System.out.println(instances.size()  + " instances loaded");		
			System.out.println("Using " + test_portion + " for test");
			
			//generate train data
			System.out.println("Generating train instances from 0 to " +  Math.round(test_portion*instances.size()) );
			PrintWriter train = new PrintWriter(new FileWriter("train-data-drugbank.txt"));
			for (int i = 0; i < Math.round(test_portion*instances.size()); i++) {			
				processDrugBank(instances.get(i), train);
			}
			train.close();
			
			
			//generate test data		
			System.out.println("Generating test instances from " + (int) (test_portion*instances.size()) + " to " + (int) ((instances.size())));
			PrintWriter test = new PrintWriter(new FileWriter("test-data-drugbank.txt"));
			for (int i = Math.round(test_portion*instances.size()); i < instances.size(); i++) {
				processDrugBank(instances.get(i), test);			
			}
			test.close();
	 }
  
	 public static void processWikipediaEN ( String file, PrintWriter out ) throws Exception {
	      
	   BufferedReader input = new BufferedReader( new FileReader(file) );   
	   String aux = null;
	   String entity1 = null;
	   String type = null;
	   List<String> avoidClasses = Arrays.asList("descendant","discovered","gpe_competition","grandmother","inventor","supported_person","uncle");
   	
	     while ( ( aux = input.readLine() ) != null ) {	   
		   if ( aux.startsWith("url=") ) entity1 = aux.substring(aux.lastIndexOf("/")+1).replace("_"," "); else if ( aux.trim().length() != 0) {
			   aux = aux.replaceAll("</?i>","").replaceAll("&amp;","&").replaceAll("</?b>","").replaceAll("<br[^>]+>","").replaceAll("<a +href *= *\"[^\"]+\"( +class *= *\"[^\"]+\")?( +title *= *\"[^\"]+\")?","<a");
			   aux = aux.replaceAll("</?span[^>]*>","").replaceAll("</?sup[^>]*>","").replaceAll("<a [^>]*class='external autonumber'[^>]*>[^<]+</a>" , "");
			   entity1 = entity1.replaceAll(" \\(.*","").replaceAll(",","").trim();		   		   
			   List<List<String>> new_sentences = new Vector<List<String>>();		   
			   List<List<String>> sentences = new ICU4JBreakIteratorSentenceSplitter().extractSentences(aux);		   		   
			   /* fix extracted sentences */
			   new_sentences = new Vector<List<String>>();
			   int i = 0;
			   
			   while (i < sentences.size()) {				   
				   List<String> s = sentences.get(i);
				   List<String> new_sentence = new ArrayList<String>();
				   new_sentence.addAll(sentences.get(i));
				   while (s.get(s.size()-1).matches("[A-Z]\\.")) {					   
					   new_sentence.addAll(sentences.get(i+1));
					   i++;
					   s = sentences.get(i);
				   }
				   new_sentences.add(new_sentence);
				   i++;
			   }   
			   String auxS[] = entity1.split(" +");
			   for ( List<String> tokens : new_sentences ) {
				String sentence = ""; for ( String auxT : tokens ) sentence += " " + auxT;
				sentence = sentence.trim().replaceAll("< a relation = \" ", "<a relation=\"").replaceAll(" \" > ", "\">").replaceAll(" < / a >", "</a>").replaceAll("< a > ", "<a>");			
				if (sentence.contains("Franklins")) sentence = sentence.replaceFirst(" Franklins ", " <a>"+entity1+"</a> ");
				if (sentence.contains("Einsteins")) sentence = sentence.replaceFirst(" Einsteins ", " <a>"+entity1+"</a> ");						
				if (sentence.contains("Doles")) sentence = sentence.replaceFirst(" Doles ", " <a>"+entity1+"</a> ");			
				if ( sentence.replaceAll("<a[^>]+>[^<]+</a>","-").contains(" " + entity1 + " ")) sentence = sentence.replaceAll(" " + entity1 + " "," <a>"+entity1+"</a> ");  else {
				   if ( sentence.startsWith(entity1 + " ")) {
					 sentence = sentence.replaceFirst(entity1 + " ","<a>"+entity1+"</a> ");
				   } else if ( sentence.startsWith("He ")) {
				   	 sentence = sentence.replaceFirst("He ","<a>"+entity1+"</a> ");
				   } else if ( sentence.startsWith("She ")) {
				   	 sentence = sentence.replaceFirst("She ","<a>"+entity1+"</a> ");
				   } else if ( sentence.startsWith("His ")) {
				   	 sentence = sentence.replaceFirst("His ","<a>"+entity1+"</a> ");
				   } else if ( sentence.startsWith("Her ")) {
				   	 sentence = sentence.replaceFirst("Her ","<a>"+entity1+"</a> ");
				   	 
				   	 /* sentence starts with surname */
			       } else if ( auxS.length > 1 && (sentence.startsWith(auxS[auxS.length-1]+ " "))) {
			    	 sentence = sentence.replace(auxS[auxS.length-1] + " ","<a>"+entity1+"</a> ");
			       }
				   	 /* sentence starts with surname + 's */
			    	 else if ( auxS.length > 1 && (sentence.startsWith(auxS[auxS.length-1]+"'s" + " "))) {
				    	 sentence = sentence.replace(auxS[auxS.length-1]+"'s" + " ","<a>"+entity1+"</a>'s ");
			    	 }
				    /* sentence starts with surname + ' */
				     else if ( auxS.length > 1 && (sentence.startsWith(auxS[auxS.length-1]+"'" + " "))) {
					    sentence = sentence.replace(auxS[auxS.length-1]+"'" + " ","<a>"+entity1+"</a>' ");
			    	 
			   	     /* sentence starts with first name */
			       } else if ( sentence.startsWith(auxS[0]+ " ")) {	     
			   	     sentence = sentence.replaceFirst(auxS[0]+ " ","<a>"+entity1+"</a> ");
				   } 
				     /* contains first name and last name */
			         else if ( auxS.length >=3 && sentence.contains(" " + auxS[0] + " " + auxS[auxS.length-1] + " ")) {
				   	 sentence = sentence.replaceAll(" " + auxS[0]+ " " + auxS[auxS.length-1] + " "," <a>"+entity1+"</a> ");
				   } 
			         else if ( auxS.length > 1 && sentence.contains(" " + auxS[auxS.length-1]+ " ")) {
				   	 sentence = sentence.replaceFirst(" " + auxS[auxS.length-1]+ " "," <a>"+entity1+"</a> ");			   	 
				   }
				   	/* contains first name only */
					else if ( sentence.contains(" " + auxS[0]+" ")) {
				   	 sentence = sentence.replaceFirst(" " + auxS[0]+ " "," <a>"+entity1+"</a> ");
				   }
				   /* contains surname name with 's */ 
					else if ( sentence.contains(" " + auxS[auxS.length-1]+"'s" + " ")) {
						sentence = sentence.replaceFirst(" " + auxS[auxS.length-1]+"'s" + " "," <a>"+entity1+"</a>'s ");
					}
				   	 else if ( sentence.contains("He ")) {
				   	 sentence = sentence.replaceFirst("He ","<a>"+entity1+"</a> ");
				   } else if ( sentence.contains("She ")) {
				   	 sentence = sentence.replaceFirst("She ","<a>"+entity1+"</a> ");
				   } else if ( sentence.contains("His ")) {
				   	 sentence = sentence.replaceFirst("His ","<a>"+entity1+"</a> ");
				   } else if ( sentence.contains("Her ")) {
				   	 sentence = sentence.replaceFirst("Her ","<a>"+entity1+"</a> ");
				   } else if ( sentence.contains(" he ")) {
				   	 sentence = sentence.replaceFirst(" he "," <a>"+entity1+"</a> ");
				   } else if ( sentence.contains(" she ")) {
				   	 sentence = sentence.replaceFirst(" she "," <a>"+entity1+"</a> ");
				   } else if ( sentence.contains(" his ")) {
				   	 sentence = sentence.replaceFirst(" his "," <a>"+entity1+"</a> ");
				   } else if ( sentence.contains(" her ")) {
				   	 sentence = sentence.replaceFirst(" her "," <a>"+entity1+"</a> ");
				   }
				   else if ( sentence.contains(" Him ")) {
					   	 sentence = sentence.replaceFirst(" Him "," <a>"+entity1+"</a> ");
					}
				   else if ( sentence.contains(" him ")) {
					   	 sentence = sentence.replaceFirst(" him "," <a>"+entity1+"</a> ");
				   }
		   	    }
			    Pattern pattern = Pattern.compile("<a[^>]*>[^<]+</a>");
			    Matcher matcher = pattern.matcher(sentence);
			    while (matcher.find()) {
				   String type1 = matcher.group();
				   if ( !type1.contains(" relation=") ) type1 = "OTHER"; else { 
					   type1 = type1.substring(type1.indexOf(" relation=")+11); 
				       type1 = type1.substring(0, type1.indexOf("\"")); 
				   }			   
				   String after1 = sentence.substring(matcher.end());
				   Matcher matcher2 = pattern.matcher(after1);
			   	   while (matcher2.find()) {
					   String type2 = matcher2.group();
					   if ( !type2.contains(" relation=") ) type2 = "OTHER"; else { 
						   type2 = type2.substring(type2.indexOf(" relation=")+11); 
						   type2 = type2.substring(0, type2.indexOf("\"")); 
					   }
					   String before = sentence.substring(0,matcher.end()).replaceAll("<[^>]+>","");
					   String after = sentence.substring(matcher.end()+matcher2.start()).replaceAll("<[^>]+>","");
					   String between = sentence.substring(matcher.end(),matcher.end()+matcher2.start()).replaceAll("<[^>]+>","");
					   before = before + " " + between;
	                   after = between + " " + after;                   
	                   before = before.replaceAll(" +", " ").trim();
	                   after = after.replaceAll(" +", " ").trim();
	                   between = between.replaceAll(" +", " ").trim();
					   type = "OTHER";
					   if ( !type1.equals("OTHER") && !type2.equals("OTHER")) type = "OTHER";
					   else if ( type1.equals("OTHER") && type2.equals("OTHER")) type = "OTHER";
					   else if ( type1.equals("OTHER") && matcher.group().contains(">"+entity1+"<")) type = type2;
					   else if ( type2.equals("OTHER") && matcher2.group().contains(">"+entity1+"<")) type = type1;
					   
					   if (avoidClasses.contains(type)) type="OTHER";				   
					   if ( (type.equals("OTHER") && Math.random() < 0.975 )) continue;				   
					   //if (!classesWikiEn.contains(type)) continue;
					   
					   processExample(before,after,between,type,out); 
				   }   
			     }
			   }
		   }
	   }
	   out.flush();
	   input.close();
	 }
 
	 public static void processAIMED ( String directory, String fold, PrintWriter out ) throws Exception {
		 
		 Set<String> dataFiles = new HashSet<String>();
		 BufferedReader inputAux = new BufferedReader( new FileReader(fold) );
		 String aux = null;
		 while ( ( aux = inputAux.readLine() ) != null ) dataFiles.add(aux);
		   inputAux.close();
		   for ( File file : new File(directory).listFiles() ) if ( dataFiles.contains(file.getName()) ){ 
		    BufferedReader input = new BufferedReader( new FileReader(file) );
		    String sentence = null;
		    String type = null;
		    while ( ( aux = input.readLine() ) != null ) {
		   	 Set<String> positiveExamples = new HashSet<String>();	 
			 Set<String> negativeExamples = new HashSet<String>();
		     int auxNum1 = 1, auxNum2 = 1;	     
		     for ( int num = 25 ; num <= 50 ; num++ ) aux = aux.replaceFirst("([\\-a-zA-Z0-9] +)<prot>([^<]+)</prot>", "$1<p3  pair=" + num + " >$2</p >");
			 aux = aux.replaceAll("</?prot>","").replaceAll("  +"," ");		 
			 while ( aux.indexOf("<p") != -1 ) {
				 String aux1 = aux.substring(0,aux.indexOf("<p")) + ("<P" + auxNum1++);
			     String aux2 = aux.substring(aux.indexOf("<p")+3);
		         int count = 0;
				 while ( aux2.indexOf("</p") != -1 ) {
					 if ( aux2.substring(0,aux2.indexOf("</p")).indexOf("<p") != -1 ) {
						 aux1 = aux1 + aux2.substring(0,aux2.indexOf("<p")+3);
						 aux2 = aux2.substring(aux2.indexOf("<p")+3);
						 count++;
					 } else if ( count-- <= 0) {
						 aux2 = aux2.substring(0,aux2.indexOf("</p")) + ("<-P" + auxNum2++) + aux2.substring(aux2.indexOf("</p")+4);			 	
						 break;
					 } else {
						 aux1 = aux1 + aux2.substring(0,aux2.indexOf("</p")+4);
						 aux2 = aux2.substring(aux2.indexOf("</p")+4);
					 }
				 }
				 aux = aux1 + aux2;
			 }
		     aux = aux.replaceAll("<P","<p").replaceAll("<-P","</p");
		     sentence = aux;
		     for ( int i = 1; i < 50; i++ ) for ( int j = 1; j < 50; j++ ) for ( int k = j + 1 ; k < 50; k++ ) {
			  if ( aux.contains("<p" + j + " pair="+i+" ") || aux.contains("<p" + k + " pair="+i+" ") ) {
			   type = ( aux.contains("<p" + j + " pair="+i+" ") && aux.contains("<p" + k + " pair="+i+" ") ) ? "related" : "not-related";
		       if ( sentence.indexOf("</p" + j + ">") < 0 || sentence.indexOf("</p" + k + ">") <= sentence.indexOf("</p" + j + ">")) continue;   
		       String before = sentence.substring(0,sentence.indexOf("</p" + j + ">")+5).trim();
			   String after = sentence.substring(sentence.indexOf("<p" + k + " pair=" + ( type.equals("related") ? i + " " : "" ) )).trim(); 
			   String between = sentence.substring(sentence.indexOf("</p" + j + ">")+5,sentence.indexOf("<p" + k + " pair=" + ( type.equals("related") ? i+" " : "" ))).trim();
			   before = before.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
			   after = after.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
		  	   between = between.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
		  	   before = before + " " + between;
		       after = between + " " + after;	       
		       String relation = before + "\t" + between + "\t" + after;
			   if (type.equals("related")) positiveExamples.add(relation); else negativeExamples.add(relation);
			  }
		     }
		     for ( String auxStr : positiveExamples) {
		    	  String auxStr2[] = auxStr.split("\t");
		    	  processExample(auxStr2[0],auxStr2[1],auxStr2[2],"related",out);
		     }
		     for ( String auxStr : negativeExamples) if ( !positiveExamples.contains(auxStr) ) {
		    	  String auxStr2[] = auxStr.split("\t");
		    	  processExample(auxStr2[0],auxStr2[1],auxStr2[2],"not-related",out);
		     }
		    }
		    input.close();
		   }
		   out.flush();
	 }

	 public static Map<String,Integer> getFrequencyMap ( String file ) throws Exception {
	   	 final Map<String,Integer> shingles = new HashMap<String,Integer>(); //for each shingle store all the classes where it occurs
	   	 BufferedReader input = new BufferedReader(new FileReader(file));
	     String line = null;
	   	 while ( (line=input.readLine()) != null ) {
	   		 line = line.substring(line.indexOf(" ") + 1);
	   		 String[] relation_shingles = line.split(" ");
	   		 for ( String shingle : relation_shingles ) {
	   			 Integer shs = shingles.get(shingle);
	   			 if ( shs == null ) shs = new Integer(1); else shs++;
	   			 shingles.put(shingle,shs);
	   		 } 
	     }
	   	 input.close();
	   	 return shingles;	 
	 }
 
	 public static Map<String,Integer> getEntropyMap ( String file ) throws Exception {
		 final Set<String> classes = new HashSet<String>();	//stores all possible classes
	   	 final Map<String,String[]> shingles = new HashMap<String,String[]>(); //for each shingle store all the classes where it occurs
	   	 final Map<String,Double> entropyMap = new HashMap<String,Double>(); //entropy value for each single
	   	 final Map<String,Integer> result = new HashMap<String,Integer>();
	   	 BufferedReader input = new BufferedReader(new FileReader(file));
	     String line = null;
	   	 while ( (line=input.readLine()) != null ) {
	   		 String relation_class = line.substring(0,line.indexOf(" ")); line = line.substring(line.indexOf(" ") + 1);
	   		 String[] relation_shingles = line.split(" ");
	   		 classes.add(relation_class);
	   		 for ( String shingle : relation_shingles ) {
	   			 List<String> aux = new ArrayList<String>();
	   			 String[] shs = shingles.get(shingle);
	   			 if ( shs != null ) for ( String sh : shingles.get(shingle) ) aux.add(sh);
	   			 aux.add(relation_class);
	   			 shingles.put(shingle,aux.toArray(new String[0]));
	   		 } 
	     }
	   	 input.close();
	   	 final double minmaxEntropy[] = { Double.MAX_VALUE, Double.MIN_VALUE };
		 for( String shingle : shingles.keySet() ) {			
	    	 Map<String,Double> classProb = new HashMap<String,Double>(); //distribution of probabilities of a shingle over classes
	   		 String[] aux = shingles.get(shingle);
	   		 for ( String cl : classes ) {
	   			 int cnt = 0;
	   			 for ( String s : aux ) if ( s.equals(cl) ) cnt++;
	   			 classProb.put(cl,((double)cnt)/(double)(aux.length));
	   		 }
	   		 double entropy = 0;
	   		 for ( String c : classProb.keySet() ) entropy += classProb.get(c) * Math.log(classProb.get(c));
	   		 entropy = 0.0 - entropy;
	   		 if ( entropy < minmaxEntropy[0]) minmaxEntropy[0] = entropy;
	   		 if ( entropy > minmaxEntropy[1]) minmaxEntropy[1] = entropy;
	   	     entropyMap.put(shingle,entropy);
	
	   	 } 
	   	 // normalization and give a weight to each single according to entropy value 
		 for( String shingle : entropyMap.keySet() ) {			
	   		 double entropy = entropyMap.get(shingle);
	   		 entropy = 1.0 - (( entropy - minmaxEntropy[0] ) / ( minmaxEntropy[1] - minmaxEntropy[0] ));
	   		 result.put(shingle, (int)Math.round(entropy * 2));
	
	   	 }
	   	 return result;	 
	 }
 
	 public static String generateNGrams(String source, String prefix2, int betweenLenght , int window ) {
	    String prefix = ( prefix2.equals("BEF") || prefix2.equals("AFT") ) ? prefix2 + "_" + window : prefix2;
		String auxPOS[] = EnglishNLP.adornText(source,1).split(" +");
		String normalized[] = EnglishNLP.adornText(source,3).split(" +");
	    String aux[] = EnglishNLP.adornText(source,0).split(" +");
	    List<String> set = new ArrayList<String>();
	    for ( int i = 0 ; i < aux.length; i++ ) {
			if ( prefix.startsWith("BEF") && aux.length - i > betweenLenght + window ) continue;
			if ( prefix.startsWith("AFT") && i > betweenLenght + window ) continue;
			source = (i == 0) ? aux[i] : source + " " + aux[i];		
			if ( auxPOS.length == normalized.length && auxPOS.length == aux.length ) {		
				if ( auxPOS[i].startsWith("v") ) { 
				  set.add(normalized[i] + "_" + ( i < aux.length - 1 ? normalized[i+1] + "_" : "" ) + prefix);
				  if ( !normalized[i].equals("be") && !normalized[i].equals("have") && auxPOS[i].equals("vvn") ) set.add(normalized[i] + "_VVN_" + prefix);			  
				  if ( !normalized[i].equals("be") && !normalized[i].equals("have") ) set.add(normalized[i] + "_" + prefix);			  
	
				  
				  //passive voice detection
				  if (i < aux.length - 4) {
					  if ((normalized[i].equals("have") && normalized[i+1].equals("be") && auxPOS[i+2].equals("vvn") && (auxPOS[i+3].startsWith("pp") || auxPOS[i+3].equals("p-acp") || auxPOS[i+3].startsWith("pf") || auxPOS[i+3].startsWith("pc-acp") || auxPOS[i+3].startsWith("acp")))) {
						  set.add(normalized[i] + "_" + normalized[i+1] + "_" + normalized[i+2] +  "_" + normalized[i+3] + "_PASSIVE" + prefix);
				  		  set.add("_PASSIVE_" + prefix);
					  }
				  }
				  if (i < aux.length - 3) {
					  if ( i > 0 && (normalized[i-1].equals("have") || normalized[i-1].equals("be"))) continue;
					  if (((normalized[i].equals("have") || normalized[i].equals("be")) && auxPOS[i+1].equals("vvn") && (auxPOS[i+2].startsWith("pp") || auxPOS[i+2].equals("p-acp") || auxPOS[i+2].startsWith("pf") || auxPOS[i+2].startsWith("pc-acp") || auxPOS[i+2].startsWith("acp")))) {
						  set.add(normalized[i] + "_" + normalized[i+1] + "_" + normalized[i+2] + "_PASSIVE" + prefix);
				  		  set.add("_PASSIVE_" + prefix);
					  }
				  }
				  
		  	      //ReVerb inspired pattern: a verb, followed by:  nouns, adjectives or adverbs, ending in a proposition
		  		  if (i < aux.length - 2) {
		  			String pattern = normalized[i];
		  			int j = i + 1;
		  			if ( j < aux.length && auxPOS[j].startsWith("pc-acp") || auxPOS[j].startsWith("acp")) {
						pattern += "_" + normalized[j++];
		  			} 			
		  			if ( j < aux.length && auxPOS[j].startsWith("av")) {
						pattern += "_" + normalized[j++];
		  			}
		  			while ( (j < aux.length - 2) && (auxPOS[j].startsWith("av") || // adverbs
		  										     auxPOS[j].equals("d") || auxPOS[j].startsWith("av-d") || auxPOS[j].equals("dc")|| auxPOS[j].equals("dg") || auxPOS[j].equals("ds") || auxPOS[j].equals("dx") || auxPOS[j].equals("n2-dx") || //determiners	  										     
		  										     auxPOS[j].startsWith("j") || //adjectives
		  										     (auxPOS[j].startsWith("n") && !auxPOS[j].startsWith("nu")) || //nouns 
		  										     auxPOS[j].startsWith("pi") || auxPOS[j].startsWith("po") || auxPOS[j].startsWith("pn") || auxPOS[j].startsWith("px"))) { //pronoun
		  				pattern += "_" + normalized[j];
		  				j++;				
					}
		  			if ( ( j < aux.length ) && (auxPOS[j].startsWith("pp") || auxPOS[j].equals("p-acp") || auxPOS[j].startsWith("pf") || auxPOS[j].startsWith("pc-acp") || auxPOS[j].startsWith("acp"))) {
							pattern += "_" + normalized[j];
					}
		  			set.add(pattern + "_RVB_" + prefix);
		  			set.add("_RVB_" + prefix);
		  			
		  			// negation detection
		  			if ( (i - 1 > 0) && ( normalized[i-1].equals("not") ||
	 	  					              normalized[i-1].equals("neither") ||
	 	  					              normalized[i-1].equals("nobody") ||
	 	  					              normalized[i-1].equals("no") ||
	 	  					              normalized[i-1].equals("none") ||
	 	  					              normalized[i-1].equals("nor") ||
	 	  					              normalized[i-1].equals("nothing") ||
	 	  					              normalized[i-1].equals("nowhere") ||
		  					              normalized[i-1].equals("never"))) set.add(normalized[i-1] + "_" + pattern + "_RVB_" + prefix);
		  		  }
		  		  
				//normalized propositions 
				} else if ( auxPOS[i].startsWith("pp") || auxPOS[i].equals("p-acp") || auxPOS[i].startsWith("pf") ) {
		  		  set.add(normalized[i] + "_PREP_" + prefix);
			    }
			}
		}
		//generate quadgrams based on the original string
	    for ( int j = 0; j < source.length() + 3; j++ ) {
		   String tok = "";
	       for ( int i = -3 ; i <= 0 ; i++ ) { char ch = (j + i) < 0 || (j + i) >= source.length()  ? '_' : source.charAt(j + i); tok += ch == ' ' ? '_' : ch; }
	       if ( frequencyMap != null && ( frequencyMap.get(tok+ "_" + prefix) == null || frequencyMap.get(tok+ "_" + prefix) < minFreqThreshold ) ) continue;
		   if ( entropyMap != null && entropyMap.get(tok+ "_" + prefix) != null) for ( int i = 1; i <= 1 + entropyMap.get(tok+ "_" + prefix); i++) set.add(tok + "_" + prefix + "_" + i);
		   else if ( entropyMap == null || entropyMap.size() == 0 ) set.add(tok + "_" + prefix);
		}
		String result = "";
	    for ( String tok : set ) result += " " + tok;
	    return result.trim();
	 }
 
	 public static void processExample ( String before, String after, String between, String type, PrintWriter out ) {
	     out.print(type);
	     if ( before.lastIndexOf(",") != -1 && before.lastIndexOf(",") < before.lastIndexOf(between) ) before = before.substring(before.lastIndexOf(",") + 1);
	     if ( after.indexOf(",") != -1 && after.indexOf(",") > between.length()) after = after.substring(0,after.lastIndexOf(","));
	     int betweenLength = EnglishNLP.adornText(between,0).split(" +").length;     
	     int beforeLength = EnglishNLP.adornText(before,0).split(" +").length;
	     int afterLength = EnglishNLP.adornText(after,0).split(" +").length;
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
	    			out.print(" " + generateNGrams(str, suffix, betweenLength, 3));
	     }
	     out.println();
	} 
 
	 public static void generateDataAIMED() throws Exception, IOException {
		 
		 long accu_train = 0;
		 long accu_test = 0;
		 for ( int f = 1 ; f <= 10; f++) {
			System.out.println("Generating AIMED data fold " + f );
			
			long startTime = System.nanoTime();
			processAIMED("Datasets/aimed", "Datasets/aimed/splits/train-203-" + f, new PrintWriter(new FileWriter("train-data-aimed.txt." + f)));
			long stopTime = System.nanoTime();
			long elapsedTime = stopTime - startTime;
			accu_train += elapsedTime;
			
			startTime = System.nanoTime();
			processAIMED("Datasets/aimed", "Datasets/aimed/splits/test-203-" + f, new PrintWriter(new FileWriter("test-data-aimed.txt." + f)));
			stopTime = System.nanoTime();
			elapsedTime = stopTime - startTime;
			accu_test += elapsedTime;
		 }
		 
		 System.out.println("Avg. Generate train data time: " + TimeUnit.SECONDS.convert(accu_train, TimeUnit.NANOSECONDS) / (float) 10 );
		 System.out.println("Avg. Generate test data time: " + TimeUnit.SECONDS.convert(accu_test, TimeUnit.NANOSECONDS) / (float) 10);
		 
	}

	 public static void generateDataSemEval() throws Exception, IOException {
		 System.out.println("Generating SemEval data...");
		 System.out.println("\nGenerating train data...");
		 long startTime = System.nanoTime();	 
		 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.txt")));	 
		 long stopTime = System.nanoTime();	 
		 long elapsedTime = stopTime - startTime;	 
		 System.out.println(TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS));	 
		 System.out.println("\nGenerating test data...");
		 startTime = System.nanoTime();
		 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_testing_keys/TEST_FILE_FULL.TXT", new PrintWriter(new FileWriter("test-data-semeval.txt")));
		 stopTime = System.nanoTime();
		 elapsedTime = stopTime - startTime;	 
		 System.out.println(TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS));
	}
	 
	 public static void generateDataDrugBank() throws Exception, IOException {
		 createTestTrain("Datasets/Francisco/DrugBank.txt", (float) 0.7);	 
	}
	
	 public static void generateDataWikiEn() throws Exception, IOException {
		 System.out.println("Generating Wikipedia data...");
		 System.out.println("\nGenerating train data...");
		 long startTime = System.nanoTime();	 
		 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.train", new PrintWriter(new FileWriter("train-data-wikien.txt")));
		 long stopTime = System.nanoTime();	 
		 long elapsedTime = stopTime - startTime;
		 System.out.println(TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS));
		 System.out.println("\n\nGenerating test data...");
		 startTime = System.nanoTime();
		 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.test", new PrintWriter(new FileWriter("test-data-wikien.txt")));
		 stopTime = System.nanoTime();	 
		 elapsedTime = stopTime - startTime;
		 System.out.println(TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS));
	}
 
}