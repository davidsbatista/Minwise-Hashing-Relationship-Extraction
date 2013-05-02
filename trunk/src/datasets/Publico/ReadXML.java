package datasets.Publico;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
public class ReadXML{
	
	public static void main(String[] args) {
			
		try {
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
 
			DefaultHandler handler = new DefaultHandler() {
 
				boolean politica = false;
				boolean extract = false;
				boolean extractEntities = false;
				
				public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
					int index = attributes.getIndex("category");
					if ((index!=-1) && (attributes.getValue(index).equals("politica"))) politica = true;
					if ((politica) && ( qName.equals("title_rembrandted") || qName.equals("subtitle_rembrandted") || qName.equals("newstext_rembrandted"))) {
						extract = true;
					}
					if ((extract) && (qName.equals("PESSOA") || qName.equals("ORGANIZACAO") || qName.equals("LOCAL"))) extractEntities = true;
					if (extractEntities) { System.out.print( "<" + qName + ">");}
				}
 
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if ( qName.equals("article") && politica) {
						extract=false;
						politica=false;
					}
					if (extractEntities) { System.out.print( "</" + qName + ">");}
					if ((extract) && (qName.equals("PESSOA") || qName.equals("ORGANIZACAO") || qName.equals("LOCAL"))) extractEntities = false;
					
				}
				
				public void characters(char ch[], int start, int length) throws SAXException {
					if (politica && extract || extractEntities) {
						String text = new String(ch, start, length);
						System.out.print(text);
					}
				}
			};
			
			saxParser.parse(args[0], handler);
 
		} catch (Exception e) {
       e.printStackTrace();
     }
   }
}