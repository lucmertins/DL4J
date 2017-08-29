package br.com.mertins.dl4j.mongo;

import java.net.URI;
import java.net.URISyntaxException;
import org.bson.types.ObjectId;

/**
 *
 * @author mertins
 */
public class MongoElement {

    private ObjectId id;
    private String label;

    public MongoElement(ObjectId id) {
        this.id = id;
    }

    public MongoElement(ObjectId id, String label) {
        this.id = id;
        this.label = label;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "MongoElement{" + "id=" + id + ", label=" + label + '}';
    }

    public URI toURI() {
        try {
            return new URI("mongoelement", null, String.format("//%s/%s", this.label.trim(), this.id.toString()), null);
        } catch (URISyntaxException x) {
            throw new Error(x); // Can't happen
        }
    }

    public static URI toURI(MongoElement element) {
        return element.toURI();
    }

    public static MongoElement fromURIPath(String value) {
        String[] values = value.replaceFirst("mongoelement://", "").split("/");
        return new MongoElement(new ObjectId(values[1]), values[0]);
    }

    public static String toURIPath(MongoElement element) {
        return element.toURI().toString();
    }

}
