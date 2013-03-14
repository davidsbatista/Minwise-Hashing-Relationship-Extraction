import java.io.*;
import java.util.*;
import java.util.regex.*;

public class GenerateSetsFromExamples {

 public static String generateNGrams(String source, String prefix, int n, int weight, int casing ) {
	//String auxPOS[] = EnglishNLP.adornText(source,1).split(" +");
	//source = EnglishNLP.adornText(source,3);	
	if ( casing == 1 ) source = source.toLowerCase();
    String aux[] = source.split(" +");
/*    for ( int i = 0 ; i < aux.length ; i++ ) {
		if ( aux[i].matches("^[0-9][0-9]0-9][0-9]+$")) aux[i] = "N"; //NUMBER
		if ( aux[i].matches("^[0-9][0-9]0-9][0-9]$")) aux[i] = "Y"; //4DIG
		if ( aux[i].matches("^[0-9][0-9]$")) aux[i] = "M"; //2DIG
    } */
    Set<String> set = new HashSet<String>();    
	if ( casing == 2 ) for ( String s : generateNGrams(source,prefix,n,weight,1).split(" ") ) set.add(s);	
	// word unigrams
    if ( n == 1 ) for ( String tok : aux ) for ( int i = 1; i <= weight; i++) set.add(tok + "_" + prefix + "_" + i);
    // word bigrams
    if ( n == 2 ) for ( int j = 0; j <= aux.length; j++ ) {
       String tok1 = j-1 >=0 ? aux[j-1] : "_";
       String tok2 = j<aux.length ? aux[j] : "_";
       for ( int i = 1; i <= weight; i++) set.add(tok1 + "_" + tok2 + "_" + prefix + "_" + i);  
    }
	// word trigrams
    if ( n == 3 ) for ( int j = 0; j <= aux.length + 1; j++ ) {
       String tok1 = j-2 >=0 ? aux[j-2] : "_";
       String tok2 = j-1 >=0 && j-1 < aux.length? aux[j-1] : "_"; 
       String tok3 = j<aux.length ? aux[j] : "_";
       for ( int i = 1; i <= weight; i++) set.add(tok1 + "_" + tok2 + "_" + tok3 + "_" + prefix + "_" + i);
    }
	// word bigrams with gaps
    if ( n == 4 ) for ( int j = 2; j < aux.length; j++ ) {
       String tok1 = aux[j-2];
       String tok2 = "GAP"; 
       String tok3 = aux[j];
       for ( int i = 1; i <= weight; i++) set.add(tok1 + "_" + tok2 + "_" + tok3 + "_" + prefix + "_" + i);
    }
	// character trigrams
    if ( n == 5 ) for ( int j = 0; j < source.length() + 3; j++ ) {
       String tok = "";
       for ( int i = -3 ; i <= 0 ; i++ ) { char ch = (j + i) < 0 || (j + i) >= source.length()  ? '_' : source.charAt(j + i); tok += ch == ' ' ? '_' : ch; }
       for ( int i = 1; i <= weight; i++) set.add(tok + "_" + prefix + "_" + i);  
    }
	// word fourgrams
    if ( n == 6 ) for ( int j = 0; j <= aux.length + 2; j++ ) {
       String tok1 = j-3 >=0 ? aux[j-3] : "_";
       String tok2 = j-2 >=0 && j-2 < aux.length ? aux[j-2] : "_";
       String tok3 = j-1 >=0 && j-1 < aux.length ? aux[j-1] : "_"; 
       String tok4 = j<aux.length ? aux[j] : "_";
       for ( int i = 1; i <= weight; i++) set.add(tok1 + "_" + tok2 + "_" + tok3 + "_" + tok4 + "_" + prefix + "_" + i);
    }
	// word unigrams with POS
    // if ( n == 7 ) for ( int j = 0; j < aux.length; j++ ) for ( int i = 1; i <= weight; i++) set.add(aux[j] + "_" + auxPOS[j] + "_" + prefix + "_" + i);
    // word bigrams with POS
    /* if ( n == 8 ) for ( int j = 0; j <= aux.length; j++ ) {
       String tok1 = j-1 >=0 ? aux[j-1] + "_" + auxPOS[j-1] : "_";
       String tok2 = j<aux.length ? aux[j] + "_" + auxPOS[j] : "_";
       for ( int i = 1; i <= weight; i++) set.add(tok1 + "_" + tok2 + "_" + prefix + "_" + i);  
    } */
    String result = "";
    for ( String tok : set ) result += " " + tok;
    return result.trim();
 }
 
 public static void processExample ( String before, String after, String between, String type, PrintWriter out ) {
     out.print(type);
     // Features with unigrams, having a weight of 1
     // out.print(" " + generateNGrams(before, "BEF", 1, 1, 1) + " " + generateNGrams(between, "BET", 1, 1, 1) + " " + generateNGrams(after, "AFT", 1, 1, 1));
     // Features with character trigrams, having a weight of 1
     out.print(" " + generateNGrams(before, "BEF", 5, 1, 0) + " " + generateNGrams(between, "BET", 5, 2, 0) + " " + generateNGrams(after, "AFT", 5, 1, 0));
     // Features with bigrams, with gaps, having a weight of 3
     // out.print(" " + generateNGrams(before, "BEF", 4, 1, 1) + " " + generateNGrams(between, "BET", 4, 2, 1) + " " + generateNGrams(after, "AFT", 4, 1, 1));
     // Features with bigrams, having a weight of 4
     // out.print(" " + generateNGrams(before, "BEF", 2, 4, 1) + " " + generateNGrams(between, "BET", 2, 5, 1) + " " + generateNGrams(after, "AFT", 2, 4, 1));
     // Features with trigrams, having a weight of 6
     // out.print(" " + generateNGrams(before, "BEF", 3, 6, 1) + " " + generateNGrams(between, "BET", 3, 7, 1) + " " + generateNGrams(after, "AFT", 3, 6, 1));   
     // Features with unigrams with POS, having a weight of 2
     // out.print(" " + generateNGrams(before, "BEF", 7, 2, 1) + " " + generateNGrams(between, "BET", 7, 3, 1) + " " + generateNGrams(after, "AFT", 7, 2, 1));   
     // Features with bigrams with POS, having a weight of 5
     // out.print(" " + generateNGrams(before, "BEF", 8, 5, 1) + " " + generateNGrams(between, "BET", 8, 6, 1) + " " + generateNGrams(after, "AFT", 8, 5, 1));   
     // Features with fourgrams, having a weight of 7
     // out.print(" " + generateNGrams(before, "BEF", 6, 7, 1) + " " + generateNGrams(between, "BET", 6, 8, 1) + " " + generateNGrams(after, "AFT", 6, 7, 1));   
     out.println();
 }
 
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
   out.flush();
 }
 
 public static void processSemEval ( String file, PrintWriter out ) throws Exception {
   BufferedReader input = new BufferedReader( new FileReader(file) );
   String aux = null;
   String sentence = null;
   String entity1 = null;
   String entity2 = null;
   String type = null;
   while ( ( aux = input.readLine() ) != null ) {
     if ( aux.contains("\t\"") ) {
       sentence = aux.substring(aux.indexOf("\"")+1,aux.lastIndexOf("\""));
	   String before = sentence.substring(0,Math.min(sentence.indexOf("<e1>"),sentence.indexOf("<e2>"))).trim();
	   String after = sentence.substring(Math.max(sentence.indexOf("</e2>")+5,sentence.indexOf("</e1>")+5)).trim();  
	   String between = sentence.substring(Math.min(sentence.indexOf("</e1>")+5,sentence.indexOf("</e2>")+5),Math.max(sentence.indexOf("</e2>"),sentence.indexOf("</e1>"))).trim();
	   before = before + " " + between;
	   after = between + " " + after;
	   sentence = sentence.replaceAll("</?e[12] *>","");
       entity1 = aux.substring(aux.indexOf("<e1>")+4,aux.lastIndexOf("</e1>"));
	   entity2 = aux.substring(aux.indexOf("<e2>")+4,aux.lastIndexOf("</e2>"));
           type = input.readLine().trim();
	   //if ( type.indexOf("(") != -1 ) type = type.substring(0,type.indexOf("("));
	   processExample(before,after,between,type,out); 
     }
   }
   out.flush();
 }
 
 public static void processWikipediaEN ( String file, PrintWriter out ) throws Exception {
   BufferedReader input = new BufferedReader( new FileReader(file) );
   String aux = null;
   String sentence = null;
   String entity1 = null;
   String type = null;
   while ( ( aux = input.readLine() ) != null ) {
	   if ( aux.startsWith("url=") ) entity1 = aux.substring(aux.lastIndexOf("/")+1).replace("_"," "); else if ( aux.trim().length() != 0) {
		   sentence = aux.replaceAll("</?b>","").replaceAll("<br[^>]+>","").replaceAll("<a +href *= *\"[^\"]+\"( +title *= *\"[^\"]+\")?","<a");
		   entity1 = entity1.replaceAll(" \\(.*","").trim();
		   if ( sentence.contains(entity1) ) sentence = sentence.replaceAll(entity1,"<a>"+entity1+"</a>"); else {
			   String auxS[] = entity1.split(" ");
			   if ( sentence.startsWith("He ")) {
			   	 sentence = sentence.replaceAll("He ","<a>"+entity1+"</a> ");
			   } else if ( sentence.startsWith("She ")) {
			   	 sentence = sentence.replaceAll("She ","<a>"+entity1+"</a> ");
		       } else if ( auxS.length > 1 && sentence.startsWith(auxS[auxS.length-1])) {
		   	     sentence = sentence.replace(auxS[auxS.length-1],"<a>"+entity1+"</a>");
		       } else if ( sentence.startsWith(auxS[0])) {
		   	     sentence = sentence.replace(auxS[0],"<a>"+entity1+"</a>");			   
			   } else if ( auxS.length >=3 && sentence.contains(auxS[0]+ " " + auxS[auxS.length-1])) {
			   	 sentence = sentence.replaceAll(auxS[0]+ " " + auxS[auxS.length-1],"<a>"+entity1+"</a>");
			   } else if ( auxS.length > 1 && sentence.contains(auxS[auxS.length-1])) {
			   	 sentence = sentence.replaceAll(auxS[auxS.length-1],"<a>"+entity1+"</a>");
			   } else if ( sentence.contains(auxS[0])) {
			   	 sentence = sentence.replaceAll(auxS[0],"<a>"+entity1+"</a>");
			   } else if ( sentence.contains("He ")) {
			   	 sentence = sentence.replaceAll("He ","<a>"+entity1+"</a> ");
			   } else if ( sentence.contains("She ")) {
			   	 sentence = sentence.replaceAll("She ","<a>"+entity1+"</a> ");
			   } else if ( sentence.contains(" he ")) {
			   	 sentence = sentence.replaceAll(" he "," <a>"+entity1+"</a> ");
			   } else if ( sentence.contains(" she ")) {
			   	 sentence = sentence.replaceAll(" she "," <a>"+entity1+"</a> ");
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
			   String before1 = sentence.substring(0,matcher.start());
			   String after1 = sentence.substring(matcher.end());
			   Matcher matcher2 = pattern.matcher(after1);
		   	   while (matcher2.find()) {
				   String type2 = matcher2.group();
				   if ( !type2.contains(" relation=") ) type2 = "OTHER"; else { 
					   type2 = type2.substring(type2.indexOf(" relation=")+11); 
					   type2 = type2.substring(0, type2.indexOf("\"")); 
				   }
				   String before = sentence.substring(0,matcher.start()).replaceAll("<[^>]*>","");
				   String after = sentence.substring(matcher.end()+matcher2.end()).replaceAll("<[^>]*>","");
				   String between = sentence.substring(matcher.end(),matcher.end()+matcher2.start()).replaceAll("<[^>]*>","");
                                   before = before + " " + between;
                                   after = between + " " + after;
				   type = "OTHER";
				   if ( !type1.equals("OTHER") && !type2.equals("OTHER")) type = "OTHER";
				   else if ( type1.equals("OTHER") && type2.equals("OTHER")) type = "OTHER";
				   else if ( type1.equals("OTHER") && !matcher.group().contains(">"+entity1+"<")) type = type2;
				   else if ( type2.equals("OTHER") && !matcher2.group().contains(">"+entity1+"<")) type = type1;
				   if ( type.equals("OTHER") && Math.random() < 0.9) continue;
				   processExample(before,after,between,type,out); 
			   }   
		   }
	   }
   }
   out.flush();
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
    String entity1 = null;
    String entity2 = null;
    String type = null;
    while ( ( aux = input.readLine() ) != null ) {
	 int auxNum1 = 1, auxNum2 = 1;
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
     for ( int i = 1; i < 20; i++ ) for ( int j = 1; j < 20; j++ ) for ( int k = j + 1 ; k < 20; k++ ) 
	  if ( aux.contains("<p" + j + " pair="+i) || aux.contains("<p" + k + " pair="+i) ) {
	   type = ( aux.contains("<p" + j + " pair="+i) && aux.contains("<p" + k + " pair="+i) ) ? "related" : "not-related";
       sentence = aux;
       if ( sentence.indexOf("</p" + j + ">") < 0 || sentence.indexOf("</p" + k + ">") <= sentence.indexOf("</p" + j + ">")) continue;
	   String before = sentence.substring(0,sentence.indexOf("<p" + j + " pair=" + ( type.equals("related") ? i : "" ) )).trim();
	   String after = sentence.substring(sentence.indexOf("</p" + k + ">")+5).trim();
	   String between = sentence.substring(sentence.indexOf("</p" + j + ">")+5,sentence.indexOf("<p" + k + " pair=" + ( type.equals("related") ? i : "" ))).trim();
	   before = before.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
	   after = after.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
  	   between = between.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
	   sentence = sentence.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
           before = before + " " + between;
           after = between + " " + after;
	   processExample(before,after,between,type,out); 
      }
    }
   }
   out.flush();
 }
 
 public static void main ( String args[] ) throws Exception {
	 //processWikipedia("Datasets/results-relation-extraction.txt",, new PrintWriter(new FileWriter("data-wikipedia.txt")));
	 //processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.test", new PrintWriter(new FileWriter("test-data-wikien.txt")));
	 //processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.train", new PrintWriter(new FileWriter("train-data-wikien.txt")));
	 //processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.txt")));
	 //processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_testing_keys/TEST_FILE_FULL.TXT", new PrintWriter(new FileWriter("test-data-semeval.txt")));
	 /*
	 for ( int f = 1 ; f <= 10; f++) {
		 processAIMED("Datasets/aimed", "Datasets/aimed/splits/train-203-" + f, new PrintWriter(new FileWriter("train-data-aimed.txt." + f)));
		 processAIMED("Datasets/aimed", "Datasets/aimed/splits/test-203-" + f, new PrintWriter(new FileWriter("test-data-aimed.txt." + f)));
	 }
	 */
 }
}