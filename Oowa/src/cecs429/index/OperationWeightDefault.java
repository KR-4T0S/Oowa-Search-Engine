package cecs429.index;

public class OperationWeightDefault implements WeightStrategy{

    @Override
    public double getWqt(int corpusSize, int postingsSize) {
        double result;
        
        if (postingsSize != 0) {
            double idft = (double) corpusSize / postingsSize;
            result = Math.log(1 + idft);
        } else {
            result = 0;
        }

        return result;
    }

    @Override
    public double getWdt(int tftd, Index index, int docId) {
        double result = 1 + Math.log(tftd);

        return result;
    }

    @Override
    public double getLd(Index index, int docId) {
        double result = index.getWeightDefault(docId);

        return result;
    }
    
}
