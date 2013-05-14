package datasets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.aliasi.symbol.SymbolTable;

import lda.LDAModel;

public class GenerateVectorsFromSets {
	
    public static int[] tokenizeDocument(String text, SymbolTable symbolTable) {
        String[] words = text.split(" ");
        List<Integer> idList = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) {
            int id = symbolTable.symbolToID(words[i]);
            if (id >= 0) idList.add(id);
        }
        int[] tokenIds = new int[idList.size()];
        for (int i = 0; i < tokenIds.length; ++i) tokenIds[i] = idList.get(i);
        return tokenIds;
    }
    
	public static double[] documentTopics ( LDAModel ldaModel , String doc ) {
        int numSamples = 200;
        long randomSeed = 6474835;
        int burnin = 0;
        int sampleLag = 1;
		int[] tokens = tokenizeDocument(doc, ldaModel.symbolTable);
		return ldaModel.bayesTopicEstimate(tokens, numSamples, burnin, sampleLag, new Random(randomSeed));
	}

	public static void main ( String args[] ) throws Exception {
		String inputTrain = args[0];
		String inputTest = args[1];
		String outputTrain = args[2];
		String outputTest = args[3];
		short numTopics = new Short(args[4]);
		generateVectors(inputTrain,inputTest,outputTrain,outputTest,numTopics);
	}
	
	public static void generateVectors(String inputTrain1, String inputTest1, String outputTrain1, String outputTest1, short numTopics) throws Exception {
		BufferedReader inputTrain = new BufferedReader( new FileReader(inputTrain1) );
		BufferedReader inputTest = new BufferedReader( new FileReader(inputTest1) );
		PrintWriter outputTrain = new PrintWriter( new FileWriter(outputTrain1) );
		PrintWriter outputTest = new PrintWriter( new FileWriter(outputTest1) );
		List<CharSequence> instanceList = new ArrayList<CharSequence>();
        List<String> classList = new ArrayList<String>();
		String aux = null;        
        System.out.println("Reading training data...");
        while ( ( aux = inputTrain.readLine()) != null ) {
        	classList.add(aux.substring(0,aux.indexOf(" ")));
        	instanceList.add(aux.substring(aux.indexOf(" ")+1));
        }
        inputTrain.close();
        System.out.println("Fitting LDA model...");
        LDAModel model = new LDAModel ( instanceList.toArray(new CharSequence[0]), numTopics, 1 );
        System.out.println("Outputting representation of training data...");
        for ( int i = 0 ; i < instanceList.size(); i++ ) {
        	CharSequence str = instanceList.get(i);
        	String cl = classList.get(i);
        	double[] topics = documentTopics(model , str.toString() );
        	outputTrain.print(cl);
        	for ( double d : topics ) { outputTrain.print(" "); outputTrain.print(d); }
        	outputTrain.println();
        }
        outputTrain.close();
        System.out.println("Outputting representation of test data...");
        while ( ( aux = inputTest.readLine()) != null ) {
        	String cl = aux.substring(0,aux.indexOf(" "));
        	String str = aux.substring(aux.indexOf(" ")+1);
        	double[] topics = documentTopics(model , str.toString() );
        	outputTest.print(cl);
        	for ( double d : topics ) { outputTest.print(" "); outputTest.print(d); }
        	outputTest.println();
        }
        inputTest.close();
        outputTest.close();
	}
	
	 public static void generateDataAIMED() throws Exception, IOException {
		 for ( int f = 1 ; f <= 1; f++) {
			System.out.println("Generating AIMED data fold " + f );
			GenerateSetsEN.processAIMED("Datasets/aimed", "Datasets/aimed/splits/train-203-" + f, new PrintWriter(new FileWriter("train-data-aimed.txt.aux." + f)));
			GenerateSetsEN.processAIMED("Datasets/aimed", "Datasets/aimed/splits/test-203-" + f, new PrintWriter(new FileWriter("test-data-aimed.txt.aux" + f)));
			generateVectors("train-data-aimed.txt.aux." + f, "test-data-aimed.txt.aux." + f , "train-data-aimed.txt." + f , "test-data-aimed.txt." + f , (short)200);
		 }
	}

	 public static void generateDataSemEval() throws Exception, IOException {
		 System.out.println("Generating SemEval data...");
		 System.out.println("\nGenerating train data...");
		 GenerateSetsEN.processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", new PrintWriter(new FileWriter("train-data-semeval.aux.txt")));
		 System.out.println("\nGenerating test data...");
		 GenerateSetsEN.processSemEval("Datasets/SemEval2010_task8_all_data/SemEval2010_task8_testing_keys/TEST_FILE_FULL.TXT", new PrintWriter(new FileWriter("test-data-semeval.aux.txt")));
		 generateVectors("train-data-semeval.aux.txt", "test-data-semeval.aux.txt" ,"train-data-semeval.txt" , "test-data-semeval.txt" , (short) 4000);
	}
	  
	 public static void generateDataWikiEn() throws Exception, IOException {
		 System.out.println("Generating Wikipedia data...");
		 System.out.println("\nGenerating train data...");
		 GenerateSetsEN.processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.train", new PrintWriter(new FileWriter("train-data-wikien.aux.txt")));
		 System.out.println("\n\nGenerating test data...");
		 GenerateSetsEN.processWikipediaEN("Datasets/wikipedia_datav1.0/wikipedia.test", new PrintWriter(new FileWriter("test-data-wikien.aux.txt")));
		 generateVectors("train-data-wikien.aux.txt", "test-data-wikien.aux.txt" ,"train-data-wikien.txt" , "test-data-wikien.txt" , (short)200);
	}

}
