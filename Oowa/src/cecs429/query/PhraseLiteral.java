package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.*;
import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
            // Override default processor
            //      No hyphen split/remove
            processor = new SimpleTokenProcessor();
            
            System.out.println("\u001B[31m" + "====== PhraseLiteral.getPostings() ======" + "\u001B[0m");
            System.out.println(processor.processToken(mTerms.toString()));
            
            List<Posting> result = new ArrayList();
            
            // First term to query gets added to results for union merge
            for (String s: processor.processToken(mTerms.get(0))) {
                result = unionMergePostings(result, index.getPostings(s));
            }
            
            // Now the rest of terms
            for (int i = 1; i < mTerms.size(); i++) {
                // Get all possible index results due to token processing
                List<Posting> tempComponentResults = new ArrayList();
                for (String s: processor.processToken(mTerms.get(i))) {
                    tempComponentResults = unionMergePostings(tempComponentResults, index.getPostings(s));
                }
                
                result = positionalIntersectMergePostings(result, tempComponentResults);
            }
            
            return result;
	}
        
        private List<Posting> positionalIntersectMergePostings(List<Posting> listA, List<Posting> listB) {
            List<Posting> listIntersect = new ArrayList(); // Placeholder List
            
            if (!listA.isEmpty() || !listB.isEmpty()) { // Neither list can be empty
                int i = 0, j = 0;
                while (i < listA.size() && j < listB.size()) {
                    if (listA.get(i).getDocumentId() < listB.get(j).getDocumentId()) {
                        i++;
                    } else if (listA.get(i).getDocumentId() > listB.get(j).getDocumentId()) {
                        j++;
                    } else { // Do Positional test here
                        List<Integer> positionsA = listA.get(i).getPositions();
                        List<Integer> positionsB = listB.get(j).getPositions();
                        System.out.println(listA.get(i).getDocumentId());
                        System.out.println("\tk: " + positionsA.toString());
                        System.out.println(listA.get(i).getDocumentId());
                        System.out.println("\tl: " + positionsB.toString());
                        System.out.println();
                        
                        int k = 0; // Pos A
                        int l = 0; // Pos B
                        
                        while (k < positionsA.size() && l < positionsB.size()) {
                            if ((positionsA.get(k) < positionsB.get(l))) {
                                // First word behind second word
                                if ((positionsB.get(l) - positionsA.get(k) == 1)) {
                                    listIntersect.add(listA.get(i));
                                    break;
                                } else {
                                    k++;
                                }
                            } else if (positionsA.get(k) > positionsB.get(l)) {
                                // Second word behind first word
                                if ((positionsA.get(k) - positionsB.get(l) == 1)) {
                                    listIntersect.add(listA.get(i));
                                    break;
                                } else {
                                    l++;
                                }
                            } else {
                                k++;
                                l++;
                            }
                        }
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
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
