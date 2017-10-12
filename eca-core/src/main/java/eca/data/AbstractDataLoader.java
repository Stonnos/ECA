package eca.data;

import org.springframework.util.Assert;

/**
 * Abstract data loader class.
 * @author Roman Batygin
 */

public abstract class AbstractDataLoader implements DataLoader {

    /**
     * Date format
     */
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * Returns date format.
     * @return date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets date format.
     * @param dateFormat date format
     */
    public void setDateFormat(String dateFormat) {
        Assert.notNull(dateFormat, "Date format is not specified!");
        this.dateFormat = dateFormat;
    }
}
