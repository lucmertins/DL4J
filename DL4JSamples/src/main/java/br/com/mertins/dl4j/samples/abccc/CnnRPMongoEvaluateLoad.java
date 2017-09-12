package br.com.mertins.dl4j.samples.abccc;

import br.com.mertins.datavec.api.io.labels.MongoPathLabelGenerator;
import br.com.mertins.datavec.api.records.reader.MongoImageRecordReader;
import br.com.mertins.datavec.api.split.MongoSplit;
import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import static br.com.mertins.dl4j.samples.abccc.CnnRPMongo.log;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

/**
 *
 * @author mertins
 */
public class CnnRPMongoEvaluateLoad {

    public static void main(String[] args) throws IOException {
        int seed = 123;
        Random rng = new Random(seed);
        int height = 100;
        int width = 100;
        int batchSize = 32;
        int numLabels = 2;
        int channels = 3;

        log.info("Load data....");
        Properties properties = new Properties();
        properties.setProperty("nosqlServer", "localhost");
        properties.setProperty("nosqlPort", "27017");
        properties.setProperty("nosqlDatabase", "IAABCCC");
        properties.setProperty("nosqlUser", "xxx");
        properties.setProperty("nosqlPasswd", "xxx");
        MongoConnection conn = new MongoConnection();
        conn.doConnectionMongo(properties, false);

        MongoDaoDL4J daoTest = new MongoDaoDL4J(conn.getMongoDatabase(), "RegProvisorioIndividualTest", "side", "RegProvisorioIndividualAdj", "fileIdAdj");
        MongoSplit test = new MongoSplit(daoTest, rng);
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

        File locationToSave = new File("/home/mertins/Desenvolvimento/Java/UFPel/IA/RedesGeradasDL4J/20170901/model.bin");
        MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        MongoPathLabelGenerator labelMaker = new MongoPathLabelGenerator();
        MongoImageRecordReader recordReader = new MongoImageRecordReader(height, width, channels, labelMaker);

        log.info("Evaluate model....");
        recordReader.initialize(test);
        DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);
        Evaluation eval = network.evaluate(dataIter);
        log.info(eval.stats(true));

    }
}
