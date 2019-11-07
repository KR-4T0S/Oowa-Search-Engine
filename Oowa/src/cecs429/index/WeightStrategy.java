package cecs429.index;

import cecs429.text.TokenProcessor;
import java.util.PriorityQueue;

public interface WeightStrategy {
    public PriorityQueue<Accumulator> get(Index diskIndex, TokenProcessor tokenProcessor, int corpusSize, String[] terms);
}
