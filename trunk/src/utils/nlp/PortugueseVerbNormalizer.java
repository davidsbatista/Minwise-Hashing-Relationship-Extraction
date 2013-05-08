package utils.nlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PortugueseVerbNormalizer {
	
	static String file = "/home/dsbatista/relations-minhash/resources/Label-Delaf_pt_v4_1.dic.utf8";	
	public static HashMap<String, String> verbs = null; 
	
	public static void initialize(){		
		BufferedReader br = null;		
		verbs = new HashMap<String, String>();		 
		try {
			String line;
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				if ((!line.startsWith("%")) && line.contains(".V")) {
					String data[] = line.split(".V")[0].split(",");
					verbs.put(data[0], data[1]);
				}				
			}			
			Set<String> unique_verbs = new HashSet<String>(verbs.values());
			System.out.println(unique_verbs.size() + " verbs loaded");			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static String normalize(String verb) {		
		String normalized = verbs.get(verb);
		return normalized;		
	}
}
