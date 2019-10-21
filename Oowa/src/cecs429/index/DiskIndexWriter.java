package cecs429.index;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class DiskIndexWriter {
    
    public void WriteIndex(Index index, Path path) throws FileNotFoundException, IOException {
        
        // TODO: Create Postings
        List<Long> postings = writePostings(index, path);
        
        // TODO: Create Vocab
        List<Long> vocab = writeVocab(index, path);
        
        
        // TODO: Create Vocab Table
        writeTable(postings, vocab, path);
        
    }
    
    private List<Long> writePostings(Index index, Path path) throws FileNotFoundException, IOException {
        ArrayList<Long> result = new ArrayList<>();
        
        // Output file
        File binPostings = new File(String.valueOf(path) + "\\index\\postings.bin");
        // Create directory
        binPostings.getParentFile().mkdirs();
        
        if (binPostings.exists()) {
            //System.out.println("postings.bin already exsits");
            return result;
        }
        
        // Set File stream vars
        FileOutputStream fileStream = new FileOutputStream(binPostings);
        DataOutputStream postingsStream = new DataOutputStream(fileStream);
        
        // Start writing postings
        for (String term: index.getVocabulary()) {
            List<Posting> postings = index.getPostings(term);
            //System.out.print("\n\"" + term + "\": ");
            
            // *** dft
            postingsStream.writeInt(postings.size());
            //System.out.print("dft: " + postings.size() + " | ");
            
            // Starting pos of term for table
            Long postingPos = fileStream.getChannel().size() - 4; 
            result.add(postingPos);
            //System.out.print(" (pos: " + postingPos + ")");
            
            // *** id_d
            int prevId = 0; // for gap
            for (Posting doc : postings) {
                int docId = doc.getDocumentId();
                
                postingsStream.writeInt(docId - prevId);
                //System.out.print("\n\tid_d: " + (docId - prevId) + " | ");
                prevId = docId;
                
                
                List<Integer> positions = doc.getPositions();
                
                // *** tf_t
                postingsStream.writeInt(positions.size());
                //System.out.print("\n\t\ttf_t: " + positions.size() + " | ");
                
                // *** pi
                int prevPos = 0; // for gap
                //int ctr = 0;
                for (Integer pos: positions) {
                    postingsStream.writeInt(pos - prevPos);
                    //System.out.print("p_" + ctr + ": " + (pos - prevPos) + " ");
                    prevPos = pos;
                    //ctr++;
                }
            }
            postingsStream.flush();
        }
        
        // Close streams
        postingsStream.close();
        fileStream.close();
        
        return result;
    }
    
    // Write Vocab
    private List<Long> writeVocab(Index index, Path path) throws FileNotFoundException, IOException{
        ArrayList<Long> result = new ArrayList<>();
        
        // Output file
        File binVocab = new File(String.valueOf(path) + "\\index\\vocab.bin");
        
        // Create directory
        binVocab.getParentFile().mkdirs();
        
        if (binVocab.exists()) {
            //System.out.println("vocab.bin already exsits");
            return result;
        }
        
        // Set File stream vars
        FileOutputStream fileStream = new FileOutputStream(binVocab);
        DataOutputStream vocabStream = new DataOutputStream(fileStream);
        
        for (String term: index.getVocabulary()) {
            //System.out.print("\n\"" + term + "\": ");
            // Writes term in binary format
            vocabStream.writeBytes(term);
            
            // Starting pos of term for table
            Long vocabPos = fileStream.getChannel().size() - term.length();
            //System.out.print(vocabPos + "\n");
            result.add(vocabPos);
            vocabStream.flush();
        }
        vocabStream.close();
        fileStream.close();
        
        return result;
    }
    
    
    // Write Vocab Table
    public void writeTable(List<Long> postingsPos, List<Long> vocabPos, Path path) throws FileNotFoundException, IOException {
        ArrayList<Long> result = new ArrayList<>();
        
        // Output file
        File binTable = new File(String.valueOf(path) + "\\index\\vocabTable.bin");
        // Create directory
        binTable.getParentFile().mkdirs();
        
        if (binTable.exists()) {
            //System.out.println("vocabTable.bin already exsits");
            return;
        }
        
        // Set File stream vars
        FileOutputStream fileStream = new FileOutputStream(binTable);
        DataOutputStream tableStream = new DataOutputStream(fileStream);
        
        // Write [ vocab pos ] [ posting pos]
        for (int i = 0; i < postingsPos.size(); i++) {
            //System.out.println("Vocab: " + vocabPos.get(i) + " | Postings: " + postingsPos.get(i));
            tableStream.writeLong(vocabPos.get(i));
            tableStream.writeLong(postingsPos.get(i));
            tableStream.flush();
        }
        
        tableStream.close();
        fileStream.close();
    }
}
