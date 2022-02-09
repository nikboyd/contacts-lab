package dev.educery.crypto;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import org.apache.commons.codec.binary.Hex;
import dev.educery.utils.Logging;

/**
 * Contains a cryptographically secured payload.
 *
 * <h4>SecurityToken Responsibilities:</h4>
 * <ul>
 * <li>knows a token creation timestamp</li>
 * <li>knows some long value(s), often a single value</li>
 * <li>decrypts the contents of a token from hex</li>
 * <li>encrypts the contents of a token to hex</li>
 * <li>packages the contents of a token for usage</li>
 * <li>unpacks the contents of a token from a package</li>
 * </ul>
 *
 * <h4>Client Responsibilities:</h4>
 * <ul>
 * <li>properly configure the cryptography used</li>
 * <li>properly configure a map of token names</li>
 * <li>supply the values or hex content of a token during construction</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class SecurityToken implements Logging {

    /**
     * Formats token time stamps.
     */
    public static final DateTimeFormatter TokenTimestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // normalize times using UTC
    static final String StandardZoneName = "UTC";
    static final ZoneId StandardZone = ZoneId.of(StandardZoneName);
    static final ZoneOffset StandardOffset = ZoneOffset.UTC;

    public static LocalDateTime now() { return LocalDateTime.now(); }
    public static long timeNow() { return now().toInstant(StandardOffset).toEpochMilli(); }
    public static LocalDateTime timeFrom(long msecs) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(msecs), StandardZone); }

    static final String Equals = "=";
    static final String Zeros = "0000000000000000";
    static final int LongNibbles = Zeros.length();
    static final int HexBase = 16;

    static final long Milliseconds = 1000;
    static final long StandardValidity = 5 * 60 * Milliseconds; // 5 mins (in msecs)

    static final int TimestampIndex = 0;
    static final int ValidityIndex  = 1;
    static final int StandardValues = 2;

    private void validity(long secs) { if (secs >= 0) this.values[ValidityIndex] = secs * Milliseconds; }
    private long validity() { return this.values[ValidityIndex] / Milliseconds; }
    private long[] values = { timeNow(), StandardValidity, 0 };
    private String tokenName = "";

    /**
     * Returns a token derived from a token package.
     * @param tokenPackage contains a token name and encrypted token content
     * @return a SecurityToken
     */
    public static SecurityToken fromPackage(String tokenPackage) {
        String[] parts = tokenPackage.split(Equals);
        SecurityToken result = SecurityToken.named(parts[0]);
        return result.withValues(parts[1]);
    }

    /**
     * Returns a named token.
     * @param tokenName a token name
     * @return a SecurityToken
     */
    public static SecurityToken named(String tokenName) {
        SecurityToken result = new SecurityToken();
        result.tokenName = tokenName;
        result.checkCryptographer();
        return result.with(0);
    }

    /**
     * Decrypts the values for this token.
     * @param cryptText contains the token values
     * @return this SecurityToken
     */
    public SecurityToken withValues(String cryptText) {
        String hexBuffer = getCryptographer().decryptFromHex(cryptText);
        int count = hexBuffer.length() / LongNibbles;
        this.values = new long[count];
        for (int index = 0; index < count; index++) {
            int pos = index * LongNibbles;
            int end = pos + LongNibbles;
            String valueHex = hexBuffer.substring(pos, end);
            this.values[index] = Long.parseLong(valueHex, HexBase);
        }
        return this;
    }

    public boolean isExpired() { return !this.isValid(); }
    public boolean isValid() { return getValidity() == 0 ? true : getExpirationTime().isAfter(now()); }
    public SecurityToken withValidity(long secs) { validity(secs); return this; }
    public long getValidity() { return validity(); }

    public LocalDateTime getExpirationTime() { return getTimestamp().plus(values[ValidityIndex], ChronoUnit.MILLIS); }
    public String formatExpirationTime() { return TokenTimestampFormat.format(getExpirationTime()); }

    /**
     * Adds a value to this token.
     * @param value a value
     * @return this SecurityToken
     */
    public SecurityToken with(long value) { this.values[StandardValues] = value; return this; }

    /**
     * Includes some values in this token.
     * @param values some values
     * @return this SecurityToken
     */
    public SecurityToken with(long[] values) {
        long[] oldValues = this.values;
        this.values = new long[values.length + StandardValues];
        this.values[TimestampIndex] = oldValues[TimestampIndex];
        this.values[ValidityIndex] = oldValues[ValidityIndex];
        System.arraycopy(values, 0, this.values, StandardValues, values.length);
        return this;
    }

    /**
     * Encrypts the content of this token.
     * @return encrypted token content
     */
    public byte[] toBytes() {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < this.values.length; index++) {
            String hex = Long.toHexString(values[index]);
            int padWidth = Zeros.length() - hex.length();
            builder.append(Zeros.substring(0, padWidth));
            builder.append(hex);
        }
        String hexBuffer = builder.toString();
        return getCryptographer().encrypt(hexBuffer);
    }

    public String toHex() { return Hex.encodeHexString(this.toBytes()); }
    public String packaged() { return this.tokenName + Equals + this.toHex(); }

    public String getName() { return this.tokenName; }
    public LocalDateTime getTimestamp() { return timeFrom(this.values[TimestampIndex]); }

    public long getValue() { return this.values[StandardValues]; }
    public long getValue(int index) {
        int actualIndex = index + StandardValues;
        if (actualIndex < 0 || actualIndex > this.values.length) return 0;
        return this.values[actualIndex];
    }

    public void dumpToLog() { report(this.toString()); }
    @Override public String toString() { return getName() + formatExpirationMessage() + formatValues(); }

    private String formatExpirationMessage() {
        if (getValidity() == 0) return " token good forever";
        return (this.isValid() ? " token good until " : " token expired at ") + formatExpirationTime();
    }

    private String formatValues() {
        long[] copy = Arrays.copyOfRange(this.values, StandardValues, this.values.length);
        return " " + Arrays.toString(copy);
    }

    static final String MissingSymmetry = "No Symmetric cryptographer was configured to handle ";
    private void checkCryptographer() {
        if (getCryptographer() == null) {
            throw new IllegalArgumentException(MissingSymmetry + tokenName);
        }
    }

    private Symmetric getCryptographer() { return Symmetric.getCryptographer(getName()); }

} // SecurityToken
