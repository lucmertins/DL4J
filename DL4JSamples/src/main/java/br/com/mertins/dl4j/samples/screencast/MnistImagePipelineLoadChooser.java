package br.com.mertins.dl4j.samples.screencast;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MnistImagePipelineLoadChooser {
    private static Logger log = LoggerFactory.getLogger(MnistImagePipelineLoadChooser.class);

    public static String fileChoose() {
        JFileChooser fc = new JFileChooser();
        int ret = fc.showOpenDialog(null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String filename = file.getAbsolutePath();
            return filename;
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        int height = 28;
        int width = 28;
        int channels = 1;

        List<Integer> labelList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        String fileChoose = fileChoose().toString();
        File locationToSave = new File("/home/mertins/Desenvolvimento/Java/Terceiros/screencasts/trained_mnist_model.zip");
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        log.info("*********TEST YOUR IMAGE AGAINST SAVED NETWORK********");

        File file = new File(fileChoose);

        NativeImageLoader loader = new NativeImageLoader(height, width, channels);
        INDArray image = loader.asMatrix(file);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.transform(image);

        INDArray output = model.output(image);

        log.info("## The FILE CHOSEN WAS " + fileChoose);
        log.info("## The Neural Nets Pediction ##");
        log.info("## list of probabilities per label ##");
        //log.info("## List of Labels in Order## ");
        // In new versions labels are always in order
        log.info(output.toString());
        log.info(labelList.toString());


    }
}
