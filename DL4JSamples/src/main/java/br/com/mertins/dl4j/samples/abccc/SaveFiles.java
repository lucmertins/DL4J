package br.com.mertins.dl4j.samples.abccc;

import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import br.com.mertins.dl4j.mongo.MongoElement;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.imageio.ImageIO;

/**
 *
 * @author mertins
 */
public class SaveFiles {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        properties.setProperty("nosqlServer", "localhost");
        properties.setProperty("nosqlPort", "27017");
        properties.setProperty("nosqlDatabase", "IAABCCC");
        properties.setProperty("nosqlUser", "xxx");
        properties.setProperty("nosqlPasswd", "xxx");

        MongoConnection conn = new MongoConnection();
        conn.doConnectionMongo(properties, false);
        MongoDaoDL4J daoTest = new MongoDaoDL4J(conn.getMongoDatabase(), "RegProvisorioIndividual", "side", "RegProvisorioIndividualAdj", "fileIdAdj");
        for (MongoElement element : daoTest.list()) {
            int newWidth = 800, newHeight = 800;
            InputStream img = daoTest.find(element);

            BufferedImage inputImage = ImageIO.read(ImageIO.createImageInputStream(img));

            BufferedImage outputImage = new BufferedImage(newWidth, newHeight, inputImage.getType());
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            ImageIO.write(outputImage, "png", new File(String.format("/home/mertins/temp/imgs/%s/%s_%s.png", element.getLabel(), element.getLabel(), element.getId().toString())));

        }

    }

}
