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
                
                // Returns positions where WORD matches
                result = positionalIntersectMergePostings(result, tempComponentResults);
            }
        }

        return result;
    }

    
    private List<Posting> positionalIntersectMergePostingsv2(List<Posting> listA, List<Posting> listB, int indexOfA, int indexOfB) {
        List<Posting> result = new ArrayList(); // Placeholder List

        if (!listA.isEmpty() || !listB.isEmpty()) { // Neither list can be empty
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) {
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) {
                    j++;
                } else {
                    // Same doc 
                    // Positional test
                    List<Integer> positionsA = listA.get(i).getPositions();
                    List<Integer> positionsB = listB.get(j).getPositions();

                    int k = 0; // Pos A
                    int l = 0; // Pos B
                    // [to, be, or, not, to, be]
                    while ((k < positionsA.size() && l < positionsB.size())) {
                        if ((positionsA.get(k) <= positionsB.get(l))) { // could be same word
                            // A behind B
                            if (positionsB.get(l) - positionsA.get(k) == 1) {
                                result.add(listB.get(j));
                                break;
                            } else {
                                k++;
                            }
                        } else { // Pos of first word is equal to 
                            l++;
                        }
                    }
                    
                    // Did not match, Next Doc
                    i++;
                    j++;
                }
            }
        }

        return result;
    }
    
    private List<Posting> positionalIntersectMergePostings(List<Posting> listA, List<Posting> listB) {
        List<Posting> result = new ArrayList(); // Placeholder List

        if (!listA.isEmpty() || !listB.isEmpty()) { // Neither list can be empty
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) {
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) {
                    j++;
                } else {
                    // Same doc 
                    // Positional test
                    List<Integer> positionsA = listA.get(i).getPositions();
                    List<Integer> positionsB = listB.get(j).getPositions();

                    int k = 0; // Pos A
                    int l = 0; // Pos B
                    // [to, be, or, not, to, be]
                    while ((k < positionsA.size() && l < positionsB.size())) {
                        if ((positionsA.get(k) <= positionsB.get(l))) { // could be same word
                            // A behind B
                            if (positionsB.get(l) - positionsA.get(k) == 1) {
                                result.add(listB.get(j));
                                break;
                            } else {
                                k++;
                            }
                        } else { // Pos of first word is equal to 
                            l++;
                        }
                    }
                    
                    // Did not match, Next Doc
                    i++;
                    j++;
                }
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
