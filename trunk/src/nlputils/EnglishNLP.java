package nlputils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.northwestern.at.utils.CharUtils;
import edu.northwestern.at.utils.corpuslinguistics.adornedword.AdornedWord;
import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.DefaultLemmatizer;
import edu.northwestern.at.utils.corpuslinguistics.lemmatizer.Lemmatizer;
import edu.northwestern.at.utils.corpuslinguistics.lexicon.Lexicon;
import edu.northwestern.at.utils.corpuslinguistics.partsofspeech.PartOfSpeechTags;
import edu.northwestern.at.utils.corpuslinguistics.postagger.DefaultPartOfSpeechTagger;
import edu.northwestern.at.utils.corpuslinguistics.postagger.PartOfSpeechTagger;
import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.DefaultSentenceSplitter;
import edu.northwestern.at.utils.corpuslinguistics.sentencesplitter.SentenceSplitter;
import edu.northwestern.at.utils.corpuslinguistics.spellingstandardizer.DefaultSpellingStandardizer;
import edu.northwestern.at.utils.corpuslinguistics.spellingstandardizer.SpellingStandardizer;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.DefaultWordTokenizer;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.PennTreebankTokenizer;
import edu.northwestern.at.utils.corpuslinguistics.tokenizer.WordTokenizer;

public class EnglishNLP {

    public static String lemmaSeparator = "|";

    static PartOfSpeechTagger partOfSpeechTagger = null;
    static Lexicon wordLexicon = null;
    static PartOfSpeechTags partOfSpeechTags = null;
    static WordTokenizer wordTokenizer = null;
    static WordTokenizer spellingTokenizer = null;
    static SentenceSplitter sentenceSplitter = null;
    static Lemmatizer lemmatizer = null;
	static SpellingStandardizer standardizer = null;

	public static Multimap<String, String> levin_verb_classes = null;
	
	public static Collection<String> getVerbClass(String verb) {
		return levin_verb_classes.get(verb);
	}
	
	public static void readVerbClasses(String path) throws IOException {
		levin_verb_classes = LinkedListMultimap.create();
		BufferedReader input = new BufferedReader( new FileReader(new File(path)) );
		String aux = null;
		String levin_class = null;
		String[] verbs = null;
		int n_classes = 0;
		while ( ( aux = input.readLine() ) != null ) {
			if ( aux.startsWith("VERB") ) {
				String[] data = aux.split("\\s+",2);
				levin_class = data[0];
				n_classes++;
			} else if ( aux.trim().length() != 0) {
				verbs = aux.split("\\s+");
				for (int z = 0; z < verbs.length; z++) levin_verb_classes.put(verbs[z],levin_class);
			}
		}
		System.out.println("Total Levin classes: " + n_classes);
		System.out.println("Number of unique verbs: " + levin_verb_classes.keySet().size());	
		input.close();
	}

    public static String adornText( String textToAdorn, int num ) {
	  try {
	    String aux = "";
		if ( partOfSpeechTagger == null) {
          partOfSpeechTagger = new DefaultPartOfSpeechTagger();
          wordLexicon = partOfSpeechTagger.getLexicon();
          partOfSpeechTags = wordLexicon.getPartOfSpeechTags();
          wordTokenizer = new DefaultWordTokenizer();
          spellingTokenizer = new PennTreebankTokenizer();
          sentenceSplitter = new DefaultSentenceSplitter();
          sentenceSplitter.setPartOfSpeechGuesser(partOfSpeechTagger.getPartOfSpeechGuesser());
          lemmatizer = new DefaultLemmatizer();
  		  standardizer = new DefaultSpellingStandardizer();
	    }
        List<List<String>> sentences  = sentenceSplitter.extractSentences( textToAdorn , wordTokenizer );
        List<List<AdornedWord>> taggedSentences = partOfSpeechTagger.tagSentences( sentences );
        for ( int i = 0 ; i < sentences.size() ; i++ ) {
            List<AdornedWord> sentence  = taggedSentences.get( i );
            for ( int j = 0 ; j < sentence.size() ; j++ ) {
                AdornedWord adornedWord = sentence.get( j );
                setStandardSpelling(adornedWord , standardizer , partOfSpeechTags );
                setLemma(adornedWord , wordLexicon , lemmatizer , partOfSpeechTags , spellingTokenizer );
				if ( num == 0 ) aux += " " + adornedWord.getSpelling().replaceAll(" ","_");
                if ( num == 1 ) aux += " " + adornedWord.getPartsOfSpeech().replaceAll(" ","_");
                if ( num == 2 ) aux += " " + adornedWord.getStandardSpelling().replaceAll(" ","_");
                if ( num == 3 ) aux += " " + adornedWord.getLemmata().replaceAll(" ","_");
            }
        }
		return aux.trim();
	  } catch ( Exception ex ) { return null; }
    }

    public static void setStandardSpelling ( AdornedWord adornedWord  , SpellingStandardizer standardizer , PartOfSpeechTags partOfSpeechTags ) {
        String spelling         = adornedWord.getSpelling();
        String standardSpelling = spelling;
        String partOfSpeech     = adornedWord.getPartsOfSpeech();
        if ( partOfSpeechTags.isProperNounTag( partOfSpeech ) ) { }
        else if ( partOfSpeechTags.isNounTag( partOfSpeech )  && CharUtils.hasInternalCaps( spelling ) ) { }
        else if ( partOfSpeechTags.isForeignWordTag( partOfSpeech ) ) { }
        else if ( partOfSpeechTags.isNumberTag( partOfSpeech ) ) { }
        else {
            standardSpelling = standardizer.standardizeSpelling ( adornedWord.getSpelling() , partOfSpeechTags.getMajorWordClass( adornedWord.getPartsOfSpeech() ) );
            if ( standardSpelling.equalsIgnoreCase( spelling ) ) { standardSpelling    = spelling; }
        }
        adornedWord.setStandardSpelling( standardSpelling );
    }

    public static void setLemma ( AdornedWord adornedWord  , Lexicon lexicon , Lemmatizer lemmatizer , PartOfSpeechTags partOfSpeechTags , WordTokenizer spellingTokenizer ) {
        String spelling     = adornedWord.getSpelling();
        String partOfSpeech = adornedWord.getPartsOfSpeech();
        String lemmata      = spelling;
        String lemmaClass   = partOfSpeechTags.getLemmaWordClass( partOfSpeech );
        if  (   lemmatizer.cantLemmatize( spelling ) || lemmaClass.equals( "none" ) ) { } else {
            lemmata = lemmatizer.lemmatize( spelling , "compound" );
            if ( lemmata.equals( spelling ) ) {
                List<String> wordList = spellingTokenizer.extractWords( spelling );
                if  (   !partOfSpeechTags.isCompoundTag( partOfSpeech ) || ( wordList.size() == 1 ) ) {
                    if ( lemmaClass.length() == 0 ) lemmata = lemmatizer.lemmatize( spelling );
                    else lemmata = lemmatizer.lemmatize( spelling , lemmaClass );
				} else {
                    lemmata             = "";
                    String lemmaPiece   = "";
                    String[] posTags    = partOfSpeechTags.splitTag( partOfSpeech );
                    if ( posTags.length == wordList.size() ) {
                        for ( int i = 0 ; i < wordList.size() ; i++ ) {
                            String wordPiece    = (String)wordList.get( i );
                            if ( i > 0 ) lemmata = lemmata + lemmaSeparator;
                            lemmaClass  = partOfSpeechTags.getLemmaWordClass ( posTags[ i ] );
                            lemmaPiece  = lemmatizer.lemmatize ( wordPiece , lemmaClass );
                            lemmata = lemmata + lemmaPiece;
                        }
                    }
                }
            }
        }
        adornedWord.setLemmata( lemmata );
    }

}