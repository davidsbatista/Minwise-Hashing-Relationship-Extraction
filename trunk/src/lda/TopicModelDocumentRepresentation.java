package lda;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.aliasi.stats.Statistics;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.util.Files;


public class TopicModelDocumentRepresentation {

	private static boolean DEBUG = true;

	public static void main ( String args[] ) throws Exception {
		Set<String> dataset = new HashSet<String>();
		if ( args.length < 3 ) throw new Exception("Invalid arguments!");
		if ( !args[0].equals("-lda") && !args[0].equals("-hdp")) throw new Exception("Invalid arguments!");
		if ( args[1].startsWith("-input=") ) {
			File file = new File(args[1].substring(7));
	                File[] trainFiles = file.isDirectory() ? file.listFiles() : new File[]{ file };
	                for (int j = 0; j < trainFiles.length; ++j) {
	                    File trainFile = trainFiles[j];
	                    dataset.add(Files.readFromFile(trainFile,"ISO-8859-1"));
	                }
			String data[] = dataset.toArray(new String[0]);
			short numTopics = 3;
		        int minTokenCount = 1;
			TopicModel model = args[0].equals("-lda") ? new LDAModel(data,numTopics,minTokenCount) : new HDPModel(data,numTopics,minTokenCount);
			model.write(args[2]);
			if ( DEBUG ) {
				System.out.println("Number of topics : " + model.numTopics());
				System.out.println("Number of words : " + model.numWords());
				System.out.println("Word probabilities");
				for ( int i = 0; i < model.numTopics(); i++) {
					System.out.print("-> ");
					for ( double d : model.wordProbabilities(i) ) System.out.print(d + "\t");
					System.out.println();
				}
				System.out.println("Document-topic probabilities");
				for ( String str : data ) {
					System.out.print("-> ");
					double[] aux = documentTopics(model , str);
					for ( double d : aux ) System.out.print(d + "\t");
					System.out.println();
				}
			}
		} else if ( args[1].startsWith("-prob=") ) {
			File file = new File(args[1].substring(6));
			if ( args.length < 3 || !args[1].startsWith("-words=") ) throw new Exception("Invalid arguments!");
			File file2 = new File(args[2].substring(7));
			TopicModel ldaModel = new LDAModel( file.getAbsolutePath(), file2.getAbsolutePath() );
			ldaModel.write(args[2]);
		} else throw new Exception("Invalid arguments!");
	}

	public static double[] documentTopics ( TopicModel ldaModel , String doc ) {
	        int numSamples = 200;
	        long randomSeed = 6474835;
	        int burnin = 0;
	        int sampleLag = 1;
	        String cleanDoc = getAnalyzedText(doc);
			int[] tokens = tokenizeDocument(cleanDoc, ldaModel.symbolTable);
		return ldaModel.bayesTopicEstimate(tokens, numSamples, burnin, sampleLag, new Random(randomSeed));
	}

	public static double documentSimilarity ( TopicModel model , String doc1, String doc2, int metric ) {
	        double docTopics1[] = documentTopics(model,doc1);
	        double docTopics2[] = documentTopics(model,doc2);
		return distributionSimilarity(docTopics1,docTopics2,metric);
	};

	public static double perplexity( TopicModel ldaModel , String documents[] ) {

	        int numSamples = 20;
	        long randomSeed = 6474835;
	        int burnin = 0;
	        int sampleLag = 1;

    		double perplexity = 0, corpusLog2Prob = 0, crossEntropy = 0;
    		double size = 0;
    		for ( String doc : documents ) {

    			String cleanDoc = getAnalyzedText(doc);
    			int[] tokens = tokenizeDocument(cleanDoc, ldaModel.symbolTable);

    			double docTopics[] = ldaModel.bayesTopicEstimate(tokens, numSamples, burnin, sampleLag, new Random(randomSeed));
    			for ( int i = 0 ; i < tokens.length; i++ ) {
    				double wordProb = 0.0;
    				for ( int j = 0; j < docTopics.length; j++ ) {
    					wordProb += docTopics[j] * ldaModel.wordProbability(j,i);
    				}
    				corpusLog2Prob += com.aliasi.util.Math.log2(wordProb);
    			}

    			size += tokens.length;
    		}

    		crossEntropy = corpusLog2Prob / size;
    		perplexity = Math.pow(2, (-crossEntropy));
    		return perplexity;
	};

	/**
     * Accepts the specified preprocessed text document returning only tokens
     * that exist in the symbol table.  This
     * method is useful within a given LDA model for tokenizing new documents
     * into lists of words.
     *
     * @param text The pre-processed text, i.e., lowercased, words separated by single whitespace.
     * @param symbolTable Symbol table to use for converting tokens
     * to symbols.
     * @return The array of integer symbols for tokens that exist in
     * the symbol table.
     */
    public static int[] tokenizeDocument(String text, SymbolTable symbolTable) {

        String[] words = text.split(" ");
        List<Integer> idList = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) {
            int id = symbolTable.symbolToID(words[i]);
            if (id >= 0)
                idList.add(id);
        }
        int[] tokenIds = new int[idList.size()];
        for (int i = 0; i < tokenIds.length; ++i)
            tokenIds[i] = idList.get(i);

        return tokenIds;
    }

    /**
     * Get a "clean" version of the given text, i.e., remove stopwords, extra whitespaces, lowercase,
     * and remove accents.
     *
     * @param text
     * @return
     * @throws IOException
     */
    public static String getAnalyzedText(String text) {

    	StringBuilder sb = new StringBuilder();

    	try {
    		MyAnalyzer a = MyAnalyzerPool.getInstance().acquireTextAnalyzer();

    		TokenStream ts = a.tokenStream( null, new StringReader(text));
    		CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);

    		ts.reset();
    		while (ts.incrementToken()) {
    			sb.append(termAtt.buffer(), 0, termAtt.length());
    			sb.append(' ');
    		}

    		ts.end();
    		ts.close();
    		MyAnalyzerPool.getInstance().releaseTextAnazyzer(a);

    	} catch(Exception e) {
    		e.printStackTrace();
    	}

    	return sb.toString();
    }


	public static Map<String,Double> topWords ( TopicModel ldaModel , int k, int t ) {
		double words[] = ldaModel.wordProbabilities(t-1);
		Map<String,Double> map = new HashMap<String,Double>();
		for ( int i = 0; i<words.length; i++) map.put(ldaModel.symbolTable.idToSymbol(i),words[i]);
		return sortByComparator(map,k);
	};

	private static Map sortByComparator(Map unsortMap, int k) {
	        List list = new LinkedList(unsortMap.entrySet());
        	Collections.sort(list, new Comparator() {
             		public int compare(Object o1, Object o2) {
	        	   return ((Comparable) ((Map.Entry) (o2)).getValue())
	           		.compareTo(((Map.Entry) (o1)).getValue());
             		}
		});
		Map sortedMap = new LinkedHashMap();
		int i = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			if( k > 0 && ++i > k) break;
	     		Map.Entry entry = (Map.Entry)it.next();
	     		sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
   	};

	public static double distributionSimilarity ( double docTopics1[], double docTopics2[], int metric ) {
		switch ( metric ) {
			case 1  : 			double cosine = 0;
							double aux1 = 0;
							double aux2 = 0;
							for ( int i = 0 ; i<docTopics1.length ; i++ ) {
								cosine += docTopics1[i] * docTopics2[i];
								aux1 += docTopics1[i] * docTopics1[i];
								aux2 += docTopics2[i] * docTopics2[i];
							}
							cosine = cosine / ( Math.sqrt(aux1) * Math.sqrt(aux2) );
							return cosine;
			case 2  :			double max1 = Double.MIN_VALUE;
							double max2 = Double.MIN_VALUE;
							int topic1 = -1;
							int topic2 = -1;
							for ( int i = 0 ; i<docTopics1.length ; i++ ) {
								if ( docTopics1[i] > max1 ) { max1 = docTopics1[i]; topic1 = i; }
								if ( docTopics2[i] > max2 ) { max2 = docTopics2[i]; topic2 = i; }
							}
							return topic1 == topic2 ? 1.0 : 0.0;
			case 3  :			return Statistics.jsDivergence(docTopics1, docTopics2);
			case 4  : 			double auxB = 0;
							for ( int i = 0 ; i<docTopics1.length ; i++ ) {
								auxB += docTopics1[i] * docTopics2[i];
							}
							return -Math.log(auxB);
			case 5  : 			double auxH = 0;
							for ( int i = 0 ; i<docTopics1.length ; i++ ) {
								auxH += docTopics1[i] * docTopics2[i];
							}
							return Math.sqrt(1 - auxH);
			case 6  : 			double auxM = 0;
							for ( int i = 0 ; i<docTopics1.length ; i++ ) {
								auxM += docTopics1[i] * docTopics2[i];
							}
							return 2 + 2 * Math.log(auxM);
			default : 			return Statistics.symmetrizedKlDivergence(docTopics1, docTopics2);
		}
	}

}
