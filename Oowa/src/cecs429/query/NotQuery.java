package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class NotQuery implements QueryComponent {

    private QueryComponent mComponent;

    public NotQuery(QueryComponent component) {
        mComponent = component;
        mComponent.setPositive(false);
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        return mComponent.getPostings(index, processor);
    }

    @Override
    public void setPositive(boolean value) {
        // Should not be possible
    }
    
    @Override
    public boolean isPositive() {
        return mComponent.isPositive();
    }
    
    @Override
    public String toString() {
        return mComponent.toString();
    }
}
