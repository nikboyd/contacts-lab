package dev.educery.codecs;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;
import dev.educery.utils.Logging;

/**
 * Confirms proper operation of codec classes.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class CodecTest implements Logging {

    private static final String Indent = "\n  ";

    @Test
    public void sampleMessages() {
        report(Indent + ValueMap.withMessages("a sample message").toJSON());
        report(Indent + ValueMap.withID("76543210-76543210-76543210").toJSON());
    }

    @Test
    public void sampleMap() {
        HashMap m = new HashMap();
        m.put("ddd", "000");
        m.put("eee", "111");
        m.put("fff", "222");

        String[] texts = {"aaa", "bbb", "ccc"};
        ValueMap vm = new ValueMap();
        vm.with("xxx", "yyy");
        vm.withAll("sss", texts);
        vm.with("mmm", m);
        vm.with("nnn", 5);
        String json = vm.toJSON();
        report(Indent + json);

        String ddd = vm.getValue("mmm.ddd");
        String sss = vm.getValue("sss[1]");
        Integer nnn = vm.getValue("nnn");
        report("ddd: " + ddd);
        report("sss[1]: " + sss);
        report("nnn: " + nnn);

        ValueMap result = ValueMap.fromJSON(json);
        assertFalse(result == null);
        assertTrue(result.resembles(vm));
        result.reportDifferences(vm);
    }

} //  CodecTest
