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
	
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {
		PrintWriter out = new PrintWriter(new FileWriter("sample.txt"));
		tag("A polícia de Manchester quer que os crimes cometidos contra elementos de subculturas alternativas, como góticos, punks ou emos, sejam registados como crimes de ódio, da mesma forma que são consideradas as agressões de origem racista, religiosa ou homofóbica.",out);
	}
	
	public static void tag(String doc, PrintWriter out) throws InvalidFormatException, FileNotFoundException, IOException {
		
		POSModel model = new POSModelLoader().load(new File("resources/pt.postagger.model"));
		TokenizerModel tModel = new TokenizerModel(new FileInputStream("resources/pt.tokenizer.model")); 
		SentenceModel sModel = new SentenceModel(new FileInputStream("resources/pt.sentdetect.model"));
		POSTaggerME tagger = new POSTaggerME(model); 
		TokenizerME token = new TokenizerME(tModel);
		SentenceDetector sent = new SentenceDetectorME(sModel);
	
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
