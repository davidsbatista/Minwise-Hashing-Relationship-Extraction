package utils.nlp;

import ptstemmer.Stemmer;
import ptstemmer.Stemmer.StemmerType;
import ptstemmer.exceptions.PTStemmerException;
import ptstemmer.implementations.OrengoStemmer;
import ptstemmer.support.PTStemmerUtilities;

public class Stemming {
	
	public static void main(String[] args) throws PTStemmerException {
		
		Stemmer stemmer1 = Stemmer.StemmerFactory(StemmerType.PORTER);
		Stemmer stemmer2 = Stemmer.StemmerFactory(StemmerType.ORENGO);
		Stemmer stemmer3 = Stemmer.StemmerFactory(StemmerType.SAVOY);
		//stemmer.ignore(PTStemmerUtilities.fileToSet("data/stopwords.txt"));
		//stemmer.ignore(PTStemmerUtilities.fileToSet("data/namedEntities.txt"));         
		
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

}
