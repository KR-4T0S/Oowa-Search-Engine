package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

    private int mDocumentId;
    private List<Integer> mPos;
    private int mTftd;
    

    public Posting(int documentId) {
        mDocumentId = documentId;
        mPos = new ArrayList<>();
        mTftd = 0;
    }

    public void addPos(int pos) {
        mPos.add(pos);
        mTftd++;
    }
    
    public void setTf(int tf) {
        mTftd = tf;
    }

    public int getDocumentId() {
        return mDocumentId;
    }

    public List getPositions() {
        return mPos;
    }
    
    public int getTftd() {
        return mTftd;
    }
}
