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
        System.out.println("\u001B[31m" + "====== NotQuery.getPostings() ======" + "\u001B[0m");
        System.out.println("\t" + mComponents.toString());

        List<Posting> result = new ArrayList();
        
        
        
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
