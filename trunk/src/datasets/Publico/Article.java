package datasets.Publico;

public class Article {
	
	public String date;
	public String lead;
	public String title;
	public String text;
	
	public Article(String l, String tit, String txt, String d) {
		lead = l;
		title = tit;
		text = txt;
		date = d;
	}
	
	public String getText() {
		return (lead.trim()+".").trim() + " " + title.trim() + " " + text.trim();
	}
}
