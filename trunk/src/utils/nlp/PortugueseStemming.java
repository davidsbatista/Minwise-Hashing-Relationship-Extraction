package utils.nlp;

import java.util.ArrayList;
import java.util.List;
import ptstemmer.Stemmer;
import ptstemmer.Stemmer.StemmerType;
import ptstemmer.exceptions.PTStemmerException;

public class PortugueseStemming {
	
	static Stemmer stemmer = null;
	
	public static void main(String[] args) throws PTStemmerException {
		
		Stemmer stemmer1 = Stemmer.StemmerFactory(StemmerType.PORTER);
		Stemmer stemmer2 = Stemmer.StemmerFactory(StemmerType.ORENGO);
		Stemmer stemmer3 = Stemmer.StemmerFactory(StemmerType.SAVOY);
		
		String sentence = "O chumbo pelo Tribunal Constitucional (TC) de várias normas do Orçamento de Estado (OE) de 2013 levou a zona euro a suspender todas as decisões relativas ao programa de assistência financeira a Portugal, incluindo o prolongamento dos prazos de reembolso dos empréstimos europeus e o desembolso da próxima parcela da ajuda.";
		
		String[] stems = stemmer1.getPhraseStems(sentence);
		for (int i = 0; i < stems.length; i++) {
			System.out.print(stems[i] + ' ');			 
		}
		System.out.println();
		
		stems = stemmer2.getPhraseStems(sentence);
		for (int i = 0; i < stems.length; i++) {
			System.out.print(stems[i] + ' ');			 
		}
		System.out.println();
		
		stems = stemmer3.getPhraseStems(sentence);
		for (int i = 0; i < stems.length; i++) {
			System.out.print(stems[i] + ' ');			 
		}
		
		//System.out.println(PTStemmerUtilities.removeDiacritics(stem));
		
	}
	
	public static void initialize(StemmerType type) throws PTStemmerException {		
		stemmer = Stemmer.StemmerFactory(type);
		//stemmer.ignore(PTStemmerUtilities.fileToSet("data/stopwords.txt"));
		//stemmer.ignore(PTStemmerUtilities.fileToSet("data/namedEntities.txt"));
	}	

	public static String[] steem(String[] tokens) throws PTStemmerException {
		List<String> stems = new ArrayList<String>();
		for (String token : tokens) {
			stems.add(stemmer.getWordStem(token));
		}
		String[] array = stems.toArray(new String[stems.size()]);		
		return array;		
	}
	
	public String[] steem(String sentence) throws PTStemmerException {			
		String[] stems = stemmer.getPhraseStems(sentence);				
		return stems;		
	}
}
