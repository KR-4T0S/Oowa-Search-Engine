/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

import cecs429.documents.DocumentCorpus;
import java.io.File;
import java.nio.file.Paths;
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
    private final DocumentCorpus mCorpus;

    // Constructor
    public PositionalInvertedIndex(DocumentCorpus corpus) {
        mIndex = new HashMap<>();
        mDocTermFrequencies = new HashMap<>();
        mCorpus = corpus;
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

    /*
    *   Doc Weights File Format
    *   [docWeights_d]_0    [docLength_d]_0     [byteSize_d]_0      [avg(tf_td)]_0
    *   [docWeights_d]_1    [docLength_d]_1     [byteSize_d]_1      [avg(tf_td)]_1
    *   ..
    *   [docWeights_d]_n-1  [docLength_d]_n-1   [byteSize_d]_n-1    [avg(tf_td)]_n-1
    *   [docWeights_d]_n    [docLength_d]_n     [byteSize_d]_n      [avg(tf_td)]_n
    *   [docLength_A]
    */
    @Override
    public List<Double> getWeights() {
        List<Double> result = new ArrayList();
        
        /* TEMP (?) */
        double docLength_A = 0; // docLength_A
        /* TEMP (?) */
        
        // For each doc
        // Set to compare with DocId 0
        int prev = -1; // dealing with empty bodies. (isn't in keyset)
        for (Integer keyMapDocs: mDocTermFrequencies.keySet()) {
            // Fills in weights for empty files
            while(keyMapDocs - prev != 1) {
                result.add((double) 0); // docWeights_d
                
                /* TEMP (?) */
                result.add((double) 0); // docLength_d
                result.add((double) 0); // byteSize_d;
                result.add((double) 0); // avg(tf_td);
                /* TEMP (?) */
                
                prev++;
            } 
                      
            /* TEMP (?) */
            double docLength_d = 0;
            double sum_tf_td = 0;
            /* TEMP (?) */
            
            // w_dt and add to sum
            // For each term
            double w_dt_sums = 0;
            for (String keyMapFreqs: mDocTermFrequencies.get(keyMapDocs).keySet()) {
                // w_dt = 1 + ln(tf_td)
                double w_dt = 1 + Math.log(mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs));
                // (wdt^2)
                w_dt_sums +=  w_dt * w_dt;
                
                /* TEMP (?) */
                docLength_d += mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs);
                docLength_A += mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs);
                sum_tf_td += mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs);
                /* TEMP (?) */
            }

            double L_d = Math.sqrt(w_dt_sums);
            /* TEMP (?) */
            double avg_tf_td = sum_tf_td / mDocTermFrequencies.get(keyMapDocs).keySet().size();
            double byteSize_d = mCorpus.getFileSize(keyMapDocs);
            /* TEMP (?) */
            
            result.add(L_d); // docWeights_d
            /* TEMP (?) */
            // TODO: ADD IN ORDER-> docLength_d, byteSize_d, avg(tf_td);
            result.add(docLength_d); // docLength_d
            result.add(byteSize_d); // byteSize_d
            result.add(avg_tf_td);  // avg(tftd)
            /* TEMP (?) */
                
            prev = keyMapDocs;
        }
        
        docLength_A = (double) docLength_A / (double) (mDocTermFrequencies.keySet().size());
        /* TEMP (?) */
        // now add Total Corpus Doc Length as final set
        result.add(docLength_A);
        /* TEMP (?) */
        
        return result; 
    }
    
    @Override
    public double getWeightDefault(int docId) {
        double w_dt_sums = 0;
        
        // w_dt and add to sum
        for (String keyMapFreqs: mDocTermFrequencies.get(docId).keySet()) {
            double w_dt = 1 + Math.log(mDocTermFrequencies.get(docId).get(keyMapFreqs));
            w_dt_sums +=  Math.pow(w_dt, 2);
        }
            
        return Math.sqrt(w_dt_sums);
    }
    
    @Override
    public double getDocLength(int docId) {
        double docLength = 0;
        
        for (String keyMapFreqs: mDocTermFrequencies.get(docId).keySet()) {
            docLength += mDocTermFrequencies.get(docId).get(keyMapFreqs);
        }
        
        return docLength;
    }

    @Override
    public double getDocByteSize(int docId) {
        return mCorpus.getFileSize(docId);
    }

    @Override
    public double getAvgTftd(int docId) {
        double tf_td = 0;
        
        for (String keyMapFreqs: mDocTermFrequencies.get(docId).keySet()) {
            tf_td += mDocTermFrequencies.get(docId).get(keyMapFreqs);
        }
        
        return tf_td / mDocTermFrequencies.get(docId).keySet().size();
    }

    @Override
    public double getAvgDocLength() {
        double docLengthSum = 0;
        
        for (Integer keyMapDocs: mDocTermFrequencies.keySet()) {
            for (String keyMapFreqs: mDocTermFrequencies.get(keyMapDocs).keySet()) {
                docLengthSum += mDocTermFrequencies.get(keyMapDocs).get(keyMapFreqs);
            }
        }
        
        return docLengthSum / (double) mDocTermFrequencies.keySet().size();
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
