package io.github.libxposed.service;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.libxposed.service.exception.ServiceException;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
class RemotePreferences implements SharedPreferences {
    private static final String TAG = "RemotePreferences";

    private final String mGroup;
    private final Set<OnSharedPreferenceChangeListener> mListeners = Collections.newSetFromMap(new WeakHashMap<>());
    private volatile Map<String, Object> mMap;

    public RemotePreferences(String group, Map<String, ?> map) {
        this.mGroup = group;
        setMap(map);
    }

    private void setMap(Map<String, ?> map) {
        synchronized (this) {
            if (map == null) mMap = Collections.emptyMap();
            else mMap = Collections.unmodifiableMap(map);
        }
    }

    @Override
    public Map<String, ?> getAll() {
        return new TreeMap<>(mMap);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return (boolean) mMap.getOrDefault(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return (float) mMap.getOrDefault(key, defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return (int) mMap.getOrDefault(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return (long) mMap.getOrDefault(key, defValue);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return (String) mMap.getOrDefault(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return (Set<String>) mMap.getOrDefault(key, defValues);
    }

    @Override
    public boolean contains(String key) {
        return mMap.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new Editor(this);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.remove(listener);
    }

    static class Editor implements SharedPreferences.Editor {
        private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

        final Set<String> mDelete = new HashSet<>();
        final Map<String, Object> mPut = new HashMap<>();
        private final RemotePreferences mPrefs;

        private Editor(RemotePreferences prefs) {
            this.mPrefs = prefs;
        }

        @Override
        public void apply() {
            updateMemory();
            triggerListeners();
            EXECUTOR.execute(() -> XposedService.updateRemotePreferences(mPrefs.mGroup, this));
        }

        @Override
        public boolean commit() {
            updateMemory();
            triggerListeners();
            try {
                XposedService.updateRemotePreferences(mPrefs.mGroup, this);
                return true;
            } catch (ServiceException e) {
                Log.e(TAG, "Failed to commit changes to framework", e);
                return false;
            }
        }

        private void updateMemory() {
            synchronized(this) {
                Map<String, Object> newMap = new HashMap<>(mPrefs.mMap);
                mDelete.forEach(newMap::remove);
                newMap.putAll(mPut);
                mPrefs.setMap(newMap);
            }
        }

        private void triggerListeners() {
            Set<String> changes = new HashSet<>();
            changes.addAll(mDelete);
            changes.addAll(mPut.keySet());
            synchronized (mPrefs.mListeners) {
                for (OnSharedPreferenceChangeListener listener : mPrefs.mListeners) {
                    for (String change : changes) {
                        listener.onSharedPreferenceChanged(mPrefs, change);
                    }
                }
            }
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putString(String key, @Nullable String value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
            put(key, values);
            return this;
        }

        private void put(String key, Object value) {
            mPut.put(key, value);
            mDelete.remove(key);
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            mDelete.add(key);
            mPut.remove(key);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            mDelete.addAll(mPrefs.mMap.keySet());
            mPut.clear();
            return this;
        }
    }
}