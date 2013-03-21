import java.io.*;
import java.util.*;
import java.util.regex.*;
import nlputils.EnglishNLP;
import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;
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
	   processExample(before,after,between,type,out); 
     }
   }
   out.flush();
   input.close();
 }
 
 public static void processWikipediaEN ( String file, PrintWriter out ) throws Exception {
   int numberOfOther = 0;
   Set<String> exclude = new HashSet<String>(Arrays.asList(new String[] {"ancestor","grandson","inventor","cousin","descendant","role","nephew","uncle","supported_person","granddaughter","owns","great_grandson","aunt","supported_idea","great_grandfather","gpe_competition","brother_in_law","grandmother","discovered" }));
   BufferedReader input = new BufferedReader( new FileReader(file) );
   String aux = null;
   String entity1 = null;
   String type = null;
   while ( ( aux = input.readLine() ) != null ) {
	   if ( aux.startsWith("url=") ) entity1 = aux.substring(aux.lastIndexOf("/")+1).replace("_"," "); else if ( aux.trim().length() != 0) {
		   aux = aux.replaceAll("</?i>","").replaceAll("</?b>","").replaceAll("<br[^>]+>","").replaceAll("<a +href *= *\"[^\"]+\"( +title *= *\"[^\"]+\")?","<a");
		   entity1 = entity1.replaceAll(" \\(.*","").trim();
		   for ( List<String> tokens : new ICU4JBreakIteratorSentenceSplitter().extractSentences(aux) ) {
			String sentence = ""; for ( String auxT : tokens ) sentence += " " + auxT; 
			sentence = sentence.trim().replaceAll("< a relation = \" ", "<a relation=\"").replaceAll(" \" > ", "\">").replaceAll(" < / a >", "</a>").replaceAll("< a > ", "<a>");
		    if ( sentence.contains(entity1)) sentence = sentence.replaceAll(entity1,"<a>"+entity1+"</a>"); else {
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
				   String before = sentence.substring(0,matcher.end()).replaceAll("<[^>]+>","");
				   String after = sentence.substring(matcher.end()+matcher2.start()).replaceAll("<[^>]+>","");
				   String between = sentence.substring(matcher.end(),matcher.end()+matcher2.start()).replaceAll("<[^>]+>","");
                   before = before + " " + between;
                   after = between + " " + after;
				   type = "OTHER";
				   if ( !type1.equals("OTHER") && !type2.equals("OTHER")) type = "OTHER";
				   else if ( type1.equals("OTHER") && type2.equals("OTHER")) type = "OTHER";
				   else if ( type1.equals("OTHER") && matcher.group().contains(">"+entity1+"<")) type = type2;
				   else if ( type2.equals("OTHER") && matcher2.group().contains(">"+entity1+"<")) type = type1;
				   if ( type.equals("OTHER") && Math.random() < 0.975 ) continue;
				   
				   
/*			       System.out.println();
				   System.out.println("sentence: " + sentence);
				   System.out.println();
				   System.out.println("* before: " + before);
				   System.out.println("* between: " + between);
				   System.out.println("* after: " + after);
				   System.out.println();
				   System.out.println("type: " + type);
				   System.out.println("=================="); */			   
				   if ( type.equals("OTHER")) numberOfOther++;
				   processExample(before,after,between,type,out); 
			   }   
		     }
		   }
	   }
   }
   out.flush();
   System.err.println("Number of elements of class OTHER : " + numberOfOther);
   input.close();
 }
 
 public static void processAIMED ( String directory, String fold, PrintWriter out ) throws Exception {
	 Set<Integer> positiveExamples = new HashSet<Integer>();	 
	 Set<Integer> negativeExamples = new HashSet<Integer>();	 
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
		 int auxNum1 = 1, auxNum2 = 1;
		 aux = aux.replaceAll("([a-zA-Z0-9] )<prot>( [^<] )</prot>", "\1<p>\2</p>");
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
	       String before = sentence.substring(0,sentence.indexOf("</p" + j + ">")+5).trim();
		   String after = sentence.substring(sentence.indexOf("<p" + k + " pair=" + ( type.equals("related") ? i : "" ) )).trim(); 
		   String between = sentence.substring(sentence.indexOf("</p" + j + ">")+5,sentence.indexOf("<p" + k + " pair=" + ( type.equals("related") ? i : "" ))).trim();
		   before = before.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
		   after = after.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
	  	   between = between.replaceAll("</?p[0-9]+( +pair=[0-9]+ +)?>","").replaceAll("  +"," ").trim();
		   before = before + " " + between;
	       after = between + " " + after;
	       
	       /*
	       System.out.println();
		   System.out.println("sentence: " + sentence);
		   System.out.println("before: " + before);
		   System.out.println("between: " + between);
		   System.out.println("after: " + after);
		   System.out.println("type: " + type);
		   System.out.println("==================");
		   */
	       
	       String relation = before + between + after;
		   if (!positiveExamples.contains(relation.hashCode()) && !negativeExamples.contains(relation.hashCode())) processExample(before,after,between,type,out);
		   if (type.equals("related")) positiveExamples.add(relation.hashCode()); else negativeExamples.add(relation.hashCode());
		  }
	    }
	    input.close();
	   }
	   System.err.println(positiveExamples.size() + " positive examples and " + negativeExamples.size() + " negative examples.");
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
			  set.add(normalized[i] + "_" + ( i < aux.length -1 ? normalized[i+1] + "_" : "" ) + prefix);
			  if ( !normalized[i].equals("be") && !normalized[i].equals("have") ) set.add(normalized[i] + "_" + prefix);
			  if ( !normalized[i].equals("be") && !normalized[i].equals("have") && auxPOS[i].equals("vvn") ) set.add(normalized[i] + "_VVN_" + prefix);
			  //for (String levin_class : EnglishNLP.getVerbClass(aux[i])) set.add(levin_class.replaceAll(" ", "_") + "_LEVIN_CLASS_" + prefix);
			} else if ( auxPOS[i].startsWith("pp") || auxPOS[i].equals("p-acp") || auxPOS[i].startsWith("pf") ) {
	  		  set.add(normalized[i] + "_PREP_" + prefix);
		    }
		}
	}
    final String source2 = source;
	// Gerar trigramas com base na string original
    for ( int j = 0; j < source.length() + 3; j++ ) {
	   String tok = "";
       for ( int i = -3 ; i <= 0 ; i++ ) { char ch = (j + i) < 0 || (j + i) >= source2.length()  ? '_' : source2.charAt(j + i); tok += ch == ' ' ? '_' : ch; }
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
	for ( int f = 1 ; f <= 10; f++) {
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
	 System.out.println("Generating train data...");
	 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.txt")));
	 System.out.println("Generating test data...");
	 processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_testing_keys/TEST_FILE_FULL.TXT", new PrintWriter(new FileWriter("test-data-semeval.txt")));
}

 public static void generateDataWikiEn() throws Exception, IOException {
	//WikiEn
	 System.out.println("Generating Wikipedia data...");
	 /*entropyMap = null;
	 System.out.println("Determining shingles entropy...");
	 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.test", new PrintWriter(new FileWriter("train-data-wikien.txt")));
	 entropyMap = getEntropyMap("train-data-wikien.txt"); */
	 System.out.println("Generating train data...");
	 processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.train", new PrintWriter(new FileWriter("train-data-wikien.txt")));
	 System.out.println("Generating test data...");
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