package cecs429.index;

public class OperationWeightWacky implements WeightStrategy{

    @Override
    public double getWqt(int corpusSize, int postingsSize) {
        if (postingsSize != 0) {
            double temp = Math.log((double)(corpusSize - postingsSize)/(double)(postingsSize));
            return Math.max(0, temp);
        }
        
        return 0;
    }

    @Override
    public double getWdt(int tftd, Index index, int docId) {
        return (double)(1 + Math.log(tftd)) / (double)(1 + Math.log(index.getAvgTftd(docId)));
    }

    @Override
    public double getLd(Index index, int docId) {
        return Math.sqrt(index.getDocByteSize(docId));
    }
    
}
