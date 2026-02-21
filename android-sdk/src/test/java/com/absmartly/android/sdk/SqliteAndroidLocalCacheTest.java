package com.absmartly.android.sdk;

import android.content.Context;

import com.absmartly.android.sdk.cache.SqliteAndroidLocalCache;
import com.absmartly.sdk.json.ContextData;
import com.absmartly.sdk.json.Experiment;
import com.absmartly.sdk.json.PublishEvent;
import com.absmartly.sdk.json.Unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SqliteAndroidLocalCacheTest {

    private SqliteAndroidLocalCache cache;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        cache = new SqliteAndroidLocalCache(context);
    }

    @After
    public void tearDown() {
        if (cache != null) {
            cache.close();
        }
    }

    @Test
    public void testWriteAndRetrievePublishEvent() {
        PublishEvent event = new PublishEvent();
        event.hashed = true;
        event.publishedAt = System.currentTimeMillis();
        event.units = new Unit[0];

        cache.writePublishEvent(event);

        List<PublishEvent> events = cache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals(event.hashed, events.get(0).hashed);
        assertEquals(event.publishedAt, events.get(0).publishedAt);
    }

    @Test
    public void testWriteMultiplePublishEvents() {
        PublishEvent event1 = new PublishEvent();
        event1.hashed = true;
        event1.publishedAt = 1000L;
        event1.units = new Unit[0];

        PublishEvent event2 = new PublishEvent();
        event2.hashed = false;
        event2.publishedAt = 2000L;
        event2.units = new Unit[0];

        cache.writePublishEvent(event1);
        cache.writePublishEvent(event2);

        List<PublishEvent> events = cache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(2, events.size());
        assertEquals(event1.hashed, events.get(0).hashed);
        assertEquals(event2.hashed, events.get(1).hashed);
    }

    @Test
    public void testRetrievePublishEventsClearsTable() {
        PublishEvent event = new PublishEvent();
        event.hashed = true;
        event.publishedAt = System.currentTimeMillis();
        event.units = new Unit[0];

        cache.writePublishEvent(event);

        List<PublishEvent> firstRetrieval = cache.retrievePublishEvents();
        assertEquals(1, firstRetrieval.size());

        List<PublishEvent> secondRetrieval = cache.retrievePublishEvents();
        assertNotNull(secondRetrieval);
        assertEquals(0, secondRetrieval.size());
    }

    @Test
    public void testRetrievePublishEventsWhenEmpty() {
        List<PublishEvent> events = cache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(0, events.size());
    }

    @Test
    public void testWriteAndGetContextData() {
        ContextData contextData = new ContextData();
        contextData.experiments = new Experiment[0];

        cache.writeContextData(contextData);

        ContextData retrieved = cache.getContextData();

        assertNotNull(retrieved);
        assertNotNull(retrieved.experiments);
    }

    @Test
    public void testGetContextDataWhenEmpty() {
        ContextData retrieved = cache.getContextData();

        assertNull(retrieved);
    }

    @Test
    public void testWriteContextDataOverwritesPrevious() {
        ContextData contextData1 = new ContextData();
        Experiment exp1 = new Experiment();
        exp1.id = 1;
        exp1.name = "experiment1";
        contextData1.experiments = new Experiment[]{exp1};

        cache.writeContextData(contextData1);

        ContextData contextData2 = new ContextData();
        Experiment exp2 = new Experiment();
        exp2.id = 2;
        exp2.name = "experiment2";
        contextData2.experiments = new Experiment[]{exp2};

        cache.writeContextData(contextData2);

        ContextData retrieved = cache.getContextData();

        assertNotNull(retrieved);
        assertNotNull(retrieved.experiments);
        assertEquals(1, retrieved.experiments.length);
        assertEquals(2, retrieved.experiments[0].id);
        assertEquals("experiment2", retrieved.experiments[0].name);
    }

    @Test
    public void testPersistenceAcrossInstances() {
        PublishEvent event = new PublishEvent();
        event.hashed = true;
        event.publishedAt = 99999L;
        event.units = new Unit[0];

        cache.writePublishEvent(event);
        cache.close();

        SqliteAndroidLocalCache newCache = new SqliteAndroidLocalCache(context);
        List<PublishEvent> events = newCache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals(event.hashed, events.get(0).hashed);

        newCache.close();
    }

    @Test
    public void testContextDataPersistenceAcrossInstances() {
        ContextData contextData = new ContextData();
        Experiment exp = new Experiment();
        exp.id = 456;
        exp.name = "persistent-experiment";
        contextData.experiments = new Experiment[]{exp};

        cache.writeContextData(contextData);
        cache.close();

        SqliteAndroidLocalCache newCache = new SqliteAndroidLocalCache(context);
        ContextData retrieved = newCache.getContextData();

        assertNotNull(retrieved);
        assertNotNull(retrieved.experiments);
        assertEquals(1, retrieved.experiments.length);
        assertEquals(456, retrieved.experiments[0].id);

        newCache.close();
    }
}
