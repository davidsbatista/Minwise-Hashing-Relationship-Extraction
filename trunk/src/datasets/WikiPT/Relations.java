package datasets.WikiPT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Relations {
	
	public static Map<String, String> aggregatedRelations = null; 		
	public static String[] ignore = {"picture","maintainedBy","politicGovernmentDepartment","regionServed"}; 	
	
	public static String[] changeDirection = {"predecessor","doctoralAdvisor","influenced","child","foundedBy"};
	
	public static void initialize() {
		aggregatedRelations = new HashMap<String, String>();				
		String locatedInArea[] = {"locatedInArea","archipelago","locatedInArea","locatedInArea","locatedInArea","location", "locationCity",
								  "locationCountry","capitalCountry","municipality","subregion","federalState","capital","district","region",
								  "city","country","province","state","county", "locatedInArea","map","campus","garrison"};				
		String partOf[] = {"type","parentOrganisation","department","distributingCompany","broadcastNetwork","sportCountry","affiliation",
						   "university","youthClub","currentMember","party","pastMember","team","associatedMusicalArtist","member"};				
		String origin[] = {"origin","birthPlace","foundationPlace","sourcePlace","headquarter","nationality","residence","hometown"};				
		String deathOrBurialPlace[] = {"deathPlace","placeOfBurial"}; 		
		String partner[] = {"spouse","partner"};		
		String keyPerson[] = {"keyPerson","president","monarch","leader", "leaderName","owner","founder"};		
		String influencedBy[] = {"influencedBy"};
		String parent[] = {"parent"};
		String successor[] = {"successor"};		
		String other[] = {"number","relative","crosses","neighboringMunicipality","sisterStation","portrayer","patron","knownFor","ground","tenant","appearancesInNationalTeam","riverMouth",};		
		
		addRelations(locatedInArea,"locatedInArea");
		addRelations(partOf,"partOf");
		addRelations(origin,"origin");		
		addRelations(deathOrBurialPlace,"deathOrBurialPlace");
		addRelations(partner,"partner");
		addRelations(keyPerson,"keyPerson");
		addRelations(influencedBy,"influencedBy");
		addRelations(parent,"parent");
		addRelations(successor,"successor");
		addRelations(other,"other");
		
		Set<String> relations = new HashSet<String>();		
		for (String relation : aggregatedRelations.keySet()) {			
			relations.add(aggregatedRelations.get(relation));
		}
		System.out.println(relations.size() + " relations");
		
		
	}
	
	public static void addRelations(String[] lowerRelations, String relationName) {
			for (String r : lowerRelations) {
				aggregatedRelations.put(r, relationName);
			}
		}
}
