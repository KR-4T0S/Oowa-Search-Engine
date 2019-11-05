package cecs429.index;

import cecs429.index.Posting;

public class Accumulator implements Comparable<Accumulator> {
    private final Posting mPosting;
    private float mDocScore;
    
    public Accumulator(Posting p) {
        mPosting = p;
        mDocScore = 0;
    }
    
    public void incrementScore(float w_dt, float w_qt) {
        mDocScore += w_dt * w_qt;
    }
    
    public void normalizeScore(float Ld) {
        mDocScore = mDocScore / Ld;
    }
    
    public Posting getPosting() {
        return mPosting;
    }

    public float getScore() {
        return mDocScore;
    }
   
    @Override
    public int compareTo(Accumulator t) {
        if (mDocScore < t.getScore()) {
            return 1;
        } else if (mDocScore > t.getScore()) {
            return -1;
        } else {
            return 0;
        }
    }
}
