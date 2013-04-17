package lda;

import java.io.*;
import java.util.*;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.tokenizer.*;
import com.aliasi.symbol.*;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;
import com.aliasi.stats.Statistics;
import java.util.zip.*;
import java.util.*;
import java.io.*;


public class HDPModel extends TopicModel implements Serializable {

        private HDPModel ( ) { }
    
        public HDPModel ( String[] data , short numTopics, int minTokenCount ) throws Exception {
         CharSequence[] articleTexts = readCorpus(data);        
	 for ( CharSequence a : articleTexts) System.out.println(a);
         int[][] docTokens = LatentDirichletAllocation.tokenizeDocuments(articleTexts,WORMBASE_TOKENIZER_FACTORY,symbolTable,minTokenCount);
         int numTokens = 0;
         for (int[] tokens : docTokens) numTokens += tokens.length;
	 int shuffleLag = 0;
	 int maxIter = 100;
	 addInstances(docTokens, symbolTable.numSymbols());		
	 run(shuffleLag, maxIter, System.out);
	 for (int k = 0; k < numberOfTopics; k++) {
 		for (int w = 0; w < sizeOfVocabulary; w++) System.out.format("%05d ", wordCountByTopicAndTerm[k][w]);
		System.out.println();
	 }
	 System.out.println("d w z t");
	 int t, docID;
	 for (int d = 0; d < docStates.length; d++) {
		HDPModel.DOCState docState = docStates[d];
		docID = docState.docID;
		for (int i = 0; i < docState.documentLength; i++) {
			t = docState.words[i].tableAssignment;
			System.out.println(docID + " " + docState.words[i].termIndex + " " + docState.tableToTopic[t] + " " + t);
		}
	 }
        }

        public HDPModel ( String file ) throws Exception { this.read(file); }

        public void write ( String file ) throws IOException {			
			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
			out.writeObject(this);
			out.writeObject(symbolTable);
			out.close();
        }

        public void read( String file ) throws IOException, ClassNotFoundException {
			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
			HDPModel model = (HDPModel)(in.readObject());
			symbolTable = (SymbolTable)(in.readObject());
			in.close();
			this.beta = model.beta;
			this.gamma = model.gamma;
			this.alpha = model.alpha;
			this.p = model.p;
			this.f = model.f;
			this.docStates = model.docStates;
			this.numberOfTablesByTopic = model.numberOfTablesByTopic;
			this.wordCountByTopic = model.wordCountByTopic;
			this.wordCountByTopicAndTerm = model.wordCountByTopicAndTerm;
			this.sizeOfVocabulary = model.sizeOfVocabulary;
			this.totalNumberOfWords = model.totalNumberOfWords;
			this.numberOfTopics = model.numberOfTopics;
			this.totalNumberOfTables = model.totalNumberOfTables;
        }

	public double beta  = 0.5; // default only
	public double gamma = 1.5;
	public double alpha = 1.0;
	
	private Random random = new Random(123456);
	private double[] p;
	private double[] f;
	
	protected DOCState[] docStates;
	protected int[] numberOfTablesByTopic;
	protected int[] wordCountByTopic;
	protected int[][] wordCountByTopicAndTerm;
	
	protected int sizeOfVocabulary;
	protected int totalNumberOfWords;
	protected int numberOfTopics = 1;
	protected int totalNumberOfTables;

	public HDPModel clone ( ) {
		HDPModel newHDP = new HDPModel();
		newHDP.beta = beta;
		newHDP.gamma = gamma;
		newHDP.alpha = alpha;
		newHDP.p = (double[])p.clone();
		newHDP.f = (double[])f.clone();
		newHDP.docStates = (DOCState[])docStates.clone();
		newHDP.numberOfTablesByTopic = (int[])numberOfTablesByTopic.clone();
		newHDP.wordCountByTopic = (int[])wordCountByTopic.clone();
		newHDP.wordCountByTopicAndTerm = (int[][])wordCountByTopicAndTerm.clone();
		newHDP.sizeOfVocabulary = sizeOfVocabulary;
		newHDP.totalNumberOfWords = totalNumberOfWords;
		newHDP.numberOfTopics = numberOfTopics;
		newHDP.totalNumberOfTables = totalNumberOfTables;
		return newHDP;
	}

	public int numTopics() { return numberOfTopics; }

	public int numWords() { return sizeOfVocabulary; }

	public int numTables() { return totalNumberOfTables; }

	public static int[] tokenizeDocument(CharSequence text, TokenizerFactory tokenizerFactory, SymbolTable symbolTable) {
		return LatentDirichletAllocation.tokenizeDocument(text, tokenizerFactory, symbolTable);
	}

	public static int[][] tokenizeDocuments(CharSequence[] texts, TokenizerFactory tokenizerFactory, SymbolTable symbolTable, int minCount) {
		return LatentDirichletAllocation.tokenizeDocuments(texts, tokenizerFactory, symbolTable, minCount);
	}

 	public double[]	wordProbabilities (int topic) {
		double[] aux = new double[sizeOfVocabulary];
		for ( int i = 0 ; i< aux.length ; i++ ) aux[i] = wordProbability(topic,i);
		return aux;
	}

	public double wordProbability(int topic, int word) {
	        if ( wordCountByTopic[word] == 0.0 ) return 0.0;
		return (double)(wordCountByTopicAndTerm[topic][word]) / (double)(wordCountByTopic[word]);
	}

        public double[] bayesTopicEstimate(int[] tokens, int numSamples, int burnin, int sampleLag, Random random) {
           short[][] sampleTopics = sampleTopics(tokens,numSamples,burnin,sampleLag,random);
           int numTopics = numTopics();
           int[] counts = new int[numTopics];
           for (short[] topics : sampleTopics) { for (int tok = 0; tok < topics.length; ++tok) ++counts[topics[tok]]; }
           double totalCount = 0;
           for (int topic = 0; topic < numTopics; ++topic) totalCount += counts[topic];
           double[] result = new double[numTopics];
           for (int topic = 0; topic < numTopics; ++topic) result[topic] = counts[topic] / totalCount;
           return result;
        }

        public short[][] sampleTopics(int[] tokens, int numSamples, int burnin, int sampleLag, Random random) {
        	if (burnin < 0) {
	            String msg = "Burnin period must be non-negative." + " Found burnin=" + burnin;
	            throw new IllegalArgumentException(msg);
	        }
	        if (numSamples < 1) {
	            String msg = "Number of samples must be at least 1." + " Found numSamples=" + numSamples;
	            throw new IllegalArgumentException(msg);
	        }
	        if (sampleLag < 1) {
	            String msg = "Sample lag must be at least 1." + " Found sampleLag=" + sampleLag;
	            throw new IllegalArgumentException(msg);
	        }
		HDPModel aux = this.clone();
		aux.docStates = new DOCState[]{ new DOCState(tokens, 0) };
		for (int i = 0; i < aux.docStates[0].documentLength; i++) aux.addWord(aux.docStates[0].docID, i, 0, 0);
		short samples[][]= new short[numSamples][tokens.length];
	        int sample = 0;
	        short[] currentSample = samples[0];
		int numEpochs = burnin + sampleLag * (numSamples - 1);
		for (int epoch = 0; epoch < numEpochs; ++epoch) try {
		        for (int i = 0; i < aux.docStates[0].documentLength; i++) {
				aux.removeWord(0, i);
				int table = aux.sampleTable(0, i);
				if (table == aux.docStates[0].numberOfTables ) {
					int topic;
					do {
						double pSum = 0.0;
						aux.p = aux.ensureCapacity(aux.p, aux.numberOfTopics);
						for (int k = 0; k < aux.numberOfTopics; k++) {
							pSum += aux.numberOfTablesByTopic[k] * f[k];
							aux.p[k] = pSum;
						}
						pSum += gamma / sizeOfVocabulary;
						aux.p[numberOfTopics] = pSum;
						double u = random.nextDouble() * pSum;
						for (topic = 0; topic < numberOfTopics; topic++) if (u < aux.p[topic]) break;
					} while ( topic == numberOfTopics );
					aux.addWord(0, i, table, topic);
				} else aux.addWord(0, i, table, aux.docStates[0].tableToTopic[table]);
		        }
		        aux.defragment();
			for (int i = 0; i < aux.docStates[0].documentLength; i++) {
				int table = aux.docStates[0].words[i].tableAssignment;
				int topic = aux.docStates[0].tableToTopic[table];
				currentSample[i] = (short)topic;
			}
	                if ((epoch >= burnin) && (((epoch - burnin) % sampleLag) == 0)) {
	                  short[] pastSample = currentSample;
	                  ++sample;
	                  currentSample = samples[sample];
	                  for (int token = 0; token < tokens.length; ++token) currentSample[token] = pastSample[token];
	                }
	        } catch ( Exception ex ) { ex.printStackTrace(); }
	        return samples;
        }

	public void addInstances(int[][] documentsInput, int V) {
		sizeOfVocabulary = V;
		totalNumberOfWords = 0;
		docStates = new DOCState[documentsInput.length];
		for (int d = 0; d < documentsInput.length; d++) {
			docStates[d] = new DOCState(documentsInput[d], d);
			totalNumberOfWords += documentsInput[d].length;
		}
		int k, i, j;
		DOCState docState;
		p = new double[20]; 
		f = new double[20];
		numberOfTablesByTopic = new int[numberOfTopics+1];
		wordCountByTopic = new int[numberOfTopics+1];
		wordCountByTopicAndTerm = new int[numberOfTopics+1][];
		for (k = 0; k <= numberOfTopics; k++) wordCountByTopicAndTerm[k] = new int[sizeOfVocabulary];
		for (k = 0; k < numberOfTopics; k++) { 
			docState = docStates[k];
			for (i = 0; i < docState.documentLength; i++) addWord(docState.docID, i, 0, k);
		}
		for (j = numberOfTopics; j < docStates.length; j++) {
			docState = docStates[j]; 
			k = random.nextInt(numberOfTopics);
			for (i = 0; i < docState.documentLength; i++) addWord(docState.docID, i, 0, k);
		}
	}

	protected void nextGibbsSweep() {
		int table;
		for (int d = 0; d < docStates.length; d++) for (int i = 0; i < docStates[d].documentLength; i++) {
				removeWord(d, i);
				table = sampleTable(d, i);
				if (table == docStates[d].numberOfTables) addWord(d, i, table, sampleTopic());
				else addWord(d, i, table, docStates[d].tableToTopic[table]);
		}
		defragment();
	}

	protected void nextGibbsSweep2() {

		for (int d = 0; d < docStates.length; d++) for (int i = 0; i < docStates[d].documentLength; i++) {
				removeWord(d, i);
				int table = sampleTable(d, i);
				if (table == docStates[d].numberOfTables) {
					int topic;
					do {
						double u, pSum = 0.0;
						p = ensureCapacity(p, numberOfTopics);
						for (int k = 0; k < numberOfTopics; k++) {
							pSum += numberOfTablesByTopic[k] * f[k];
							p[k] = pSum;
						}
						pSum += gamma / sizeOfVocabulary;
						p[numberOfTopics] = pSum;
						u = random.nextDouble() * pSum;
						for (topic = 0; topic <= numberOfTopics; topic++) if (u < p[topic]) break;
					} while (topic == numberOfTopics );
					addWord(d, i, table, topic);
				} else addWord(d, i, table, docStates[d].tableToTopic[table]);
		}
		defragment();
	}

	private int sampleTopic() {
		double u, pSum = 0.0;
		int k;
		p = ensureCapacity(p, numberOfTopics);
		for (k = 0; k < numberOfTopics; k++) {
			pSum += numberOfTablesByTopic[k] * f[k];
			p[k] = pSum;
		}
		pSum += gamma / sizeOfVocabulary;
		p[numberOfTopics] = pSum;
		u = random.nextDouble() * pSum;
		for (k = 0; k <= numberOfTopics; k++) if (u < p[k]) break;
		return k;
	}

	int sampleTable(DOCState docState, int i) {	
		int k, j;
		double pSum = 0.0, vb = sizeOfVocabulary * beta, fNew, u;
		f = ensureCapacity(f, numberOfTopics);
		p = ensureCapacity(p, docState.numberOfTables);
		fNew = gamma / sizeOfVocabulary;
		for (k = 0; k < numberOfTopics; k++) {
			f[k] = (wordCountByTopicAndTerm[k][docState.words[i].termIndex] + beta) / (wordCountByTopic[k] + vb);
			fNew += numberOfTablesByTopic[k] * f[k];
		}
		for (j = 0; j < docState.numberOfTables; j++) {
			if (docState.wordCountByTable[j] > 0) pSum += docState.wordCountByTable[j] * f[docState.tableToTopic[j]];
			p[j] = pSum;
		}
		pSum += alpha * fNew / (totalNumberOfTables + gamma);
		p[docState.numberOfTables] = pSum;
		u = random.nextDouble() * pSum;
		for (j = 0; j <= docState.numberOfTables; j++) if (u < p[j]) break;
		return j;
	}

	int sampleTable(int docID, int i) {	
		DOCState docState = docStates[docID];
		return sampleTable(docState,i);
	}

	public void run(int shuffleLag, int maxIter, PrintStream log) throws IOException {
		for (int iter = 0; iter < maxIter; iter++) {
			if ((shuffleLag > 0) && (iter > 0) && (iter % shuffleLag == 0)) doShuffle();
			nextGibbsSweep();
			log.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = " + totalNumberOfTables );
		}
	}

	protected DOCState removeWord(DOCState docState, int i){
		int table = docState.words[i].tableAssignment;
		int k = docState.tableToTopic[table];
		docState.wordCountByTable[table]--; 
		wordCountByTopic[k]--; 		
		wordCountByTopicAndTerm[k][docState.words[i].termIndex] --;
		if (docState.wordCountByTable[table] == 0) {
			totalNumberOfTables--; 
			numberOfTablesByTopic[k]--; 
			docState.tableToTopic[table] --; 
		}
		return docState;
	}
		
	protected void removeWord(int docID, int i){
		docStates[docID] = removeWord(docStates[docID],i);
	}

	protected DOCState addWord(DOCState docState, int i, int table, int k) {
		docState.words[i].tableAssignment = table; 
		docState.wordCountByTable[table]++; 
		wordCountByTopic[k]++;
		wordCountByTopicAndTerm[k][docState.words[i].termIndex] ++;
		if (docState.wordCountByTable[table] == 1) {
			docState.numberOfTables++;
			docState.tableToTopic[table] = k;
			totalNumberOfTables++;
			numberOfTablesByTopic[k]++; 
			docState.tableToTopic = ensureCapacity(docState.tableToTopic, docState.numberOfTables);
			docState.wordCountByTable = ensureCapacity(docState.wordCountByTable, docState.numberOfTables);
			if (k == numberOfTopics) {
				numberOfTopics++; 
				numberOfTablesByTopic = ensureCapacity(numberOfTablesByTopic, numberOfTopics); 
				wordCountByTopic = ensureCapacity(wordCountByTopic, numberOfTopics);
				wordCountByTopicAndTerm = add(wordCountByTopicAndTerm, new int[sizeOfVocabulary], numberOfTopics);
			}
		}
		return docState;
	}
	
	protected void addWord(int docID, int i, int table, int k) {
		docStates[docID] = addWord(docStates[docID],i,table,k);
	}

	protected void defragment() {
		int[] kOldToKNew = new int[numberOfTopics];
		int k, newNumberOfTopics = 0;
		for (k = 0; k < numberOfTopics; k++) {
			if (wordCountByTopic[k] > 0) {
				kOldToKNew[k] = newNumberOfTopics;
				swap(wordCountByTopic, newNumberOfTopics, k);
				swap(numberOfTablesByTopic, newNumberOfTopics, k);
				swap(wordCountByTopicAndTerm, newNumberOfTopics, k);
				newNumberOfTopics++;
			} 
		}
		numberOfTopics = newNumberOfTopics;
		for (int j = 0; j < docStates.length; j++) docStates[j].defragment(kOldToKNew);
	}
	
	protected void doShuffle(){
		List<DOCState> h = Arrays.asList(docStates);
		Collections.shuffle(h);
		docStates = h.toArray(new DOCState[h.size()]);
		for (int j = 0; j < docStates.length; j ++){
			List<WordState> h2 = Arrays.asList(docStates[j].words);
			Collections.shuffle(h2);
			docStates[j].words = h2.toArray(new WordState[h2.size()]);
		}
	}
	
	public static void swap(int[] arr, int arg1, int arg2){
		   int t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static void swap(int[][] arr, int arg1, int arg2) {
		   int[] t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static double[] ensureCapacity(double[] arr, int min){
		int length = arr.length;
		if (min < length) return arr;
		double[] arr2 = new double[min*2];
		for (int i = 0; i < length; i++) arr2[i] = arr[i];
		return arr2;
	}

	public static int[] ensureCapacity(int[] arr, int min) {
		int length = arr.length;
		if (min < length) return arr;
		int[] arr2 = new int[min*2];
		for (int i = 0; i < length; i++) arr2[i] = arr[i];
		return arr2;
	}

	public static int[][] add(int[][] arr, int[] newElement, int index) {
		int length = arr.length;
		if (length <= index){
			int[][] arr2 = new int[index*2][];
			for (int i = 0; i < length; i++) arr2[i] = arr[i];
			arr = arr2;
		}
		arr[index] = newElement;
		return arr;
	}
	
	class DOCState implements Serializable {

		int docID, documentLength, numberOfTables;
		int[] tableToTopic; 
		int[] wordCountByTable;
		WordState[] words;

		public DOCState(int[] instance, int docID) {
		    this.docID = docID;
		    numberOfTables = 0;  
		    documentLength = instance.length;
		    words = new WordState[documentLength];	
		    wordCountByTable = new int[2];
		    tableToTopic = new int[2];
		    for (int position = 0; position < documentLength; position++) words[position] = new WordState(instance[position], -1);
		}

		public void defragment(int[] kOldToKNew) {
		    int[] tOldToTNew = new int[numberOfTables];
		    int t, newNumberOfTables = 0;
		    for (t = 0; t < numberOfTables; t++){
		        if (wordCountByTable[t] > 0){
		            tOldToTNew[t] = newNumberOfTables;
		            tableToTopic[newNumberOfTables] = kOldToKNew[tableToTopic[t]];
		            swap(wordCountByTable, newNumberOfTables, t);
		            newNumberOfTables ++;
		        } else tableToTopic[t] = -1;
		    }
		    numberOfTables = newNumberOfTables;
		    for (int i = 0; i < documentLength; i++) words[i].tableAssignment = tOldToTNew[words[i].tableAssignment];
		}
	}
	
	class WordState implements Serializable {   
	
		int termIndex;
		int tableAssignment;
		
		public WordState(int wordIndex, int tableAssignment){
			this.termIndex = wordIndex;
			this.tableAssignment = tableAssignment;
		}

	}

}
