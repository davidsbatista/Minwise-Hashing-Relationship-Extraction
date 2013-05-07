package nbtree;

import java.io.*;
import java.util.*;

// This class implements the family of Minkowski distances and norms
public class MinkowskiDistances {
	
    // The class could be a singleton...
	public MinkowskiDistances() { }
	
	public static double normEuclidean ( Double weights[] ) { return normLp(weights, 2); }

	public static double distanceEuclidean ( Double weights1[] , Double weights2[] ) { return distanceLp(weights1, weights2, 2); }
	
	public static double normManhattan ( Double weights[] ) { return normLp(weights, 1); }

	public static double distanceManhattan ( Double weights1[] , Double weights2[] ) { return distanceLp(weights1, weights2, 1); }

	public static double normFractional ( Double weights[] ) { return normLp(weights, 0.5); }

	public static double distanceFractional ( Double weights1[] , Double weights2[] ) { return distanceLp(weights1, weights2, 0.5); }
	
	public static double normMaximum ( Double weights[] ) {
		double sum = Double.MIN_VALUE;
		for ( int i = 0 ; i < weights.length; i++ ) sum = Math.max( weights[i] , sum);
		return sum;
	}

	public static double distanceMaximum ( Double weights1[] , Double weights2[] ) {
        if ( weights1.length != weights2.length ) throw new Error("The feature vectors being compared do not have the same size.");
		double sum = Double.MIN_VALUE;
		for ( int i = 0 ; i < weights1.length; i++ ) sum += Math.max( weights2[i] - weights1[i] , sum );
		return sum;
	}
	
	public static double normLp ( Double weights[], double p ) {
        if ( p == 0 ) throw new Error("The parameter p cannot be zero in an Lp norm.");
		double sum = 0;
		for ( int i = 0 ; i < weights.length; i++ ) sum += Math.pow(weights[i],p);
		return Math.pow(sum, 1.0 / p);
	}

	public static double distanceLp ( Double weights1[] , Double weights2[], double p ) {
		if ( p == 0 ) throw new Error("The parameter p cannot be zero in an Lp norm.");
        if ( weights1.length != weights2.length ) throw new Error("The feature vectors being compared do not have the same size.");
		double sum = 0;
		for ( int i = 0 ; i < weights1.length; i++ ) sum += Math.pow( weights2[i] - weights1[i] , p );
		return Math.pow(sum, 1.0 / p);
	}
	
	public static double fastMap ( Double[] p1 ) {
		Double pivot1[] = new Double[p1.length];
		Double pivot2[] = new Double[p1.length];
		for ( int i = 0 ; i < (int)Math.floor(p1.length / 2.0) ; i++ ) {
			pivot1[i] = 1.0 / (double)(p1.length);	
			pivot2[i] = 0.0;
		}
		for ( int i = (int)Math.floor(p1.length / 2.0); i < p1.length; i++ ) {
			pivot1[i] = 0.0;
			pivot2[i] = 1.0 / (double)(p1.length);
		}		
		return (Math.pow(jensenShannonDivergence(p1,pivot1),2) + Math.pow(jensenShannonDivergence(pivot1,pivot2),2) - Math.pow(jensenShannonDivergence(p1,pivot2),2)) / 
		       (2.0 * jensenShannonDivergence(pivot1,pivot2));
	}

    public static double jensenShannonDivergence( Double[] p1, Double[] p2 ) {
      if ( p1.length != p2.length ) throw new Error("The feature vectors being compared do not have the same size.");
      Double[] average = new Double[p1.length];
      for (int i = 0; i < p1.length; i++) average[i] = (p1[i] + p2[i]) / 2.0;
      return Math.sqrt((klDivergence(p1, average) * 0.5) + (klDivergence(p2, average) * 0.5));
    }
    
	public static double sklDivergence( Double[] p1, Double[] p2 ) {
	  return Math.sqrt(( 0.5 * klDivergence(p1,p2) ) + ( 0.5 * klDivergence(p2,p1) ));
    }

    public static double klDivergence( Double[] p1, Double[] p2 ) {
      if ( p1.length != p2.length ) throw new Error("The feature vectors being compared do not have the same size.");
      double klDiv = 0.0;
      for (int i = 0; i < p1.length; ++i) {
        if (p1[i] == 0.0) { continue; }
        if (p2[i] == 0.0) { continue; }
        klDiv += p1[i] * Math.log( p1[i] / p2[i] );
      }
      return klDiv / Math.log(2); 
    }

}