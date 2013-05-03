package nbtree;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import utils.misc.TopN;

public class NBTree {
	
 // The tree indexing the norms
 private NavigableMap<Double,Set<Integer>> index;
 
 // The vector representions for each example in the database
 private Map<Integer,Double[]> representation;
 
 // The class assigned to each example in the database
 private Map<Integer,String> value;
 
 private int norm;
 
 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public NBTree ( int norm ) {
	 this(createTempFile("bh-tree"), norm);
 }
  
 // A constructor that initializes the LSH index with a given number of hash functions for the min-hash signatures, and with a given number of bands
 public NBTree( File file, int norm ) {
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

 // Returns the number of examples that are currently indexed that are of a particular type
 public int indexSize ( String type ) { 
	 int counter = 0;
	 for ( Integer key : value.keySet() ) if( value.get(key).equals(type)) counter++; 
	 return counter;
 }
 
 // Adds a new example to the index
 public void index ( Integer id, Double[] data , String result ) {
     try {
         double code = norm(data);
         double fastMap = MinkowskiDistances.fastMap(data);
		 HashSet<Integer> auxSet = new HashSet<Integer>();			 
		 if ( index.containsKey(code) ) auxSet.addAll(index.get(code));
		 auxSet.add(id);
		 index.put(code,Collections.unmodifiableSet(auxSet));
		 System.out.println(" ** INDEXING NORM (L2) 	 : " + code + " " + id + " " + result);
		 System.out.println(" ** INDEXING NORM (FastMap) : " + fastMap + " " + id + " " + result);
	 } catch ( Exception ex ) { ex.printStackTrace(System.err); }
	 representation.put(id,data);
	 value.put(id,result);
 }
 
 // Returns all instances whose norm is within a given range
 public Set<Integer> queryRange( double low, double high ) {
		 HashSet<Integer> auxSet = new HashSet<Integer>();
		 for ( Set<Integer> entry : index.subMap(low, true, high, true).values() ) auxSet.addAll(entry);
		 return auxSet;
 }
 
 // Returns the top-k most similar examples in the database
 public TopN<String> queryNearest ( Double[] data , int k ) {
	 TopN<String> result = new TopN<String>(k);
	 double code = norm(data);
	 k = Math.min(k,indexSize());
	 double range = 0.1;
	 while ( result.size() != k ) {
		 Set<Integer> auxSet = queryRange(code - range, code + range);
		 if ( auxSet != null ) for ( Integer candidate : auxSet ) {
			 String value = this.value.get(candidate);
			 Double data2[] = this.representation.get(candidate);
		  	 if ( norm == 0 ) result.add(value, 1.0 / (1.0 + MinkowskiDistances.distanceEuclidean(data,data2)));
		 	 if ( norm == 1 ) result.add(value, 1.0 / (1.0 + MinkowskiDistances.distanceManhattan(data,data2)));
		 	 if ( norm == 2 ) result.add(value, 1.0 / (1.0 + MinkowskiDistances.distanceFractional(data,data2)));
		     if ( norm == 3 ) result.add(value, 1.0 / (1.0 + MinkowskiDistances.distanceMaximum(data,data2)));
		 }
		 range = range * 2.0;
	 }
	 return result;	 
 }
 
 // Returns the norm for a given feature vector
 private double norm ( Double[] data ) {
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