package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {

    private List<QueryComponent> mComponents;

    public AndQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        List<Posting> result = new ArrayList();
        
        boolean firstPos = false;
        int i = 0;
        List<Posting> nots = new ArrayList(); // Temporary list of IDs to remove
        while (i < mComponents.size()) {
            QueryComponent tempComp = mComponents.get(i);
            
            // Until we find the first positive, we want to keep track of all docs to remove
            // Once we have an actual positive, we just want to get difference.
            if (firstPos) { // we had found our first positive
                if (tempComp.isPositive() == true) { // this is positive, just AND merge
                    result = intersectMergePostings(result, tempComp.getPostings(index, processor));
                } else { // not positive, NOT AND merge
                    // Do regular AND NOT merge
                    result = notIntersectMergePostings(result, tempComp.getPostings(index, processor));
                }
            } else { // we haven't found a positive yet
                if (tempComp.isPositive() == true) { // our first positive
                    
                    if (i == 0) { // first positive is first component
                        result = tempComp.getPostings(index, processor);
                    } else { // first positive is not first component
                        result = notIntersectMergePostings(tempComp.getPostings(index, processor), nots);
                    }
                    
                    firstPos = true; // toggle flag
                } else { // still on negatives
                    // Collect all unique IDs to remove
                    nots = unionMergePostings(nots, tempComp.getPostings(index, processor));
                }
            }
            
            // Next component
            i++;
        }
        
        return result;
    }

    private List<Posting> notIntersectMergePostings(List<Posting> listA, List<Posting> listB) {
        List<Posting> result = new ArrayList(); // Placeholder List
        
        if (!listA.isEmpty() || !listB.isEmpty()) { // Neither list can be empty
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) { // List A has element not in B
                    result.add(listA.get(i));
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) { // List B has element not in A
                    j++;
                } else {
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
        }

        return result;
    }
    
    private List<Posting> intersectMergePostings(List<Posting> listA, List<Posting> listB) {
        List<Posting> result = new ArrayList(); // Placeholder List

        if (!listA.isEmpty()) {
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) { // Lowest element in A is not in B
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) { // Lowest element in B is not in A
                    j++;
                } else { // Elements match
                    result.add(listA.get(i));
                    i++;
                    j++;
                }
            }
        }
        
       

        System.out.println("===");
        System.out.print("[");
        for (Posting p: result) {
            System.out.print(p.getDocumentId() + " ");
        }
        System.out.print("]\n");
        
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
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) { // Lowest of all elements is in A
                    result.add(listA.get(i));
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) { // Lowest of all elements is in B
                    result.add(listB.get(j));
                    j++;
                } else { // Both elements have lowest
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
        // null
    }
    
    public boolean isPositive() {
        return true;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
