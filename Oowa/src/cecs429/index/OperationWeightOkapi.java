package cecs429.index;

public class OperationWeightOkapi implements WeightStrategy{

    @Override
    public double getWqt(int corpusSize, int postingsSize) {
        double temp = Math.log((double)(corpusSize - postingsSize + 0.5) / (double)(postingsSize + 0.5));
        return Math.max(0.1, temp);
    }

    @Override
    public double getWdt(int tftd, Index index, int docId) {
        return (double)(2.2 * tftd) / (double)((1.2 * (0.25 + (0.75 * (index.getDocLength(docId) / index.getAvgDocLength())))) + tftd);
    }

    @Override
    public double getLd(Index index, int docId) {
        return 1;
    }
    
}
