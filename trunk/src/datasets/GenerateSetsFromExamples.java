package datasets;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import nlputils.EnglishNLP;
import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.ICU4JBreakIteratorSentenceSplitter;


public class GenerateSetsFromExamples {

 public static Map<String,Integer> entropyMap;
 public static Map<String,Integer> frequencyMap;
 public static int minFreqThreshold = 2;
 
 public static void processWikipedia ( String file, PrintWriter out ) throws Exception {
   BufferedReader input = new BufferedReader( new FileReader(file) );
   String aux = null;
   String sentence = null;
   String entity1 = null;
   String entity2 = null;
   String type = null;
   while ( ( aux = input.readLine() ) != null ) {
     if ( aux.startsWith("SENTENCE : ") ) {
       if ( sentence != null ) {
		   if (sentence.indexOf(entity1) < 0 || sentence.indexOf(entity2) < 0) return;
		   if (entity1.equals(entity2)) return;
		   if (entity1.contains(entity2) || entity2.contains(entity1)) return;
		   String before = sentence.substring(0,Math.min(sentence.indexOf(entity1),sentence.indexOf(entity2))).trim();
		   String after = sentence.substring(Math.max(sentence.indexOf(entity1)+entity1.length(),sentence.indexOf(entity2)+entity2.length())).trim();   
		   String between = sentence.substring(Math.min(sentence.indexOf(entity1)+entity1.length(),sentence.indexOf(entity2)+entity2.length()),
		                                       Math.max(sentence.indexOf(entity1),sentence.indexOf(entity2))).trim();
		   processExample(before,after,between,type,out); 
	   }
       sentence = aux.substring(11).trim();
       entity1 = null;
       entity2 = null;
       type = null;
     } else if ( aux.startsWith("ENTITY1 : ") ) entity1 = aux.substring(10).trim();
       else if ( aux.startsWith("ENTITY2 : ") ) entity2 = aux.substring(10).trim();
       else if ( aux.startsWith("REL TYPE : ") ) type = aux.substring(11).trim();
   }
   input.close();
   out.flush();
 }
 
 public static void processSemEval ( String file, PrintWriter out ) throws Exception {
   BufferedReader input = new BufferedReader( new FileReader(file) );
   String aux = null;
   String sentence = null;
   String type = null;
   List<Integer> sentences_size = new Vector<Integer>();
   /*
   Map<String,Integer> class_instances = new HashMap<String,Integer>();
   Map<String,Integer> nominals = new HashMap<String,Integer>();
   int num_terms = 0;
   */
   while ( ( aux = input.readLine() ) != null ) {
     if ( aux.contains("\t\"") ) {
       sentence = aux.substring(aux.indexOf("\"")+1,aux.lastIndexOf("\""));       
       sentences_size.add(sentence.length());
       //num_terms += sentence.split("\\s+").length;
	   String before = sentence.substring(0,Math.min(sentence.indexOf("</e1>"),sentence.indexOf("</e2>"))).trim();
	   String after = sentence.substring(Math.max(sentence.indexOf("<e2>")+4,sentence.indexOf("<e1>")+4)).trim();  	   
	   String between = sentence.substring(Math.min(sentence.indexOf("</e1>")+5,sentence.indexOf("</e2>")+5),Math.max(sentence.indexOf("<e2>"),sentence.indexOf("<e1>"))).trim();  
	   between = between.replaceAll("</?e[12] *>","");	   
	   before = before.replaceAll("</?e[12] *>","") + " " + between;
	   after = between + " " + after.replaceAll("</?e[12] *>","");
	   type = input.readLine().trim();
	   	   
	   //String entity1 = sentence.substring(sentence.indexOf("<e1>")+4,sentence.indexOf("</e1>")).trim();
	   //String entity2 = sentence.substring(sentence.indexOf("<e2>")+4,sentence.indexOf("</e2>")).trim();
	   
	   /*
	   try {
		   int num = nominals.get(entity1);
		   num++;
		   nominals.put(type, num);
	   } catch (Exception e) {
		   nominals.put(entity1, 1);
	   }
	   
	   try {
		   int num = nominals.get(entity2);
		   num++;
		   nominals.put(type, num);
	   } catch (Exception e) {
		   nominals.put(entity2, 1);
	   }
	   
	   try {
		   int num = class_instances.get(type);
		   num++;
		   class_instances.put(type, num);
	   } catch (Exception e) {
		   class_instances.put(type, 1);
	   }
	   */
	   
	   if (!TestClassification.SemEvalAsymmetrical) type = type.split("\\(")[0];
	   processExample(before,after,between,type,out); 
     }
   }
   out.flush();
   input.close();

   /*
   int n_nominals = 0;
   for (String e : nominals.keySet()) {
	   n_nominals += nominals.get(e);
   }
   */
   //System.out.println("#Nominals: " + nominals.keySet().size());   
   //System.out.println("#Terms: " + num_terms);
   
   /* sentence length statistics 
   System.out.println();
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
   
   /* class instance statistics/
   System.out.println();
   total = 0;
   for (String c : class_instances.keySet()) { total += class_instances.get(c);}
   average = (double) total / (double) class_instances.keySet().size();
   System.out.println("Avg. class instances: " + average);
   
   distance_to_average = new Vector<Double>();
   for (String c : class_instances.keySet()) { 
	   double difference = (double) class_instances.get(c) - (average);
	   distance_to_average.add(Math.pow(difference, 2));
   }   
   stdvt = 0.0;
   for (Double d: distance_to_average) {stdvt += d;}   
   System.out.println("StDev. class instances: " +  Math.sqrt(stdvt / (double) distance_to_average.size()));   
   */
 }
  
 public static void processWikipediaEN ( String file, PrintWriter out ) throws Exception {
      
   BufferedReader input = new BufferedReader( new FileReader(file) );
   
   String aux = null;
   String entity1 = null;
   String type = null;
   
   //int numberOfOther = 0;
   //int num_terms = 0;
   List<String> avoidClasses = Arrays.asList("descendant","discovered","gpe_competition","grandmother","inventor","supported_person","uncle");
      
   int numberOfOther = 0;
   int num_terms = 0;
   
   List<Integer> sentences_size = new Vector<Integer>();
   Map<String,Integer> class_instances = new HashMap<String,Integer>();   
   //int number_sentences = 0;
   boolean debug = false;
   
   //Multimap<String, String> relation_sentences = LinkedListMultimap.create();
   //Set<String >all_sentences = null;
   
   //top15
   //List<String> classesWikiEn = Arrays.asList("job_title","visited","birth_place","associate","birth_year","member_of","birth_day","opus","death_year","death_day","education","nationality","executive","employer","death_place");
   
   //top 25 classes
   List<String> classesWikiEn = Arrays.asList("job_title","visited","birth_place","associate","birth_year","member_of","birth_day","opus","death_year","death_day","education","nationality","executive","employer","death_place","award","father","participant","brother","son","associate_competition","wife","superior","mother","political_affiliation");
  
   
   while ( ( aux = input.readLine() ) != null ) {	   
	   if ( aux.startsWith("url=") ) entity1 = aux.substring(aux.lastIndexOf("/")+1).replace("_"," "); else if ( aux.trim().length() != 0) {
		   aux = aux.replaceAll("</?i>","").replaceAll("&amp;","&").replaceAll("</?b>","").replaceAll("<br[^>]+>","").replaceAll("<a +href *= *\"[^\"]+\"( +class *= *\"[^\"]+\")?( +title *= *\"[^\"]+\")?","<a");
		   aux = aux.replaceAll("</?span[^>]*>","").replaceAll("</?sup[^>]*>","").replaceAll("<a [^>]*class='external autonumber'[^>]*>[^<]+</a>" , "");
		   entity1 = entity1.replaceAll(" \\(.*","").replaceAll(",","").trim();		   
		   //num_terms += aux.split("\\s+").length;		   
		   
		   /*
		   relation_sentences = LinkedListMultimap.create();
		   all_sentences = new HashSet<String>();
		   debug=false;
		   relations=0;
		   if (aux.contains("relation=\"birthplace\"")) debug=true;
		   */
		   
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
		   
		   //number_sentences += new_sentences.size();
		   String auxS[] = entity1.split(" +");
		   
		   /*
		   if (auxS[auxS.length-1].equals("Jr.")) {
			auxS[auxS.length-1] = auxS[auxS.length-2];
		   }
		   */
		   
		   for ( List<String> tokens : new_sentences ) {
			String sentence = ""; for ( String auxT : tokens ) sentence += " " + auxT;
			sentence = sentence.trim().replaceAll("< a relation = \" ", "<a relation=\"").replaceAll(" \" > ", "\">").replaceAll(" < / a >", "</a>").replaceAll("< a > ", "<a>");
			sentences_size.add(sentence.length());
			
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
			
			//if (debug==true) all_sentences.add(sentence);
			
		    Pattern pattern = Pattern.compile("<a[^>]*>[^<]+</a>");
		    Matcher matcher = pattern.matcher(sentence);
		    while (matcher.find()) {
			   String type1 = matcher.group();
			   if ( !type1.contains(" relation=") ) type1 = "OTHER"; else { 
				   type1 = type1.substring(type1.indexOf(" relation=")+11); 
			       type1 = type1.substring(0, type1.indexOf("\"")); 
			   }
			   String before1 = sentence.substring(0,matcher.start());
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
				   
				   if (!classesWikiEn.contains(type)) continue;
				   
				   try {
					   int num = class_instances.get(type);
					   num++;
					   class_instances.put(type, num);
				   } catch (Exception e) {
					   class_instances.put(type, 1);
				   }
				   
				   //if (debug==true) all_sentences.put(type,sentence);
					   /*
					   System.out.println("\nentity1: " + entity1);				   
					   System.out.println();
					   System.out.println("sentence: " + sentence);
					   System.out.println();
					   System.out.println("* before: " + before);
					   System.out.println("* between: " + between);
					   System.out.println("* after: " + after);
					   System.out.println();
					   System.out.println("type: " + type);
					   System.out.println("==================");
					   */
				   
				   if ( type.equals("OTHER")) numberOfOther++;
				   processExample(before,after,between,type,out); 
			   }   
		     }
		   }
		   /*
		   if (debug==true && relation_sentences.get("visited").size()==0) {
		    	System.out.println("==================================");		    	
		    	if (!relation_sentences.keySet().contains("visited")) {
		    		System.out.println("entity: " + entity1);
		    		
		    		for (String n : entity1.split("\\s+")) {
						System.out.print(n+'\t');
					}
		    		System.out.println();
		    		
		    		System.out.println("paragrah: " + aux);
		    		System.out.println("");
		    		System.out.println("sentences: ");
		    		for (List<String> x : new_sentences) System.out.println(x+"\n");
		    		System.out.println("sentences replaced: ");
		    		for (String x : all_sentences) System.out.println(x+"\n");
		    		
		    		System.out.println("relations/sentences: ");
		    		for (String t : relation_sentences.keySet()) {
						System.out.println("* " + t + "\n");
						for (String s : relation_sentences.get(t)) {
							System.out.println(s);
						}
						System.out.println();
					}
					
		    	}
			    System.out.println("==================================");		    	
		    }
		    */
	   }
   }
   out.flush();
   System.err.println("Number of elements of class OTHER : " + numberOfOther);   
   //System.err.println("Number of sentences : " + number_sentences);
   System.out.println("total number of classes: " + class_instances.size());   
   int acc_class_instances = 0;
   for (String c : class_instances.keySet()) {
	   acc_class_instances += class_instances.get(c);
   }
   System.out.println();
   System.out.println("total number of class instances: " + acc_class_instances);
   
   /* statistics  */
   System.out.println("#Terms: " + num_terms);    
   System.out.println();
   
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
   
   /* class instance statistics 
   System.out.println();
   total = 0;
   for (String c : class_instances.keySet()) { total += class_instances.get(c);}
   average = (double) total / (double) class_instances.keySet().size();
   System.out.println("Avg. class instances: " + average);
   
   distance_to_average = new Vector<Double>();
   for (String c : class_instances.keySet()) { 
	   double difference = (double) class_instances.get(c) - (average);
	   distance_to_average.add(Math.pow(difference, 2));
   }   
   stdvt = 0.0;
   for (Double d: distance_to_average) {stdvt += d;}   
   System.out.println("StDev. class instances: " +  Math.sqrt(stdvt / (double) distance_to_average.size()));
   */
   input.close();
 }
 
 public static void processAIMED ( String directory, String fold, PrintWriter out ) throws Exception {
	 
	 Set<String> dataFiles = new HashSet<String>();
	 BufferedReader inputAux = new BufferedReader( new FileReader(fold) );
	 String aux = null; String original = null;
	 //List<Integer> sentences_size = new Vector<Integer>();	 
	 //Map<String,Integer> class_instances = new HashMap<String,Integer>();
	 //int num_terms = 0;
	 while ( ( aux = inputAux.readLine() ) != null ) dataFiles.add(aux);
	   inputAux.close();
	   for ( File file : new File(directory).listFiles() ) if ( dataFiles.contains(file.getName()) ){ 
	    BufferedReader input = new BufferedReader( new FileReader(file) );
	    String sentence = null;
	    String type = null;
	    while ( ( aux = input.readLine() ) != null ) {
	   	 Set<String> positiveExamples = new HashSet<String>();	 
		 Set<String> negativeExamples = new HashSet<String>();
	     original = aux;
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
	     //sentences_size.add(sentence.length());
	     //num_terms += sentence.split("\\s+").length;	     
	     		   
		// System.out.println("==================");
		// System.out.println("original: " + original);
		// System.out.println("sentence: " + sentence);
	     for ( int i = 1; i < 50; i++ ) for ( int j = 1; j < 50; j++ ) for ( int k = j + 1 ; k < 50; k++ ) {
		  if ( aux.contains("<p" + j + " pair="+i+" ") || aux.contains("<p" + k + " pair="+i+" ") ) {
		   type = ( aux.contains("<p" + j + " pair="+i+" ") && aux.contains("<p" + k + " pair="+i+" ") ) ? "related" : "not-related";
		   
		   /*
		   try {
			   int num = class_instances.get(type);
			   num++;
			   class_instances.put(type, num);
		   } catch (Exception e) {
			   class_instances.put(type, 1);
		   }
		   */
		   
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
		      //System.out.println();
			  //System.out.println("before: " + auxStr2[0]);
			  //System.out.println("between: " + auxStr2[1]);
			  //System.out.println("after: " + auxStr2[2]);
			  //System.out.println("type: related");
		      //System.out.println();
	     }
	     for ( String auxStr : negativeExamples) if ( !positiveExamples.contains(auxStr) ) {
	    	  String auxStr2[] = auxStr.split("\t");
	    	  processExample(auxStr2[0],auxStr2[1],auxStr2[2],"not-related",out);
		      //System.out.println();
			  //System.out.println("before: " + auxStr2[0]);
			  //System.out.println("between: " + auxStr2[1]);
			  //System.out.println("after: " + auxStr2[2]);
			  //System.out.println("type: not-related");
		      //System.out.println();
	     }
		 //System.out.println("==================");
	    }
	    input.close();
	   }
	   
	   /*
	   System.out.println();
	   System.out.println("#Terms: " + num_terms);
	   
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
	   for (Double d: distance_to_average) {
		   stdvt += d;
	   }   
	   System.out.println("StDev. sentence Length: " +  Math.sqrt(stdvt / (double) distance_to_average.size()));
		*/
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
    // Parallel.forEach(shingles.keySet().iterator(), new Function<String, Void>() { public Void apply(String shingle) {
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
   	    // return null;
   	 } // });
   	 // normalization and give a weight to each single according to entropy value 
	 for( String shingle : entropyMap.keySet() ) {			
	 //Parallel.forEach(entropyMap.keySet().iterator(), new Function<String, Void>() { public Void apply(String shingle) {
   		 double entropy = entropyMap.get(shingle);
   		 entropy = 1.0 - (( entropy - minmaxEntropy[0] ) / ( minmaxEntropy[1] - minmaxEntropy[0] ));
   		 result.put(shingle, (int)Math.round(entropy * 2));
   		 // return null;
   	 } //});
   	 return result;	 
 }
 
 public static String generateNGrams(String source, String prefix2, int betweenLenght , int window ) {
    String prefix = ( prefix2.equals("BEF") || prefix2.equals("AFT") ) ? prefix2 + "_" + window : prefix2;
	String auxPOS[] = EnglishNLP.adornText(source,1).split(" +");
	String normalized[] = EnglishNLP.adornText(source,3).split(" +");
    String aux[] = EnglishNLP.adornText(source,0).split(" +");
    Set<String> set = new HashSet<String>();
    for ( int i = 0 ; i < aux.length; i++ ) {
		if ( prefix.startsWith("BEF") && aux.length - i > betweenLenght + window ) continue;
		if ( prefix.startsWith("AFT") && i > betweenLenght + window ) continue;
		source = (i == 0) ? aux[i] : source + " " + aux[i];
		
		if ( auxPOS.length == normalized.length && auxPOS.length == aux.length ) {		
			if ( auxPOS[i].startsWith("v") ) { 
			  set.add(normalized[i] + "_" + ( i < aux.length - 1 ? normalized[i+1] + "_" : "" ) + prefix);
			  if ( !normalized[i].equals("be") && !normalized[i].equals("have") && auxPOS[i].equals("vvn") ) set.add(normalized[i] + "_VVN_" + prefix);
			  if ( !normalized[i].equals("be") && !normalized[i].equals("have") ) set.add(normalized[i] + "_" + prefix);			  
			  
			//Levin classes
			//if (EnglishNLP.levin_verb_classes!=null) for (String levin_class : EnglishNLP.getVerbClass(normalized[i])) set.add(levin_class.substring(0,12) + "_" + prefix );
			
			  /*
			//ReVerb inspired: um verbo seguido de uma preposição
		  	if (i < aux.length - 1 && (auxPOS[i+1].startsWith("pp") || auxPOS[i+1].equals("p-acp") || auxPOS[i+1].startsWith("pf"))) {
		  	  	  set.add( normalized[i] + "_" + normalized[i+1] + "_RVB_" + prefix);
		  	}
		  	*/
			  
	  	    //ReVerb inspired: um verbo, seguido de vários nomes, adjectivos ou adverbios, terminando numa preposição.
	  		  if (i < aux.length - 2) {
	  			String pattern = normalized[i];
	  			int j = i+1;				
				while ( (j < aux.length - 2) && (auxPOS[j].startsWith("av") || auxPOS[j].startsWith("j")) || auxPOS[j].startsWith("n")) {	  				
					pattern += "_" + normalized[j];
					j++;				
				}					
				if (auxPOS[j].startsWith("pp") || auxPOS[j].equals("p-acp") || auxPOS[j].startsWith("pf")) {
						pattern += "_" + normalized[j];
						set.add(pattern + "_RVB_" + prefix);
					}
	  		  }
			//preposições normalizadas 
			} else if ( auxPOS[i].startsWith("pp") || auxPOS[i].equals("p-acp") || auxPOS[i].startsWith("pf") ) {
	  		  set.add(normalized[i] + "_PREP_" + prefix);
		    }
		}
	}
	// Gerar trigramas com base na string original
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
	
	 //Process AIMED
	 //line below is only for gathering statistics concerning the whole dataset
	 //processAIMED("Datasets/aimed", "Datasets/aimed/full/full.txt", new PrintWriter(new FileWriter("full-dataset-processed.txt")));
	 
	 for ( int f = 1 ; f <= 1; f++) {
		System.out.println("Generating AIMED data fold " + f );
		/*
		entropyMap = null; 
		processAIMED("Datasets/aimed", "Datasets/aimed/splits/train-203-" + f, new PrintWriter(new FileWriter("train-data-aimed.txt." + f)));
		entropyMap = getEntropyMap("train-data-aimed.txt." + f);
		*/
		processAIMED("Datasets/aimed", "Datasets/aimed/splits/train-203-" + f, new PrintWriter(new FileWriter("train-data-aimed.txt." + f)));
		processAIMED("Datasets/aimed", "Datasets/aimed/splits/test-203-" + f, new PrintWriter(new FileWriter("test-data-aimed.txt." + f)));
	 }
}

 public static void generateDataSemEval() throws Exception, IOException {
	//Process SemEval
	 System.out.println("Generating SemEval data...");
	 /*
	 entropyMap = null;
	 System.out.println("Determining shingles entropy...");
	 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.txt")));
	 entropyMap = getEntropyMap("train-data-semeval.txt");
	 */
	 /*
	 frequencyMap = null;
	 System.out.println("Determining shingles frequency...");
	 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.txt")));
	 frequencyMap = getFrequencyMap("train-data-semeval.txt");
	 */
	 System.out.println("\nGenerating train data...");
	 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.txt")));
	 System.out.println("\nGenerating test data...");
	 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_testing_keys/TEST_FILE_FULL.TXT", new PrintWriter(new FileWriter("test-data-semeval.txt")));
}

 public static void generateDataWikiEn() throws Exception, IOException {
	//WikiEn
	 System.out.println("Generating Wikipedia data...");
	 /*entropyMap = null;
	 System.out.println("Determining shingles entropy...");
	 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.test", new PrintWriter(new FileWriter("train-data-wikien.txt")));
	 entropyMap = getEntropyMap("train-data-wikien.txt"); */
	 System.out.println("\nGenerating train data...");
	 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.train", new PrintWriter(new FileWriter("train-data-wikien.txt")));
	 System.out.println("\n\nGenerating test data...");
	 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.test", new PrintWriter(new FileWriter("test-data-wikien.txt")));
}
 
 public static void generateAll() throws Exception {
		generateDataWikiEn();
		System.out.println();
		generateDataSemEval();
		System.out.println();
		generateDataAIMED();
 }
}