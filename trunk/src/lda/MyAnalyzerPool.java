package lda;



import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.util.Version;

/**
 * Implements the Object Pool Design Pattern so that unnecessary instantiations of Analyzers are
 * avoided by providing a pool of already created and unused analyzers.
 *
 * @author ianastacio
 *
 */
public class MyAnalyzerPool {

	private final File stopwords = new File("etc/stopwords-en.txt");

	private Stack<MyAnalyzer> stack;

	/////////// Thread-safe Singleton ///////////////////
	private volatile static MyAnalyzerPool instance;

	private MyAnalyzerPool(){
		stack = new Stack<MyAnalyzer>();
	}

	public static MyAnalyzerPool getInstance() {

		if (instance == null)
			synchronized (MyAnalyzerPool.class) {
				if (instance == null) {
					instance = new MyAnalyzerPool();
				}
			}

		return instance;
	}
	////////////////////////////////////////////////////

	/**
	 * This methods returns instances from a pool of Analyzer objects. If the pool is empty a new
	 * analyzer is returned.
	 *
	 * In order to improve performance, it is important not to forget to release the
	 * analyzer once it is no longer useful.
	 *
	 * @param stopwords
	 * @param language
	 * @return
	 * @throws IOException
	 */
    public MyAnalyzer acquireTextAnalyzer() throws IOException
    {
    	if (stack.isEmpty()) {
    		return new MyAnalyzer(
    				Version.LUCENE_31,
    				stopwords);
    	}

    	return stack.pop();
    }

    /**
     * Makes the given analyzer available for others to reuse.
     *
     * @param a
     */
	public void releaseTextAnazyzer(MyAnalyzer a) {
		stack.push(a);
	}
}
