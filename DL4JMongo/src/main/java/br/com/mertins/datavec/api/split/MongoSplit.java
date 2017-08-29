package br.com.mertins.datavec.api.split;

import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import br.com.mertins.dl4j.mongo.MongoElement;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import org.datavec.api.split.BaseInputSplit;
import org.datavec.api.util.RandomUtils;
import org.datavec.api.writable.WritableType;
import org.nd4j.linalg.collection.CompactHeapStringList;

/**
 *
 * @author mertins
 */
public class MongoSplit extends BaseInputSplit {

    protected MongoDaoDL4J mongoDAO;
    protected boolean randomize = false;
    protected Random random;

    protected MongoSplit(MongoDaoDL4J mongoDAO, Random random, boolean runMain) {
        this.mongoDAO = mongoDAO;
        if (random != null) {
            this.random = random;
            this.randomize = true;
        }
        if (runMain) {
            this.initialize();
        }
    }

    public MongoSplit(MongoDaoDL4J rootDir, Random rng) {
        this(rootDir, rng, true);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public void reset() {
        if (randomize) {
            //Shuffle the iteration order
            RandomUtils.shuffleInPlace(iterationOrder, random);
        }
    }

    @Override
    public void write(DataOutput d) throws IOException {

    }

    @Override
    public void readFields(DataInput di) throws IOException {

    }

    @Override
    public WritableType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI[] locations() {
        URI[] uris = new URI[uriStrings.size()];
        int i = 0;
        for (String s : uriStrings) {
            try {
                uris[i++] = new URI(s);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return uris;
    }

    public MongoDaoDL4J getMongoDAO() {
        return mongoDAO;
    }

    protected void initialize() {
        List<MongoElement> list = mongoDAO.list();

        if (randomize) {
            iterationOrder = new int[list.size()];
            for (int i = 0; i < iterationOrder.length; i++) {
                iterationOrder[i] = i;
            }
            RandomUtils.shuffleInPlace(iterationOrder, random);
        }
        uriStrings = new CompactHeapStringList();
        list.forEach((elem) -> {
            uriStrings.add(MongoElement.toURIPath(elem));
        });
        length = uriStrings.size();
    }

}
