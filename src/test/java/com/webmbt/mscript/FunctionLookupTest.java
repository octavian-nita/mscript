package com.webmbt.mscript;

import org.junit.After;
import org.junit.Before;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 2.0, Mar 14, 2015
 */
public class FunctionLookupTest {

    protected Functions functions;

    @Before
    public void setUp() {
        functions = new Functions();
    }

    @After
    public void tearDown() {
        functions.clearCache();
        functions = null;
    }
}
