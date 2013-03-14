import java.io.*;
import java.util.*;

// This class implements the min-wise hashing scheme
public class MinHash {
	
	private static final int MAX_INT_SMALLER_TWIN_PRIME = 2147482949;
	
    // The type of hash function to use in the generation of the min-hash signatures
	public enum HashType { LINEAR, POLYNOMIAL };
	
	// General interface for all hash functions
	protected interface HashFunction { int hash(byte[] bytes); }

    // The class could be a singleton...
	public MinHash() { }
	
	public static int[] minHashFromWeightedSet( String[] documentTokens, int weights[], int numHashFunctions, HashType hashType ) {
		HashFunction[] hashFunction = createHashFunctions(hashType, numHashFunctions);
		return minHashFromWeightedSet(documentTokens,weights,hashFunction);
	}
	
	public static int[] minHashFromWeightedSet ( String[] documentTokens, int weights[], HashFunction[] hashFunction ) {
        if ( documentTokens.length != weights.length ) throw new Error("The arrays with the tokens and with the weights do not have the same size.");
		Set<String> newSet = new HashSet<String>();
		for ( int i = 0; i < documentTokens.length; i++ ) for ( int j = 0; j < weights[i]; i++ ) {
			newSet.add(documentTokens[i] + "_WEIGHT_" + j);
		}
		return minHashFromSet(newSet.toArray(new String[0]), hashFunction);
	}
	
	public static int[] minHashFromSet ( String[] documentTokens, HashFunction[] hashFunction ) {
		int[] minHashValues = new int[hashFunction.length];
	    for (int i = 0; i < hashFunction.length; i++) minHashValues[i] = Integer.MAX_VALUE;
		for (int i = 0; i < hashFunction.length; i++) {
			for(String token : documentTokens ) {
				byte[] bytesToHash = token.getBytes();
				int hashIndex = hashFunction[i].hash(bytesToHash);
				if (minHashValues[i] > hashIndex) minHashValues[i] = hashIndex;
			}
		}		
		return minHashValues;
	}
	
	public static int[] minHashFromSet( String[] documentTokens, int numHashFunctions, HashType hashType ) {
		HashFunction[] hashFunction = createHashFunctions(hashType, numHashFunctions);
		return minHashFromSet(documentTokens,hashFunction);
	}
	
	public static double jaccardSimilarity ( int[] hashDoc1, int[] hashDoc2 ) {
        if ( hashDoc1.length != hashDoc2.length ) throw new Error("The document signatures being compared do not have the same size.");
		int identicalMinHashes = 0;
        for (int i = 0; i < hashDoc1.length; i++) if (hashDoc1[i] == hashDoc2[i]) identicalMinHashes++;
        return (1.0 * identicalMinHashes) / hashDoc1.length;
	}
	
    public static double realJaccardSimilarity ( String[] s1 , String[] s2 ) {
        double union = s2.length;
        double intersection = 0.0;
        for ( String aux : s1 ) {
           boolean auxB = true;
           for ( String aux2 : s2 ) if ( aux.equals(aux2) ) { intersection++; auxB = false; break; }
           if ( auxB ) union++;
        }
        return intersection / union;
    }

    public static HashFunction[] createHashFunctions( HashType type, int numFunctions ) {
     HashFunction[] hashFunction = new HashFunction[numFunctions];
     Random seed = new Random(11);
     switch (type) {
      case LINEAR:
        for (int i = 0; i < numFunctions; i++) {
          hashFunction[i] = new LinearHash(seed.nextInt(), seed.nextInt());
        }
        break;
      case POLYNOMIAL:
        for (int i = 0; i < numFunctions; i++) {
          hashFunction[i] = new PolynomialHash(seed.nextInt(), seed.nextInt(), seed.nextInt());
        }
        break;
      default: throw new IllegalStateException("Unknown type: " + type);
    }
    return hashFunction;
  }

  static class LinearHash implements HashFunction {
    private final int seedA;
    private final int seedB;

    LinearHash(int seedA, int seedB) {
      this.seedA = seedA;
      this.seedB = seedB;
    }

    @Override
    public int hash(byte[] bytes) {
      long hashValue = 31;
      for (long byteVal : bytes) {
        hashValue *= seedA * byteVal;
        hashValue += seedB;
      }
      return Math.abs((int) (hashValue % MAX_INT_SMALLER_TWIN_PRIME));
    }
  }

  static class PolynomialHash implements HashFunction {
    private final int seedA;
    private final int seedB;
    private final int seedC;

    PolynomialHash(int seedA, int seedB, int seedC) {
      this.seedA = seedA;
      this.seedB = seedB;
      this.seedC = seedC;
    }

    @Override
    public int hash(byte[] bytes) {
      long hashValue = 31;
      for (long byteVal : bytes) {
        hashValue *= seedA * (byteVal >> 4);
        hashValue += seedB * byteVal + seedC;
      }
      return Math.abs((int) (hashValue % MAX_INT_SMALLER_TWIN_PRIME));
    }
  }

}