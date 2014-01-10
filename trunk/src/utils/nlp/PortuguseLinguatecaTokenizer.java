package utils.nlp;

public class PortuguseLinguatecaTokenizer extends com.aliasi.tokenizer.RegExTokenizerFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String regex =   "(\\d+((/\\d+)+))" + "|" + 				/** dates or similar, e.g. 12/21/1 */
	                                 "(\\d+\\:\\d+(\\:\\d+)?)" + "|" + 		/** the time, e.g. 12:12:2 */
	                                 "(\\d+(([.]?[oaºª°])+))" + "|" + 		/** ordinal numbers, e.g. 12.o */
	                                 "(\\d+(([.,]\\d+)*\\%?)?)" + "|" + 	/** numbers */
	                                 "(\\p{L}+(-\\p{L}+)+)" + "|" + 		/** tokens like dá-lo-à */
	                                 "(\\p{L}+\\'\\p{L}+)" + "|" + 			/** word connected by ' */
	                                 "([\\p{L}_.-]+ ?\\@ ?[\\p{L}_.-]+\\p{L})" + "|" + 		/** emails */
	                                 "(\\p{L}+\\.?[ºª°]\\.?)" + "|" + 		/** ordinals */                                 
	                                 "((((http(s?))|(ftp)|(gopher))://|www)[\\p{L}_./~:-]+\\p{L})" + "|" + 		/** urls */
	                                 "(\\p{L}+\\.((com)|(org)|(net)|(pt))?)" + "|" + 					   		/** simplified urls */
	                                 "(\\\\\\\\unicode\\{\\d+\\})" + "|" + 										/** unicode */
	                                 "((\\p{L}+\\.(?:(exe)|(htm(l?))|(zip)|(jpg)|(gif)|(wav)|(mp3)|(png)|((t?)gz)|(pl)|(xml))))" + "|" + /** filenames */
	                                 "(\\p{L}+)" + "|" + 					/**  word characters */
	                                 "([\\.,;:\\?!])" + "|" + 				/** punctation */
	                                 "(\\bn\\.o)"; 							/** number */


	public PortuguseLinguatecaTokenizer ( ) { super(regex); }

	public com.aliasi.tokenizer.Tokenizer tokenizer(String str) { return tokenizer(str.toCharArray(),0,str.length()); } 

	public String[] tokenize(char[] cs, int start, int length) { 
	   com.aliasi.tokenizer.Tokenizer tokenizer = tokenizer(cs,start,length);
	   return tokenizer.tokenize();
	}

	public String[] tokenize(String str) { 
	  com.aliasi.tokenizer.Tokenizer tokenizer = tokenizer(str.toCharArray(),0,str.length());
	  return tokenizer.tokenize();
	} 
	 
	public static void main ( String args[] ) {
		System.out.println("Testing tokenization of the input parameters...");
		PortuguseLinguatecaTokenizer tf = new PortuguseLinguatecaTokenizer();
		for ( String str : args ) {
			System.out.println();
			for ( String token : tf.tokenize(str) ) System.out.println(token);
			}		
	}
}