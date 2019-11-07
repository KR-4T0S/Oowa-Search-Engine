package cecs429.index;

public class OperationWeightDefault implements WeightStrategy{

    @Override
    public double getWqt(int corpusSize, int postingsSize) {
        double w_qt;
        if (postingsSize != 0) {
            double idft = (double) corpusSize / postingsSize;
            w_qt = Math.log(1 + idft);
        } else {
            w_qt = 0;
        }
        return w_qt;
    }

    @Override
    public double getWdt(int tftd) {
        return 1 + Math.log(tftd);
    }

    @Override
    public double getLd(Index index, int docId) {
        return index.getWeightDefault(docId);
    }
    
}
