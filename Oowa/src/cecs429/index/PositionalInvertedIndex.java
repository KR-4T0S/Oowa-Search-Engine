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
    private final Map<Integer, Map<String, Integer>> mDocTermFrequencies;

    // Constructor
    public PositionalInvertedIndex() {
        mIndex = new HashMap<>();
        mDocTermFrequencies = new HashMap<>();
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
                } else { // New document, new posting
                    mIndex.get(term).add(new Posting(documentId));
                    mIndex.get(term).getLast().addPos(pos);
                }
            } else {
                // Term hasn't been indexed, add with new LinkedList as object
                // new document, new posting
                LinkedList<Posting> postings = new LinkedList<>();
                postings.add(new Posting(documentId));
                mIndex.put(term, postings);
                mIndex.get(term).getLast().addPos(pos);
            }
            
            // Now Term Frequency
            // Document exists
            if (mDocTermFrequencies.containsKey(documentId)) {
                // Document has term
                if (mDocTermFrequencies.get(documentId).containsKey(term)) {
                    Map<String, Integer> freqs =  mDocTermFrequencies.get(documentId);
                    int tft = mDocTermFrequencies.get(documentId).get(term) + 1;
                    freqs.replace(term, tft);
                    mDocTermFrequencies.replace(documentId, freqs);
                } else { // Term is new in doc
                    Map<String, Integer> freqs =  mDocTermFrequencies.get(documentId);
                    freqs.put(term, 1);
                    mDocTermFrequencies.replace(documentId, freqs);
                }
            } else { // New document, new term/tft
                Map<String, Integer> newFreq = new HashMap();
                newFreq.put(term, 1);
                mDocTermFrequencies.put(documentId, newFreq);
            }
            
            //System.out.println("Doc: " + documentId + " Term: " + term);
            //System.out.println("mDocTermFrequencies: " + mDocTermFrequencies);
        }
    }
    
    @Override
    public List<Double> getWeights() {
        List<Double> result = new ArrayList();
        
        for (Integer keyMapDocs: mDocTermFrequencies.keySet()) {
            double w_dt_sums = 0;
            // w_dt and add to sum
            for (String keyMapFreqs: mDocTermFrequencies.get(keyMapDocs).keySet()) {
                double w_dt = 1 + Math.log(mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs));
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

    @Override
    public double getWeight(int docId) {
        double w_dt_sums = 0;
        
        // w_dt and add to sum
        for (String keyMapFreqs: mDocTermFrequencies.get(docId).keySet()) {
            double w_dt = 1 + Math.log(mDocTermFrequencies.get(docId).get(keyMapFreqs));
            w_dt_sums +=  Math.pow(w_dt, 2);
        }
            
        return Math.sqrt(w_dt_sums);
    }
}
