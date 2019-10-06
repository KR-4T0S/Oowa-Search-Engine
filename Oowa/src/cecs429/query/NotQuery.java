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
public class NotQuery implements QueryComponent {

    private List<QueryComponent> mComponents;

    public NotQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        //System.out.println("\u001B[31m" + "====== AndQuery.getPostings() ======" + "\u001B[0m");
        //System.out.println("\t" + mComponents.toString());

        List<Posting> result = new ArrayList();

        // For each component retrieved, merge results to one list of results
        // Only merge similar postings
        //      First term to query gets added to results for union merge
        //          Prevents Intersect merging with empty array
        result = mComponents.get(0).getPostings(index, processor);

        // Now merge rest of component results
        for (int i = 1; i < mComponents.size(); i++) {
            // Get all possible index results due to token processing
            List<Posting> tempComponentResults = mComponents.get(i).getPostings(index, processor);

            result = intersectMergePostings(result, tempComponentResults);
        }

        return result;
    }

    private List<Posting> intersectMergePostings(List<Posting> listA, List<Posting> listB) {
        List<Posting> listIntersect = new ArrayList(); // Placeholder List

        if (!listA.isEmpty() || !listB.isEmpty()) { // Neither list can be empty
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) {
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) {
                    j++;
                } else {
                    listIntersect.add(listA.get(i));
                    i++;
                    j++;
                }
            }
        }

        return listIntersect;
    }

    private List<Posting> unionMergePostings(List<Posting> listA, List<Posting> listB) {
        List<Posting> listUnion = new ArrayList(); // Placeholder List

        if (listA.isEmpty()) { // no need for merge algorithm
            listUnion.addAll(listB);
        } else if (listB.isEmpty()) {
            listUnion.addAll(listA);
        } else { // Union merge algorithm
            int i = 0, j = 0;
            while (i < listA.size() && j < listB.size()) {
                if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) {
                    listUnion.add(listA.get(i));
                    i++;
                } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) {
                    listUnion.add(listB.get(j));
                    j++;
                } else {
                    listUnion.add(listA.get(i));
                    i++;
                    j++;
                }
            }

            // Now add the remaining components of the larger array.
            // Add rest of items of component results
            while (i < listA.size()) {
                listUnion.add(listA.get(i));
                i++;
            }
            // Add rest of items of component results
            while (j < listB.size()) {
                listUnion.add(listB.get(j));
                j++;
            }
        }

        return listUnion;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
