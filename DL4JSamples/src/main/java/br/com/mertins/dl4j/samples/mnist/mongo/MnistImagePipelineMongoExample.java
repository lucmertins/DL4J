package br.com.mertins.dl4j.samples.mnist.mongo;

import br.com.mertins.datavec.api.records.reader.MongoImageRecordReader;
import br.com.mertins.datavec.api.io.labels.MongoPathLabelGenerator;
import br.com.mertins.datavec.api.split.MongoSplit;
import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class MnistImagePipelineMongoExample {

    private static Logger log = LoggerFactory.getLogger((MnistImagePipelineMongoExample.class));

    public static void main(String[] args) throws Exception {
        int height = 28;
        int width = 28;
        int channels = 1;

        int rngseed = 123;
        Random randNumGen = new Random(rngseed);
        int batchSize = 1;
        int outputNum = 10;

        Properties properties = new Properties();
        properties.setProperty("nosqlServer", "localhost");
        properties.setProperty("nosqlPort", "27017");
        properties.setProperty("nosqlDatabase", "DP4JIMAGES");
        properties.setProperty("nosqlUser", "xxx");
        properties.setProperty("nosqlPasswd", "xxx");
        MongoConnection conn = new MongoConnection();
        conn.doConnectionMongo(properties, false);

        MongoDaoDL4J trainColl = new MongoDaoDL4J(conn.getMongoDatabase(), "imgTraining", "label", "imgTrainingContent", "contentId");
        MongoDaoDL4J testColl = new MongoDaoDL4J(conn.getMongoDatabase(), "imgTest", "label", "imgTestContent", "contentId");

        MongoSplit train = new MongoSplit(trainColl, randNumGen);
        MongoSplit test = new MongoSplit(testColl, randNumGen);

        MongoPathLabelGenerator labelMaker = new MongoPathLabelGenerator();
        MongoImageRecordReader recordReader = new MongoImageRecordReader(height, width, channels, labelMaker);
        recordReader.initialize(train);
        recordReader.setListeners(new LogRecordListener());
        DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);
        for (int i = 1; i <= 10; i++) {
            DataSet ds = dataIter.next();
            System.out.println(ds);
            System.out.println(dataIter.getLabels());
        }

        conn.close();
    }
}
