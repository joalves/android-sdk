package com.absmartly.android.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.absmartly.sdk.Context;
import com.absmartly.sdk.ContextConfig;
import com.absmartly.sdk.ContextDataProvider;
import com.absmartly.sdk.ContextEventHandler;
import com.absmartly.sdk.ContextEventLogger;
import com.absmartly.sdk.json.ContextData;
import com.absmartly.sdk.json.Experiment;
import com.absmartly.sdk.json.PublishEvent;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import java8.util.concurrent.CompletableFuture;

public class ContextLifecycleTest {

    private Map<String, String> units;
    private ContextData contextData;
    private ContextDataProvider dataProvider;
    private ContextEventHandler eventHandler;
    private ContextEventLogger eventLogger;

    @Before
    public void setUp() {
        units = new HashMap<>();
        units.put("session_id", "e791e240fcd3df7d238cfc285f475e8152fcc0ec");
        units.put("user_id", "123456789");
        units.put("email", "bleh@absmartly.com");

        contextData = new ContextData();
        contextData.experiments = new Experiment[0];

        dataProvider = mock(ContextDataProvider.class);
        eventHandler = mock(ContextEventHandler.class);
        eventLogger = mock(ContextEventLogger.class);

        CompletableFuture<Void> publishFuture = CompletableFuture.completedFuture(null);
        when(eventHandler.publish(any(), any())).thenReturn(publishFuture);
    }

    private Context createContext(CompletableFuture<ContextData> dataFuture) {
        ContextConfig config = ContextConfig.create()
                .setDataProvider(dataProvider)
                .setEventHandler(eventHandler)
                .setEventLogger(eventLogger)
                .setUnits(units);

        return new Context(config, dataFuture);
    }

    private Context createReadyContext() {
        CompletableFuture<ContextData> dataFuture = CompletableFuture.completedFuture(contextData);
        return createContext(dataFuture);
    }

    @Test
    public void testConstructorSetsOverrides() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("exp_test", 2);
        overrides.put("exp_test_1", 1);

        ContextConfig config = ContextConfig.create()
                .setDataProvider(dataProvider)
                .setEventHandler(eventHandler)
                .setUnits(units)
                .setOverrides(overrides);

        CompletableFuture<ContextData> dataFuture = CompletableFuture.completedFuture(contextData);
        Context context = new Context(config, dataFuture);

        for (Map.Entry<String, Integer> entry : overrides.entrySet()) {
            assertEquals(entry.getValue(), context.getOverride(entry.getKey()));
        }
    }

    @Test
    public void testConstructorSetsCustomAssignments() {
        Map<String, Integer> customAssignments = new HashMap<>();
        customAssignments.put("exp_test", 2);
        customAssignments.put("exp_test_1", 1);

        ContextConfig config = ContextConfig.create()
                .setDataProvider(dataProvider)
                .setEventHandler(eventHandler)
                .setUnits(units)
                .setCustomAssignments(customAssignments);

        CompletableFuture<ContextData> dataFuture = CompletableFuture.completedFuture(contextData);
        Context context = new Context(config, dataFuture);

        for (Map.Entry<String, Integer> entry : customAssignments.entrySet()) {
            assertEquals(entry.getValue(), context.getCustomAssignment(entry.getKey()));
        }
    }

    @Test
    public void testBecomesReadyWithCompletedFuture() {
        Context context = createReadyContext();
        assertTrue(context.isReady());
        assertSame(contextData, context.getData());
    }

    @Test
    public void testBecomesReadyAndFailedWithCompletedExceptionallyFuture() {
        CompletableFuture<ContextData> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new Exception("FAILED"));

        Context context = createContext(failedFuture);
        assertTrue(context.isReady());
        assertTrue(context.isFailed());
    }

    @Test
    public void testBecomesReadyAndFailedWithException() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        assertFalse(context.isReady());
        assertFalse(context.isFailed());

        dataFuture.completeExceptionally(new Exception("FAILED"));
        context.waitUntilReady();

        assertTrue(context.isReady());
        assertTrue(context.isFailed());
    }

    @Test
    public void testCallsEventLoggerWhenReady() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        dataFuture.complete(contextData);
        context.waitUntilReady();

        verify(eventLogger, timeout(5000).times(1))
                .handleEvent(context, ContextEventLogger.EventType.Ready, contextData);
    }

    @Test
    public void testCallsEventLoggerWithCompletedFuture() {
        Context context = createReadyContext();

        verify(eventLogger, timeout(5000).times(1))
                .handleEvent(context, ContextEventLogger.EventType.Ready, contextData);
    }

    @Test
    public void testCallsEventLoggerWithException() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        Exception error = new Exception("FAILED");
        dataFuture.completeExceptionally(error);
        context.waitUntilReady();

        verify(eventLogger, timeout(5000).times(1))
                .handleEvent(context, ContextEventLogger.EventType.Error, error);
    }

    @Test
    public void testWaitUntilReady() throws InterruptedException {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        assertFalse(context.isReady());

        Thread completer = new Thread(() -> dataFuture.complete(contextData));
        completer.start();

        context.waitUntilReady();
        completer.join();

        assertTrue(context.isReady());
        assertSame(contextData, context.getData());
    }

    @Test
    public void testWaitUntilReadyWithCompletedFuture() {
        Context context = createReadyContext();
        assertTrue(context.isReady());

        context.waitUntilReady();
        assertSame(contextData, context.getData());
    }

    @Test
    public void testWaitUntilReadyAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        assertFalse(context.isReady());

        CompletableFuture<Context> readyFuture = context.waitUntilReadyAsync();
        assertFalse(context.isReady());

        Thread completer = new Thread(() -> dataFuture.complete(contextData));
        completer.start();

        readyFuture.join();
        completer.join();

        assertTrue(context.isReady());
        assertSame(context, readyFuture.get());
        assertSame(contextData, context.getData());
    }

    @Test
    public void testWaitUntilReadyAsyncWithCompletedFuture() throws ExecutionException, InterruptedException {
        Context context = createReadyContext();
        assertTrue(context.isReady());

        CompletableFuture<Context> readyFuture = context.waitUntilReadyAsync();
        readyFuture.join();

        assertTrue(context.isReady());
        assertSame(context, readyFuture.get());
        assertSame(contextData, context.getData());
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForPeekTreatment() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        assertFalse(context.isReady());
        assertFalse(context.isFailed());

        context.peekTreatment("exp_test_ab");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForGetTreatment() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        context.getTreatment("exp_test_ab");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForGetData() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        context.getData();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForGetExperiments() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        context.getExperiments();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForGetVariableValue() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        context.getVariableValue("banner.border", 17);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForPeekVariableValue() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        context.peekVariableValue("banner.border", 17);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenNotReadyForGetVariableKeys() {
        CompletableFuture<ContextData> dataFuture = new CompletableFuture<>();
        Context context = createContext(dataFuture);

        context.getVariableKeys();
    }

    @Test
    public void testSetAndGetUnit() {
        Context context = createReadyContext();

        context.setUnit("test_unit", "test_value");
        assertEquals("test_value", context.getUnit("test_unit"));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetUnitThrowsOnDuplicateType() {
        Context context = createReadyContext();

        context.setUnit("session_id", "new_session_id");
    }

    @Test
    public void testSetUnitAcceptsSameValue() {
        Context context = createReadyContext();

        context.setUnit("session_id", "e791e240fcd3df7d238cfc285f475e8152fcc0ec");
    }

    @Test
    public void testSetAndGetAttribute() {
        Context context = createReadyContext();

        context.setAttribute("test_attr", "test_value");
        assertEquals("test_value", context.getAttribute("test_attr"));
    }

    @Test
    public void testSetAttributes() {
        Context context = createReadyContext();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", 123);

        context.setAttributes(attributes);

        assertEquals("value1", context.getAttribute("attr1"));
        assertEquals(123, context.getAttribute("attr2"));
    }

    @Test
    public void testClosingState() {
        Context context = createReadyContext();
        assertTrue(context.isReady());
        assertFalse(context.isFailed());

        Map<String, Object> properties = new HashMap<>();
        properties.put("amount", 125);
        context.track("goal1", properties);

        CompletableFuture<Void> publishFuture = new CompletableFuture<>();
        when(eventHandler.publish(any(), any())).thenReturn(publishFuture);

        context.closeAsync();

        assertTrue(context.isClosing());
        assertFalse(context.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenClosingForSetAttribute() {
        Context context = createReadyContext();

        Map<String, Object> properties = new HashMap<>();
        properties.put("amount", 125);
        context.track("goal1", properties);

        CompletableFuture<Void> publishFuture = new CompletableFuture<>();
        when(eventHandler.publish(any(), any())).thenReturn(publishFuture);

        context.closeAsync();
        assertTrue(context.isClosing());

        context.setAttribute("attr1", "value1");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenClosingForSetUnit() {
        Context context = createReadyContext();

        Map<String, Object> properties = new HashMap<>();
        properties.put("amount", 125);
        context.track("goal1", properties);

        CompletableFuture<Void> publishFuture = new CompletableFuture<>();
        when(eventHandler.publish(any(), any())).thenReturn(publishFuture);

        context.closeAsync();
        assertTrue(context.isClosing());

        context.setUnit("test", "test");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenClosingForSetOverride() {
        Context context = createReadyContext();

        Map<String, Object> properties = new HashMap<>();
        properties.put("amount", 125);
        context.track("goal1", properties);

        CompletableFuture<Void> publishFuture = new CompletableFuture<>();
        when(eventHandler.publish(any(), any())).thenReturn(publishFuture);

        context.closeAsync();
        assertTrue(context.isClosing());

        context.setOverride("exp_test", 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenClosedForGetTreatment() {
        Context context = createReadyContext();
        context.close();

        assertTrue(context.isClosed());

        context.getTreatment("exp_test_ab");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenClosedForSetAttribute() {
        Context context = createReadyContext();
        context.close();

        assertTrue(context.isClosed());

        context.setAttribute("attr1", "value1");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsWhenClosedForTrack() {
        Context context = createReadyContext();
        context.close();

        assertTrue(context.isClosed());

        context.track("goal1", null);
    }

    @Test
    public void testSetOverride() {
        Context context = createReadyContext();

        context.setOverride("exp_test", 2);
        assertEquals(Integer.valueOf(2), context.getOverride("exp_test"));
    }

    @Test
    public void testSetOverrides() {
        Context context = createReadyContext();

        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("exp_test", 2);
        overrides.put("exp_test_1", 1);

        context.setOverrides(overrides);

        for (Map.Entry<String, Integer> entry : overrides.entrySet()) {
            assertEquals(entry.getValue(), context.getOverride(entry.getKey()));
        }
    }

    @Test
    public void testSetCustomAssignment() {
        Context context = createReadyContext();

        context.setCustomAssignment("exp_test", 2);
        assertEquals(Integer.valueOf(2), context.getCustomAssignment("exp_test"));
    }

    @Test
    public void testSetCustomAssignments() {
        Context context = createReadyContext();

        Map<String, Integer> assignments = new HashMap<>();
        assignments.put("exp_test", 2);
        assignments.put("exp_test_1", 1);

        context.setCustomAssignments(assignments);

        for (Map.Entry<String, Integer> entry : assignments.entrySet()) {
            assertEquals(entry.getValue(), context.getCustomAssignment(entry.getKey()));
        }
    }
}
