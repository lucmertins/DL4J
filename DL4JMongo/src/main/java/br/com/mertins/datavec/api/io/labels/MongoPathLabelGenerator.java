package br.com.mertins.datavec.api.io.labels;

import java.net.URI;
import org.apache.commons.io.FilenameUtils;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

/**
 *
 * @author mertins
 */
public class MongoPathLabelGenerator implements PathLabelGenerator {

    @Override
    public Writable getLabelForPath(String string) {
        return new Text(FilenameUtils.getBaseName(string));
    }

    @Override
    public Writable getLabelForPath(URI uri) {
        return getLabelForPath(uri.getHost());
    }

}
