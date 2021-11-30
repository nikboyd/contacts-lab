package dev.educery.crypto;

/**
 * Generates a long hash of text or byte data. Taken from:
 * https://www.javamex.com/tutorials/collections/strong_hash_code_implementation.shtml
 *
 * @author nik <nikboyd@sonic.net>
 */
public class LongHash {

    private static final long[] LookupTable = new long[256];
    static { initializeLookupTable(); }
    private static void initializeLookupTable() {
        long h = 0x544B2FBACAAF1684L;
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 31; j++) {
                h = (h >>> 7) ^ h;
                h = (h << 11) ^ h;
                h = (h >>> 10) ^ h;
            }
            LookupTable[i] = h;
        }
    }

    static final long HSTART = 0xBB40E64DA205B064L;
    static final long HMULT = 7664345821815920749L;

    public static long hash(byte[] data) {
        long h = HSTART;
        final long hmult = HMULT;
        final long[] ht = LookupTable;
        for (int len = data.length, i = 0; i < len; i++) {
            h = (h * hmult) ^ ht[data[i] & 0xff];
        }
        return h;
    }

    public static long hash(CharSequence cs) {
        long h = HSTART;
        final long hmult = HMULT;
        final long[] ht = LookupTable;
        final int len = cs.length();
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            h = (h * hmult) ^ ht[ch & 0xff];
            h = (h * hmult) ^ ht[(ch >>> 8) & 0xff];
        }
        return h;
    }

} // LongHash
