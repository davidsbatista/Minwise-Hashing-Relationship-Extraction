package utils.misc;

import java.util.Comparator;

public class ScorePatternComparator implements Comparator<Pair<Float,String>> {

	@Override
	public int compare(Pair<Float,String> o1, Pair<Float,String> o2) {
		if (o1.getFirst()>o2.getFirst()) return -1;
		else if (o1.getFirst()<o2.getFirst()) return 1;
		else return 0;
	}

}
