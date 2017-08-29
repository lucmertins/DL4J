package br.com.mertins.datavec.api.records.reader;

import br.com.mertins.datavec.api.util.MongoElementFromStringIterator;
import br.com.mertins.datavec.api.split.MongoSplit;
import br.com.mertins.dl4j.mongo.MongoElement;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.datavec.api.conf.Configuration;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.records.Record;
import org.datavec.api.records.metadata.RecordMetaData;
import org.datavec.api.records.metadata.RecordMetaDataURI;
import org.datavec.api.records.reader.BaseRecordReader;
import static org.datavec.api.records.reader.RecordReader.APPEND_LABEL;
import static org.datavec.api.records.reader.RecordReader.LABELS;
import org.datavec.api.split.InputSplit;
import org.datavec.api.util.ndarray.RecordConverter;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.Writable;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.loader.ImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.BaseImageRecordReader;
import static org.datavec.image.recordreader.BaseImageRecordReader.CHANNELS;
import static org.datavec.image.recordreader.BaseImageRecordReader.CROP_IMAGE;
import static org.datavec.image.recordreader.BaseImageRecordReader.HEIGHT;
import static org.datavec.image.recordreader.BaseImageRecordReader.IMAGE_LOADER;
import static org.datavec.image.recordreader.BaseImageRecordReader.WIDTH;
import org.datavec.image.transform.ImageTransform;
import org.nd4j.linalg.api.concurrency.AffinityManager;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author mertins
 */
public class MongoImageRecordReader extends BaseRecordReader {

    protected Iterator<MongoElement> iter;
    protected int height = 28, width = 28, channels = 1;
    protected PathLabelGenerator labelGenerator = null;
    protected ImageTransform imageTransform;
    protected List<Writable> record;
    protected boolean hitImage = false;
    protected boolean appendLabel = false;
    protected boolean writeLabel = false;
    protected boolean cropImage = false;
    protected BaseImageLoader imageLoader;
    protected MongoSplit inputSplit;
    protected Configuration conf;
    protected List<String> labels = new ArrayList<>();
    protected MongoElement currentMongoElement;

    public MongoImageRecordReader(int height, int width, int channels, PathLabelGenerator labelGenerator) {
        this(height, width, channels, labelGenerator, null);
    }

    public MongoImageRecordReader(int height, int width, int channels, PathLabelGenerator labelGenerator, ImageTransform imageTransform) {
        this.height = height;
        this.width = width;
        this.channels = channels;
        this.labelGenerator = labelGenerator;
        this.imageTransform = imageTransform;
        this.appendLabel = labelGenerator != null ? true : false;
    }

    @Override
    public void initialize(InputSplit split) throws IOException, InterruptedException {
        if (imageLoader == null) {
            imageLoader = new NativeImageLoader(height, width, channels, imageTransform);
        }
        if (!(split instanceof MongoSplit)) {
            throw new IllegalArgumentException("InputSplit isn't MongoSplit");
        }
        inputSplit = (MongoSplit) split;
        URI[] locations = split.locations();
        if (locations != null && locations.length >= 1) {
            if (appendLabel) {
                for (URI location : locations) {
                    String name = null;
                    if (labelGenerator != null) {
                        name = labelGenerator.getLabelForPath(location).toString();
                        if (!labels.contains(name)) {
                            labels.add(name);
                        }
                    }
                }
            }
            iter = new MongoElementFromStringIterator(inputSplit.locationsPathIterator()); //This handles randomization internally if necessary
        } else {
            throw new IllegalArgumentException("No path locations found in the MongoSplit. Maybe not a Mongo Source?");
        }
        Collections.sort(labels);
    }

    @Override
    public void initialize(Configuration conf, InputSplit split) throws IOException, InterruptedException {
        this.appendLabel = conf.getBoolean(APPEND_LABEL, false);
        this.labels = new ArrayList<>(conf.getStringCollection(LABELS));
        this.height = conf.getInt(HEIGHT, height);
        this.width = conf.getInt(WIDTH, width);
        this.channels = conf.getInt(CHANNELS, channels);
        this.cropImage = conf.getBoolean(CROP_IMAGE, cropImage);
        if ("imageio".equals(conf.get(IMAGE_LOADER))) {
            this.imageLoader = new ImageLoader(height, width, channels, cropImage);
        } else {
            this.imageLoader = new NativeImageLoader(height, width, channels, imageTransform);
        }
        this.conf = conf;
        initialize(split);
    }

    @Override
    public List<Writable> next() {
        if (iter != null) {
            List<Writable> ret;
            currentMongoElement = iter.next();
            try {
                invokeListeners(currentMongoElement);
                INDArray row = imageLoader.asMatrix(inputSplit.getMongoDAO().find(currentMongoElement));
                Nd4j.getAffinityManager().ensureLocation(row, AffinityManager.Location.DEVICE);
                ret = RecordConverter.toRecord(row);
                if (appendLabel || writeLabel) {
                    ret.add(new IntWritable(labels.indexOf(currentMongoElement.getLabel())));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return ret;
        } else if (record != null) {
            hitImage = true;
            invokeListeners(record);
            return record;
        }
        throw new IllegalStateException("No more elements");
    }

    @Override
    public boolean hasNext() {
        if (iter != null) {
            return iter.hasNext();
        } else if (record != null) {
            return !hitImage;
        }
        throw new IllegalStateException("Indeterminant state: record must not be null, or a file iterator must exist in Mongodb");
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

    @Override
    public void reset() {
        if (inputSplit == null) {
            throw new UnsupportedOperationException("Cannot reset without first initializing");
        }
        inputSplit.reset();
        if (iter != null) {
            iter = new MongoElementFromStringIterator(inputSplit.locationsPathIterator());
            hitImage = false;
        }
    }

    @Override
    public List<Writable> record(URI uri, DataInputStream dataInputStream) throws IOException {
        invokeListeners(uri);
        if (imageLoader == null) {
            imageLoader = new NativeImageLoader(height, width, channels, imageTransform);
        }
        INDArray row = imageLoader.asMatrix(dataInputStream);
        List<Writable> ret = RecordConverter.toRecord(row);
        if (appendLabel) {
            ret.add(new IntWritable(labels.indexOf(getLabel(uri.getPath()))));
        }
        return ret;
    }

    @Override
    public Record nextRecord() {
        List<Writable> list = next();
        return new org.datavec.api.records.impl.Record(list, new RecordMetaDataURI(currentMongoElement.toURI(), BaseImageRecordReader.class));
    }

    @Override
    public Record loadFromMetaData(RecordMetaData recordMetaData) throws IOException {
        return loadFromMetaData(Collections.singletonList(recordMetaData)).get(0);
    }

    @Override
    public List<Record> loadFromMetaData(List<RecordMetaData> recordMetaDatas) throws IOException {
        List<Record> out = new ArrayList<>();
        for (RecordMetaData meta : recordMetaDatas) {
            URI uri = meta.getURI();
            File f = new File(uri);
            List<Writable> next;
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                next = record(uri, dis);
            }
            out.add(new org.datavec.api.records.impl.Record(next, meta));
        }
        return out;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void setConf(Configuration c) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    /**
     * Get the label from the given path
     *
     * @param path the path to get the label from
     * @return the label for the given path
     */
    public String getLabel(String path) {
        if (labelGenerator != null) {
            return labelGenerator.getLabelForPath(path).toString();
        }
        return (new File(path)).getParentFile().getName();
    }
}
