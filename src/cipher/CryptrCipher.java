package cipher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * CryptrCipher supports key-based and password-based encryption/decryption using AES-256.
 *
 * For key-based encryption, it generates a secret key and initialization vector and then
 * stores them in a file. The same file is needed for decryption.
 *
 * For password-based encryption, it deterministically generates a secret key from a given
 * password. The same password is used to generate the same key during decryption.
 *
 * NOTE: An initialization vector is not used for password-based encryption, making it less
 * secure than key-based encryption.
 */
public class CryptrCipher {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KDF_ITERATIONS = 65536;
    private static final byte[] PEPPER = {-116, 55, 32, 63, -124, 39, 31, -21, 121, 45, -24, 27, 95, -47, -36, 110};

    /**
     * Represents the operations encrypt and decrypt for dual-use methods.
     */
    public static enum Mode {
        ENCRYPT, DECRYPT
    };

    /**
     * Encrypts a given file with key-based AES-256 encryption.
     *
     * @param inputFile - path of the file to encrypt
     * @param outputFile - path to store the encrypted file
     * @param keyFile - path to store the key file
     * @return -2 on failure
     *         -1 if JRE does not support essential operations
     *          0 on success
     *          1 on error reading input file
     *          2 on error writing output file
     *          3 on error writing key file
     */
    public static int encryptWithKey(String inputFile, String outputFile, String keyFile) {
        byte[] inputBytes, encryptedBytes;
        CryptrKey key;

        try {
            inputBytes = Files.readAllBytes(Paths.get(inputFile));
        }
        catch (IOException e) {
            return 1;
        }

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGen.init(KEY_LENGTH);
            key = new CryptrKey(keyGen.generateKey());
            IvParameterSpec ivps = new IvParameterSpec(key.iv);

            Cipher aesCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            aesCipher.init(Cipher.ENCRYPT_MODE, key.key, ivps);
            encryptedBytes = aesCipher.doFinal(inputBytes);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return -1;
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            return -2;
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(encryptedBytes);
        }
        catch (IOException e) {
            return 2;
        }

        try (ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(keyFile))) {
            oout.writeObject(key);
        }
        catch (IOException e) {
            return 3;
        }

        return 0;
    }

    /**
     * Decrypts a given file with key-based AES-256 decryption.
     *
     * @param encryptedFile - path of the file to decrypt
     * @param outputFile - path to store the decrypted file
     * @param keyFile - path of the key file generated during encryption
     * @return -2 on failure
     *         -1 if JRE does not support essential operations
     *          0 on success
     *          1 on error reading encrypted file
     *          2 on error writing output file
     *          3 on error reading key file
     */
    public static int decryptWithKey(String encryptedFile, String outputFile, String keyFile) {
        byte[] encryptedBytes, decryptedBytes;
        CryptrKey key;

        try {
            encryptedBytes = Files.readAllBytes(Paths.get(encryptedFile));
        }
        catch (IOException e) {
            return 1;
        }

        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(keyFile))) {
            key = (CryptrKey) oin.readObject();
        }
        catch (ClassNotFoundException e) {
            return -2;
        }
        catch (IOException e) {
            return 3;
        }

        try {
            IvParameterSpec ivps = new IvParameterSpec(key.iv);
            Cipher aesCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            aesCipher.init(Cipher.DECRYPT_MODE, key.key, ivps);
            decryptedBytes = aesCipher.doFinal(encryptedBytes);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return -1;
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            return -2;
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decryptedBytes);
        }
        catch (IOException e) {
            return 2;
        }

        return 0;
    }

    /**
     * Encrypts or decrypts a given file using password-based AES-256.
     *
     * @param inputFile - path to the file to encrypt or decrypt
     * @param outputFile - path to store the encrypted/decrypted output
     * @param password - password to use in secret key generation
     * @param mode - which operation to perform on the file
     * @return -2 on failure
     *         -1 if JRE does not support essential operations
     *          0 on success
     *          1 on error reading input file
     *          2 on error writing output file
     *          3 on error reading/writing key file
     */
    public static int cipherWithPassword(String inputFile, String outputFile, String password, Mode mode) {
        byte[] inputBytes, outputBytes;
        int cipherMode = mode == Mode.ENCRYPT ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        try {
            inputBytes = Files.readAllBytes(Paths.get(inputFile));
        }
        catch (IOException e) {
            return 1;
        }

        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), PEPPER, KDF_ITERATIONS, KEY_LENGTH);
            SecretKey temp = SecretKeyFactory.getInstance(KDF_ALGORITHM).generateSecret(keySpec);
            SecretKey key = new SecretKeySpec(temp.getEncoded(), ENCRYPTION_ALGORITHM);

            Cipher aesCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            aesCipher.init(cipherMode, key);
            outputBytes = aesCipher.doFinal(inputBytes);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return -1;
        }
        catch (InvalidKeySpecException | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException e) {
            return -2;
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(outputBytes);
        }
        catch (IOException e) {
            return 2;
        }

        return 0;
    }

}
