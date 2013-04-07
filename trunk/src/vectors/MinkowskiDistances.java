package vectors;

import java.io.*;
import java.util.*;

// This class implements the family of Minkowski distances and norms
public class MinkowskiDistances {
	
    // The class could be a singleton...
	public MinkowskiDistances() { }
	
	public static double normEuclidean ( double weights[] ) { return normLp(weights, 2); }

	public static double distanceEuclidean ( double weights1[] , double weights2[] ) { return distanceLp(weights1, weights2, 2); }
	
	public static double normManhattan ( double weights[] ) { return normLp(weights, 1); }

	public static double distanceManhattan ( double weights1[] , double weights2[] ) { return distanceLp(weights1, weights2, 1); }

	public static double normFractional ( double weights[] ) { return normLp(weights, 0.5); }

	public static double distanceFractional ( double weights1[] , double weights2[] ) { return distanceLp(weights1, weights2, 0.5); }
	
	public static double normMaximum ( double weights[] ) {
		double sum = Double.MIN_VALUE;
		for ( int i = 0 ; i < weights.length; i++ ) sum = Math.max( weights[i] , sum);
		return sum;
	}

	public static double distanceMaximum ( double weights1[] , double weights2[] ) {
        if ( weights1.length != weights2.length ) throw new Error("The feature vectors being compared do not have the same size.");
		double sum = Double.MIN_VALUE;
		for ( int i = 0 ; i < weights1.length; i++ ) sum += Math.max( weights2[i] - weights1[i] , sum );
		return sum;
	}
	
	public static double normLp ( double weights[], double p ) {
        if ( p == 0 ) throw new Error("The parameter p cannot be zero in an Lp norm.");
		double sum = 0;
		for ( int i = 0 ; i < weights.length; i++ ) sum += Math.pow(weights[i],p);
		return Math.pow(sum, 1.0 / p);
	}

	public static double distanceLp ( double weights1[] , double weights2[], double p ) {
		if ( p == 0 ) throw new Error("The parameter p cannot be zero in an Lp norm.");
        if ( weights1.length != weights2.length ) throw new Error("The feature vectors being compared do not have the same size.");
		double sum = 0;
		for ( int i = 0 ; i < weights1.length; i++ ) sum += Math.pow( weights2[i] - weights1[i] , p );
		return Math.pow(sum, 1.0 / p);
	}

}