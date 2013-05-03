package datasets.Publico;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class ReadXML{
	
	static LinkedList<Paragraph> paragraphs = new LinkedList<Paragraph>();
	
	public static LinkedList<Paragraph> parse(String xml) {
			
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
				StringBuffer paragraph = new StringBuffer();
				String date = "";
												
				public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
					int index = attributes.getIndex("category");
					if ((index!=-1) && (attributes.getValue(index).equals("politica"))) politica = true;
					if (politica && qName.equals("date")) extractDate = true;
					if ((politica) && ( qName.equals("title_rembrandted") || qName.equals("subtitle_rembrandted") || qName.equals("newstext_rembrandted"))) extract = true;					
					if ((extract) && qName.equals("ALT")) ALT = true;
					if ((ALT) && (!firstSUBALT) && (qName.equals("SUBALT"))) firstSUBALT = true;										
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
					
					if (politica && qName.equals("date")) extractDate = false;
					
					if ((ALT) && (firstSUBALT) && (qName.equals("SUBALT"))) extract = false;;
					
					
					if ((extract) && (qName.equals("title_rembrandted") || qName.equals("subtitle_rembrandted") || qName.equals("newstext_rembrandted"))) {												
						
						if (qName.equals("title_rembrandted")) paragraph.append(".");
						Paragraph p = new Paragraph(paragraph.toString(),date);
						paragraphs.add(p);						
						paragraph = new StringBuffer();
						date = "";
					}
					
					if ((ALT) && (!extract) && qName.equals("ALT")) extract = true;
					
				}
				
				public void characters(char ch[], int start, int length) throws SAXException {
					if (politica && extract || extractEntities) {
						String text = new String(ch, start, length);
						paragraph.append(text);
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
		
		return paragraphs;
   }
}