package eca.client;

import lombok.Builder;
import lombok.Data;

/**
 * Eca - service details model.
 *
 * @author Roman Batygin
 */
@Data
@Builder
public class EcaServiceDetails {

    /**
     * Api url
     */
    private String apiUrl;

    /**
     * Token url
     */
    private String tokenUrl;

    /**
     * Application id
     */
    private String clientId;

    /**
     * Application secret
     */
    private String clientSecret;

    /**
     * User login
     */
    private String userName;

    /**
     * User password
     */
    private String password;
}
