package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {


    private List<String> mTerms = new ArrayList<>();
    private boolean mIsPositive;
    
    /**
     * Constructs a PhraseLiteral with the given individual phrase terms.
     */
    public PhraseLiteral(List<String> terms, boolean isPositive) {
        mTerms.addAll(terms);
        mIsPositive = isPositive;
    }

    /**
     * Constructs a PhraseLiteral given a string with one or more individual
     * terms separated by spaces.
     */
    public PhraseLiteral(String terms) {
        mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        
        List<Posting> result = new ArrayList();

        // First term to query gets added to results for union merge
        for (String s : processor.processToken(mTerms.get(0))) {
            result = unionMergePostings(result, index.getPostings(s));
        }

        // Now the rest of terms
        if (mTerms.size() > 1) {
            for (int i = 1; i < mTerms.size(); i++) {
                // Get all possible index results due to token processing
                List<Posting> tempComponentResults = new ArrayList();
                for (String s : processor.processToken(mTerms.get(i))) {
                    tempComponentResults = unionMergePostings(tempComponentResults, index.getPostings(s));
                }

                result = positionalIntersect(result, tempComponentResults, i);
            }
        }

        return result;
    }
    
    private List<Posting> positionalIntersect(List<Posting> p1, List<Posting> p2, int k) {
        List<Posting> result = new ArrayList();
        
        int i = 0, j = 0;
        while (i < p1.size() && j < p2.size()) {                           
            if (p1.get(i).getDocumentId() == p2.get(j).getDocumentId()) {
                List<Integer> l = new ArrayList();
                List<Integer> pp1 = p1.get(i).getPositions();
                List<Integer> pp2 = p2.get(j).getPositions();
                
                
                // For each of the VALID POSITIONS
                for (int m = 0; m < pp1.size(); m++) {
                    // Compare with each of SECOND SET OF POSITIONS
                    for (int n = 0; n < pp2.size(); n++) {
                        if (pp2.get(n) - pp1.get(m) == k) {
                            l.add(pp1.get(m));
                        }
                    }
                }
//                int m = 0, n = 0;
//                while (m < pp1.size()) {
//                    while (n < pp2.size()) {
//                        System.out.println("\tComparing: " + pp1.get(m) + " | " + pp2.get(n));
//                        if (pp2.get(n) - pp1.get(m) == k) {
//                            //System.out.println("\t\tMatch: " + pp1.get(m) + " | " + pp2.get(n));
//                            l.add(pp1.get(m));
//                        } else if (pp2.get(n) > pp1.get(m)) {
//                            //System.out.println("\t\tNO Match: " + pp1.get(m) + " | " + pp2.get(n));
//                            break;
//                        }
//                        n++;
//                    }
//                    m++;
//                }
                
                if (!l.isEmpty()) {
                    Posting tempPosting = new Posting(p1.get(i).getDocumentId());
                    for (Integer ps: l) {
                        tempPosting.addPos(ps);
                    }
                    result.add(tempPosting);
                }
                
                i++;
                j++;
            } else if (p1.get(i).getDocumentId() < p2.get(j).getDocumentId()) {
                i++;
            } else {
                j++;
            }
        }
        
        return result;
    }
    
    private List<Posting> unionMergePostings(List<Posting> listA, List<Posting> listB) {
        List<Posting> result = new ArrayList(); // Placeholder List

        if (listA.isEmpty()) { // no need for merge algorithm
            result.addAll(listB);
        } else if (listB.isEmpty()) {
            result.addAll(listA);
        } else { // Union merge algorithm
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) {
                    result.add(listA.get(i));
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) {
                    result.add(listB.get(j));
                    j++;
                } else {
                    result.add(listA.get(i));
                    i++;
                    j++;
                }
            }

            // Now add the remaining components of the larger array.
            // Add rest of items of component results
            while (i < listA.size()) {
                result.add(listA.get(i));
                i++;
            }
            // Add rest of items of component results
            while (j < listB.size()) {
                result.add(listB.get(j));
                j++;
            }
        }

        return result;
    }
    
    public void setPositive(boolean value) {
        mIsPositive = value;
    }

    public boolean isPositive() {
        return mIsPositive;
    }
    
    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }
}
