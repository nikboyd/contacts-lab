package dev.educery.crypto;

import java.util.HashMap;
import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import dev.educery.context.SpringContext;
import dev.educery.utils.Logging;

/**
 * Symmetrically encrypts and decrypts data under AES.
 *
 * <h4>Symmetric Responsibilities:</h4>
 * <ul>
 * <li>knows which cryptographer supports a given kind of usage</li>
 * <li>knows an AES initialization vector</li>
 * <li>knows an AES key</li>
 * <li>encrypts clear text data under the configured IV and key</li>
 * <li>decrypts cypher data under the same IV and key</li>
 * </ul>
 *
 * <h4>Client Responsibilities:</h4>
 * <ul>
 * <li>properly configure an instance of this class with IV and key</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Symmetric implements Logging {

    /**
     * Initializes mappings from usage names to cryptographer names.
     */
    public static class Mapper implements Logging {

        static final String Comma = ",";
        static final String Equals = "=";
        static final String Separator = ";";

        /**
         * Initializes the name mappings.
         * @param mapping a formatted description of the mappings
         */
        public void setMapElements(String mapping) {
            String[] parts = mapping.split(Separator);
            for (String part : parts) {
                String[] map = part.trim().split(Equals);
                String[] terms = map[1].trim().split(Comma);
                for (String term : terms) {
                    CryptographerMap.put(term.trim(), map[0].trim());
                }
                report("registered cryptographer " + part.trim());
            }
        }
    }

    static final String ConfigurationFile = "cryptographers.xml";
    static final HashMap<String, String> CryptographerMap = new HashMap<>();
    static {
        Security.addProvider(new BouncyCastleProvider());
        SpringContext.named(ConfigurationFile).getBean(Mapper.class);
    }

    static final String Blank = " ";
    static final String Pad = Hex.encodeHexString(Blank.getBytes());

    static final byte[] EmptyBuffer = {};
    public Symmetric() { }

    /**
     * Returns the cryptographer configured to handle usage of a given kind.
     * @param usageName identifies a kind of usage
     * @return a Symmetric, or null if none was configured for the supplied usageName
     */
    public static Symmetric getCryptographer(String usageName) {
        String cryptName = CryptographerMap.get(usageName);
        return cryptName == null ? null : Symmetric.named(cryptName);
    }

    /**
     * Returns a configured cryptographer.
     * @param configuredName a configured cryptographer name.
     * @return a Symmetric, or null
     */
    public static Symmetric named(String configuredName) {
        return SpringContext.named(ConfigurationFile).getBean(Symmetric.class, configuredName);
    }

    /**
     * Returns a new Symmetric.
     * @param seedValue an AES initialization vector (32 hex digits)
     * @return a new Symmetric
     */
    public static Symmetric withSeed(String seedValue) {
        Symmetric result = new Symmetric();
        result.seedValue = checkLength(seedValue, BadSeed);
        return result;
    }

    /**
     * Sets the AES key.
     * @param keyValue an AES key (32 hex digits).
     * @return this Symmetric
     */
    public Symmetric withKey(String keyValue) { this.keyValue = checkLength(keyValue, BadKey); return this; }

    public String encryptAsHex(String clearText) { return Hex.encodeHexString(encrypt(clearText)); }
    public byte[] encrypt(String clearText) {
        try {
            return encryptBytes(clearText.getBytes(Encoding));
        } catch (Exception e) {
            error(e.getMessage(), e);
            return EmptyBuffer;
        }
    }

    public byte[] encryptBytes(byte[] clearData) {
        if (clearData == null || clearData.length == 0) return EmptyBuffer;

        try {
            clearData = normalize(clearData);
            return buildEncrypter().doFinal(clearData);
        } catch (Exception e) {
            error(e.getMessage(), e);
            return EmptyBuffer;
        }
    }

    public String decryptFromHex(String cypherText) {
        try {
            return decrypt(Hex.decodeHex(cypherText.toCharArray()));
        } catch (Exception e) {
            error(e.getMessage(), e);
            return Empty;
        }
    }

    public byte[] decryptBytes(byte[] cypherData) {
        if (cypherData == null || cypherData.length == 0) return EmptyBuffer;

        try {
            return buildDecrypter().doFinal(cypherData);
        } catch (Exception e) {
            error(e.getMessage(), e);
            return EmptyBuffer;
        }
    }

    static final String Encoding = "UTF-8";
    public String decrypt(byte[] cypherData) {
        try {
            return new String(buildDecrypter().doFinal(cypherData), Encoding).trim();
        } catch (Exception e) {
            error(e.getMessage(), e);
            return Empty;
        }
    }

    private String seedValue;
    public String getSeedValue() { return this.seedValue; }
    public void setSeedValue(String seedValue) { this.seedValue = seedValue; }

    private String keyValue;
    public String getKeyValue() { return this.keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    /**
     * Normalizes a buffer to the length needed for encryption.
     * @param clearData a buffer containing clear text data
     * @return a buffer containing padded clear text data
     * @throws Exception if raised during conversion
     */
    private static byte[] normalize(byte[] clearData) throws Exception {
        int length = clearData.length;
        int extra = length % BlockSize;
        if (extra < 1) return clearData;

        String hex = Hex.encodeHexString(clearData);
        int padding = BlockSize - extra;
        while (padding-- > 0) hex += Pad;
        return Hex.decodeHex(hex.toCharArray());
    }

    private Cipher buildEncrypter() throws Exception {
        Cipher result = getCipher();
        result.init(Cipher.ENCRYPT_MODE, buildKey(), buildSeed());
        return result;
    }

    private Cipher buildDecrypter() throws Exception {
        Cipher result = getCipher();
        result.init(Cipher.DECRYPT_MODE, buildKey(), buildSeed());
        return result;
    }

    static final String Algorithm = "AES";
    static final String Transform = Algorithm + "/CBC/NoPadding";
    private Cipher getCipher() throws Exception {
        return Cipher.getInstance(Transform, BouncyCastleProvider.PROVIDER_NAME);
    }

    private Key buildKey() throws Exception {
        return new SecretKeySpec(Hex.decodeHex(getKeyValue().toCharArray()), Algorithm);
    }

    private IvParameterSpec buildSeed() throws Exception {
        return new IvParameterSpec(Hex.decodeHex(getSeedValue().toCharArray()));
    }

    static final int BlockSize = 16;
    static final int ByteNibbles = 2;
    static final int VectorSize = BlockSize * ByteNibbles;

    @SuppressWarnings("unused")
    private static String checkLength(String value, String failMessage) {
        String result = StringUtils.defaultString(value).trim();
        if (result.length() != VectorSize) {
            throw new IllegalArgumentException(failMessage);
        }

        try {
            // test hex conversion
            byte[] bytes = Hex.decodeHex(result.toCharArray());
            int size = bytes.length;
        } catch (Exception e) {
            throw new IllegalArgumentException(failMessage);
        }

        return result;
    }

    private static final String BadSeed = "seed value must be a hex value of length " + VectorSize + " digits";
    private static final String BadKey = "key value must be a hex value of length " + VectorSize + " digits";

} // Symmetric
