package cipher;

import java.io.Serializable;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

/**
 * CryptrKey holds a SecretKey and an initialization vector, and can be serialized
 * for permanent storage.
 */
public class CryptrKey implements Serializable {

    private static final long serialVersionUID = -5722145353306525208L;
    public SecretKey key;
    public byte[] iv;

    /**
     * Creates a new CryptrKey with the given SecretKey, and generates an initialization
     * vector in a cryptographically-secure manner.
     *
     * @param key - SecretKey to store in this CryptrKey
     */
    public CryptrKey(SecretKey key) {
        this.key = key;
        SecureRandom random = new SecureRandom();
        iv = new byte[16];
        random.nextBytes(iv);
    }

}
