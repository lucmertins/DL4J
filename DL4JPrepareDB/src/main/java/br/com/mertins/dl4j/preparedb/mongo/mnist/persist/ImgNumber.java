package br.com.mertins.dl4j.preparedb.mongo.mnist.persist;

/**
 *
 * @author mertins
 */
public class ImgNumber {
     private String id;
     private String label;
     private String fileName;
     private byte[] content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
     
}
