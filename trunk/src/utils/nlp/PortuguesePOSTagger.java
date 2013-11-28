package utils.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class PortuguesePOSTagger {
	
	static POSModel model = null;
	static TokenizerModel tModel = null; 
	static SentenceModel sModel = null;
	static POSTaggerME tagger = null; 
	static TokenizerME token = null;
	static SentenceDetector sent = null;
	static String RESOURCES = "/home/dsbatista/relations-minhash/resources/";
	
	public static void initialize() throws InvalidFormatException, FileNotFoundException, IOException {
		
		/* normal model */
		/*
		model = new POSModelLoader().load(new File(RESOURCES + "pt.postagger.model"));
        tModel = new TokenizerModel(new FileInputStream(RESOURCES + "pt.tokenizer.model")); 
        sModel = new SentenceModel(new FileInputStream(RESOURCES + "pt.sentdetect.model"));
        */
		
        /* with VPP tag */
        model = new POSModelLoader().load(new File(RESOURCES + "pt.postaggerVerbPP.model"));
        tModel = new TokenizerModel(new FileInputStream(RESOURCES + "pt.tokenizerVerbPP.model")); 
        sModel = new SentenceModel(new FileInputStream(RESOURCES + "pt.sentDetectVerbPP.model"));
                
        tagger = new POSTaggerME(model); 
        token = new TokenizerME(tModel);
        sent = new SentenceDetectorME(sModel);
	}
	
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {
		initialize();		
		/*
		String[] tags = posTags("A decisão foi anunciada por Jeroen Dijsselbloem, ministro da finanças da Holanda que preside às reuniões do Eurogrupo e refere-se aos 26 mil milhões de euros dos empréstimos do Fundo Europeu de Estabilidade Financeira (FEEF) garantidos pelos países do euro");
		System.out.println("A decisão foi anunciada por Jeroen Dijsselbloem, ministro da finanças da Holanda que preside às reuniões do Eurogrupo e refere-se aos 26 mil milhões de euros dos empréstimos do Fundo Europeu de Estabilidade Financeira (FEEF) garantidos pelos países do euro");
		for (int i = 0; i < tags.length; i++) {
			System.out.print(tags[i] + '\t');
		}
		*/
		String s = "Vídeo: Números do PIB \"são muito negativos\"";
		tag(s);
	}
	
	public static List<String> tag(String text) {
		
		List<String> tagged_sentences = new LinkedList<String>();		
		
		//split the text into sentences
		for (String s : sent.sentDetect(text)) {
			
			//tokenize the sentence and attribute a pos-tag 
			String tokens[] = null;
			tokens = token.tokenize(s);
			String[] postTags = tagger.tag(tokens);
			
			//build string: token1_postag1 token2_postag2 .... tokenN_postagN
			POSSample tokensTagged = new POSSample(tokens, postTags);			
			String sentenceTagged = tokensTagged.toString();
			String t = sentenceTagged.replaceAll("\\_","\\/");			
			tagged_sentences.add(t);
		}		
		return tagged_sentences;
	}
	
	
	
	public static String[] tokenize(String text){
		String whitespaceTokenizerLine[] = token.tokenize(text);
		return whitespaceTokenizerLine;		
	}
	
	public static String[] posTags(String text) {
		String tags[] = null;
		for (String s: sent.sentDetect(text)) {			
			String whitespaceTokenizerLine[] = token.tokenize(s);			
			String[] mTags = tagger.tag(whitespaceTokenizerLine);
			POSSample sample = new POSSample(whitespaceTokenizerLine, mTags);
			String sentence = sample.toString();
			String pairs[] = sentence.split(" ");
			tags = new String[pairs.length];
			for ( int i = 0; i < pairs.length; i++) {				
				tags[i] = pairs[i].substring(pairs[i].indexOf("_")+1);				 
			}
		}		
		return tags;
	}	
}
