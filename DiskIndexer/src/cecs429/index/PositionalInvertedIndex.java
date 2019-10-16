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

    // Constructor
    public PositionalInvertedIndex() {
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
                LinkedList<Posting> postings = new LinkedList<>();
                postings.add(new Posting(documentId));
                mIndex.put(term, postings);
                mIndex.get(term).getLast().addPos(pos);
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
}
