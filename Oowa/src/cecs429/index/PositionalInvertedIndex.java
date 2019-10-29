/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author RICHIE
 */
public class PositionalInvertedIndex implements Index {

    private final Map<String, LinkedList<Posting>> mIndex;
    private final LinkedList<Map<String, Integer>> mDocTermFrequencies;

    // Constructor
    public PositionalInvertedIndex() {
        mIndex = new HashMap<>();
        mDocTermFrequencies = new LinkedList<>();
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

    public void addTerm(List<String> terms, int documentId, int pos) {
        for (String term : terms) {
            // Since it's sequential, we're never returning to the same 
            // doc again in the future. LinkedList helps do O(1)
            if (mIndex.containsKey(term)) {
                // Still working on existing document
                if (mIndex.get(term).getLast().getDocumentId() == documentId) {
                    mIndex.get(term).getLast().addPos(pos);
                    
                    // Update weight for term in current document
                    //System.out.println(mDocTermFrequencies.getLast());
                    Map<String, Integer> newWeight = new HashMap();
                    newWeight.put(term, mDocTermFrequencies.getLast().get(term) + 1);
                    mDocTermFrequencies.removeLast();
                    mDocTermFrequencies.add(newWeight);
                } else { // New document, new posting
                    mIndex.get(term).add(new Posting(documentId));
                    mIndex.get(term).getLast().addPos(pos);
                    
                    // Create weight map for this term in new document
                    Map<String, Integer> newWeight = new HashMap();
                    newWeight.put(term, 1);
                    mDocTermFrequencies.add(newWeight);
                }
            } else {
                // Term hasn't been indexed, add with new LinkedList as object
                LinkedList<Posting> postings = new LinkedList<>();
                postings.add(new Posting(documentId));
                mIndex.put(term, postings);
                mIndex.get(term).getLast().addPos(pos);
                
                // Create weight map for this term in new document
                Map<String, Integer> newWeight = new HashMap();
                newWeight.put(term, 1);
                mDocTermFrequencies.add(newWeight);
            }
        }
    }
    
    @Override
    public List<Double> getWeights() {
        List<Double> result = new ArrayList();
        
        for (Map<String, Integer> e: mDocTermFrequencies) {
            double w_dt_sums = 0;
            // w_dt and add to sum
            for (String key: e.keySet()) {
                double w_dt = 1 + Math.log(e.get(key));
                w_dt_sums +=  Math.pow(w_dt, 2);
            }
            
            double L_d = Math.sqrt(w_dt_sums);
            result.add(L_d);
        }
        
        return result; 
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
