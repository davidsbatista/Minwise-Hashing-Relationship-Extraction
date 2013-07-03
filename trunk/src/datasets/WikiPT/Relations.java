package datasets.WikiPT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Relations {
	
	public static Map<String, String> aggregatedRelations = null; 		

	public static String[] ignore = {"beatifiedPlace","broadcastArea","beatifiedPlace","award","anthem","majorShrine","largestCountry","knownFor","officialLanguage","lessPopulousCountry",
									 "smallestCountry","canonizedPlace","majorVulcano","lessPopulatedCountry","product","poorestCountry","servingRailwayLine","picture",
									 "maintainedBy","politicGovernmentDepartment","regionServed","leader","owner","city","garrison","county","mostPopulousCountry",
									 "citizenship", "spouse", "governmentType", "timeZone", "instrument", "ethnicity", "origin", "anthem", "heir", "employer", 
									 "club", "beatifiedPlace", "tenant", "saint", "patent", "distributingLabel", "distributingCompany", "chairman", "award", 
									 "affiliation", "president", "portrayer", "militaryUnit", "mayor", "club","ethnicity","governmentType","heir","instrument",
									 "notableWork","owningOrganisation","occupation","timeZone","majorVolcano","ignore","product","patent","mostPopulatedCountry",
									 "mostPopulousCountry","distributingLabel","division","largestCountry","lessPopulatedCountry","lessPopulousCountry","militaryUnit",
									 "poorestCountry","richestCountry","smallestCountry","sourceMountain","recordLabel","parentCompany","subsidiary","commune","occupation"};
	
	public static String other[] = {"number","relative","crosses","neighboringMunicipality","sisterStation","portrayer","patron","knownFor","ground",
									"tenant","appearancesInNationalTeam","riverMouth",};	

	public static String[] changeDirection = {"predecessor","doctoralAdvisor","influenced","child","foundedBy",
											  "capitalCountry","currentMember","pastMember","keyPerson","president","leaderName",
											  "successor","capital","parent","largestCity"};
	
	public static void initialize() {
		aggregatedRelations = new HashMap<String, String>();
		
		String locatedInArea[] = {"locatedInArea","archipelago","location", "locationCity",
								  "locationCountry","municipality","subregion","federalState","district","region",
								  "province","state","county", "map","campus","department","country"};		
		
		String partOf[] = {"type","parentOrganisation","distributingCompany","broadcastNetwork","affiliation",
						   "university","youthClub","party","pastMember","team","associatedMusicalArtist","member","associatedBand","owningCompany"};						
		
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
