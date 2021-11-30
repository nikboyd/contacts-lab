package dev.educery.crypto;

import org.junit.Test;
import static org.junit.Assert.*;
import dev.educery.utils.Logging;

/**
 * Confirms proper operation of symmetric cryptography.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class SymmetricTest implements Logging {

    private static final String InitialValue = "01234567012345670123456701234567";
    private static final String KeyValue = "76543210765432107654321076543210";

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void unconfiguredCrypto() {
        SecurityToken token = SecurityToken.named("unknown");
    }

    /**
     * Confirms symmetric crypto works.
     */
    @Test
    public void samples() {
        Symmetric crypto = Symmetric.withSeed(InitialValue).withKey(KeyValue);
        String clearText = "a clear text sample";
        String cypher = crypto.encryptAsHex(clearText);
        String sample = crypto.decryptFromHex(cypher);
        assertTrue(sample.equals(clearText));
    }

    /**
     * Confirms configured crypto works.
     */
    @Test public void namedCrypto() {
        Symmetric crypto = Symmetric.named("AuthCryptographer");
        String clearText = "a clear text sample";
        String cypher = crypto.encryptAsHex(clearText);
        String sample = crypto.decryptFromHex(cypher);
        assertTrue(sample.equals(clearText));
    }

    /**
     * Confirms that a perishable security token works.
     */
    @Test public void finiteToken() {
        SecurityToken token = SecurityToken.named("reset").with(10002);
        String tokenPackage = token.packaged();
        token.dumpToLog();

        SecurityToken result = SecurityToken.fromPackage(tokenPackage);
        assertTrue(result.getValue() == token.getValue());
        assertTrue(result.getName().equals(token.getName()));
        assertTrue(result.getTimestamp().equals(token.getTimestamp()));
        assertTrue(result.isValid());
        result.dumpToLog();

        waitForExpiration(token.withValidity(2));
        token.dumpToLog();
    }

    /**
     * Confirms that a non-perishable token works.
     */
    @Test public void foreverToken() {
        long[] tokenValues = {10200, 10300, 10400};
        SecurityToken token = SecurityToken.named("reset").withValidity(60 * 60 * 8).with(tokenValues);
        token.dumpToLog();

        String tokenPackage = token.packaged();
        report(tokenPackage);

        SecurityToken result = SecurityToken.fromPackage(tokenPackage);
        assertTrue(result.getTimestamp().equals(token.getTimestamp()));
        assertTrue(result.getName().equals(token.getName()));
        assertTrue(result.getValue(0) == token.getValue(0));
        assertTrue(result.getValue(1) == token.getValue(1));
        assertTrue(result.getValue(2) == token.getValue(2));
        assertTrue(result.isValid());
        result.dumpToLog();
    }

    @Test public void unpackagedToken() {
        String tokenName = "reset";
        long tokenLife = 8 * 60 * 60; // 8 hours
        SecurityToken token = SecurityToken.named(tokenName).withValidity(tokenLife).with(10002);
        String tokenPayload = token.toHex();
        report("unpackaged " + tokenPayload);

        SecurityToken result = SecurityToken.named(tokenName).withValues(tokenPayload);
        assertTrue(result.getValue() == token.getValue());
        assertTrue(result.getName().equals(token.getName()));
        assertTrue(result.getTimestamp().equals(token.getTimestamp()));
        assertTrue(result.isValid());
        result.dumpToLog();
    }

    /**
     * Waits for a valid token to expire.
     */
    private void waitForExpiration(SecurityToken token) {
        if (!token.isValid()) return; // already expired
        // don't wait longer than 20 secs
        long duration = token.getValidity() * 1100;
        if (duration == 0 || duration > 20000) return;

        try {
            Thread.sleep(duration);
        } catch (Exception e) {
            error(e.getMessage(), e);
        }
    }

} // SymmetricTest
