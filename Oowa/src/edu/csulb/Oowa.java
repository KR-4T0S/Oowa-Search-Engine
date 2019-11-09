package edu.csulb;

import cecs429.documents.*;
import cecs429.index.*;
import cecs429.text.*;
import cecs429.query.*;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class Oowa {
    
    // Integer Constants
    public static final int MAX_VOCAB = 1000;
    public static final int MAX_RANKED_RESULTS = 10;
    public static final int MAX_DOC_LINE_SIZE = 100;
    
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
    public static final String QUERY_MODE_BOOLEAN = "1";
    public static final String QUERY_MODE_RANKED = "2";
    public static final String WEIGHT_DEFAULT = "1";
    public static final String WEIGHT_TRADITIONAL = "2";
    public static final String WEIGHT_OKAPI = "3";
    public static final String WEIGHT_WACKY = "4";

    public static void main(String[] args) throws IOException {
        // Variables for input
        String directory;
        Scanner inputDirectory = new Scanner(System.in);

        // Prompt for directory
        System.out.print("Enter directory: ");
        directory = inputDirectory.nextLine();

        // Load corpus
        menu(directory);
    }
    
    public static void menu(String directory) throws IOException {
        // Variables for input
        String modeQuery, modeIndex;
        String weightMode = WEIGHT_DEFAULT;
        Scanner inputModeIndex = new Scanner(System.in);
        Scanner inputModeQuery = new Scanner(System.in);

        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".json");
        corpus.getDocuments();
        
        // Init index
        Index index = null;
        
        // Variables for input
        String query;
        Scanner inputQuery = new Scanner(System.in);
        
        // Prompt for Indexing Mode
        System.out.print("Start [ 1 = Build Index | 2 = Query Index ]: ");
        modeIndex = inputModeIndex.nextLine();

        if (modeIndex.equals("1")) {
            index = startIndex(corpus, Paths.get(directory));
        } else {
            index = new DiskPositionalIndex(directory);
            
            // Prompt for Querying Mode
            System.out.print("Choose Mode [ 1 = Boolean | 2 = Ranked ]: ");
            modeQuery = inputModeQuery.nextLine();

            do {
                System.out.print("[" + ANSI_RED + ":q " + ANSI_RESET + " → Quits Program] ");
                System.out.print("[" + ANSI_RED + ":stem " + ANSI_RESET
                        + ANSI_BOLD + "word" + ANSI_RESET + " → Quits Program] ");
                System.out.print("[" + ANSI_RED + ":index " + ANSI_RESET
                        + ANSI_BOLD + "directoryname" + ANSI_RESET + " → Index new directory] ");
                System.out.print("[" + ANSI_RED + ":vocab " + ANSI_RESET
                        + " → View vocab (first 1000)] ");
                System.out.print("\n[" + ANSI_RED + ":mode " + ANSI_RESET
                        + " → Toggle between Boolean (1) or Ranked (2)] ");
                if (modeQuery.equals("2")) {
                    System.out.println("\n[" + ANSI_RED + ":w [1 = Default | 2 = tf-idf | 3 = Okapi BM25 | 4 = Wacky] " + ANSI_RESET
                        + " → Change weight scheme] ");
                    System.out.println("Current Weight Scheme: " + weightMode);
                }

                // Start prompt for word
                System.out.print("\nSearch:\t");
                query = inputQuery.nextLine();
                System.out.println();

                String choiceCommand = "";
                String choiceParameter = "";
                // Detect if it's command
                if (query.charAt(0) == ':' && !query.equals(":q") && query.length() > 0) {
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
                        // Change to new directory
                        directory = choiceParameter;
                        // Load corpus from new directory
                        corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory), ".json");
                        corpus.getDocuments(); // Loads files from corpus for callback.
                        // Creates/Gets index from given directory.
                        index = startIndex(corpus, Paths.get(directory));
                    } else if (choiceCommand.equals(":vocab")) {
                        // This will grab vocab right from vocab.bin
                        printVocab(index);
                    } else if (choiceCommand.equals(":mode")) {
                        // Just toggle mode. Nothing else need sto change here.
                        if (modeQuery.equals(QUERY_MODE_BOOLEAN)) {
                            modeQuery = QUERY_MODE_RANKED;
                        } else {
                            modeQuery = QUERY_MODE_BOOLEAN;
                        }
                    } else {
                        // Conditional querying, depending on query mode.
                        if (modeQuery.equals(QUERY_MODE_BOOLEAN)) {
                            getResultsBoolean(query, index, corpus);
                        } else {
                            // Weight formula option for Ranked query.
                            if (choiceCommand.equals(":w")) {
                                choiceParameter = query.substring(query.indexOf(' ') + 1);
                                weightMode = choiceParameter;
                            } else { // Get results given current weight mode.
                                getResultsRanked(query, index, corpus, weightMode);
                            }
                        }
                    }
                }
            } while (!query.equals(":q"));
        }
        
    }
    
    private static void getResultsBoolean(String query, Index index, DocumentCorpus corpus) {
        // Init query parsing component
        BooleanQueryParser queryParser = new BooleanQueryParser();
        QueryComponent queryComponent = queryParser.parseQuery(query);
        // Default token processor
        TokenProcessor tokenProcessor = new SimpleTokenProcessor();

        try {
            List<Posting> results = queryComponent.getPostings(index, tokenProcessor);

            // Don't do anything if there's no results.. duh
            if (results.isEmpty()) {
                System.out.println("\tNo Results..." + "\n\n");
            } else {
                int counter = 0;
                // Console output format for every result.
                for (Posting p : results) {
                    counter++;
                    System.out.println("\t[ID:" + p.getDocumentId() + "] " 
                            + corpus.getDocument(p.getDocumentId()).getTitle()
                    );
                }

                System.out.println("\nTotal Results: " + counter + "\n\n");

                readDocument(corpus);
            }
            System.out.println("------------");

        } catch (Exception e) {
            System.out.println("\u001B[31m" + e + "\u001B[0m");
        }
    }
    
    public static void getResultsRanked(String query, Index index, DocumentCorpus corpus, String weightMode) {
        TokenProcessor tokenProcessor = new SimpleTokenProcessor();
        WeightStrategy strategy = null;

        // Just consider terms by space character.
        String[] terms = query.split("\\s+");

        // Heap for results.
        PriorityQueue<Accumulator> results = new PriorityQueue(); 
        
        // Use strategy depending on weightMode param
        switch(weightMode) {
            case (WEIGHT_TRADITIONAL):
                strategy = new OperationWeightTFIDF();
                break;
            case (WEIGHT_OKAPI):
                strategy = new OperationWeightOkapi();
                break;
            case (WEIGHT_WACKY):
                strategy = new OperationWeightWacky();
                break;
            default:
                strategy = new OperationWeightDefault();               
        }
        
        try {
            WeightStrategyContext context = new WeightStrategyContext(strategy);
            results = context.get(index, tokenProcessor, corpus.getCorpusSize(), terms);
            
            // Don't do anything if heap is empty
            if (results.isEmpty()) {
                System.out.println("\tNo Results..." + "\n\n");
            } else {
                // Either print MAX results if heap has that or more.
                // or just as many results as heap contains.
                int K = Math.min(MAX_RANKED_RESULTS, results.size());
                for (int i = 0; i < K; i++) {
                    Accumulator acc = results.poll(); // Pop and Get greatest value from heap.
                    Posting p = acc.getPosting(); // Retrieve Posting from the Accumulator
                    // Format result for console output.
                    System.out.println("\t[ID:" + p.getDocumentId() + "] " 
                            + corpus.getDocument(p.getDocumentId()).getTitle()
                            + ": "
                            + acc.getScore()
                    );
                }
                System.out.println();
                System.out.println("\nTotal Results: " + K + "\n\n");
                
                
                readDocument(corpus);
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m" + e + "\u001B[0m");
        }

    }

    private static void readDocument(DocumentCorpus corpus) throws IOException {
        // Prompt to view doc
        String input;
        Scanner inputView = new Scanner(System.in);

        System.out.print("View Document [ID / N]: ");
        input = inputView.nextLine();

        // If user wants to read
        if (!input.toLowerCase().equals("n")) {
            int docId = Integer.parseInt(input);
            StringReader reader = (StringReader) corpus.getDocument(docId).getContent();
            int intValueOfChar;
            String docBody = "";
            // Read reader stream
            int charCounter = 0;
            // This'll create an artificial text-wrap for document's content.
            while ((intValueOfChar = reader.read()) != -1) {
                if ((charCounter >= MAX_DOC_LINE_SIZE && ((char) intValueOfChar) == ' ')) {
                    intValueOfChar = 10;
                    charCounter = 0;
                }
                docBody += (char) intValueOfChar;
                charCounter++;
            }
            reader.close();
            System.out.println("------------");
            System.out.println("\nTitle: " + corpus.getDocument(docId).getTitle() + "\n");
            System.out.println("\n" + docBody + "\n");
        }
    }
    
    private static Index startIndex(DocumentCorpus corpus, Path path) throws IOException {
        DiskIndexWriter writer = new DiskIndexWriter();
        // If the index is already written, just use that.
        if (writer.exists(path)) {
            //corpus.getDocuments();
            return new DiskPositionalIndex(path.toString());
        }
        
        // Start tracking time for indexing
        long startTimeIndex = System.currentTimeMillis();
        System.out.println("\nIndexing...");

        // Create index
        Index index = indexCorpus(corpus);

        // record total time for indexing
        long endTimeIndex = System.currentTimeMillis();
        long durationIndex = (endTimeIndex - startTimeIndex);
        
        System.out.println("\n== Indexing time: " + durationIndex + " ms / " + durationIndex / 1000.0 + " s ==");
        
        // Write Index
        long startTimeWrite = System.currentTimeMillis();
        System.out.println("\nWriting to disk...");

        writer.WriteIndex(index, path);

        // record total time for writing
        long endTimeWrite = System.currentTimeMillis();
        long durationWrite = (endTimeWrite - startTimeWrite);

        System.out.println("\n== Writing time: " + durationWrite + " ms / " + durationWrite / 1000.0 + " s ==");

        return index;
    }
    
    private static Index indexCorpus(DocumentCorpus corpus) {
        //HashSet<String> vocabulary = new HashSet<>();
        AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
        PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex(corpus);

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
            System.out.println("\t" + (i+1) + ": " + vocab.get(i));
            i++;
        }

        System.out.println("\nTotal Vocabulary: " + vocab.size() + "\n");
    }
}
