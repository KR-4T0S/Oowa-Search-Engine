package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
            //System.out.println("\u001B[31m" + "====== OrQuery.getPostings() ======" + "\u001B[0m");
            //System.out.println("\t" + mComponents.toString());
            List<Posting> result = new ArrayList();

            // For each component retrieved, merge results to one list of results
            // Only merge if postings does not already exist
            for (QueryComponent component: mComponents ) {
                //component.getPostings(index, processor)
                List<Posting> componentResults = new ArrayList(component.getPostings(index, processor));
                //for (String s: processor.processToken(component.toString())) {
                //    componentResults = unionMergePostings(component.getPostings(index, processor), componentResults);
                //}
                result = unionMergePostings(result, componentResults);
            }
                
            return result;
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
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
