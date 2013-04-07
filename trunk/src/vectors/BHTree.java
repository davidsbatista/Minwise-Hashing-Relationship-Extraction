package vectors;

import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import org.mapdb.*;

/** This class implements a simple Locality Sensitive Hashing (LSH) strategy, relying on min-hash for measuring similarity between instances */
public class BHTree {
	
 // The tree indexing the norms
 private NavigableMap<Double,Set<Integer>> index;
 
 // The vector representions for each example in the database
 private Map<Integer,double[]> representation;
 
 // The class assigned to each example in the database
 private Map<Integer,String> value;
 
 private int norm;
 
 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public BHTree ( int norm ) {
	 this(createTempFile("bh-tree"), norm);
 }
  
 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public BHTree( File file, int norm ) {
	 if ( norm != 0 ) throw new Error("Unknown norm.");
     try {
  	   DB db = DBMaker.newFileDB(file).closeOnJvmShutdown().make();
       this.representation = db.getTreeMap("representation");
	   this.value = db.getTreeMap("value");
	   this.index = db.getTreeMap("index");
	   this.norm = norm;
 	 } catch ( Exception ex ) { ex.printStackTrace(); throw new Error(ex); } 
 }
 
 // Returns the number of examples that are currently indexed
 public int indexSize ( ) { return value.size(); }
  
 // Adds a new example to the index
 public void index ( Integer id, double[] data , String result ) {
     try {
         double code = norm(data);
		 HashSet<Integer> auxSet = new HashSet<Integer>();			 
		 if ( index.containsKey(code) ) auxSet.addAll(index.get(code));
		 auxSet.add(id);
		 index.put(code,Collections.unmodifiableSet(auxSet));
	 } catch ( Exception ex ) { ex.printStackTrace(System.err); }
	 representation.put(id,data);
	 value.put(id,result);
 }
 
 // Returns all instances whose norm is within a given range
 public Set<Integer> queryRange( double[] data , double low, double high ) {
		 HashSet<Integer> auxSet = new HashSet<Integer>();
		 double code = norm(data);
		 for ( Set<Integer> entry : index.subMap(low, true, high, true).values() ) auxSet.addAll(entry);
		 return auxSet;
 }
 
 // Returns the top-k most similar examples in the database
 public TopN<String> queryNearest ( double[] data , int k ) {
	 TopN<String> result = new TopN<String>(k);
	 double code = norm(data);
	 k = Math.min(k,indexSize());
	 double range = 0.01;
	 while ( result.size() != k ) {
		 Set<Integer> auxSet = queryRange(data, code - range, code + range);
		 if ( auxSet != null ) for ( Integer candidate : auxSet ) {
			 String value = this.value.get(candidate);
			 double data2[] = this.representation.get(candidate);
		  	 if ( norm == 0 ) result.add(value, MinkowskiDistances.distanceEuclidean(data,data2));
		 	 if ( norm == 1 ) result.add(value, MinkowskiDistances.distanceManhattan(data,data2));
		 	 if ( norm == 2 ) result.add(value, MinkowskiDistances.distanceFractional(data,data2));
		     if ( norm == 3 ) result.add(value, MinkowskiDistances.distanceMaximum(data,data2));
		 }
		 range = range * 2.0;
	 }
	 return result;	 
 }
 
 // Returns the norm for a given feature vector
 private double norm ( double[] data ) {
 	if ( norm == 0 ) return MinkowskiDistances.normEuclidean(data);
	if ( norm == 1 ) return MinkowskiDistances.normManhattan(data);
	if ( norm == 2 ) return MinkowskiDistances.normFractional(data);
    if ( norm == 3 ) return MinkowskiDistances.normMaximum(data);
	return -1;
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