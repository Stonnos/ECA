/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.data.db;

import eca.data.AbstractDataLoader;
import org.apache.commons.lang3.StringUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Implements loading data from database.
 *
 * @author Roman Batygin
 */
public class JdbcQueryExecutor extends AbstractDataLoader<String> implements AutoCloseable {

    /**
     * Default relation name
     **/
    private static final String DEFAULT_RELATION_NAME = "Relation";

    private static final int RELATION_NAME_INDEX = 1;

    /**
     * Available column types of numeric attribute
     **/
    private static final int[] NUMERIC_TYPES = {Types.DOUBLE, Types.FLOAT,
            Types.INTEGER, Types.SMALLINT,
            Types.DECIMAL, Types.NUMERIC,
            Types.REAL, Types.TINYINT,
            Types.BIGINT};

    /**
     * Available column types of nominal attribute
     **/
    private static final int[] NOMINAL_TYPES = {Types.CHAR, Types.VARCHAR,
            Types.NCHAR, Types.NVARCHAR, Types.BOOLEAN, Types.BIT,
            Types.LONGVARCHAR, Types.LONGNVARCHAR};

    /**
     * Available column types of date attribute
     **/
    private static final int[] DATES_TYPES = {Types.DATE, Types.TIME,
            Types.TIMESTAMP};

    /**
     * Database connection object
     **/
    private Connection connection;

    /**
     * Datasource descriptor
     **/
    private ConnectionDescriptor connectionDescriptor;

    /**
     * Opens connection with database.
     *
     * @throws SQLException
     */
    public void open() throws SQLException, ClassNotFoundException {
        Class.forName(connectionDescriptor.getDriver());
        connection = DriverManager.getConnection(connectionDescriptor.getUrl(),
                connectionDescriptor.getLogin(), connectionDescriptor.getPassword());
    }


    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Returns <tt>ConnectionDescriptor</tt> object.
     *
     * @return <tt>ConnectionDescriptor</tt> object
     */
    public ConnectionDescriptor getConnectionDescriptor() {
        return connectionDescriptor;
    }

    /**
     * Sets <tt>ConnectionDescriptor</tt> object.
     *
     * @param connectionDescriptor <tt>ConnectionDescriptor</tt> object
     */
    public void setConnectionDescriptor(ConnectionDescriptor connectionDescriptor) {
        Objects.requireNonNull(connectionDescriptor, "Connection descriptor is not specified!");
        Objects.requireNonNull(connectionDescriptor.getDriver(), "Driver is not specified!");
        this.connectionDescriptor = connectionDescriptor;
    }

    @Override
    public Instances loadInstances() throws Exception {
        Instances data;
        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY); ResultSet result = statement.executeQuery(getSource())) {
            checkColumnsTypes(result);
            ResultSetMetaData meta = result.getMetaData();
            String tableName = meta.getTableName(RELATION_NAME_INDEX);
            data = new Instances(StringUtils.isEmpty(tableName) ? DEFAULT_RELATION_NAME
                    : tableName, createAttributes(result), result.getFetchSize());
            result.beforeFirst();

            while (result.next()) {
                Instance obj = new DenseInstance(data.numAttributes());
                obj.setDataset(data);
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    Attribute attribute = data.attribute(i - 1);
                    if (result.getObject(i) == null) {
                        obj.setValue(attribute, Utils.missingValue());
                    } else if (isNumeric(meta.getColumnType(i))) {
                        obj.setValue(attribute, result.getDouble(i));
                    } else if (isDate(meta.getColumnType(i))) {
                        obj.setValue(attribute, result.getDate(i).getTime());
                    } else {
                        String stringValue = result.getObject(i).toString().trim();
                        if (!StringUtils.isEmpty(stringValue)) {
                            obj.setValue(attribute, stringValue);
                        } else {
                            obj.setValue(attribute, Utils.missingValue());
                        }
                    }
                }
                data.add(obj);
            }

        }

        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    private void checkColumnsTypes(ResultSet result) throws SQLException {
        ResultSetMetaData meta = result.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (!isNumeric(meta.getColumnType(i)) && !isNominal(meta.getColumnType(i)) &&
                    !isDate(meta.getColumnType(i))) {
                throw new SQLException(String.format(DataBaseDictionary.BAD_COLUMN_TYPE_ERROR_FORMAT,
                        meta.getColumnTypeName(i)));
            }

        }
    }

    private boolean isNominal(int type) {
        return existType(NOMINAL_TYPES, type);
    }

    private boolean isDate(int type) {
        return existType(DATES_TYPES, type);
    }

    private boolean isNumeric(int type) {
        return existType(NUMERIC_TYPES, type);
    }

    private boolean existType(int[] types, int type) {
        for (int t : types) {
            if (type == t) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Attribute> createAttributes(ResultSet result) throws SQLException {
        ResultSetMetaData meta = result.getMetaData();
        ArrayList<Attribute> attr = new ArrayList<>(meta.getColumnCount());
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (isNumeric(meta.getColumnType(i))) {
                attr.add(new Attribute(meta.getColumnName(i)));
            } else if (isDate(meta.getColumnType(i))) {
                attr.add(new Attribute(meta.getColumnName(i), getDateFormat()));
            } else {
                attr.add(new Attribute(meta.getColumnName(i), createNominalAttribute(result, i)));
            }
        }
        return attr;
    }

    private ArrayList<String> createNominalAttribute(ResultSet result, int i) throws SQLException {
        ArrayList<String> values = new ArrayList<>();
        result.beforeFirst();
        while (result.next()) {
            Object value = result.getObject(i);
            if (value != null) {
                String trimValue = value.toString().trim();
                if (!StringUtils.isEmpty(trimValue) && !values.contains(trimValue)) {
                    values.add(trimValue);
                }
            }
        }
        return values;
    }

}