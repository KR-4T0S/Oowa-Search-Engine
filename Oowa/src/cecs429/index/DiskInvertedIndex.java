package cecs429.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DiskInvertedIndex implements Index {

   private String mPath;
   private List<String> mFileNames;
   private RandomAccessFile mVocabList;
   private RandomAccessFile mPostings;
   private long[] mVocabTable;

    // Opens a disk inverted index that was constructed in the given path.
    public DiskInvertedIndex(String path) {
      try {
         mPath = path + "/index/";
         mVocabList = new RandomAccessFile(new File(mPath, "vocab.bin"), "r");
         mPostings = new RandomAccessFile(new File(mPath, "postings.bin"), "r");
         mVocabTable = readVocabTable(mPath);
         mFileNames = readFileNames(mPath);
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
    }

  

    // Locates the byte position of the postings for the given term.
    // For example, binarySearchVocabulary("angel") will return the byte position
    // to seek to in postings.bin to find the postings for "angel".
    private long binarySearchVocabulary(String term) {
      // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
      int i = 0, j = mVocabTable.length / 2 - 1;
      while (i <= j) {
         try {
            int m = (i + j) / 2;
            long vListPosition = mVocabTable[m * 2];
            int termLength;
            if (m == mVocabTable.length / 2 - 1) {
               termLength = (int)(mVocabList.length() - mVocabTable[m*2]);
            }
            else {
               termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
            }

            mVocabList.seek(vListPosition);

            byte[] buffer = new byte[termLength];
            mVocabList.read(buffer, 0, termLength);
            String fileTerm = new String(buffer, "ASCII");

            int compareValue = term.compareTo(fileTerm);
            if (compareValue == 0) {
               // found it!
               return mVocabTable[m * 2 + 1];
            }
            else if (compareValue < 0) {
               j = m - 1;
            }
            else {
               i = m + 1;
            }
         }
         catch (IOException ex) {
            System.out.println(ex.toString());
         }
      }
      return -1;
   }

    // Reads the file vocabTable.bin into memory.
    private static long[] readVocabTable(String indexName) {
      try {
         long[] vocabTable;
         
         RandomAccessFile tableFile = new RandomAccessFile(
          new File(indexName, "vocabTable.bin"),
          "r");
         
         byte[] byteBuffer = new byte[4];
         tableFile.read(byteBuffer, 0, byteBuffer.length);
        
         int tableIndex = 0;
         vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
         byteBuffer = new byte[8];
         
         while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 4 bytes
            vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
            tableIndex++;
         }
         tableFile.close();
         return vocabTable;
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }

    public int getTermCount() {
      return mVocabTable.length / 2;
   }

    private List<String> readFileNames(String path) {
       List<String> result = new ArrayList();
       Path dir = Paths.get(path);
       
       try {
           // First discover all the files in the directory that match the filter.
           Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
               
               public FileVisitResult preVisitDirectory(Path dir,
                       BasicFileAttributes attrs) {
                   // make sure we only process the current working directory
                   if (dir.equals(dir)) {
                       return FileVisitResult.CONTINUE;
                   }
                   return FileVisitResult.SKIP_SUBTREE;
               }
               
               public FileVisitResult visitFile(Path file,
                       BasicFileAttributes attrs) {
                   result.add(file.getFileName().toString());
                   return FileVisitResult.CONTINUE;
               }
               
               // don't throw exceptions if files are locked/other errors occur
               public FileVisitResult visitFileFailed(Path file,
                       IOException e) {
                   return FileVisitResult.CONTINUE;
               }
           });
       } catch (IOException ex) {
           Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
       }
       
       
       return result;
   }
   
    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> result = new ArrayList();
        
        long current = binarySearchVocabulary(term); // Start of Postings for term
        
        // dft, d_i, tf_t,d, p_i...
        try {
            // dft
            mPostings.seek(current);
            int dft = mPostings.readInt(); // reads dft
            
            // For each doc
            for (int i = 0; i < dft; i++) {
                // Get to d_i (doc id)
                current += 4;
                mPostings.seek(current);
                int d_i = mPostings.readInt(); // Doc ID
                Posting post = new Posting(d_i);
                
                // Get to tf_t
                current += 4;
                mPostings.seek(current);
                int tf_t = mPostings.readInt();
                
                // Read p_i's
                for (int j = 0; j < tf_t; j++) {
                    // Jump to start of p_i
                    current += 4;
                    mPostings.seek(current);
                    int pos = mPostings.readInt(); // Pos
                    post.addPos(pos);
                }
                
                result.add(post);
            }
        } catch (IOException ex) {
            Logger.getLogger(DiskInvertedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }

    @Override
    public List<String> getVocabulary() {
        // TODO: implement method to retrieve all vocab for index
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
