package minhash;
import java.util.*;
import java.io.*;
import java.lang.reflect.Array;


import org.mapdb.*;
import com.davidsoergel.conja.Function;
import com.davidsoergel.conja.Parallel;

/** This class implements a simple Locality Sensitive Hashing (LSH) strategy, relying on min-hash for measuring similarity between instances */
public class LocalitySentitiveHashing {
	
 // The hash functions for generating the min-hash signatures
 private MinHash.HashFunction function[];
 
 // The bands from the LSH index
 private Map<Integer,Set<Integer>> index[];
 
 // The min-hash representions for each example in the database
 private Map<Integer,int[]> representation;
 
 // The class assigned to each example in the database
 private Map<Integer,String> value;

 // The optimal k value for each training example
 private Map<Integer,Short> kvalue;
 
 // An optional map with importance weights assigned to each of the elements that are used in the representation of the examples
 private Map<String,Integer> featureWeights;
 
 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public LocalitySentitiveHashing ( int numFunctions , int numBands ) {
	 this(createTempFile("locality-sentitive-hashing"),numFunctions,numBands,new HashMap<String,Integer>());
 }

 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public LocalitySentitiveHashing ( File file, int numFunctions , int numBands ) {
	 this(file,numFunctions,numBands,new HashMap<String,Integer>());
 }

 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public LocalitySentitiveHashing ( int numFunctions , int numBands, Map<String,Integer> featureWeights ) {
	  this(createTempFile("locality-sentitive-hashing"),numFunctions,numBands,featureWeights);
 }
  
 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public LocalitySentitiveHashing ( File file, int numFunctions , int numBands, Map<String,Integer> featureWeights ) {
	 if ( numFunctions % numBands != 0 ) throw new Error("Number of hash functions is not divisible by the number of bands.");
     try {
  	   DB db = DBMaker.newFileDB(file).journalDisable().closeOnJvmShutdown().make();
       this.featureWeights = featureWeights;
	   this.function = MinHash.createHashFunctions(MinHash.HashType.POLYNOMIAL,numFunctions);
       this.representation = db.getTreeMap("representation");
	   this.value = db.getTreeMap("value");
	   this.kvalue = db.getTreeMap("kvalue");
	   this.index = (Map[]) Array.newInstance(db.getTreeMap("index").getClass(),numBands);	 
	   for ( int i = 0 ; i < numBands ; i++ ) this.index[i] = db.getTreeMap("index-"+i);
 	 } catch ( Exception ex ) { ex.printStackTrace(); throw new Error(ex); } 
 }
 
 // Returns the number of examples that are currently indexed
 public int indexSize ( ) { return value.size(); }

 // Returns the number of examples that are currently indexed that are of a particular type
 public int indexSize ( String type ) { 
	 int counter = 0;
	 for ( Integer key : value.keySet() ) if( value.get(key).equals(type)) counter++; 
	 return counter;
 }
 
 // Adds a new example to the index
 public void index ( Integer id, String[] data , int weights[], String result ) {
    if ( data.length != weights.length ) throw new Error("The arrays with the data and with the weights do not have the same size.");
	Set<String> newSet = new HashSet<String>();
	for ( int i = 0; i < data.length; i++ ) for ( int j = 0; j < weights[i]; i++ ) newSet.add(data[i] + "_VALUE_" + j);
	index(id, newSet.toArray(new String[0]), result);
 }
 
 // Adds a new example to the index
 public void index ( Integer id, String[] data , String result ) {
     int weights[] = null;
	 if ( featureWeights.size() > 0 ) {
	 	   weights = new int[data.length];
		   for ( int i = 0 ; i < weights.length; i++ ) weights[i] = featureWeights.containsKey(data[i]) ? featureWeights.get(data[i]) : 0;
	 }
     int size = function.length / index.length;
	 int[] minhash = weights == null ? MinHash.minHashFromSet(data,function) : MinHash.minHashFromWeightedSet(data,weights,function);
	 for ( int i = 0 ; i < index.length; i++ ) try {
         int code = function[0].hash(integersToBytes(minhash,i*size,size));
		 HashSet<Integer> auxSet = new HashSet<Integer>();			 
		 if ( index[i].containsKey(code) ) auxSet.addAll(index[i].get(code));
		 auxSet.add(id);
		 index[i].put(code,Collections.unmodifiableSet(auxSet));
	 } catch ( Exception ex ) { ex.printStackTrace(System.err); }
	 representation.put(id,minhash);
	 value.put(id,result);
 }
 
 // Computes the optimal K value for each example in the database, through a leave-one-out methodology
 public void computeOptimalK( ) { computeOptimalK(new short[]{3,5,7,15} ); }

 // Computes the optimal K value for each example in the database, through a leave-one-out methodology
 public void computeOptimalK( final short[] valuesToTest ) {
	 for( Integer example : value.keySet() ) {
	 //Parallel.forEach(value.keySet().iterator(), new Function<Integer, Void>() { public Void apply(Integer example) {
		 String cl = value.get(example);
		 int minhash[] = representation.get(example);
		 for ( short k : valuesToTest ) {			
			 TopN<String> result = new TopN<String>(k);
		     int size = function.length / index.length;
			 for ( int i = 0 ; i < index.length; i++ ) {
		         int code = function[0].hash(integersToBytes(minhash,i*size,size));
				 Set<Integer> auxSet = index[i].get(code);
				 if ( auxSet != null ) for ( Integer candidate : auxSet ) if ( candidate != example ) {
					 String val = value.get(candidate);
					 int rep[] = representation.get(candidate);
				     result.add(val, MinHash.jaccardSimilarity(minhash,rep));			
				 }
			 }
			 String newCl = result.mostFrequent();
			 kvalue.put(example, new Short(k));
			 if ( newCl.equals(cl) ) break;
		 }
		// return null;
	 }// });
 }

 // Returns the top-k most similar examples in the database
 public TopN<String> queryNearest ( String[] data , int weights[], int k ) {
    if ( data.length != weights.length ) throw new Error("The arrays with the data and with the weights do not have the same size.");
	Set<String> newSet = new HashSet<String>();
	for ( int i = 0; i < data.length; i++ ) for ( int j = 0; j < weights[i]; i++ ) {
		newSet.add(data[i] + "_VALUE_" + j);
	}
	return queryNearest(newSet.toArray(new String[0]), k);
 }
 
 // Returns the top-k most similar examples in the database
 public TopN<String> queryNearest ( String[] data , int k ) {
	 int weights[] = null;
	 if ( featureWeights.size() > 0 ) {
	 	   weights = new int[data.length];
		   for ( int i = 0 ; i < weights.length; i++ ) weights[i] = featureWeights.containsKey(data[i]) ? featureWeights.get(data[i]) : 0;
	 }
     final int size = function.length / index.length;
	 final int[] minhash = weights == null ? MinHash.minHashFromSet(data,function) : MinHash.minHashFromWeightedSet(data,weights,function);
	 if ( k <= 0 ) {
		 final TopN<Integer> result = new TopN<Integer>(1);
//		 Parallel.forEach(index.length, new Function<Integer, Void>() { public Void apply(Integer i) {
		 for ( int i = 0 ; i < index.length; i++ ) {
	         int code = function[0].hash(integersToBytes(minhash,i*size,size));
			 Set<Integer> auxSet = index[i].get(code);
			 if ( auxSet != null ) for ( Integer candidate : auxSet ) {
				 int rep[] = representation.get(candidate);
			     result.add(candidate, MinHash.jaccardSimilarity(minhash,rep));			
			 }
//			 return null;
		 }// });
		 k = result.get().size() == 1 ? kvalue.get(result.mostFrequent()) : 7;
	 }
	 final TopN<String> result = new TopN<String>(k);
//	 Parallel.forEach(index.length, new Function<Integer, Void>() { public Void apply(Integer i) {
	 for ( int i = 0 ; i < index.length; i++ ) { 
         int code = function[0].hash(integersToBytes(minhash,i*size,size));
		 Set<Integer> auxSet = index[i].get(code);
		 if ( auxSet != null ) for ( Integer candidate : auxSet ) {
			 String valueS = value.get(candidate);
			 int rep[] = representation.get(candidate);
		     result.add(valueS, MinHash.jaccardSimilarity(minhash,rep));			
		 }
//		 return null;
	 }// });
	 return result;	 
 }
 
 // Utility method for converting between an a subset of the values in an array of integers, and an array of bytes
 private byte[] integersToBytes(int[] values, int position, int length ) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for(int i=position; i < position + length && i < values.length; ++i) try { dos.writeInt(values[i]); } catch ( Exception ex ) { }
    return baos.toByteArray();
 }
 
 // Create a temporary directory
 public static File createTempDirectory( String name ) {
   try {
     final File temp;
     temp = File.createTempFile(name, Long.toString(System.nanoTime()));
     if(!(temp.delete())) throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
     if(!(temp.mkdir())) throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
     return temp;
   } catch ( Exception ex ) { ex.printStackTrace(); throw new Error(ex); } 
 }
 
 // Create a temporary file
 public static File createTempFile( String name ) {
   try {
     final File temp;
     temp = File.createTempFile(name, Long.toString(System.nanoTime()));
     return temp;
   } catch ( Exception ex ) { ex.printStackTrace(); throw new Error(ex); } 
 }

}