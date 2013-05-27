package datasets.WikiPT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Relations {
	
	public static Map<String, String> aggregatedRelations = null; 		
	public static String[] ignore = {"picture","maintainedBy","politicGovernmentDepartment","regionServed","leader","owner"};
	
	public static String[] changeDirection = {"predecessor","doctoralAdvisor","influenced","child","foundedBy",
											  "capitalCountry","currentMember","pastMember","keyPerson","president","leaderName",
											  "successor","city","capital"};
	
	public static String other[] = {"number","relative","crosses","neighboringMunicipality","sisterStation","portrayer","patron","knownFor","ground","tenant","appearancesInNationalTeam","riverMouth",};
	
	public static void initialize() {
		aggregatedRelations = new HashMap<String, String>();
		
		//trocar a direcção para encaixar em "locatedinArea"
		//capitalCountry
		//*city
		//*capital
		String locatedInArea[] = {"locatedInArea","archipelago","location", "locationCity",
								  "locationCountry","municipality","subregion","federalState","district","region",
								  "province","state","county", "map","campus","garrison","department","country"};		
		
		//trocar a direccao para encaixar em "partOf"
		//currentMember
		//pastMember
		String partOf[] = {"type","parentOrganisation","distributingCompany","broadcastNetwork","affiliation",
						   "university","youthClub","party","pastMember","team","associatedMusicalArtist","member"};						
		
		String origin[] = {"origin","birthPlace","foundationPlace","sourcePlace","headquarter","nationality","residence","hometown","sportCountry"};						
		String deathOrBurialPlace[] = {"deathPlace","placeOfBurial"};		
		String partner[] = {"spouse","partner"};

		//trocar direcção para encaixar em "e1, keyperson in e2"
		//keyPerson
		//president
		//leaderName
		String keyPerson[] = {"keyPerson","president","monarch","leader","leaderName","founder"};		
		
		String influencedBy[] = {"influencedBy"};
		
		String parent[] = {"parent"};
		
		//trocar direcção para encaixar em "e1, succesor of e2"
		String successor[] = {"successor"};	
		
				
		
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
	}
	
	public static void addRelations(String[] lowerRelations, String relationName) {
			for (String r : lowerRelations) {
				aggregatedRelations.put(r, relationName);
			}
		}
}
