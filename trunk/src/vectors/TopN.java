package vectors;

import java.io.*;
import java.util.*;

import utils.misc.Pair;

// A simple utilitary class for keeping the top-n most frequent results
public class TopN<T> {
	
  private TreeSet<Pair<T, Double>> elements;
  
  private int n;

  public TopN(int n) {
    this.n = n;
    this.elements = new TreeSet<Pair<T, Double>>(
        new Comparator<Pair<T, Double>>() {
          @Override
          public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
            if (o1.getSecond() > o2.getSecond()) return -1;
            if (o1.getSecond() < o2.getSecond()) return 1;
            return o1.getFirst() == null ? 1 : o2.getFirst() == null ? -1 : new Integer(o1.getFirst().hashCode()).compareTo(o2.getFirst().hashCode());
          }
    });
  }

  public void add(T element, double score) {
    Pair<T, Double> keyVal = new Pair<T, Double>(element,score);
    elements.add(keyVal);
    if (elements.size() > n) elements.pollLast();
  }

  public int size() { return elements.size(); }

  public TreeSet<Pair<T, Double>> get() { return elements; }
  
  public T mostFrequent() {
	  Map<T, Double> map = new HashMap<T, Double>();
	  TreeSet<Pair<T, Double>> aux = get();
	  if ( aux == null || aux.size() == 0 ) return null;
	  for ( Pair<T, Double> t : aux ) {
		  Double score = map.get(t.getFirst());
		  if ( score == null ) score = 0.0;
		  score += t.getSecond();
		  map.put(t.getFirst(),score);		  
	  }
 	  double max = Double.MIN_VALUE;
 	  T result = null;
	  for ( T t : map.keySet() ) if ( map.get(t) > max ) {
		  max = map.get(t);
		  result = t;
	  }
	  return result;
  }
  
  public Double interpolate() {
	  Map<T, Double> map = new HashMap<T, Double>();
	  TreeSet<Pair<T, Double>> aux = get();
	  if ( aux == null || aux.size() == 0 ) return null;
	  for ( Pair<T, Double> t : aux ) {
		  Double score = map.get(t.getFirst());
		  if ( score == null ) score = 0.0;
		  score += t.getSecond();
		  map.put(t.getFirst(),score);		  
	  }
 	  double max = 0.0;
 	  Double result = 0.0;
	  for ( T t : map.keySet() ) {
		  max += map.get(t);		  
		  result += (new Double(t.toString()) * max);
	  }
	  return (max != 0.0) ? (result / max) : result;
  }
  
  public double[] interpolateGeospatial() {
	  Map<T, Double> map = new HashMap<T, Double>();
	  TreeSet<Pair<T, Double>> aux = get();
	  if ( aux == null || aux.size() == 0 ) return null;
	  for ( Pair<T, Double> t : aux ) {
		  Double score = map.get(t.getFirst());
		  if ( score == null ) score = 0.0;
		  score += t.getSecond();
		  map.put(t.getFirst(),score);		  
	  }
      ArrayList<Double> lats = new ArrayList<Double>();
      ArrayList<Double> lons = new ArrayList<Double>();
	  ArrayList<Double> weights = new ArrayList<Double>();
	  for ( T t : map.keySet() ) if ( map.get(t) != 0.0 ) {
		  weights.add(map.get(t));
		  if ( t instanceof Double[] ) {
  		    lats.add(((Double[])(t))[0]);
  		    lons.add(((Double[])(t))[1]);
		  } else {
			String auxS[] = t.toString().split(";"); 
		    lats.add(new Double(auxS[0].trim()));
			lons.add(new Double(auxS[1].trim()));	  	
		  }
	  }
      for(int i= 0; i<weights.size(); i++){
            lats.set(i, lats.get(i) * (Math.PI / 180.0));
            lons.set(i, lons.get(i) * (Math.PI / 180.0));
      }
      ArrayList<Double> x = new ArrayList<Double>();
      ArrayList<Double> y = new ArrayList<Double>();
      ArrayList<Double> z = new ArrayList<Double>();   
      for(int i=0; i<weights.size(); i++){
            x.add(Math.cos(lats.get(i)) * Math.cos(lons.get(i)));
            y.add(Math.cos(lats.get(i)) * Math.sin(lons.get(i)));
            z.add(Math.sin(lats.get(i)));
      }
      double totalWeight = 0.0;
      double xSum = 0.0;
      double ySum = 0.0;
      double zSum = 0.0;
      for(int i = 0; i<weights.size() ; i++) {
            totalWeight += weights.get(i);
            xSum += x.get(i) * weights.get(i);
            ySum += y.get(i) * weights.get(i);
            zSum += z.get(i) * weights.get(i);
      }
      double xAVG = xSum / totalWeight;
      double yAVG = ySum / totalWeight;
      double zAVG = zSum / totalWeight;
      double weightedLon = Math.atan2(yAVG, xAVG);
      double hyp = Math.sqrt(xAVG * xAVG + yAVG * yAVG);
      double weightedLat = Math.atan2(zAVG, hyp);
      weightedLat = weightedLat * (180.0 /Math.PI);
      weightedLon = weightedLon * (180.0 /Math.PI);
      return new double[]{ weightedLat , weightedLon };
  }
  
}