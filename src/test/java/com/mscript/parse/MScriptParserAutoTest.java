package com.mscript.parse;

import com.mscript.Function;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

public class MScriptParserAutoTest extends MScriptParserBaseTest {

    protected static final Logger logger = Logger.getLogger(MScriptParserAutoTest.class.getName());

    protected static final String NL = System.getProperty("line.separator", "\n");

    protected static final FileFilter MSCRIPT_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName().toLowerCase();
            return pathname.isFile() && (name.endsWith(".mscript") || name.endsWith(".mst") || name.endsWith(".ms"));
        }
    };

    @Test
    public void testScriptParsing() throws IOException {
        Function.loadLibrary("functions.properties");

        File scriptBaseDir = new File("mscript"); // automatically parse all MScript files in this directory
        if (!scriptBaseDir.isDirectory()) {
            fail(scriptBaseDir.getAbsolutePath() + " is not a directory or does not exist");
        }

        Map<String, Throwable> failures = new HashMap<>();

        logger.info("Parsing MScript files in " + scriptBaseDir.getAbsolutePath() + "..." + NL);
        for (File script : scriptBaseDir.listFiles(MSCRIPT_FILE_FILTER)) {
            String absolutePath = script.getAbsolutePath();
            logger.info("Parsing " + absolutePath + " ...");
            try {
                parse(script);
                logger.info("[OK]" + NL);
            } catch (Throwable throwable) {
                logger.info("[ERROR]" + NL);
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
        Function.define("foo", 0, 4);
        Function.define("bar.foo", 1, 2);
        parseText("$foo(1, 2, 3, 4); $foo('$bar.foo(1, 2 + 1)')");
    }
}
