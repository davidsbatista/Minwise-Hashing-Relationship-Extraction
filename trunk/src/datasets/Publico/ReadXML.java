package datasets.Publico;

import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class ReadXML{
	
	static Set<String> paragraphs = new HashSet<String>();
	
	public static Set<String> parse(String xml) {
			
		try {
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
 
			DefaultHandler handler = new DefaultHandler() {
 
				boolean politica = false;
				boolean extract = false;				
				boolean extractEntities = false;
				StringBuffer paragraph = new StringBuffer();
				
				public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
					int index = attributes.getIndex("category");
					if ((index!=-1) && (attributes.getValue(index).equals("politica"))) politica = true;
					if ((politica) && ( qName.equals("title_rembrandted") || qName.equals("subtitle_rembrandted") || qName.equals("newstext_rembrandted"))) {
						extract = true;
					}
					
					//if ((politica) && qName.equals("SUBALT")) insideSUBALT = true;
					if ((extract) && (qName.equals("PESSOA") || qName.equals("ORGANIZACAO") || qName.equals("LOCAL"))) extractEntities = true; 
					if (extractEntities) { paragraph.append( "<" + qName + ">");}
				}
 
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if ( qName.equals("article") && politica) {
						extract=false;
						politica=false;
					}
					if (extractEntities) { paragraph.append( "</" + qName + ">");}
					if ((extract) && (qName.equals("PESSOA") || qName.equals("ORGANIZACAO") || qName.equals("LOCAL"))) extractEntities = false;
					
					if ((extract) && (qName.equals("title_rembrandted") || qName.equals("subtitle_rembrandted") || qName.equals("newstext_rembrandted"))) {
						System.out.println(new String(paragraph));
						//sentences.add(new String(paragraph));						
						paragraph = new StringBuffer();
					}					
				}
				
				public void characters(char ch[], int start, int length) throws SAXException {
					if (politica && extract || extractEntities) {
						String text = new String(ch, start, length);
						paragraph.append(text);
						
					}
				}
			};
			
			saxParser.parse(xml, handler);
 
		} catch (Exception e) {
			e.printStackTrace();
		  }
		
		return paragraphs;
   }
}