package cecs429.index;

import cecs429.text.TokenProcessor;
import java.util.PriorityQueue;

public interface WeightStrategy {
    public double getWqt(int corpusSize, int postingsSize);
    
    public double getWdt(int tftd, Index index, int docId);
    
    public double getLd(Index index, int docId);
}
