package br.com.mertins.dl4j.preparedb.mongo.mnist;

import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.preparedb.mongo.mnist.dao.IMGNumberDAO;
import br.com.mertins.dl4j.preparedb.mongo.mnist.persist.ImgNumber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Transporta os arquivos do MNIST que estão descompactados no filesystem para uma instancia do Mongodb. 
 * Base pode ser encontrada em http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz
 * @author mertins
 */
public class ImportFileToMongodb {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        properties.setProperty("nosqlServer", "localhost");
        properties.setProperty("nosqlPort", "27017");
        properties.setProperty("nosqlDatabase", "DP4JIMAGES");
        properties.setProperty("nosqlUser", "xxx");
        properties.setProperty("nosqlPasswd", "xxx");
        MongoConnection conn = new MongoConnection();
        conn.doConnectionMongo(properties, false);

        String pathTraining = "/home/mertins/Desenvolvimento/Java/Terceiros/screencasts/mnist_png/training";
        IMGNumberDAO dao = new IMGNumberDAO(conn.getMongoDatabase(), "imgTraining");
        File root = new File(pathTraining);
        for (File dirLabel : root.listFiles()) {
            String label = dirLabel.getName();
            for (File imgFile : dirLabel.listFiles()) {
                ImgNumber img = new ImgNumber();
                img.setLabel(label);
                img.setFileName(imgFile.getName());
                ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
                try (FileInputStream fis = new FileInputStream(imgFile)) {
                    byte[] buffer = new byte[512];
                    int numRead = 0;
                    while ((numRead = fis.read(buffer)) > -1) {
                        baos.write(buffer, 0, numRead);
                    }
                }
                img.setContent(baos.toByteArray());
                dao.insert(img);
            }
        }

        String pathTest = "/home/mertins/Desenvolvimento/Java/Terceiros/screencasts/mnist_png/testing";
        dao = new IMGNumberDAO(conn.getMongoDatabase(), "imgTest");
        root = new File(pathTest);
        for (File dirLabel : root.listFiles()) {
            String label = dirLabel.getName();
            for (File imgFile : dirLabel.listFiles()) {
                ImgNumber img = new ImgNumber();
                img.setLabel(label);
                img.setFileName(imgFile.getName());
                ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
                try (FileInputStream fis = new FileInputStream(imgFile)) {
                    byte[] buffer = new byte[512];
                    int numRead = 0;
                    while ((numRead = fis.read(buffer)) > -1) {
                        baos.write(buffer, 0, numRead);
                    }
                }
                img.setContent(baos.toByteArray());
                dao.insert(img);
            }
        }
        conn.close();

    }
}
