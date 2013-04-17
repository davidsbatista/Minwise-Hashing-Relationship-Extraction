package lda;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.tokenizer.*;
import com.aliasi.symbol.*;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;
import com.aliasi.stats.Statistics;
import com.aliasi.util.Files;
import java.util.*;
import java.io.*;

public abstract class TopicModel {

    public SymbolTable symbolTable = new MapSymbolTable();

    static final char[] VOWELS = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };

    static final String[] STOPWORD_LIST = new String[] {
        "these",
        "elegan",
        "caenorhabditi",
        "both",
        "may",
        "between",
        "our",
        "et",
        "al",
        "however",
        "many",
        "thu",
        "thus",
        "how",
        "while",
        "same",
        "here",
        "although",
        "those",
        "might",
        "see",
        "like",
        "likely",
        "where",
        "first",
        "second",
        "third",
        "fourth",
        "fifth",
        "sixth",
        "seventh",
        "eighth",
        "ninth",
        "i",
        "ii",
        "iii",
        "iv",
        "v",
        "vi",
        "vii",
        "viii",
        "ix",
        "x",
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen",
        "twenty",
        "thirty",
        "forty",
        "fifty",
        "sixty",
        "seventy",
        "eighty",
        "ninety",
        "hundred",
        "thousand",
        "million"
    };

    static final Set<String> STOPWORD_SET = new HashSet<String>(Arrays.asList(STOPWORD_LIST));

    static final TokenizerFactory BASE_TOKENIZER_FACTORY = new RegExTokenizerFactory("[\\x2Da-zA-Z0-9]+");

    static final TokenizerFactory simpleTokenizerFactory() {
        TokenizerFactory factory = BASE_TOKENIZER_FACTORY;
        factory = new NonAlphaStopTokenizerFactory(factory);
        factory = new LowerCaseTokenizerFactory(factory);
        factory = new EnglishStopTokenizerFactory(factory);
        factory = new StopTokenizerFactory(factory,STOPWORD_SET);
        factory = new StemTokenizerFactory(factory);
        return factory;
    }

    static boolean validStem(String stem) {
        if (stem.length() < 2) return false;
        for (int i = 0; i < stem.length(); ++i) {
            char c = stem.charAt(i);
            for (int k = 0; k < VOWELS.length; ++k)
                if (c == VOWELS[k]) return true;
        }
        return false;
    }

    static final TokenizerFactory WORMBASE_TOKENIZER_FACTORY = simpleTokenizerFactory();

    static CharSequence[] readCorpus(String data[]) {
        List<CharSequence> articleTextList = new ArrayList<CharSequence>();
        for ( String s : data ) articleTextList.add(s);
        int charCount = 0;
        for (CharSequence cs : articleTextList) charCount += cs.length();
        System.out.println("#articles=" + articleTextList.size() + " #chars=" + charCount);
        CharSequence[] articleTexts = articleTextList.<CharSequence>toArray(new CharSequence[articleTextList.size()]);
        return articleTexts;
    }

    static class NonAlphaStopTokenizerFactory extends ModifyTokenTokenizerFactory {
        static final long serialVersionUID = -3401639068551227864L;
        public NonAlphaStopTokenizerFactory(TokenizerFactory factory) {
            super(factory);
        }
        public String modifyToken(String token) {
            return stop(token) ? null : token;
        }
        public boolean stop(String token) {
            if (token.length() < 2) return true;
            for (int i = 0; i < token.length(); ++i) if (Character.isLetter(token.charAt(i))) return false;
            return true;
        }
    }

    static class StemTokenizerFactory extends ModifyTokenTokenizerFactory {
        static final long serialVersionUID = -6045422132691926248L;
        public StemTokenizerFactory(TokenizerFactory factory) {
            super(factory);
        }
        static final String[] SUFFIXES = new String[] { "ss", "ies", "sses", "s" };
        public String modifyToken(String token) {
            for (String suffix : SUFFIXES) {
                if (token.endsWith(suffix)) {
                    String stem = token.substring(0,token.length()-suffix.length());
                    return validStem(stem) ? stem : token;
                }
            }
            return token;
        }
    }

    protected TopicModel() { }

    public TopicModel ( String file ) throws Exception { read(file); }

    public abstract void write ( String file ) throws IOException;

    public abstract void read( String file ) throws IOException, ClassNotFoundException;

    public abstract int numTopics();

    public abstract int numWords();

    public abstract double[] wordProbabilities (int topic);

    public abstract double wordProbability(int topic, int word);

    public abstract double[] bayesTopicEstimate(int[] tokens, int numSamples, int burnin, int sampleLag, Random random);

}