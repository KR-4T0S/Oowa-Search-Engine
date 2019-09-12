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
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		
		// TODO:
		// Get all the documents in the corpus by calling GetDocuments().
                Iterable<Document> docs = corpus.getDocuments();
                
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.
		for (Document d: docs) {
                    System.out.println("Title: " + d.getTitle() + " \t| ID: " + d.getId());
                    EnglishTokenStream stream = new EnglishTokenStream(d.getContent());
                    Iterable<String> tokens = stream.getTokens();
                    for (String s: tokens) {
                        vocabulary.add(processor.processToken(s));
                    }
                 }
                
		// TODO:
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
                InvertedIndex invertedIndex = new InvertedIndex(vocabulary);
                
		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.
                for (Document d: docs) {
                    EnglishTokenStream stream = new EnglishTokenStream(d.getContent());
                    Iterable<String> tokens = stream.getTokens();
                    
                    for (String s: tokens) {
                        invertedIndex.addTerm(processor.processToken(s), d.getId());
                    }
                 }
                
		return invertedIndex;
	}
}
