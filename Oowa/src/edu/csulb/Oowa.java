package edu.csulb;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.text.*;
import cecs429.query.*;
import java.io.IOException;
import java.io.StringReader;

import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class Oowa {
    
    // Integer Constants
    public static final int MAX_VOCAB = 1000;
    
    // String Constants
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_ITALIC = "\u001B[3m";
    public static final String ANSI_BOLD = "\u001B[1m";

    public static void main(String[] args) throws IOException {
        // Variables for input/query
        String query, directory;
        Scanner inputQuery = new Scanner(System.in);
        Scanner inputDirectory = new Scanner(System.in);

        // Prompt for directory
        System.out.print("Enter directory: ");
        directory = inputDirectory.nextLine();

        // Load corpus
        //DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("D:\\test"), ".json");
        //DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("C:\\Users\\RICHIE\\Desktop\\CECS 429\\JsonSeparator\\jsonfiles"), ".json");
        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".json");
        Index index = startIndex(corpus);

        // Init menu 
        do {
            System.out.print("[" + ANSI_RED + ":q " + ANSI_RESET + " → Quits Program] ");
            System.out.print("[" + ANSI_RED + ":stem " + ANSI_RESET
                    + ANSI_BOLD + "word" + ANSI_RESET + " → Quits Program] ");
            System.out.print("[" + ANSI_RED + ":index " + ANSI_RESET
                    + ANSI_BOLD + "directoryname" + ANSI_RESET + " → Index new directory] ");
            System.out.print("[" + ANSI_RED + ":vocab " + ANSI_RESET
                    + " → View vocab (first 1000)] ");
            
            // Start prompt for word
            System.out.print("\nSearch:\t");
            query = inputQuery.nextLine();
            System.out.println();

            String choiceCommand = "";
            String choiceParameter = "";
            // Detect if it's command
            if (query.charAt(0) == ':' && !query.equals(":q")) {
                if (query.indexOf(' ') >= 0) {
                    choiceCommand = query.substring(0, query.indexOf(' '));
                } else {
                    choiceCommand = query;
                }
            }

            if (!query.equals(":q")) {
                if (choiceCommand.equals(":stem")) {
                    choiceParameter = query.substring(query.indexOf(' ') + 1);
                    System.out.println("\tOriginal: " + choiceParameter);
                    System.out.println("\tStemmed: " + stemmer(choiceParameter) + "\n");
                } else if (choiceCommand.equals(":index")) {
                    choiceParameter = query.substring(query.indexOf(' ') + 1);
                    corpus = DirectoryCorpus.loadTextDirectory(Paths.get(choiceParameter), ".json");
                    index = startIndex(corpus);
                } else if (choiceCommand.equals(":vocab")) {
                    printVocab(index);
                } else {
                    getResults(query, index, corpus);
                }
            }
        } while (!query.equals(":q"));
    }

    private static Index startIndex(DocumentCorpus corpus) {
        // Start tracking time for indexing
        long startTime = System.currentTimeMillis();
        System.out.println("\nIndexing...");

        // Create index
        Index index = indexCorpus(corpus);

        // record total time for indexing
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        System.out.println("\n== Indexing time: " + duration + " ms / " + duration / 1000.0 + " s ==");

        return index;
    }

    private static void getResults(String query, Index index, DocumentCorpus corpus) throws IOException {
        // Init query parsing component
        BooleanQueryParser queryParser = new BooleanQueryParser();
        QueryComponent queryComponent = queryParser.parseQuery(query);
        // Default token processor
        TokenProcessor tokenProcessor = new SimpleTokenProcessor();

        // TODO: remove try-catch once fixed
        try {
            List<Posting> results = queryComponent.getPostings(index, tokenProcessor);

            if (results.isEmpty()) {
                System.out.println("\tNo Results..." + "\n\n");
            } else {
                int counter = 0;
                for (Posting p : results) {
                    counter++;
                    System.out.println("\t[ID:" + p.getDocumentId() + "] " + corpus.getDocument(p.getDocumentId()).getTitle());
                }

                System.out.println("\nTotal Results: " + counter + "\n\n");

                // Prompt to view doc
                String view;
                Scanner inputView = new Scanner(System.in);

                System.out.print("View Document [ID / N]: ");
                view = inputView.nextLine();

                if (!view.toLowerCase().equals("n")) {
                    int docId = Integer.parseInt(view);
                    StringReader reader = (StringReader) corpus.getDocument(docId).getContent();
                    int intValueOfChar;
                    String docBody = "";
                    // Read reader stream
                    int charCounter = 0;
                    while ((intValueOfChar = reader.read()) != -1) {
                        if ((charCounter >= 100 && ((char) intValueOfChar) == ' ')) {
                            intValueOfChar = 10;
                            charCounter = 0;
                        }
                        docBody += (char) intValueOfChar;
                        charCounter++;
                    }
                    reader.close();
                    System.out.println("------------");
                    //docBody = docBody.replaceAll("(.{100})", "$1\n");
                    System.out.println("\nTitle: " + corpus.getDocument(docId).getTitle() + "\n");
                    System.out.println("\n" + docBody + "\n");
                }
            }
            System.out.println("------------");

        } catch (Exception e) {
            System.out.println("\u001B[31m" + e + "\u001B[0m");
        }
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
        for (Document d : docs) {
            int currentPosition = 0;
            EnglishTokenStream stream = new EnglishTokenStream(d.getContent());
            Iterable<String> tokens = stream.getTokens();

            for (String s : tokens) {
                //System.out.println("\t Term: " + processor.processToken(s) + " | " + d.getId());
                invertedIndex.addTerm(processor.processToken(s), d.getId(), currentPosition);
                currentPosition++;
            }
        }

        return invertedIndex;
    }

    private static String stemmer(String str) {
        SnowballStemmer snowballStemmer = new englishStemmer();
        snowballStemmer.setCurrent(str);
        snowballStemmer.stem();
        String result = snowballStemmer.getCurrent();

        return result;
    }

    private static void printVocab(Index index) {
        List<String> vocab = index.getVocabulary();
        System.out.println("Vocabulary: ");
        int i = 0;
        while (i < vocab.size() && i < MAX_VOCAB) {
            System.out.println("\t" + (i+1) + ": \t" + vocab.get(i));
            i++;
        }
//        for (int i = 0; i < 1000; i++) {
//            System.out.println("\t" + (i+1) + ": \t" + vocab.get(i));
//        }
        System.out.println("\nTotal Vocabulary: " + vocab.size() + "\n");
    }
}
