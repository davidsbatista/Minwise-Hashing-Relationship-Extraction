package utils.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
		
		model = new POSModelLoader().load(new File(RESOURCES + "pt.postagger.model"));
        tModel = new TokenizerModel(new FileInputStream(RESOURCES + "pt.tokenizer.model")); 
        sModel = new SentenceModel(new FileInputStream(RESOURCES + "pt.sentdetect.model"));
        tagger = new POSTaggerME(model); 
        token = new TokenizerME(tModel);
        sent = new SentenceDetectorME(sModel);
	}
	
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {
		
		model = new POSModelLoader().load(new File("resources/pt.postagger.model"));
        tModel = new TokenizerModel(new FileInputStream("resources/pt.tokenizer.model")); 
        sModel = new SentenceModel(new FileInputStream("resources/pt.sentdetect.model"));
        tagger = new POSTaggerME(model); 
        token = new TokenizerME(tModel);
        sent = new SentenceDetectorME(sModel);
		
        String text = "Margaret Thatcher não gostava de consensos. Porque haveria então de se preocupar com o que diziam dela? E com o que, a favor dela ou contra ela, os artistas britânicos produziram entre o dia em que chegou ao poder, 4 de Maio de 1979, até ao dia em que o abandonou, em Novembro de 1990?";
		
        String[] sentences = sent.sentDetect(text);
        for (int i = 0; i < sentences.length; i++) {
        	posTags(sentences[i]);
		}        
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
	
	public static void tag(String doc, PrintWriter out) throws InvalidFormatException, FileNotFoundException, IOException {
				
		for ( String s : sent.sentDetect(doc) ) {
			String whitespaceTokenizerLine[] = token.tokenize(s);
			String[] mTags = tagger.tag(whitespaceTokenizerLine);
			POSSample sample = new POSSample(whitespaceTokenizerLine, mTags);
			String sentence = sample.toString();
			String pairs[] = sentence.split(" ");
			String words[] = new String[pairs.length];
			String tags[] = new String[pairs.length];
			for ( int i = 0; i < pairs.length; i++) {
				words[i] = pairs[i].substring(0,pairs[i].indexOf("_"));
				tags[i] = pairs[i].substring(pairs[i].indexOf("_")+1);
				out.write( "  <token number='" + (i+1) +"' word='" + words[i] + "' pos='" + tags[i] + "' " + "/>\n" );
			}
			out.close();
		}

	}
}
