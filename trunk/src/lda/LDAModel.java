package lda;

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

public class LDAModel extends TopicModel {

    public LatentDirichletAllocation model = null;

    public int numTopics() { return model.numTopics(); }

    public int numWords() { return model.numWords(); }

    public static int[] tokenizeDocument(CharSequence text, TokenizerFactory tokenizerFactory, SymbolTable symbolTable) {
	return LatentDirichletAllocation.tokenizeDocument(text,tokenizerFactory,symbolTable);
    }

    public static int[][] tokenizeDocuments(CharSequence[] texts, TokenizerFactory tokenizerFactory, SymbolTable symbolTable, int minCount) {
	return LatentDirichletAllocation.tokenizeDocuments(texts,tokenizerFactory,symbolTable,minCount);
    }

    public double[] wordProbabilities (int topic) { return model.wordProbabilities(topic); }

    public double wordProbability(int topic, int word) { return model.wordProbability(topic,word); }

    public double[] bayesTopicEstimate(int[] tokens, int numSamples, int burnin, int sampleLag, Random random) {
	return model.bayesTopicEstimate(tokens,numSamples,burnin,sampleLag,random);
    }
    
    public LDAModel ( String probs, String words ) throws Exception {
	readFromText(probs,words);
    }

    public LDAModel ( String[] data , short numTopics, int minTokenCount ) throws Exception {
        double topicPrior = 0.1;
        double wordPrior = 0.01;
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 2000;
        long randomSeed = 6474835;
        CharSequence[] articleTexts = readCorpus(data);        
	for ( CharSequence a : articleTexts) System.out.println(a);
        int[][] docTokens = LatentDirichletAllocation.tokenizeDocuments(articleTexts,WORMBASE_TOKENIZER_FACTORY,symbolTable,minTokenCount);
        int numTokens = 0;
        for (int[] tokens : docTokens) numTokens += tokens.length;
        LdaReportingHandler handler = new LdaReportingHandler(symbolTable);
        LatentDirichletAllocation.GibbsSample sample = LatentDirichletAllocation.gibbsSampler(docTokens, numTopics, topicPrior, wordPrior, burninEpochs, sampleLag, numSamples, new Random(randomSeed), handler);
        int maxWordsPerTopic = 200;
        int maxTopicsPerDoc = numTopics;
        boolean reportTokens = true;
        handler.fullReport(sample,maxWordsPerTopic,maxTopicsPerDoc,reportTokens);
        this.model = sample.lda();
    }

    public LDAModel ( String file ) throws Exception { this.read(file); }

    public void write ( String file ) throws IOException {			
			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
			out.writeObject(model);
			out.writeObject(symbolTable);
			out.close();
    }

    public void read( String file ) throws IOException, ClassNotFoundException {
			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
			model = (LatentDirichletAllocation)(in.readObject());
			symbolTable = (SymbolTable)(in.readObject());
			in.close();
    }

    public void readFromText ( String fileP , String fileW ) throws IOException {
	Set<double[]> set = new HashSet<double[]>();
	BufferedReader prob = new BufferedReader(new FileReader(fileP));
	BufferedReader words = new BufferedReader(new FileReader(fileW));
	String line = null;
	Map<String,Integer> symbolToIdMap = new HashMap<String,Integer>();
	while ( (line=words.readLine()) != null ) {
		String aux[] = line.split("(\t| )+");
		if (aux.length > 1) symbolToIdMap.put(aux[0],new Integer(aux[1]));
	}
	symbolTable = new MapSymbolTable(symbolToIdMap);
	while ( (line=prob.readLine()) != null ) {
		String aux[] = line.split(" ");
		double vals[] = new double[aux.length];
		for ( int i=0; i<aux.length; i++ ) vals[i] = new Double(aux[i]).doubleValue();
		set.add(vals);
	}
	model = new LatentDirichletAllocation(0.5, set.toArray(new double[0][0]));
    }
 
}

class LdaReportingHandler implements ObjectHandler<LatentDirichletAllocation.GibbsSample> {

    private final SymbolTable mSymbolTable;
    private final long mStartTime;

    LdaReportingHandler(SymbolTable symbolTable) {
        mSymbolTable = symbolTable;
        mStartTime = System.currentTimeMillis();
    }

    public void handle(LatentDirichletAllocation.GibbsSample sample) {
        System.out.printf("Epoch=%3d   elapsed time=%s\n",
                          sample.epoch(),
                          Strings.msToString(System.currentTimeMillis() - mStartTime));
        if ((sample.epoch() % 10) == 0) {
            double corpusLog2Prob = sample.corpusLog2Probability();
            System.out.println("      log2 p(corpus|phi,theta)=" + corpusLog2Prob
                               + "     token cross-entropy rate=" + (-corpusLog2Prob/sample.numTokens()));
        }
    }

    void fullReport(LatentDirichletAllocation.GibbsSample sample, int maxWordsPerTopic, int maxTopicsPerDoc, boolean reportTokens) {
        System.out.println("\nFull Report");
        int numTopics = sample.numTopics();
        int numWords = sample.numWords();
        int numDocs = sample.numDocuments();
        int numTokens = sample.numTokens();
        System.out.println("epoch=" + sample.epoch());
        System.out.println("numDocs=" + numDocs);
        System.out.println("numTokens=" + numTokens);
        System.out.println("numWords=" + numWords);
        System.out.println("numTopics=" + numTopics);
        for (int topic = 0; topic < numTopics; ++topic) {
            int topicCount = sample.topicCount(topic);
            ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
            for (int word = 0; word < numWords; ++word) counter.set(Integer.valueOf(word),sample.topicWordCount(topic,word));
            List<Integer> topWords = counter.keysOrderedByCountList();
            System.out.println("\nTOPIC " + topic  + "  (total count=" + topicCount + ")");
            System.out.println("SYMBOL             WORD    COUNT   PROB          Z");
            System.out.println("--------------------------------------------------");
            for (int rank = 0; rank < maxWordsPerTopic && rank < topWords.size(); ++rank) {
                int wordId = topWords.get(rank);
                String word = mSymbolTable.idToSymbol(wordId);
                int wordCount = sample.wordCount(wordId);
                int topicWordCount = sample.topicWordCount(topic,wordId);
                double topicWordProb = sample.topicWordProb(topic,wordId);
                double z = binomialZ(topicWordCount,
                                     topicCount,
                                     wordCount,
                                     numTokens);
                System.out.printf("%6d  %15s  %7d   %4.3f  %8.1f\n",
                                  wordId,
                                  word,
                                  topicWordCount,
                                  topicWordProb,
                                  z);
            }
        }
        for (int doc = 0; doc < numDocs; ++doc) {
            int docCount = 0;
            for (int topic = 0; topic < numTopics; ++topic) docCount += sample.documentTopicCount(doc,topic);
            ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
            for (int topic = 0; topic < numTopics; ++topic) counter.set(Integer.valueOf(topic),sample.documentTopicCount(doc,topic));
            List<Integer> topTopics = counter.keysOrderedByCountList();
            System.out.println("\nDOC " + doc);
            System.out.println("TOPIC    COUNT    PROB");
            System.out.println("----------------------");
            for (int rank = 0; rank < topTopics.size() && rank < maxTopicsPerDoc; ++rank) {
                int topic = topTopics.get(rank);
                int docTopicCount = sample.documentTopicCount(doc,topic);
                double docTopicPrior = sample.documentTopicPrior();
                double docTopicProb = (sample.documentTopicCount(doc,topic) + docTopicPrior) / (docCount + numTopics * docTopicPrior);
                System.out.printf("%5d  %7d   %4.3f\n", topic, docTopicCount, docTopicProb);
            }
            System.out.println();
            if (!reportTokens) continue;
            int numDocTokens = sample.documentLength(doc);
            for (int tok = 0; tok < numDocTokens; ++tok) {
                int symbol = sample.word(doc,tok);
                short topic = sample.topicSample(doc,tok);
                String word = mSymbolTable.idToSymbol(symbol);
                System.out.print(word + "(" + topic + ") ");
            }
            System.out.println();
        }
    }

    static double binomialZ(double wordCountInDoc, double wordsInDoc, double wordCountinCorpus, double wordsInCorpus) {
        double pCorpus = wordCountinCorpus / wordsInCorpus;
        double var = wordsInCorpus * pCorpus * (1 - pCorpus);
        double dev = Math.sqrt(var);
        double expected = wordsInDoc * pCorpus;
        double z = (wordCountInDoc - expected) / dev;
        return z;
    }

}