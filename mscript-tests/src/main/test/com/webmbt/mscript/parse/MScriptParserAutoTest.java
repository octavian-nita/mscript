package com.webmbt.mscript.parse;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

public class MScriptParserAutoTest extends MScriptParserBaseTest {

    protected static final Logger LOG = Logger.getLogger(MScriptParserAutoTest.class.getName());

    protected static final String NL = System.getProperty("line.separator", "\n");

    protected static final FileFilter MSCRIPT_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName().toLowerCase();
            return pathname.isFile() && (name.endsWith(".mscript") || name.endsWith(".mst") || name.endsWith(".ms"));
        }
    };

    @Ignore
    @Test
    public void testScriptParsing() throws IOException {
        File scriptBaseDir = new File("mscript"); // automatically parse all MScript files in this directory
        if (!scriptBaseDir.isDirectory()) {
            fail(scriptBaseDir.getAbsolutePath() + " is not a directory or does not exist");
        }

        Map<String, Throwable> failures = new HashMap<>();

        LOG.info("Parsing MScript files in " + scriptBaseDir.getAbsolutePath() + "..." + NL);
        for (File script : scriptBaseDir.listFiles(MSCRIPT_FILE_FILTER)) {
            String absolutePath = script.getAbsolutePath();
            LOG.info("Parsing " + absolutePath + " ...");
            try {
                parse(script);
                LOG.info("[FOUND]" + NL);
            } catch (Throwable throwable) {
                LOG.info("[ERROR]" + NL);
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
        parse("$g(1, 2, 3, 4); $g('$dataGen.randFromList('1,2,3', ',')')");
    }
}
