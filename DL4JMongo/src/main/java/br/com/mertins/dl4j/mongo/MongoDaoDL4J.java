package br.com.mertins.dl4j.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author mertins
 */
public class MongoDaoDL4J {

    private final com.mongodb.client.MongoCollection<Document> collection;
    private final MongoDatabase mongoDatabase;
    private final String fieldLabel;
    private final String gridFSCollection;
    private final String idFSCollection;

    public MongoDaoDL4J(MongoDatabase mongoDatabase, String collectionName, String fieldLabel, String gridFSCollection, String idFSCollection) {
        this.mongoDatabase = mongoDatabase;
        this.collection = mongoDatabase.getCollection(collectionName);
        this.fieldLabel = fieldLabel;
        this.gridFSCollection = gridFSCollection;
        this.idFSCollection = idFSCollection;
    }

    public List<MongoElement> list() {
        List<MongoElement> list = new ArrayList<>();
        FindIterable<Document> find = this.collection.find();
        find.forEach((Block<Document>) document -> {
            list.add(new MongoElement(document.getObjectId("_id"), document.getString(this.fieldLabel)));
        });
        return list;
    }

    public InputStream find(MongoElement element) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", element.getId());
        FindIterable<Document> cursor = this.collection.find(query);
        List<InputStream> list = new ArrayList<>();
        cursor.forEach((Block<Document>) document -> {
            ObjectId id = document.getObjectId(this.idFSCollection);
            GridFSBucket gridBucket = GridFSBuckets.create(this.mongoDatabase, this.gridFSCollection);
            GridFSFile gridFSFile;
            byte[] bytesToWriteTo;
            try (GridFSDownloadStream downloadStream = gridBucket.openDownloadStream(id)) {
                gridFSFile = downloadStream.getGridFSFile();
                bytesToWriteTo = new byte[(int) gridFSFile.getLength()];
                int pos = 0;
                while (pos > -1) {
                    pos += downloadStream.read(bytesToWriteTo, pos, pos + gridFSFile.getChunkSize());
                }
            }
            list.add(new ByteArrayInputStream(bytesToWriteTo));
        });
        return list.isEmpty() ? null : list.get(0);
    }
}
