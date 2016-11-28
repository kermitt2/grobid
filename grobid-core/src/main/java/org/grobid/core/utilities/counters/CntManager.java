package org.grobid.core.utilities.counters;

import org.grobid.core.engines.counters.Countable;

import java.io.Serializable;
import java.util.Map;

public interface CntManager extends Serializable {
    void i(Countable e);

    void i(Countable e, long val);

    void i(String group, String name);

    void i(String group, String name, long val);

    long cnt(Countable e);

    long cnt(String group, String name);

    Counter getCounter(Countable e);

    Counter getCounter(String group, String name);

    Map<String, Long> getCounters(Class<? extends Countable> enumClass);

    Map<String, Long> getCounters(String group);

    Map<String, Map<String, Long>> getAllCounters();

    Map<String, Long> flattenAllCounters(String separator);

    void addMetric(String name, CntsMetric cntsMetric);

    void removeMetric(String name);
}
