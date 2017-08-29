package br.com.mertins.dl4j.samples.abccc;

import br.com.mertins.datavec.api.records.reader.MongoImageRecordReader;
import br.com.mertins.datavec.api.io.labels.MongoPathLabelGenerator;
import br.com.mertins.datavec.api.split.MongoSplit;
import br.com.mertins.dl4j.mongo.MongoConnection;
import br.com.mertins.dl4j.mongo.MongoDaoDL4J;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class MnistImageRPPipelineMongo {

    private static Logger log = LoggerFactory.getLogger((MnistImageRPPipelineMongo.class));

    public static void main(String[] args) throws Exception {
        int height = 28;
        int width = 28;
        int channels = 1;

        int rngseed = 123;
        Random randNumGen = new Random(rngseed);
        int batchSize = 32;
        int outputNum = 2;
        int numEpochs = 15;

        Properties properties = new Properties();
        properties.setProperty("nosqlServer", "localhost");
        properties.setProperty("nosqlPort", "27017");
        properties.setProperty("nosqlDatabase", "IAABCCC");
        properties.setProperty("nosqlUser", "xxx");
        properties.setProperty("nosqlPasswd", "xxx");
        MongoConnection conn = new MongoConnection();
        conn.doConnectionMongo(properties, false);

        MongoDaoDL4J daoTrain = new MongoDaoDL4J(conn.getMongoDatabase(), "RegistroProvisorioTrain", "side", "RegistroProvisorioAdj", "fileIdAdj");
        MongoDaoDL4J daoTest = new MongoDaoDL4J(conn.getMongoDatabase(), "RegistroProvisorioTest", "side", "RegistroProvisorioAdj", "fileIdAdj");

        MongoSplit train = new MongoSplit(daoTrain, randNumGen);
        MongoSplit test = new MongoSplit(daoTest, randNumGen);

        MongoPathLabelGenerator labelMaker = new MongoPathLabelGenerator();
        
        MongoImageRecordReader recordReader = new MongoImageRecordReader(height, width, channels, labelMaker);
        recordReader.initialize(train);
        
//        recordReader.setListeners(new LogRecordListener());
        DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);

        log.info(("**** Build Model ****"));
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngseed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .learningRate(0.006)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(height * width)
                        .nOut(100)
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(100)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .pretrain(false).backprop(true)
                .setInputType(InputType.convolutional(height, width, channels))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);

        model.init();

        model.setListeners(new ScoreIterationListener(10));

        log.info("**** Train Model ****");
        for (int i = 0; i < numEpochs; i++) {
            model.fit(dataIter);
        }

        log.info("**** Evaluate Model");
        recordReader.reset();
        recordReader.initialize(test);
        DataSetIterator testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
        scaler.fit(testIter);
        testIter.setPreProcessor(scaler);

        log.info(recordReader.getLabels().toString());

        Evaluation eval = new Evaluation(outputNum);
        while (testIter.hasNext()) {
            DataSet next = testIter.next();
            INDArray output = model.output(next.getFeatureMatrix());
            eval.eval(next.getLabels(), output);

        }
        log.info(eval.stats());

        conn.close();
    }
}
