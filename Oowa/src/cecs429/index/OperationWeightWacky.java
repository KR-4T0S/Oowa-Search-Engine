package cecs429.index;

import cecs429.text.TokenProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class OperationWeightWacky implements WeightStrategy{

    @Override
    public PriorityQueue<Accumulator> get(Index diskIndex, TokenProcessor tokenProcessor, int corpusSize, String[] terms) {
        Map<Integer, Accumulator> mapAccumulator = new HashMap();
        PriorityQueue<Accumulator> heap = new PriorityQueue(); 
        
        // for each term in query
        for (String term: terms) {
            List<String> proccessedTerm = tokenProcessor.processToken(term);
            // SimpleTokenProcessor List is always 1 String.
            for (String token: proccessedTerm) {
                // df_t
                List<Posting> postings = diskIndex.getNonPositionalPostings(token);
                float w_qt;
                if (!postings.isEmpty()) {
                    float temp = (float) Math.log((corpusSize - postings.size())/(postings.size()));
                    w_qt = Math.max(0, temp);
                } else {
                    w_qt = 0;
                }

                for (Posting p: postings) {
                    Accumulator A_d = null;

                    // We already have an accumulator for this doc
                    // tf_td can't be 0, so no need to worry about that case
                    int tf_td = p.getTftd();
                    // w_dt = [1 + ln(tf_td)] / [1 + ln( avg(tf_td) )]
                    float w_dt = 0;
                    if (mapAccumulator.containsKey(p.getDocumentId())) {
                        A_d = mapAccumulator.get(p.getDocumentId());
                        A_d.incrementScore(w_dt, w_qt);
                    } else { // Doc does not have accumulator
                        A_d = new Accumulator(p);    
                        A_d.incrementScore(w_dt, w_qt);
                    }

                    mapAccumulator.put(p.getDocumentId(), A_d);
                }
            }
        }

        // Add accumulators to a binary heap priority queue
        for (Accumulator A_d: mapAccumulator.values()) {
            if (A_d.getScore() != 0) {
                float L_d = (float) diskIndex.getWeightDefault(A_d.getPosting().getDocumentId());
                A_d.normalizeScore(L_d);
            }
            heap.add(A_d);
        }
        
        
        return heap;
    }
    
}
