package br.com.mertins.datavec.api.util;

import br.com.mertins.dl4j.mongo.MongoElement;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;

/**
 *
 * @author mertins
 */
@AllArgsConstructor
public class MongoElementFromStringIterator implements Iterator<MongoElement> {

    private final Iterator<String> paths;

    @Override
    public boolean hasNext() {
        return paths.hasNext();
    }

    @Override
    public MongoElement next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No next element");
        }
        return MongoElement.fromURIPath(paths.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
