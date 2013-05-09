package datasets.Publico;

import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class ReadXML{
	
	static LinkedList<Article> articles = new LinkedList<Article>();
	
	public static LinkedList<Article> parse(String xml) {
			
		try {
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
 
			DefaultHandler handler = new DefaultHandler() {
 
				boolean politica = false;
				boolean extract = false;				
				boolean extractEntities = false;				
				boolean ALT = false;
				boolean firstSUBALT = false;
				boolean extractDate = false;
				StringBuffer txt = new StringBuffer();
				String date = "";
				String lead = "";
				String title = "";
				String text = "";
												
				public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
					int index = attributes.getIndex("category");
					if ((index!=-1) && (attributes.getValue(index).equals("politica"))) politica = true;
					if (politica && qName.equals("date")) extractDate = true;
					if ((politica) && ( qName.equals("title_rembrandted") || qName.equals("subtitle_rembrandted") || qName.equals("newstext_rembrandted"))) extract = true;					
					if ((extract) && qName.equals("ALT")) ALT = true;
					if ((ALT) && (!firstSUBALT) && (qName.equals("SUBALT"))) firstSUBALT = true;										
					if ((extract) && (qName.equals("PESSOA") || qName.equals("ORGANIZACAO") || qName.equals("LOCAL"))) extractEntities = true; 
					if (extractEntities) { txt.append( "<" + qName + ">");}
				}
 
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if ( qName.equals("article") && politica) {
						extract=false;
						politica=false;
					}					
					if (extractEntities) { txt.append( "</" + qName + ">");}
					if ((extract) && (qName.equals("PESSOA") || qName.equals("ORGANIZACAO") || qName.equals("LOCAL"))) extractEntities = false;										
					if (politica && qName.equals("date")) extractDate = false;
					if ((ALT) && (firstSUBALT) && (qName.equals("SUBALT"))) extract = false;;
					if (extract) {
						if (qName.equals("title_rembrandted")) {
							lead = txt.toString();
							txt = new StringBuffer();
						}
						else if (qName.equals("subtitle_rembrandted"))  {
							title = txt.toString();
							txt = new StringBuffer();
						}
						else if (qName.equals("newstext_rembrandted")) {
							text = txt.toString();
							Article a = new Article(lead,title,text,date);
							articles.add(a);
							txt = new StringBuffer();
							date = "";
							title = "";
							lead = "";
							text = "";						
						}
					}					
					if ((ALT) && (!extract) && qName.equals("ALT")) extract = true;					
				}
				
				public void characters(char ch[], int start, int length) throws SAXException {
					if (politica && extract || extractEntities) {
						String text = new String(ch, start, length);
						txt.append(text);
					}
					
					if (extractDate) {
						String text = new String(ch, start, length);
						date = text;
					}
				
				}
			};
			
			saxParser.parse(xml, handler);
 
		} catch (Exception e) {
			e.printStackTrace();
		  }
		
		return articles;
   }
}