package com.absmartly.android.sdk.cache;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.absmartly.sdk.json.ContextData;
import com.absmartly.sdk.json.PublishEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqliteAndroidLocalCache extends SQLiteOpenHelper {

    private static final String TAG = "ABSmartlyCache";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "absmartly.db";

    private final ObjectMapper mapper;

    public SqliteAndroidLocalCache(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS events (id INTEGER PRIMARY KEY AUTOINCREMENT, event TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS context (id INTEGER PRIMARY KEY AUTOINCREMENT, context TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS context");
        onCreate(db);
        Log.w(TAG, "Database upgraded by dropping and recreating tables");
    }

    @NonNull
    private String serialize(@NonNull Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to serialize " + value.getClass().getSimpleName(), e);
            throw new CacheSerializationException("Failed to serialize " + value.getClass().getSimpleName(), e);
        }
    }

    @Nullable
    private <T> T deserialize(@Nullable String json, @NonNull Class<T> clazz) {
        if (json == null) {
            Log.w(TAG, "Attempted to deserialize null JSON for " + clazz.getSimpleName());
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            Log.e(TAG, "Failed to deserialize " + clazz.getSimpleName() + ": " + json.substring(0, Math.min(100, json.length())), e);
            return null;
        }
    }

    @NonNull
    String serializeEvent(@NonNull PublishEvent event) {
        return serialize(event);
    }

    @Nullable
    PublishEvent deserializeEvent(@Nullable String eventStr) {
        return deserialize(eventStr, PublishEvent.class);
    }

    @NonNull
    String serializeContext(@NonNull ContextData context) {
        return serialize(context);
    }

    @Nullable
    ContextData deserializeContext(@Nullable String contextStr) {
        return deserialize(contextStr, ContextData.class);
    }

    public synchronized void writePublishEvent(@NonNull PublishEvent publishEvent) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.execSQL("INSERT INTO events (event) VALUES (?)", new Object[]{serializeEvent(publishEvent)});
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to write publish event", e);
            throw new CacheOperationException("Failed to write publish event", e);
        } catch (CacheSerializationException e) {
            Log.e(TAG, "Failed to serialize publish event", e);
            throw e;
        }
    }

    @NonNull
    public synchronized List<PublishEvent> retrievePublishEvents() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            try {
                cursor = db.rawQuery("SELECT event FROM events", null);
                List<PublishEvent> events = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String eventStr = cursor.getString(0);
                    PublishEvent event = deserializeEvent(eventStr);
                    if (event != null) {
                        events.add(event);
                    } else {
                        Log.w(TAG, "Skipping corrupted event data");
                    }
                }
                db.execSQL("DELETE FROM events");
                db.setTransactionSuccessful();
                return events;
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to retrieve publish events", e);
            throw new CacheOperationException("Failed to retrieve publish events", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized void writeContextData(@NonNull ContextData contextData) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM context");
                db.execSQL("INSERT INTO context (context) VALUES (?)", new Object[]{serializeContext(contextData)});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to write context data", e);
            throw new CacheOperationException("Failed to write context data", e);
        } catch (CacheSerializationException e) {
            Log.e(TAG, "Failed to serialize context data", e);
            throw e;
        }
    }

    @Nullable
    public synchronized ContextData getContextData() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("SELECT context FROM context ORDER BY id DESC LIMIT 1", null);
            if (cursor.moveToNext()) {
                String contextStr = cursor.getString(0);
                return deserializeContext(contextStr);
            }
            return null;
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to get context data", e);
            throw new CacheOperationException("Failed to get context data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static class CacheOperationException extends RuntimeException {
        public CacheOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CacheSerializationException extends RuntimeException {
        public CacheSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}