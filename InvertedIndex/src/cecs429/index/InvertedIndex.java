package cecs429.index;

import java.util.*;

public class InvertedIndex implements Index{
    private final Map<String, LinkedList<Posting>> mIndex;
    
    // Constructor
    public InvertedIndex() {
        mIndex = new HashMap<>();
    }
    
    
    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> result;
        if (mIndex.containsKey(term)) {
            result = new ArrayList<>(mIndex.get(term));
            return result;  // Returns term if it's indexed.
        }
        return result = new ArrayList<>(); // Returns empty list otherwise
    }
    
    public void addTerm(String term, int documentId) { 
        // Since it's sequential, we're never returning to the same 
        // doc again in the future.
        if (mIndex.containsKey(term)) {
            // Only add to Posting list if it hasn't already 
            // been done for current document (last of list)
            if (mIndex.get(term).getLast().getDocumentId() != documentId) {
                // Adds posting to term
                mIndex.get(term).add(new Posting(documentId));
            //    System.out.println("Adding Existing Term: " + term + " | Doc: " 
            //            + documentId);
            }
            
        } else {
            // Term hasn't been indexed, add with new LinkedList as object
            LinkedList<Posting> postings = new LinkedList<>();
            postings.add(new Posting(documentId));
            mIndex.put(term, postings);
            //System.out.println("Adding New Term: " + term + " | Doc: " 
            //        + documentId);
        }
    }
    
    @Override
    public List<String> getVocabulary() {
        // O(1)
        // Turn keyset into an arraylist
        ArrayList<String> result = new ArrayList<>(mIndex.keySet());
        
        // O(n*logn)
        Collections.sort(result);
        
        // O(1) * O(n*logn) =  O(n*logn)
        return result;
    }
}
