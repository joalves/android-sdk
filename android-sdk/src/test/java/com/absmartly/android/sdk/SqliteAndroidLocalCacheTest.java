package com.absmartly.android.sdk;

import android.content.Context;

import com.absmartly.android.sdk.cache.SqliteAndroidLocalCache;
import com.absmartly.sdk.json.ContextData;
import com.absmartly.sdk.json.Experiment;
import com.absmartly.sdk.json.PublishEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
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
        event.setHashed(true);
        event.setPublishedAt(System.currentTimeMillis());
        event.setUnits(new ArrayList<>());

        cache.writePublishEvent(event);

        List<PublishEvent> events = cache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals(event.isHashed(), events.get(0).isHashed());
        assertEquals(event.getPublishedAt(), events.get(0).getPublishedAt());
    }

    @Test
    public void testWriteMultiplePublishEvents() {
        PublishEvent event1 = new PublishEvent();
        event1.setHashed(true);
        event1.setPublishedAt(1000L);
        event1.setUnits(new ArrayList<>());

        PublishEvent event2 = new PublishEvent();
        event2.setHashed(false);
        event2.setPublishedAt(2000L);
        event2.setUnits(new ArrayList<>());

        cache.writePublishEvent(event1);
        cache.writePublishEvent(event2);

        List<PublishEvent> events = cache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(2, events.size());
        assertEquals(event1.isHashed(), events.get(0).isHashed());
        assertEquals(event2.isHashed(), events.get(1).isHashed());
    }

    @Test
    public void testRetrievePublishEventsClearsTable() {
        PublishEvent event = new PublishEvent();
        event.setHashed(true);
        event.setPublishedAt(System.currentTimeMillis());
        event.setUnits(new ArrayList<>());

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
        contextData.setExperiments(new ArrayList<Experiment>());

        cache.writeContextData(contextData);

        ContextData retrieved = cache.getContextData();

        assertNotNull(retrieved);
        assertNotNull(retrieved.getExperiments());
    }

    @Test
    public void testGetContextDataWhenEmpty() {
        ContextData retrieved = cache.getContextData();

        assertNull(retrieved);
    }

    @Test
    public void testWriteContextDataOverwritesPrevious() {
        ContextData contextData1 = new ContextData();
        List<Experiment> experiments1 = new ArrayList<>();
        Experiment exp1 = new Experiment();
        exp1.setId(1);
        exp1.setName("experiment1");
        experiments1.add(exp1);
        contextData1.setExperiments(experiments1);

        cache.writeContextData(contextData1);

        ContextData contextData2 = new ContextData();
        List<Experiment> experiments2 = new ArrayList<>();
        Experiment exp2 = new Experiment();
        exp2.setId(2);
        exp2.setName("experiment2");
        experiments2.add(exp2);
        contextData2.setExperiments(experiments2);

        cache.writeContextData(contextData2);

        ContextData retrieved = cache.getContextData();

        assertNotNull(retrieved);
        assertNotNull(retrieved.getExperiments());
        assertEquals(1, retrieved.getExperiments().size());
        assertEquals(2, retrieved.getExperiments().get(0).getId());
        assertEquals("experiment2", retrieved.getExperiments().get(0).getName());
    }

    @Test
    public void testSerializeDeserializeEvent() {
        PublishEvent original = new PublishEvent();
        original.setHashed(true);
        original.setPublishedAt(12345L);
        original.setUnits(new ArrayList<>());

        String serialized = cache.serializeEvent(original);

        assertNotNull(serialized);
        assertTrue(serialized.contains("hashed"));
        assertTrue(serialized.contains("publishedAt"));

        PublishEvent deserialized = cache.deserializeEvent(serialized);

        assertNotNull(deserialized);
        assertEquals(original.isHashed(), deserialized.isHashed());
        assertEquals(original.getPublishedAt(), deserialized.getPublishedAt());
    }

    @Test
    public void testSerializeDeserializeContext() {
        ContextData original = new ContextData();
        List<Experiment> experiments = new ArrayList<>();
        Experiment exp = new Experiment();
        exp.setId(123);
        exp.setName("test-experiment");
        experiments.add(exp);
        original.setExperiments(experiments);

        String serialized = cache.serializeContext(original);

        assertNotNull(serialized);
        assertTrue(serialized.contains("experiments"));

        ContextData deserialized = cache.deserializeContext(serialized);

        assertNotNull(deserialized);
        assertNotNull(deserialized.getExperiments());
        assertEquals(1, deserialized.getExperiments().size());
        assertEquals(123, deserialized.getExperiments().get(0).getId());
        assertEquals("test-experiment", deserialized.getExperiments().get(0).getName());
    }

    @Test
    public void testPersistenceAcrossInstances() {
        PublishEvent event = new PublishEvent();
        event.setHashed(true);
        event.setPublishedAt(99999L);
        event.setUnits(new ArrayList<>());

        cache.writePublishEvent(event);
        cache.close();

        SqliteAndroidLocalCache newCache = new SqliteAndroidLocalCache(context);
        List<PublishEvent> events = newCache.retrievePublishEvents();

        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals(event.isHashed(), events.get(0).isHashed());

        newCache.close();
    }

    @Test
    public void testContextDataPersistenceAcrossInstances() {
        ContextData contextData = new ContextData();
        List<Experiment> experiments = new ArrayList<>();
        Experiment exp = new Experiment();
        exp.setId(456);
        exp.setName("persistent-experiment");
        experiments.add(exp);
        contextData.setExperiments(experiments);

        cache.writeContextData(contextData);
        cache.close();

        SqliteAndroidLocalCache newCache = new SqliteAndroidLocalCache(context);
        ContextData retrieved = newCache.getContextData();

        assertNotNull(retrieved);
        assertNotNull(retrieved.getExperiments());
        assertEquals(1, retrieved.getExperiments().size());
        assertEquals(456, retrieved.getExperiments().get(0).getId());

        newCache.close();
    }
}
