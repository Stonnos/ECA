package eca.converters;

import eca.util.SerializationUtils;

import java.io.File;
import java.util.Objects;

/**
 * Implements saving and loading serialized object from file.
 *
 * @author Roman Batygin
 */
public class ModelConverter {

    public static final String FILE_EXTENSION = ".txt";

    /**
     * Serialise and saves given object to file.
     *
     * @param file  file object
     * @param model object
     * @throws Exception
     */
    public static void saveModel(File file, Object model) throws Exception {
        Objects.requireNonNull(file, "File is not specified!");
        Objects.requireNonNull(model, "Object is not specified!");
        if (!file.getName().endsWith(FILE_EXTENSION)) {
            throw new IllegalArgumentException(String.format("Can't save object %s to file '%s'",
                    model, file.getAbsoluteFile()));
        }
        SerializationUtils.serialize(model, file.getAbsolutePath());
    }

    /**
     * Loads serialized object from file.
     *
     * @param file file object
     * @return deserialize object
     * @throws Exception
     */
    public static Object loadModel(File file) throws Exception {
        Objects.requireNonNull(file, "File is not specified!");
        if (!file.getName().endsWith(FILE_EXTENSION)) {
            throw new IllegalArgumentException(
                    String.format("Can't load object from file '%s'", file.getAbsoluteFile()));
        }
        return SerializationUtils.deserialize(file.getAbsolutePath());
    }
}
