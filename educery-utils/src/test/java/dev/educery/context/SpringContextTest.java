package dev.educery.context;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.*;

import static org.junit.Assert.*;
import dev.educery.utils.Logging;

/**
 * Confirms the proper operation of SpringContext.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class SpringContextTest implements Logging {

    /**
     * A test sample.
     */
    public static class Sample implements Logging {

        public int x = 0;
        public int getX() { return this.x; }
        public void setX(int x) { this.x = x; }

        public String name = "";
        public String getName() { return this.name; }
        public void setName(String value) { this.name = value; }

        static final String Dump = "bean: '%s' value: %d";
        public void dumpReport(String ... lookup) {
            if (getName().isEmpty()) {
                report(String.format(Dump, lookup[0], getX()));
            } else {
                report(String.format(Dump, getName(), getX()));
            }
        }

    } // Sample

    @Test public void loadTest() {
        // use standard naming
        Sample sample = SpringContext.getConfigured(Sample.class);
        assertTrue(sample != null);
        assertTrue(sample.getX() == 1);
        sample.dumpReport("");

        // use explicit naming
        sample = SpringContext.getConfigured(Sample.class, "AnotherSample");
        assertTrue(sample != null);
        assertTrue(sample.getX() == 2);
        sample.dumpReport("Another");

        // use default bean creation
        sample = SpringContext.getConfigured(Sample.class, "NotFound");
        assertTrue(sample != null);
        assertTrue(sample.getX() == 0);
        sample.dumpReport("NotFound");
    }

    static final String HexReport = "bytes: %s encoded: %s";
    @Test public void codecTest() {
        byte[] sample = {1, 3, 2, 4, 5, 14, 15, 120};
        byte[] encoded = Base64.encodeBase64(sample);
        byte[] decoded = Base64.decodeBase64(encoded);
        report(String.format(HexReport, Hex.encodeHexString(decoded), Hex.encodeHexString(encoded)));
    }

} // SpringContextTest
