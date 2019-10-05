package edu.csulb;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.text.*; 
import cecs429.query.*;

import java.nio.file.Paths;
import java.util.Scanner;

public class Oowa {
    public static void main(String[] args) {
                // Variables for input/query
                String query, directory;
                Scanner inputQuery = new Scanner(System.in);
                Scanner inputDirectory = new Scanner(System.in);
                
                // Prompt for directory
                //System.out.println("Enter directory: \t");
                //directory = inputDirectory.nextLine();
                
                
                long startTime = System.currentTimeMillis();
                
                // Load corpus
                DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("D:\\test"), ".json");
                Index index = indexCorpus(corpus);
                
                
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                
                System.out.println("== Time to index: " + duration + "ms ==");
                
		do {
                    // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
                    System.out.println("===== Simple Search Engine =====");
                    System.out.println("[\'quit\' to exit program]");
                    
                    // Start prompt for word
                    System.out.print("Search:\t");
                    query = inputQuery.nextLine();
                    
                    if (!query.equals("quit")) {
//                        // Stem query
//                        SnowballStemmer snowballStemmer = new englishStemmer();
//                        snowballStemmer.setCurrent(query);
//                        snowballStemmer.stem();
//                        query = snowballStemmer.getCurrent();
                        
                        getResults(query, index, corpus);
                    }
                } while(!query.equals("quit"));
                  
	}
    
    private static void getResults(String query, Index index, DocumentCorpus corpus) {
            // Init query parsing component
            BooleanQueryParser queryParser = new BooleanQueryParser();
            QueryComponent queryComponent = queryParser.parseQuery(query);
            // Default token processor
            TokenProcessor tokenProcessor = new AdvancedTokenProcessor();
            
            // TODO: remove try-catch once fixed
            try {
                if (queryComponent.getPostings(index, tokenProcessor).isEmpty()) {
                    System.out.println("\tNo Results..." + "\n\n");
                } else {
                    int counter = 0;
                    for (Posting p : queryComponent.getPostings(index, tokenProcessor)) {
                        counter++;
                        System.out.println("\t" + counter + ": [ID:" + p.getDocumentId() + "] " + corpus.getDocument(p.getDocumentId()).getTitle());
                    }
                        System.out.println("Total Results: " + counter + "\n\n");
                    }
            } catch (Exception e) {
                System.out.println("\u001B[31m" + e + "\u001B[0m");
            }
    }
    
    private static Index indexCorpus(DocumentCorpus corpus) {
                long startTime = System.currentTimeMillis();

		//HashSet<String> vocabulary = new HashSet<>();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex();
                
		// Get all the documents in the corpus by calling GetDocuments().
                Iterable<Document> docs = corpus.getDocuments();
                
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the Inverted Index
		for (Document d: docs) {
                    int currentPosition = 0;
                    EnglishTokenStream stream = new EnglishTokenStream(d.getContent());
                    Iterable<String> tokens = stream.getTokens();
                    
                    System.out.println("Title: " + d.getTitle() + " \t| ID: " + d.getId());
                    for (String s: tokens) {
                        //vocabulary.add(processor.processToken(s));
                        currentPosition++;
                        invertedIndex.addTerm(processor.processToken(s), d.getId(), currentPosition);
                    }
                 }
                
		return invertedIndex;
	}
}
