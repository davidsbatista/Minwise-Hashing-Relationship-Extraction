package nlputils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PortugueseNLP {

	/*
	 * - unigramas de palavras (i.e., bag-of-words) 
	 * - bi-gramas de palavras 
	 * - bi-gramas de stems de palavras 
	 * - tri-gramas de caracteres 
	 * - tri-gramas de caracteres + verbos 
	 * - tri-gramas de caracteres + verbos + preposições 
	 * - tri-gramas de caracteres + verbos + preposições + padrões_reverb (os 2 padrões)
	 * 
	 * Além dos testes com aquele dataset da Wikipédia, como te disse hoje,
	 * também pode fazer sentido tentarmos usar o dataset do HAREM para testar o
	 * modelo.
	 * 
	 * http://www.linguateca.pt/aval_conjunta/HAREM/GlossarioReRelEM.html
	 * http://
	 * comum.rcaap.pt/bitstream/123456789/242/1/Capitulo_04-MotaSantos2008.pdf
	 */

	public static void processExample(String before, String after,String between, String type, PrintWriter out) {
		out.print(type);
		
		if (before.lastIndexOf(",") != -1 && before.lastIndexOf(",") < before.lastIndexOf(between))
			before = before.substring(before.lastIndexOf(",") + 1);
		
		if (after.indexOf(",") != -1 && after.indexOf(",") > between.length())
			after = after.substring(0, after.lastIndexOf(","));
		
		int betweenLength = EnglishNLP.adornText(between, 0).split(" +").length;
		int beforeLength = EnglishNLP.adornText(before, 0).split(" +").length;
		int afterLength = EnglishNLP.adornText(after, 0).split(" +").length;
		
		if (beforeLength >= Math.max(betweenLength, afterLength)) out.print(" " + "LARGER_BEF");
		if (afterLength >= Math.max(betweenLength, beforeLength)) out.print(" " + "LARGER_AFT");
		if (betweenLength >= Math.max(afterLength, beforeLength)) out.print(" " + "LARGER_BET");
		if (beforeLength == 0) out.print(" " + "EMPTY_BEF");
		if (afterLength == 0) out.print(" " + "EMPTY_AFT");
		if (betweenLength == 0) out.print(" " + "EMPTY_BET");
		ArrayList<String> someCollection = new ArrayList<String>();
		for (String aux : new String[] { "BEF\t" + before, "BET\t" + between, "AFT\t" + after })
			someCollection.add(aux);
		for (String obj : someCollection) {
			String prefix = obj.substring(0, obj.indexOf("\t"));
			String str = obj.substring(obj.indexOf("\t") + 1);
			out.print(" " + generateNGrams(str, prefix, betweenLength, 1, 3));
		}
		out.println();
	}

	public static String generateNGrams(String source, String prefix, int n, int weight, int casing) {

		if (casing == 1) source = source.toLowerCase();
		String aux[] = source.split(" +");
		
		for (int i = 0; i < aux.length; i++) {
			if (aux[i].matches("^[0-9][0-9]0-9][0-9]+$"))
				aux[i] = "NUMBER"; // NUMBER
			if (aux[i].matches("^[0-9][0-9]0-9][0-9]$"))
				aux[i] = "4DIG"; // 4DIG
			if (aux[i].matches("^[0-9][0-9]$"))
				aux[i] = "2DIG"; // 2DIG
		}
		
		Set<String> set = new HashSet<String>();
		
		if (casing == 2)
			for (String s : generateNGrams(source, prefix, n, weight, 1).split(" "))
				set.add(s);
		// word unigrams
		if (n == 1)
			for (String tok : aux)
				for (int i = 1; i <= weight; i++)
					set.add(tok + "_" + prefix + "_" + i);
		// word bigrams
		if (n == 2)
			for (int j = 0; j <= aux.length; j++) {
				String tok1 = j - 1 >= 0 ? aux[j - 1] : "_";
				String tok2 = j < aux.length ? aux[j] : "_";
				for (int i = 1; i <= weight; i++)
					set.add(tok1 + "_" + tok2 + "_" + prefix + "_" + i);
			}
		// word trigrams
		if (n == 3)
			for (int j = 0; j <= aux.length + 1; j++) {
				String tok1 = j - 2 >= 0 ? aux[j - 2] : "_";
				String tok2 = j - 1 >= 0 && j - 1 < aux.length ? aux[j - 1]: "_";
				String tok3 = j < aux.length ? aux[j] : "_";
				for (int i = 1; i <= weight; i++)
					set.add(tok1 + "_" + tok2 + "_" + tok3 + "_" + prefix + "_" + i);
			}
		
		// word bigrams with gaps
		if (n == 4)
			for (int j = 2; j < aux.length; j++) {
				String tok1 = aux[j - 2];
				String tok2 = "GAP";
				String tok3 = aux[j];
				for (int i = 1; i <= weight; i++)
					set.add(tok1 + "_" + tok2 + "_" + tok3 + "_" + prefix + "_" + i);
			}
		
		// character trigrams
		if (n == 5)
			for (int j = 0; j < source.length() + 3; j++) {
				String tok = "";
				for (int i = -3; i <= 0; i++) {
					char ch = (j + i) < 0 || (j + i) >= source.length() ? '_' : source.charAt(j + i);
					tok += ch == ' ' ? '_' : ch;
				}
				for (int i = 1; i <= weight; i++)
					set.add(tok + "_" + prefix + "_" + i);
			}
		// word fourgrams
		if (n == 6)
			for (int j = 0; j <= aux.length + 2; j++) {
				String tok1 = j - 3 >= 0 ? aux[j - 3] : "_";
				String tok2 = j - 2 >= 0 && j - 2 < aux.length ? aux[j - 2] : "_";
				String tok3 = j - 1 >= 0 && j - 1 < aux.length ? aux[j - 1] : "_";
				String tok4 = j < aux.length ? aux[j] : "_";
				for (int i = 1; i <= weight; i++)
					set.add(tok1 + "_" + tok2 + "_" + tok3 + "_" + tok4 + "_"
							+ prefix + "_" + i);
			}
		// word unigrams with POS
		// if ( n == 7 ) for ( int j = 0; j < aux.length; j++ ) for ( int i = 1;
		// i <= weight; i++) set.add(aux[j] + "_" + auxPOS[j] + "_" + prefix +
		// "_" + i);
		// word bigrams with POS
		/*
		 * if ( n == 8 ) for ( int j = 0; j <= aux.length; j++ ) { String tok1 =
		 * j-1 >=0 ? aux[j-1] + "_" + auxPOS[j-1] : "_"; String tok2 =
		 * j<aux.length ? aux[j] + "_" + auxPOS[j] : "_"; for ( int i = 1; i <=
		 * weight; i++) set.add(tok1 + "_" + tok2 + "_" + prefix + "_" + i); }
		 */
		String result = "";
		for (String tok : set)
			result += " " + tok;
		return result.trim();
	}
}
