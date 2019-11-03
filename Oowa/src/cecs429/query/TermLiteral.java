package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.*;
import java.util.ArrayList;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {

    private String mTerm;
    private boolean mIsPositive;

    public TermLiteral(String term, boolean isPositive) {
        mTerm = term;
        mIsPositive = isPositive;
    }

    public String getTerm() {
        return mTerm;
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        List<Posting> result = new ArrayList();

        for (String s : processor.processToken(mTerm)) {
            result = unionMergePostings(index.getNonPositionalPostings(s), result);
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
            
            // Add remaining results if listB is larger
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
        return mTerm;
    }
}
