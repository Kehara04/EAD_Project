package com.smartsolar;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

//Contains instrumented tests that run on an Android device or emulator.
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    // Verifies that the application context has the correct package name.
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.smartsolar", appContext.getPackageName());
    }
}