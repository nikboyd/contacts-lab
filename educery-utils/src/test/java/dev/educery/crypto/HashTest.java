package dev.educery.crypto;

import org.junit.Test;
import static org.junit.Assert.*;
import dev.educery.utils.Logging;

/**
 * Confirms proper operation of the long hash generator.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class HashTest implements Logging {

    @Test public void hashSamples() {
        reportHash(Empty);
        reportHash("aaa");
        reportHash("bbb");
        reportHash("888-888-8888");
        reportHash("George Jungleman");
        reportHash("walk a mile in my shoes");
        reportHash("a quick brown fox jumped over the lazy dog");
    }

    static final String LogForm = "hash %d = '%s'";
    void reportHash(String value) {
        long test = LongHash.hash(value);
        report(String.format(LogForm, test, value));
    }

} // HashTest
