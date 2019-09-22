package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream; 

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

public class InvertedIndexer {
    public static void main(String[] args) {
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
                Index index = indexCorpus(corpus);
                
                // Variables for input/search
                String query;
                Scanner input = new Scanner(System.in);

		do {
                    // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
                    System.out.println("===== Simple Search Engine =====");
                    System.out.println("[\'quit\' to exit program]");
                    
                    // Start prompt for word
                    System.out.print("Search:\t");
                    query = input.nextLine();
                    //query = query.toLowerCase();
                    
                    if (!query.equals("quit")) {
                        for (Posting p : index.getPostings(query)) {
                                System.out.println("\tDocument " + corpus.getDocument(p.getDocumentId()).getTitle());
                        }
                    }
                } while(!query.equals("quit"));
                  
	}
    
    private static Index indexCorpus(DocumentCorpus corpus) {
		//HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		InvertedIndex invertedIndex = new InvertedIndex();
                
		// Get all the documents in the corpus by calling GetDocuments().
                Iterable<Document> docs = corpus.getDocuments();
                
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the Inverted Index
		for (Document d: docs) {
                    EnglishTokenStream stream = new EnglishTokenStream(d.getContent());
                    Iterable<String> tokens = stream.getTokens();
                    
                    System.out.println("Title: " + d.getTitle() + " \t| ID: " + d.getId());
                    for (String s: tokens) {
                        //vocabulary.add(processor.processToken(s));
                        invertedIndex.addTerm(processor.processToken(s), d.getId());
                    }
                 }
                
		return invertedIndex;
	}
}
