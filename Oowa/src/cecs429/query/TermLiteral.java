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
	
	public TermLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
            // Override default processor
            //      No hyphen split/remove
            
            //System.out.println("\u001B[31m" + "====== TermLiteral.getPostings() ======" + "\u001B[0m");
            //System.out.println("\t" + processor.processToken(mTerm));
            
            List<Posting> result = new ArrayList();
            
            for (String s: processor.processToken(mTerm)) {
                result = unionMergePostings(index.getPostings(s), result);
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
		return mTerm;
	}
}
