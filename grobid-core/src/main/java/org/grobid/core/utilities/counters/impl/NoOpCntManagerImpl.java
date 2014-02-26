package org.grobid.core.utilities.counters.impl;

import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.counters.CntsMetric;
import org.grobid.core.utilities.counters.Counter;

import java.util.Collections;
import java.util.Map;

class NoOpCntManagerImpl implements CntManager {
    @Override
    public void i(Enum<?> e) {
    }

    @Override
    public void i(Enum<?> e, long val) {
    }

    @Override
    public void i(String group, String name) {
    }

    @Override
    public void i(String group, String name, long val) {
    }

    @Override
    public long cnt(Enum<?> e) {
        return 0;
    }

    @Override
    public long cnt(String group, String name) {
        return 0;
    }

    @Override
    public Counter getCounter(Enum<?> e) {
        return new NoOpCounterImpl();
    }

    @Override
    public Counter getCounter(String group, String name) {
        return new NoOpCounterImpl();
    }

    @Override
    public Map<String, Long> getCounters(Class<? extends Enum<?>> enumClass) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> getCounters(String group) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Map<String, Long>> getAllCounters() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> flattenAllCounters(String separator) {
        return Collections.emptyMap();
    }

    @Override
    public void addMetric(String name, CntsMetric cntsMetric) {
    }

    @Override
    public void removeMetric(String name) {
    }
}
