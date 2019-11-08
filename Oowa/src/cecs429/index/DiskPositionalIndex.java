package cecs429.index;

import cecs429.documents.DocumentCorpus;
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


public class DiskPositionalIndex implements Index {

   private String mPath;
   private List<String> mFileNames;
   private RandomAccessFile mVocabList;
   private RandomAccessFile mPostings;
   private RandomAccessFile mWeights;
   private long[] mVocabTable;

    // Opens a disk inverted index that was constructed in the given path.
    public DiskPositionalIndex(String path) {
      try {
         mPath = path + "/index/";
         mVocabList = new RandomAccessFile(new File(mPath, "vocab.bin"), "r");
         mPostings = new RandomAccessFile(new File(mPath, "postings.bin"), "r");
         mWeights = new RandomAccessFile(new File(mPath, "docWeights.bin"), "r");
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
            String fileTerm = new String(buffer, "UTF-8");

            //System.out.println(fileTerm);
            int compareValue = term.compareTo(fileTerm);
            if (compareValue == 0) {
               // found it!
               return mVocabTable[m * 2 + 1];
            } else if (compareValue < 0) {
               j = m - 1;
            } else {
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
         
         //byte[] byteBuffer = new byte[4];
         //tableFile.read(byteBuffer, 0, byteBuffer.length);
        
         int tableIndex = 0;
         vocabTable = new long[(int) tableFile.length() / 8];
         byte[] byteBuffer = new byte[8];
         
         while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 8 bytes
            vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
            tableIndex++;
         }
         tableFile.close();
         //System.out.println(vocabTable.toString());
         return vocabTable;
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      //System.out.println("readVocabTable: null");
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
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       }
       
       
       return result;
   }
   
    public List<Posting> getNonPositionalPostings(String term) {
        List<Posting> result = new ArrayList();
        
        long current = binarySearchVocabulary(term); // Start of Postings for term
        if (current >= 0) {
            // dft, d_i, tf_t,d, p_i...
            try {
                //System.out.print("\n\"" + term + "\": ");
                // dft
                mPostings.seek(current);
                int dft = mPostings.readInt(); // reads dft
                //System.out.println("dft: " + dft);
                
                // For each doc
                int d_i = 0; // set for gap
                for (int i = 0; i < dft; i++) {
                    // Get to d_i (doc id)
                    current += 4;
                    mPostings.seek(current);
                    d_i += mPostings.readInt(); // Doc ID
                    Posting post = new Posting(d_i);
                    //System.out.println("\t\tID:" + d_i);
                    
                    // Get to tf_t
                    current += 4;
                    mPostings.seek(current);
                    int tf_t = mPostings.readInt();
                    //System.out.println("\t\t\t tf_t:" + tf_t);
                    post.setTf(tf_t); // We just want to know count
                    result.add(post);
                    
                    // skip p_i's
                    current += tf_t * 4;
                }
            } catch (IOException ex) {
                Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }
    
    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> result = new ArrayList();
        
        long current = binarySearchVocabulary(term); // Start of Postings for term
        if (current >= 0) {
            // dft, d_i, tf_t,d, p_i...
            try {
                //System.out.print("\n\"" + term + "\": ");
                // dft
                mPostings.seek(current);
                int dft = mPostings.readInt(); // reads dft
                //System.out.println("dft: " + dft);
                
                // For each doc
                int d_i = 0; // set for gap
                for (int i = 0; i < dft; i++) {
                    // Get to d_i (doc id)
                    current += 4;
                    mPostings.seek(current);
                    d_i += mPostings.readInt(); // Doc ID
                    Posting post = new Posting(d_i);
                    //System.out.println("\t\tID:" + d_i);

                    // Get to tf_t
                    current += 4;
                    mPostings.seek(current);
                    int tf_t = mPostings.readInt();
                    //System.out.println("\t\t\t tf_t:" + tf_t);

                    // Read p_i's
                    //System.out.print("\t\t\t\t");
                    int pos = 0;
                    for (int j = 0; j < tf_t; j++) {
                        // Jump to start of p_i
                        current += 4;
                        mPostings.seek(current);
                        pos += mPostings.readInt(); // Pos
                        post.addPos(pos);
                        //System.out.print(pos + " ");
                    }
                    //System.out.println();
                    result.add(post);
                }
            } catch (IOException ex) {
                Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> result = new ArrayList();
        
        for (int i = 0; i < mVocabTable.length; i++) {
            if (i % 2 == 0) {
                int termLength;
                try {
                    if (i == mVocabTable.length - 2) { // is last term
                        termLength = (int)(mVocabList.length() - mVocabTable[i]);
                    } else {
                        termLength = (int) (mVocabTable[i + 2] - mVocabTable[i]);
                    }
                    
                    // Read Term
                    byte[] buffer = new byte[termLength];
                    mVocabList.seek(mVocabTable[i]); // Jumps to first byte of word
                    mVocabList.read(buffer, 0, termLength);
                    
                    // Read bytes as String
                    String vocab = new String(buffer, "UTF-8");
                    result.add(vocab);
                } catch (IOException ex) {
                    Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return result;
    }

    /*
    *   Doc Weights File Format
    *   0                   8                   16                  24
    *   [docWeights_d]_0    [docLength_d]_0     [byteSize_d]_0      [avg(tf_td)]_0
    *   [docWeights_d]_1    [docLength_d]_1     [byteSize_d]_1      [avg(tf_td)]_1
    *   ..
    *   [docWeights_d]_n-1  [docLength_d]_n-1   [byteSize_d]_n-1    [avg(tf_td)]_n-1
    *   [docWeights_d]_n    [docLength_d]_n     [byteSize_d]_n      [avg(tf_td)]_n
    *   [docLength_A]
    */
    @Override
    public List<Double> getWeights() {
       List<Double> result = new ArrayList();
       
       try {
           // for every 8 bytes(i.e every doc)
           for (int i = 0; i < mWeights.length(); i += 8) {
                mWeights.seek(i);
                result.add(mWeights.readDouble());
           }
       } catch (IOException ex) {
           Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
       }
        
        return result;    
    }

    @Override
    public double getWeightDefault(int docId) {
        double result = -1;
        long start = docId * 8;
        
        try {
            mWeights.seek(start);
            result = mWeights.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return result;
    }

    @Override
    public double getDocLength(int docId) {
        double result = -1;
        long start = (docId * 8) + 8;
        
        try {
            mWeights.seek(start);
            result = mWeights.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return result;
    }

    @Override
    public double getDocByteSize(int docId) {
        double result = -1;
        long start = (docId * 8) + 16;
        
        try {
            mWeights.seek(start);
            result = mWeights.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return result;
    }

    @Override
    public double getAvgTftd(int docId) {
        double result = -1;
        long start = (docId * 8) + 24;
        
        try {
            mWeights.seek(start);
            result = mWeights.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return result;
    }

    @Override
    public double getAvgDocLength() {
        double result = -1;
        
        try {
            long start = mWeights.length() - 8;
            mWeights.seek(start);
            result = mWeights.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(DiskPositionalIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return result;
    }
}
