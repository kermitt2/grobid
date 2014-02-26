package org.grobid.core.utilities.counters;

import java.io.Serializable;
import java.util.Map;

public interface CntManager extends Serializable {
    void i(Enum<?> e);
    void i(Enum<?> e, long val);
    void i(String group, String name);
    void i(String group, String name, long val);
    long cnt(Enum<?> e);
    long cnt(String group, String name);

    Counter getCounter(Enum<?> e);
    Counter getCounter(String group, String name);
    Map<String, Long> getCounters(Class<? extends Enum<?>> enumClass);
    Map<String, Long> getCounters(String group);
    Map<String, Map<String, Long>> getAllCounters();
    Map<String, Long> flattenAllCounters(String separator);

    void addMetric(String name, CntsMetric cntsMetric);
    void removeMetric(String name);
}
