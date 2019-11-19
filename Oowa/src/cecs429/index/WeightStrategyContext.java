package cecs429.index;

import cecs429.text.TokenProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class WeightStrategyContext {
    
    private final WeightStrategy mStrategy;
    
    public WeightStrategyContext(WeightStrategy strategy) {
        mStrategy = strategy;
    }
    
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
                double w_qt = mStrategy.getWqt(corpusSize, postings.size());
                //System.out.println("wQT(\""+ token +"\")" + w_qt);
                
                for (Posting p: postings) {
                    Accumulator A_d = null;

                    // We already have an accumulator for this doc
                    // tf_td can't be 0, so no need to worry about dividing by 0
                    int tf_td = p.getTftd();
                    //System.out.println("tfTD(" + p.getDocumentId() + "): " + tf_td);
                    double w_dt = mStrategy.getWdt(tf_td, diskIndex, p.getDocumentId());
                    //System.out.println("wDt(" + p.getDocumentId() + "): " + w_dt);
                    
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
            double L_d = mStrategy.getLd(diskIndex, A_d.getPosting().getDocumentId());
            //System.out.println("Ld(" + A_d.getPosting().getDocumentId() + "): " + L_d);
            if (A_d.getScore() != 0) {
                A_d.normalizeScore(L_d);
            }
            heap.add(A_d);
        }
        
        return heap;
    }
}
