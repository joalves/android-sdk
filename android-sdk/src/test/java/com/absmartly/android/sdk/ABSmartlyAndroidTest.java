package com.absmartly.android.sdk;

import android.content.Context;

import com.absmartly.android.sdk.cache.SqliteAndroidLocalCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ABSmartlyAndroidTest {

    private Context androidContext;
    private SqliteAndroidLocalCache cache;

    @Before
    public void setUp() {
        androidContext = RuntimeEnvironment.getApplication();
        cache = new SqliteAndroidLocalCache(androidContext);
    }

    @After
    public void tearDown() {
        if (cache != null) {
            cache.close();
        }
    }

    @Test
    public void builder_missingEndpoint_throwsIllegalStateException() {
        try {
            ABSmartlyAndroid.builder()
                    .apiKey("key")
                    .application("app")
                    .environment("env")
                    .context(androidContext)
                    .build();
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("endpoint"));
        }
    }

    @Test
    public void builder_missingApiKey_throwsIllegalStateException() {
        try {
            ABSmartlyAndroid.builder()
                    .endpoint("https://test.absmartly.io/v1")
                    .application("app")
                    .environment("env")
                    .context(androidContext)
                    .build();
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("apiKey"));
        }
    }

    @Test
    public void builder_missingApplication_throwsIllegalStateException() {
        try {
            ABSmartlyAndroid.builder()
                    .endpoint("https://test.absmartly.io/v1")
                    .apiKey("key")
                    .environment("env")
                    .context(androidContext)
                    .build();
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("application"));
        }
    }

    @Test
    public void builder_missingEnvironment_throwsIllegalStateException() {
        try {
            ABSmartlyAndroid.builder()
                    .endpoint("https://test.absmartly.io/v1")
                    .apiKey("key")
                    .application("app")
                    .context(androidContext)
                    .build();
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("environment"));
        }
    }

    @Test
    public void builder_missingAndroidContext_throwsIllegalStateException() {
        try {
            ABSmartlyAndroid.builder()
                    .endpoint("https://test.absmartly.io/v1")
                    .apiKey("key")
                    .application("app")
                    .environment("env")
                    .build();
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Context"));
        }
    }
}
