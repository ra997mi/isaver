package com.remmoo997.igtvsaver;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.remmoo997.igtvsaver", appContext.getPackageName());
    }
}