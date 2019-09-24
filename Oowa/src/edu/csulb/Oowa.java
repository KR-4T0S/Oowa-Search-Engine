package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream; 

import java.nio.file.Paths;
import java.util.Scanner;

public class Oowa {
    public static void main(String[] args) {
		
                
                // Variables for input/search
                String query, directory;
                Scanner inputQuery = new Scanner(System.in);
                Scanner inputDirectory = new Scanner(System.in);
                
                // Prompt for directory
                //System.out.println("Enter directory: \t");
                //directory = inputDirectory.nextLine();
                
                // Load corpus
                //DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".json");
                DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("D:\\test"), ".json");
                Index index = indexCorpus(corpus);
                
		do {
                    // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
                    System.out.println("===== Simple Search Engine =====");
                    System.out.println("[\'quit\' to exit program]");
                    
                    // Start prompt for word
                    System.out.print("Search:\t");
                    query = inputQuery.nextLine();
                    //query = query.toLowerCase();
                    
                    if (!query.equals("quit")) {
                        if (index.getPostings(query).isEmpty()) {
                            System.out.println("\tNo Results! :(");
                        } else {
                            System.out.println("\t" + index.getPostings(query).size() + " Total Results: ");
                            int counter = 0;
                            for (Posting p : index.getPostings(query)) {
                                counter++;
                                System.out.println("\t\t" + counter + ": " + corpus.getDocument(p.getDocumentId()).getTitle());
                            }
                        }
                    }
                } while(!query.equals("quit"));
                  
	}
    
    private static Index indexCorpus(DocumentCorpus corpus) {
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
