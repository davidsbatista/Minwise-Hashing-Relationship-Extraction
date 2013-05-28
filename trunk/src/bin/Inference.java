package bin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

public class Inference {
	
	static Model model;
		
	public static void main(String[] args) throws IOException {
		
		/* load a stored Model */
		String directory = "/home/dsbatista/tdb/" ;
		Location loc = new Location(directory);		
		model = TDBFactory.createModel(loc);
		
		String[] relations = {"deathOrBurialPlace","keyPerson","locatedInArea","origin","parent","successor","partner","influencedBy","partOf"};		
		/* write original relations to file */
		for (String relation : relations) listAll(relation, model, false);
        
        /* inference new relations */
		Model dedu = inference(model);
		
		/* write inferred relations to file */
		for (String relation : relations) listAll(relation, dedu, true);
		
		model.close();
	}
	
	public static void listAll(String relation, Model model, boolean inferred) throws IOException{
		StmtIterator stmItr = model.listStatements();
		String filename;		
		if (!inferred) filename = relation+"_original.n3";
		else filename = relation+"_inferred.n3";
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(filename)));
		
		while (stmItr.hasNext()) {			
			Statement stmt      = stmItr.nextStatement();  	// get next statement
		    Resource  subject   = stmt.getSubject();     	// get the subject
		    Property  predicate = stmt.getPredicate();   	// get the predicate
		    RDFNode   object    = stmt.getObject();      	// get the object
		    if (predicate.toString().equals(relation)) {
		    	out.write(subject.toString());
		    	out.write(" " + predicate.toString() + " ");
			    if (object instanceof Resource) {
			       out.write(object.toString());
			    } else {
			        // object is a literal
			    	out.write(" \"" + object.toString() + "\"");
			    }
			out.write(" .\n"); 
			}
		}
		out.close();
	}
	
	public static Model inference(Model model) {
		
		/* load rules */
		String rulesFile = "rules.txt";
        List<Rule> rules = Rule.rulesFromURL(rulesFile);
        
        /* load reasoner */
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        
        // load an Inference Model, from reasoner and ontology        
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        
        // make the inference!
        Model dedu = inf.getDeductionsModel();
        
        return dedu;
	}
	
}
