/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.net;

import eca.core.converters.XLSLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.function.BiPredicate;

import static eca.core.converters.DataFileExtension.ARFF;
import static eca.core.converters.DataFileExtension.CSV;
import static eca.core.converters.DataFileExtension.XLS;
import static eca.core.converters.DataFileExtension.XLSX;


/**
 * Class for loading data from network using http and ftp protocols.
 *
 * @author Roman Batygin
 */
@Slf4j
public class DataLoaderImpl implements DataLoader {

    /**
     * Available protocols
     **/
    private static final String[] PROTOCOLS = {"http", "ftp"};

    /**
     * Available files extensions
     **/
    private static final String[] FILE_EXTENSIONS = {XLS, XLSX, CSV, ARFF};

    /**
     * Source url
     **/
    private URL url;

    /**
     * Creates object with given <tt>URL</tt>
     *
     * @param url source url
     * @throws Exception if given url contains incorrect protocol or file extension
     */
    public DataLoaderImpl(URL url) throws Exception {
        this.setURL(url);
    }

    @Override
    public Instances loadInstances() throws Exception {
        Instances data;
        URLConnection connection = url.openConnection();
        if (url.getFile().endsWith(FILE_EXTENSIONS[0]) || url.getFile().endsWith(FILE_EXTENSIONS[1])) {
            XLSLoader loader = new XLSLoader();
            loader.setInputStream(connection.getInputStream());
            data = loader.getDataSet();
        } else {
            AbstractFileLoader saver = url.getFile().endsWith(FILE_EXTENSIONS[2])
                    ? new CSVLoader() : new ArffLoader();
            saver.setSource(connection.getInputStream());
            data = saver.getDataSet();
        }
        return data;
    }

    /**
     * Sets source url.
     *
     * @param url source url
     * @throws Exception if given url contains incorrect protocol or file extension
     */
    public final void setURL(URL url) throws Exception {
        Assert.notNull(url, "URL is not specified!");
        if (!contains(PROTOCOLS, url.getProtocol(), (x, y) -> x.equals(y))) {
            String errorMessage = String.format(DataLoaderDictionary.BAD_PROTOCOL_ERROR_FORMAT,
                    Arrays.asList(PROTOCOLS));
            log.error(errorMessage);
            throw new Exception(errorMessage);
        }
        if (!contains(FILE_EXTENSIONS, url.getFile(), (x, y) -> x.endsWith(y))) {
            String errorMessage = String.format(DataLoaderDictionary.BAD_FILE_EXTENSION_ERROR_FORMAT,
                    Arrays.asList(FILE_EXTENSIONS));
            log.error(errorMessage);
            throw new Exception(errorMessage);
        }
        this.url = url;
    }

    private boolean contains(String[] list, String val, BiPredicate<String, String> predicate) {
        for (int i = 0; i < list.length; i++) {
            if (predicate.test(val, list[i])) {
                return true;
            }
        }
        return false;
    }

}
