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

    protected static final Logger log = Logger.getLogger(MScriptParserAutoTest.class.getName());

    protected static final String NL = System.getProperty("line.separator", "\n");

    protected static final FileFilter M_SCRIPT_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName();
            return pathname.isFile() && (name.endsWith(".mscript") || name.endsWith(".mst") || name.endsWith(".ms"));
        }
    };

    @Test
    public void testScriptParsing() throws IOException {
        Function.loadLibrary("pluginFuncList.properties");

        File scriptBaseDir = new File("mscript");
        if (!scriptBaseDir.isDirectory()) {
            fail(scriptBaseDir.getAbsolutePath() + " is not a directory or does not exist");
        }

        Map<String, Throwable> failures = new HashMap<>();

        log.info("Parsing MScript files in " + scriptBaseDir.getAbsolutePath() + "..." + NL);
        for (File script : scriptBaseDir.listFiles(M_SCRIPT_FILE_FILTER)) {
            String absolutePath = script.getAbsolutePath();
            log.info("Parsing " + absolutePath + " ...");
            try {
                parse(script);
                log.info("[OK]" + NL);
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
        Function.define("foo", 0, 4);
        Function.define("bar.foo", 1, 2);
        parseText("$foo(1, 2, 3, 4); $foo('$bar.foo(1, 2 + 1)')");
    }
}
