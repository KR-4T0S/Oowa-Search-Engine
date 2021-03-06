package cecs429.index;

public class OperationWeightTFIDF implements WeightStrategy{

    @Override
    public double getWqt(int corpusSize, int postingsSize) {
        double w_qt;
        if (postingsSize != 0) {
            double idft = (double) corpusSize / (double) postingsSize;
            w_qt = Math.log(idft);
        } else {
            w_qt = 0;
        }
        return w_qt;
    }

    @Override
    public double getWdt(int tftd, Index index, int docId) {
        return tftd;
    }

    @Override
    public double getLd(Index index, int docId) {
        return index.getWeightDefault(docId);
    }

}
