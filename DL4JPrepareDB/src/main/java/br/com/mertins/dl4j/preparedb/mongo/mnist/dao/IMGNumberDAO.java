package br.com.mertins.dl4j.preparedb.mongo.mnist.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import br.com.mertins.dl4j.preparedb.mongo.mnist.persist.ImgNumber;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author mertins
 */
public class IMGNumberDAO {

    private MongoDatabase mongoDatabase;
    private final String collectionName;
    private MongoCollection<Document> collection;

    public IMGNumberDAO(MongoDatabase mongoDatabase, String collectionName) {
        this.mongoDatabase = mongoDatabase;
        this.collectionName = collectionName;
        this.collection = mongoDatabase.getCollection(collectionName);
    }

    public void insert(ImgNumber imgNumber) {
        try {
            GridFSBucket gridBucket = GridFSBuckets.create(mongoDatabase, String.format("%sContent", collectionName));
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            GridFSUploadOptions uploadOptions = new GridFSUploadOptions().chunkSizeBytes(1024).metadata(new Document("upload_date",
                    format.format(new Date())).append("content_type", "image/png").append("label", imgNumber.getLabel().toString()));
            ObjectId contentId = gridBucket.uploadFromStream(imgNumber.getFileName(),
                    new ByteArrayInputStream(imgNumber.getContent()), uploadOptions);

            this.collection.insertOne(prepareDoc(imgNumber, contentId));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Document prepareDoc(ImgNumber imgNumber, ObjectId contentId) {
        Document doc = new Document();
        doc.append("label", imgNumber.getLabel().toString());
        doc.append("filename", imgNumber.getFileName());
        doc.append("contentId", contentId);
        return doc;
    }
}
