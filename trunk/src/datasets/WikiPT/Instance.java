package datasets.WikiPT;

public class Instance {
	
	public String rel_type;
	public String before;
	public String after;
	public String between;
	
	public Instance(String rel_type, String before, String after, String between) {
		super();
		this.rel_type = rel_type;
		this.before = before;
		this.after = after;
		this.between = between;
	}
	
	
	
	
}
