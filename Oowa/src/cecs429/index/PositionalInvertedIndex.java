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
        int termCtr = 0;
        for (String term : terms) {
            // Since it's sequential, we're never returning to the same 
            // doc again in the future. LinkedList helps do O(1)
            if (mIndex.containsKey(term)) { // Term is already indexed
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
                    int tft = mIndex.get(term).getLast().getTftd();
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
        }
        //System.out.println("mDocTermFrequencies: " + mDocTermFrequencies);
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
    public List<Double> getWeights() {
        //System.out.println("mDocTermFrequencies: " + mDocTermFrequencies);
        List<Double> result = new ArrayList();
        
        // For each doc
        // Set to compare with DocId 0
        int prev = -1; // dealing with empty bodies. (isn't in keyset)
        for (Integer keyMapDocs: mDocTermFrequencies.keySet()) {
            // Fills in Ld as 0 for all empty files.
            while(keyMapDocs - prev != 1) {
                result.add((double) 0); //
                prev++;
                //System.out.println("\tL_" + prev + ": " + 0);
            } 
            
            //System.out.println("DocID: " + keyMapDocs);
           
            // w_dt and add to sum
            // For each term
            double w_dt_sums = 0;
            for (String keyMapFreqs: mDocTermFrequencies.get(keyMapDocs).keySet()) {
                // w_dt = 1 + ln(tf_td)
                double w_dt = 1 + Math.log(mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs));
                // (wdt^2)
                w_dt_sums +=  w_dt * w_dt;

                //System.out.println("\tw_"+ keyMapFreqs + ": " + w_dt + " | " + mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs));
            }


            double L_d = Math.sqrt(w_dt_sums);
            //System.out.println("\tSum(w_dt): " + w_dt_sums);
            //System.out.println("\tL_" + keyMapDocs + ": " + L_d);
            
            result.add(L_d);
                
            prev = keyMapDocs;
        }
        
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

    @Override
    public List<Posting> getNonPositionalPostings(String term) {
        return getPostings(term);
    }

    @Override
    public int getTermCount() {
        return mIndex.keySet().size();
    }
}
