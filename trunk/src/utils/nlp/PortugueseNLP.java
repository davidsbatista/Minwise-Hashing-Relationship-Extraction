package utils.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PortugueseNLP {

	/* 
	 * Além dos testes com aquele dataset da Wikipédia, como te disse hoje,
	 * também pode fazer sentido tentarmos usar o dataset do HAREM para testar o
	 * modelo.
	 * 
	 * http://www.linguateca.pt/aval_conjunta/HAREM/GlossarioReRelEM.html
	 * http://comum.rcaap.pt/bitstream/123456789/242/1/Capitulo_04-MotaSantos2008.pdf
	 */

	public static String generateNGrams(String source, String prefix, int betweenLenght, int casing, int window) {
		
		String[] sourcePOS = PortuguesePOSTagger.posTags(source);
		String[] sourceTokens = PortuguesePOSTagger.tokenize(source);

		/* - tri-gramas de caracteres
		 * - tri-gramas de caracteres + verbos 
		 * - tri-gramas de caracteres + verbos + preposições 
		 * - tri-gramas de caracteres + verbos + preposições + padrão_reverb
		 */
		
		Set<String> set = new HashSet<String>();
		
		if ((sourcePOS!=null && sourceTokens!=null) && sourceTokens.length == sourcePOS.length) {
			for ( int i = 0 ; i < sourceTokens.length; i++ ) {				
				if ( prefix.startsWith("BEF") && sourceTokens.length - i > betweenLenght + window ) continue;
				if ( prefix.startsWith("AFT") && i > betweenLenght + window ) continue;
				
				if ( sourcePOS[i].startsWith("verb") || sourcePOS[i].startsWith("pp") ) {
				  set.add(sourceTokens[i].toLowerCase() + "_" + ( i < sourceTokens.length - 1 ? sourceTokens[i+1].toLowerCase() + "_" : "" ) +  prefix);
				  set.add(sourceTokens[i].toLowerCase() + "_" + prefix);
				  if ( sourcePOS[i].startsWith("pp")  ) set.add(sourceTokens[i].toLowerCase() + "_PP_" + prefix);
				  
				  //ReVerb inspired: um verbo, seguido de vários nomes, adjectivos ou adverbios, terminando numa preposição.
				  if (i < sourceTokens.length - 2) {
		  			String pattern = sourceTokens[i].toLowerCase();
		  			int j = i+1;				
					while ( ((j < sourceTokens.length - 2)) && ((sourcePOS[j].startsWith("adverb") || sourcePOS[j].startsWith("adjective")) || sourcePOS[j].startsWith("noun"))) {	  				
						pattern += "_" + sourceTokens[j].toLowerCase();
						j++;				
					}
					if (sourcePOS[j].startsWith("preposition")) {
							pattern += "_" + sourceTokens[j].toLowerCase();
							set.add(pattern + "_RVB_" + prefix);
					}
		  		  }		  	
				} else if (sourcePOS[i].startsWith("preposition")) set.add(sourceTokens[i].toLowerCase() + "_PREP_" + prefix);
			}
			// character quadgrams
			for (int j = 0; j < source.length() + 3; j++) {
					String tok = "";
					for (int i = -3; i <= 0; i++) {
						char ch = (j + i) < 0 || (j + i) >= source.length() ? '_' : source.charAt(j + i);
						tok += ch == ' ' ? '_' : ch;
					}
					set.add(tok + "_" + prefix + "_");
				}
		} 
		String result = "";
		for (String tok : set)
			result += " " + tok;
		return result.trim();
	}
}
