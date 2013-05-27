package bin;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

public class Inference {
	
	static Model model;
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		String directory = "/home/dsbatista/tdb/" ;
		Location loc = new Location(directory);		
		model = TDBFactory.createModel(loc);
		
		ResIterator rsiter = model.listSubjects();
		
		while (rsiter.hasNext()) {
			Resource subject = rsiter.next();
			System.out.println(subject.toString());
		}
		
		StmtIterator stmItr = model.listStatements();
				
		while (stmItr.hasNext()) {			
			Statement stmt      = stmItr.nextStatement();  // get next statement
		    Resource  subject   = stmt.getSubject();     // get the subject
		    Property  predicate = stmt.getPredicate();   // get the predicate
		    RDFNode   object    = stmt.getObject();      // get the object
		    System.out.print(subject.toString());
		    System.out.print(" " + predicate.toString() + " ");
		    if (object instanceof Resource) {
		       System.out.print(object.toString());
		    } else {
		        // object is a literal
		        System.out.print(" \"" + object.toString() + "\"");
		    }
		System.out.println(" ."); 
		}		
		model.close() ;		
	}
	
	public static void createRules() {
		
		String rulesFile = "rules.txt";
		
		// load reasoner
        List<Rule> rules = Rule.rulesFromURL(rulesFile);
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        // load an Inference Model, from reasoner, ontology and instance file
        //InfModel inf = ModelFactory.createInfModel(reasoner, ontology, source);
        //inf.setNsPrefixes( ontology );
        // make the inference!
        //Model dedu = inf.getDeductionsModel();
        
        
        //String s = RDFTool.modelToString(dedu, "N3");
        // dump deductions 
        //System.out.println(s);
        
        // dump all
        System.out.println("=============all================");
        //inf.write(System.out, "N3");
	}
	
}
