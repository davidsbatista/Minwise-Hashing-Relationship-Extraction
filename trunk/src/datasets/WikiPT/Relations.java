package datasets.WikiPT;

import java.util.HashMap;
import java.util.Map;

public class Relations {
	
	public static Map<String, String> relations = null; 
		
	
	public Relations() {
		relations = new HashMap<String, String>();
				
		String locatedInArea[] = {"locatedInArea","location", "locationCity","locationCountry","capitalCountry","municipality","subregion","federalState","capital","district","region","city","country","province","state","county", "locatedInArea"};		
		String affiliation[] = {"affiliation","university","youthClub","currentMember","party","pastMember","team","associatedMusicalArtist","member"};		
		String keyPerson[] = {"keyPerson","president","monarch","keyPerson","leader", "leaderName","owner"};		
		String parentOrganisation[] = {"parentOrganisation","department","parentOrganisation","distributingCompany","broadcastNetwork"};
		String origin[] = {"origin","foundationPlace","sourcePlace","headquarter","nationality","residence","hometown"};
		
		addRelations(relations,locatedInArea,"locatedInArea");
		addRelations(relations,affiliation,"affiliation");
		addRelations(relations,keyPerson,"keyPerson");
		addRelations(relations,parentOrganisation,"parentOrganisation");
		addRelations(relations,origin,"origin");
	}

	
	public static void addRelations(Map<String, String> topRelationsMap, String[] lowerRelations, String relationName) {
			for (String r : lowerRelations) {
				topRelationsMap.put(r, relationName);
			}
		}

}
