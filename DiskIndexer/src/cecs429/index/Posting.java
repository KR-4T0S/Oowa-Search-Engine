package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

    private int mDocumentId;
    private List<Integer> mPos;

    public Posting(int documentId) {
        mDocumentId = documentId;
        mPos = new ArrayList<>();
    }

    public void addPos(int pos) {
        mPos.add(pos);
    }

    public int getDocumentId() {
        return mDocumentId;
    }

    public List getPositions() {
        return mPos;
    }
}
