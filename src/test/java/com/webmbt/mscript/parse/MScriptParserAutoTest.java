package com.webmbt.mscript.parse;

import com.webmbt.mscript.Function;
import com.webmbt.mscript.Functions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

public class MScriptParserAutoTest extends MScriptParserBaseTest {

    protected static final Logger log = Logger.getLogger(MScriptParserAutoTest.class.getName());

    protected static final String NL = System.getProperty("line.separator", "\n");

    protected static final FileFilter MSCRIPT_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName().toLowerCase();
            return pathname.isFile() && (name.endsWith(".mscript") || name.endsWith(".mst") || name.endsWith(".ms"));
        }
    };

    protected Functions library;

    @Before
    public void setUp() {
        library = new Functions();
    }

    @Test
    public void testScriptParsing() throws IOException {
        library.load("functions.properties");

        File scriptBaseDir = new File("mscript"); // automatically parse all MScript files in this directory
        if (!scriptBaseDir.isDirectory()) {
            fail(scriptBaseDir.getAbsolutePath() + " is not a directory or does not exist");
        }

        Map<String, Throwable> failures = new HashMap<>();

        log.info("Parsing MScript files in " + scriptBaseDir.getAbsolutePath() + "..." + NL);
        for (File script : scriptBaseDir.listFiles(MSCRIPT_FILE_FILTER)) {
            String absolutePath = script.getAbsolutePath();
            log.info("Parsing " + absolutePath + " ...");
            try {
                parse(script, library);
                log.info("[FOUND]" + NL);
            } catch (Throwable throwable) {
                log.info("[ERROR]" + NL);
                failures.put(absolutePath, throwable);
            }
        }

        if (failures.size() > 0) {
            StringBuilder buff = new StringBuilder("Failed to parse the following scripts:").append(NL);
            for (Map.Entry<String, Throwable> failure : failures.entrySet()) {
                buff.append("  -> ").append(failure.getKey()).append(": ").append(failure.getValue()).append(NL);
            }
            fail(buff.toString());
        }
    }

    @Test
    public void testStringInterpolatedFunctionCall() throws IOException {
        library.add(new Function("foo", 0, 4));
        library.add(new Function("foo", "bar", 1, 2));
        parseText("$foo(1, 2, 3, 4); $foo('$bar.foo(1, 2 + 1)')", library);
    }
}
