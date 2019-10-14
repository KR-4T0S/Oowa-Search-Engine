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
        
        
        // TODO: Create Vocab Table
        
        
    }
    
    private List<Long> writePostings(Index index, Path path) throws FileNotFoundException, IOException {
        ArrayList<Long> result = new ArrayList<>();
        
        // Output file
        File binPostings = new File(String.valueOf(path) + "/index/postings.bin");
        // We don't want to create file every time
        if (!binPostings.getParentFile().mkdirs()) {
            System.out.println("Error: Posting file exists.");
            return result;
        }
        
        // Set File stream vars
        FileOutputStream fileStream = new FileOutputStream(binPostings);
        DataOutputStream postingsStream = new DataOutputStream(fileStream);
        
        // Start writing postings
        for (String vocab: index.getVocabulary()) {
            List<Posting> postings = index.getPostings(vocab);
            
            // *** dft
            postingsStream.writeInt(postings.size());
            Long postingPos = fileStream.getChannel().size() - 4;
            //System.out.println(postingPos);
            
            // location on vocabTable 
            result.add(postingPos);
            
            // *** id_d
            int prevId = 0; // for gap
            for (Posting doc : postings) {
                int docId = doc.getDocumentId();
                
                postingsStream.write(docId - prevId);
                //System.out.println(docId + " | " + prevId + " | " + (docId - prevId));        
                prevId = docId;
                
                
                List<Integer> positions = doc.getPositions();
                
                // *** tf_t
                postingsStream.write(positions.size());
                
                // *** pi
                int prevPos = 0; // for gap
                for (Integer pos: positions) {
                    postingsStream.write(pos - prevPos);
                    prevPos = pos;
                }
            }
        }
        
        // Close streams
        fileStream.close();
        postingsStream.close();
        
        return result;
    }
    
    // Write Vocab
    
    
    // Write Vocab Table
    
}
