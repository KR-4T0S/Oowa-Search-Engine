package cecs429.index;

import java.util.List;
import java.util.Map;

/**
 * An Index can retrieve postings for a term from a data structure associating
 * terms and the documents that contain them.
 */
public interface Index {
    
    List<Posting> getNonPositionalPostings(String term);
    
    /**
     * Retrieves a list of Postings of documents that contain the given term.
     */
    List<Posting> getPostings(String term);
    
    
    /**
     * Retrieves the map of term weights for each document.
     */
    List<Double> getWeights();
    
    double getWeight(int docId);

    /**
     * A (sorted) list of all terms in the index vocabulary.
     */
    List<String> getVocabulary();
}
