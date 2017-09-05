package br.com.mertins.dl4j.samples.abccc;

import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import br.com.mertins.dl4j.mongo.MongoElement;
import static br.com.mertins.dl4j.samples.abccc.CnnRPMongo.log;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.bson.types.ObjectId;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

/**
 *
 * @author mertins
 */
public class CnnRPMongoLoad {

    public static void main(String[] args) throws IOException {
        int seed = 123;
        Random rng = new Random(seed);
        int height = 100;
        int width = 100;
        int batchSize = 32;
        int numLabels = 2;
        int channels = 3;

        List<String> labelList = Arrays.asList("FRONT", "VERSE");
        log.info("Load data....");
        Properties properties = new Properties();
        properties.setProperty("nosqlServer", "localhost");
        properties.setProperty("nosqlPort", "27017");
        properties.setProperty("nosqlDatabase", "IAABCCC");
        properties.setProperty("nosqlUser", "xxx");
        properties.setProperty("nosqlPasswd", "xxx");
        MongoConnection conn = new MongoConnection();
        conn.doConnectionMongo(properties, false);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

        File locationToSave = new File("/home/mertins/Desenvolvimento/Java/UFPel/IA/RedesGeradasDL4J/20170901/model.bin");
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        
        
        NativeImageLoader loader = new NativeImageLoader(height, width, channels);

        MongoDaoDL4J daoTest = new MongoDaoDL4J(conn.getMongoDatabase(), "RegistroProvisorioTest", "side", "RegistroProvisorioAdj", "fileIdAdj");
        List<MongoElement> list = daoTest.list();

//        for (MongoElement element : list) {
        MongoElement element = new MongoElement(new ObjectId("596e0c003dbc0e19bf68ea00"));
        INDArray image = loader.asMatrix(daoTest.find(element));
        scaler.transform(image);
        INDArray output = model.output(image);
        
       
        
        
        
        log.info(String.format("## ObjectId %s    %s", element.getId().toString(), element.getLabel()));
        log.info("## The Neural Nets Pediction ##");
        log.info("## list of probabilities per label ##");
        log.info("## List of Labels in Order## ");
        // In new versions labels are always in order

        log.info(output.toString());

//        Evaluation eval = new Evaluation(labelList);
//
//        eval.eval(, output);
//        log.info(eval.stats());

//        }
    }
}
