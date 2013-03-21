import java.io.*;
import java.util.*;

import nlputils.EnglishNLP;

import minhash.LocalitySentitiveHashing;
import minhash.Pair;

public class TestClassification {

  private static int knn = 7;
  private static int signature = 400;
  private static int bands = 50;
  private static boolean separateDirection = false;
  
  private static LocalitySentitiveHashing dataIndex;
  
  private static LocalitySentitiveHashing directionIndex;

  public static void readTrainData ( String file ) throws Exception {
    readTrainData(file,-1);
  }
  		
  public static void readTrainData ( String file, int number ) throws Exception {
	File dbfile = new File("mapdb-relations-index");
	File dbfile2 = new File("mapdb-directions-index");
	dbfile.deleteOnExit();
    dbfile2.deleteOnExit();
    dataIndex = new LocalitySentitiveHashing( dbfile, signature , bands );
    directionIndex = new LocalitySentitiveHashing( dbfile2, signature , bands );
    BufferedReader input = new BufferedReader( new FileReader(file) );
    String aux = null;
	int num=0;
    while ( ( aux = input.readLine() ) != null ) {
      HashSet<String> set = new HashSet<String>();
      for ( String element : aux.substring(aux.indexOf(" ")+1).trim().split(" ") ) set.add(element);
      String cl = aux.substring(0,aux.indexOf(" "));
      if ( cl.indexOf("(") != -1 && separateDirection ) {
    	  String dr = cl.substring(cl.indexOf("("));
    	  cl = cl.substring(0,cl.indexOf("("));
          directionIndex.index(directionIndex.indexSize(),set.toArray(new String[0]),dr);
      }
      dataIndex.index(dataIndex.indexSize(),set.toArray(new String[0]),cl);      
      if ( number > 0 && num++ > number) break;
    }
    if ( knn <= 0 ) dataIndex.computeOptimalK();
    input.close();
  }
  
  public static LinkedList<Pair<String, String>> evaluateTestData ( String file ) throws Exception { 
    return evaluateTestData(file,-1); 
  }
  		
  public static LinkedList<Pair<String,String>> evaluateTestData ( String file, int number) throws Exception {
	if ( number > 0 ) readTrainData(file,number); 	
    BufferedReader input = new BufferedReader( new FileReader(file) );
    String aux = null;    
	LinkedList<Pair<String,String>> results = new LinkedList<Pair<String,String>>();
    while ( ( aux = input.readLine() ) != null ) {
	  if ( number-- > 0) continue;
      HashSet<String> set = new HashSet<String>();
      for ( String element : aux.substring(aux.indexOf(" ")+1).trim().split(" ") ) set.add(element);
      String cl = aux.substring(0,aux.indexOf(" "));      
      String clResult = dataIndex.queryNearest(set.toArray(new String[0]),knn).mostFrequent();
      if ( clResult!= null && separateDirection) clResult += directionIndex.queryNearest(set.toArray(new String[0]),knn).mostFrequent();
      Pair<String,String> p = new Pair<String, String>(cl, clResult);
      results.add(p);      
    }
    input.close();
	return results;
  }

  public static double[] evaluateResults(LinkedList<Pair<String,String>> results, String class_relation){
	  double numInstancesOfClass = 0;
	  double numCorrectClassified = 0;
	  double numClassified = 0;	  
	  double numCorrect = 0;
	  for (Pair<String, String> pair : results) {		  
		  if (pair.getSecond() == null) pair.setSecond("UNKNOWN");	
		  String first = pair.getFirst();
		  String second = pair.getSecond();
		  if (first.equalsIgnoreCase(class_relation)) {
			  numInstancesOfClass++;
			  if (first.equalsIgnoreCase(second) ) numCorrectClassified++;
		  }
		  if (second.equalsIgnoreCase(class_relation)) numClassified++;
		  if (first.equalsIgnoreCase(second)) numCorrect++;
	  }
	  double precision = numClassified == 0 ? 1.0 : (numCorrectClassified / numClassified);
	  double recall = numInstancesOfClass == 0 ? 1.0 : (numCorrectClassified / numInstancesOfClass);
	  double f1 = precision == 0 && recall == 0 ? 0.0 : (2.0*((precision*recall)/(precision+recall)));	  
	  System.out.println("Results for class \"" + class_relation + "\" ...");
	  System.out.println("Number of training instances : " + dataIndex.indexSize( class_relation ) );
	  System.out.println("Number of test instances : " + numInstancesOfClass );
	  System.out.println("Number of classifications : " + numClassified );
	  System.out.println("Precision : " + precision );
	  System.out.println("Recall : " + recall );
	  System.out.println("F1 : " + f1 );
	  double accuracy = numCorrect / (float) results.size(); 
	  return new double[]{ accuracy, precision, recall, f1 };
  }
   
  public static void testSemEval() throws Exception{
	  System.out.println();
	  System.out.println("Test classification on SemEval...");
	  System.out.println("Reading train data SemEval...");
      readTrainData("train-data-semeval.txt");
      System.out.println("Reading test data SemEval...");
      LinkedList<Pair<String,String>> all_results = evaluateTestData("test-data-semeval.txt");	      
      double[] results = { 0.0, 0.0, 0.0, 0.0 };
      String[] classes = {"Cause-Effect(e1,e2)","Cause-Effect(e2,e1)",
    		  "Component-Whole(e1,e2)","Component-Whole(e2,e1)",
    		  "Content-Container(e1,e2)","Content-Container(e2,e1)",
    		  "Entity-Destination(e1,e2)","Entity-Destination(e2,e1)",
    		  "Entity-Origin(e1,e2)","Entity-Origin(e2,e1)",
    		  "Instrument-Agency(e1,e2)","Instrument-Agency(e2,e1)",
    		  "Member-Collection(e1,e2)","Member-Collection(e2,e1)",
    		  "Message-Topic(e1,e2)","Message-Topic(e2,e1)",
    		  "Product-Producer(e1,e2)","Product-Producer(e2,e1)"};	  
      for ( String c : classes  ) {		  
    	  System.out.println();		  		  
    	  double[] results_aux = evaluateResults(all_results,c);		  
    	  for ( int j = 1; j < results_aux.length; j++) results[j] = results[j] + results_aux[j];
    	  results[0] = results_aux[0];
      }    
      for (int i = 1; i < results.length; i++) results[i] = results[i] / classes.length;
      System.out.println();
      System.out.println("Macro-Average results for all classes...");	
      System.out.println("Accuracy : " + results[0] );
  	  System.out.println("Precision : " + results[1]);
  	  System.out.println("Recall : " + results[2]);
  	  System.out.println("F1 : " + results[3]);
	  
  }
  
  public static void testWikiEN() throws Exception{
  	  //
	  // TESTING WITH Wikipedia English
	  //
  	  System.out.println();
	  System.out.println("Test classification on English Wikipedia...");
      System.out.println("Reading train data WikiEN...");
      readTrainData("train-data-wikien.txt");
	  System.out.println("Reading test data WikiEN...");
      LinkedList<Pair<String,String>> aux = evaluateTestData("test-data-wikien.txt");	  
      double[] resultsWiki = { 0.0, 0.0, 0.0, 0.0 };
      
      String[] classesWikiEn = {"job_title","visited","birth_place","associate","birth_year","member_of","birth_day","opus","death_year","death_day","education","nationality","executive","employer","death_place","award","father","participant","brother","son","associate_competition","wife","superior","mother","political_affiliation","friend","founder","daughter","husband","religion","influence","underling","sister","grandfather","ancestor","grandson","inventor","cousin","descendant","role","nephew","uncle","supported_person","granddaughter","owns","great_grandson","aunt","supported_idea","great_grandfather","gpe_competition","brother_in_law","grandmother","discovered" };
      //String[] classesWikiEn = {"job_title","visited","birth_place","associate","birth_year","member_of","birth_day","opus","death_year","death_day","education","nationality","executive","employer","death_place","award","father","participant","brother","son","associate_competition","wife","superior","mother","political_affiliation","friend","founder","daughter","husband","religion","influence","underling","sister","grandfather"};    		  
      
      for ( String c : classesWikiEn  ) {		  
    	  System.out.println();
    	  double[] results_aux = evaluateResults(aux,c);		  
    	  for ( int j = 1; j < results_aux.length; j++)
    		  resultsWiki[j] = resultsWiki[j] + results_aux[j];
    	  resultsWiki[0] = results_aux[0];
      }
      
      for (int i = 1; i < resultsWiki.length; i++) {
    	  resultsWiki[i] = resultsWiki[i] / classesWikiEn.length;
      }
      
      System.out.println();
      System.out.println("Macro-Average results for all classes...");	
      System.out.println("Accuracy : " + resultsWiki[0] );
  	  System.out.println("Precision : " + resultsWiki[1]);
  	  System.out.println("Recall : " + resultsWiki[2]);
  	  System.out.println("F1 : " + resultsWiki[3]);
	  
  }
  
  public static void testAIMED() throws Exception{
	  System.out.println();
	  System.out.println("Test classification on AIMED...");
      double[] results = new double[] { 0.0, 0.0, 0.0, 0.0 };    
      for ( int i = 1 ; i <= 10; i++) {
		  System.out.println();
		  System.out.println("Results for fold " + i + "...");
		  System.out.println("Reading train data ...");
		  readTrainData("train-data-aimed.txt." + i);
		  System.out.println("Reading test data ...");
		  LinkedList<Pair<String, String>> aux = evaluateTestData("test-data-aimed.txt." + i);
		  double[] results_aux = evaluateResults(aux,"related");
		  System.out.println("Accuracy : " + results_aux[0]);
		  for ( int j = 0; j < results_aux.length; j++) {  
			  results[j] = results[j] + results_aux[j];
		  }
      }
	  for ( int j = 0; j < results.length; j++) results[j] = results[j] / 10;   
	  System.out.println();
      System.out.println("Results for cross validation...");	
      System.out.println("Accuracy : " + results[0] );
  	  System.out.println("Precision : " + results[1] );
  	  System.out.println("Recall : " + results[2] );
  	  System.out.println("F1 : " + results[3] );
  }

  public static void main ( String args[] ) throws Exception {
	  
	  EnglishNLP.verbClasses("resources/levin-verb-classes.txt");
	  
	  if (args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("true")) {
		  GenerateSetsFromExamples.generateAll();
		  testSemEval();
		  testAIMED();
		  testWikiEN();
	  }
	  
	  else if (args[0].equalsIgnoreCase("all") && args[1].equalsIgnoreCase("false")) {
		  testSemEval();
		  testAIMED();
		  testWikiEN();
	  }
	  
	  else if (args[0].equalsIgnoreCase("semeval") && args[1].equalsIgnoreCase("true")) {
		  GenerateSetsFromExamples.generateDataSemEval();
		  testSemEval();
	  }
	  
	  else if (args[0].equalsIgnoreCase("aimed") && args[1].equalsIgnoreCase("true")) {
		  GenerateSetsFromExamples.generateDataAIMED();
		  testAIMED();
	  }
	  
	  else if (args[0].equalsIgnoreCase("wiki") && args[1].equalsIgnoreCase("true")) {
		  GenerateSetsFromExamples.generateDataWikiEn();
		  testWikiEN();
	  }
	  
	  else if (args[0].equalsIgnoreCase("semeval") && args[1].equalsIgnoreCase("false")) {
		  testSemEval();
	  }
	  
	  else if (args[0].equalsIgnoreCase("aimed") && args[1].equalsIgnoreCase("false")) {
		  testAIMED();
	  }
	  
	  else if (args[0].equalsIgnoreCase("wiki") && args[1].equalsIgnoreCase("false")) {
		  testWikiEN();
	  }
	  System.exit(0);
	}
}