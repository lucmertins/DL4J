package br.com.mertins.dl4j.samples.abccc;

import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import br.com.mertins.dl4j.mongo.MongoElement;
import static br.com.mertins.dl4j.samples.abccc.CnnRPMongo.log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.datavec.image.loader.NativeImageLoader;
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
        int height = 100;
        int width = 100;
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

        MongoDaoDL4J daoTest = new MongoDaoDL4J(conn.getMongoDatabase(), "RegProvisorioIndividualTest", "side", "RegProvisorioIndividualAdj", "fileIdAdj");

        String[] labels = {"FRONT", "VERSE"};

        for (MongoElement element : daoTest.list()) {
            INDArray image = loader.asMatrix(daoTest.find(element));
            scaler.transform(image);
            INDArray output = model.output(image);

            double value = Double.NEGATIVE_INFINITY;
            int pos = -1;
            for (int i = 0; i < output.length(); i++) {
                if (value < output.getDouble(i)) {
                    value = output.getDouble(i);
                    pos = i;
                }
            }

            if (!element.getLabel().equals(labels[pos])) {
                log.info(String.format("## ObjectId [%s]   Label  [%s]   Output [%s]   Escolheu [%s]",
                        element.getId().toString(), element.getLabel(), output.toString(), labels[pos]));
            }
            InputStream img = daoTest.find(element);
            byte[] buffer = new byte[2048];
            try (FileOutputStream fos = new FileOutputStream(String.format("/home/mertins/temp/imgs/%s_%s_%s.png",
                    element.getLabel().equals(labels[pos]) ? "OK" : "RUIM",
                    labels[pos], element.getId().toString()))) {
                int read = -1;
                while ((read = img.read(buffer)) > -1) {
                    fos.write(buffer, 0, read);
                }
            }

        }

    }
}
