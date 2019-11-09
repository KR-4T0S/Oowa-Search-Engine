package cecs429.index;

public class Accumulator implements Comparable<Accumulator> {
    private final Posting mPosting;
    private double mDocScore;
    
    public Accumulator(Posting p) {
        mPosting = p;
        mDocScore = 0;
    }
    
    public void incrementScore(double w_dt, double w_qt) {
        mDocScore += w_dt * w_qt;
    }
    
    public void normalizeScore(double Ld) {
        mDocScore = mDocScore / Ld;
    }
    
    public Posting getPosting() {
        return mPosting;
    }

    public double getScore() {
        return mDocScore;
    }
   
    @Override
    public int compareTo(Accumulator t) {
        // For heap comparison, reversed as to not use Collections Reverse Order
        if (mDocScore < t.getScore()) {
            return 1;
        } else if (mDocScore > t.getScore()) {
            return -1;
        } else {
            return 0;
        }
    }
}
