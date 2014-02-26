package org.grobid.core.utilities.counters.impl;

import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.counters.CntsMetric;
import org.grobid.core.utilities.counters.Counter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class CntManagerImpl implements CntManager {
    private static final long serialVersionUID = 2305126306757162275L;

    private ConcurrentMap<String, ConcurrentMap<String, Counter>> enumCnts = new ConcurrentHashMap<String, ConcurrentMap<String, Counter>>();
    private ConcurrentMap<String, ConcurrentMap<String, Counter>> strCnts = new ConcurrentHashMap<String, ConcurrentMap<String, Counter>>();
    transient private ConcurrentMap<String, CntsMetric> metrics = null;

    private void checkGroupName(String groupName) {
        if (enumCnts.containsKey(groupName)) {
            throw new IllegalStateException("Group name " + groupName + " coincides with the enum type counter name");
        }
    }

    private void checkEnumClass(String enumClass) {
        if (strCnts.containsKey(enumClass)) {
            throw new IllegalStateException("Enum class name " + enumClass + " coincides with the string type counter name");
        }
    }

    @Override
    public void i(Enum<?> e) {
        i(e, 1);
    }

    @Override
    public void i(Enum<?> e, long val) {
        checkEnumClass(e.getDeclaringClass().getName());
        enumCnts.putIfAbsent(e.getDeclaringClass().getName(), new ConcurrentHashMap<String, Counter>());

        ConcurrentMap<String, Counter> cntMap = enumCnts.get(e.getDeclaringClass().getName());
        cntMap.putIfAbsent(e.name(), new CounterImpl());
        Counter cnt = cntMap.get(e.name());
        cnt.i(val);
    }

    @Override
    public void i(String group, String name) {
        i(group, name, 1);
    }

    @Override
    public void i(String group, String name, long val) {
        checkGroupName(group);

        strCnts.putIfAbsent(group, new ConcurrentHashMap<String, Counter>());
        ConcurrentMap<String, Counter> cntMap = strCnts.get(group);

//        if (cntMap == null) {
//            cntMap = new ConcurrentHashMap<String, Counter>();
//            Counter cnt = new CounterImpl(val);
//            cntMap.put(name, cnt);
//            strCnts.put(group, cntMap);
//            return;
//        }

        cntMap.putIfAbsent(name, new CounterImpl());
        Counter cnt = cntMap.get(name);

//        if (cnt == null) {
//            cnt = new CounterImpl();
//            cntMap.put(name, cnt);
//        }
        cnt.i(val);
    }

    @Override
    public long cnt(Enum<?> e) {
        Map<String, Counter> cntMap = enumCnts.get(e.getDeclaringClass().getName());
        if (cntMap == null) {
            return 0;
        }
        Counter cnt = cntMap.get(e.name());
        return cnt == null ? 0 : cnt.cnt();
    }

    @Override
    public long cnt(String group, String name) {
        Map<String, Counter> cntMap = strCnts.get(group);
        if (cntMap == null) {
            return 0;
        }
        Counter cnt = cntMap.get(name);
        return cnt == null ? 0 : cnt.cnt();
    }

    @Override
    public Counter getCounter(Enum<?> e) {
        checkEnumClass(e.getDeclaringClass().getName());
        enumCnts.putIfAbsent(e.getDeclaringClass().getName(), new ConcurrentHashMap<String, Counter>());

        ConcurrentMap<String, Counter> cntMap = enumCnts.get(e.getDeclaringClass().getName());
        cntMap.putIfAbsent(e.name(), new CounterImpl());

//        if (cntMap == null) {
//
//            cntMap = new ConcurrentHashMap<String, Counter>();
//            Counter cnt = new CounterImpl();
//            cntMap.put(e.name(), cnt);
//            enumCnts.put(e.getDeclaringClass().getName(), cntMap);
//            return cnt;
//        }

        //        if (cnt == null) {
//            cnt = new CounterImpl();
//            cntMap.put(e.name(), cnt);
//        }
        return cntMap.get(e.name());
    }

    @Override
    public Counter getCounter(String group, String name) {
        checkGroupName(group);
        strCnts.putIfAbsent(group, new ConcurrentHashMap<String, Counter>());
        ConcurrentMap<String, Counter> cntMap = strCnts.get(group);

//        if (cntMap == null) {
//            checkGroupName(group);
//            cntMap = new ConcurrentHashMap<String, Counter>();
//            Counter cnt = new CounterImpl();
//            cntMap.put(name, cnt);
//            strCnts.put(group, cntMap);
//            return cnt;
//        }

        cntMap.putIfAbsent(name, new CounterImpl());
        //        if (cnt == null) {
//            cnt = new CounterImpl();
//            cntMap.put(name, cnt);
//        }
        return cntMap.get(name);
    }

    @Override
    public Map<String, Long> getCounters(Class<? extends Enum<?>> enumClass) {
        Map<String, Long> toReturn = new ConcurrentHashMap<String, Long>();
        for (Enum<?> e : enumClass.getEnumConstants()) {
            toReturn.put(e.name(), getCounter(e).cnt());
        }
        return toReturn;
    }

    @Override
    public Map<String, Long> getCounters(String group) {
        Map<String, Long> toReturn = new ConcurrentHashMap<String, Long>();
        if (strCnts.containsKey(group)) {
            for (Map.Entry<String, Counter> e : strCnts.get(group).entrySet()) {
                toReturn.put(e.getKey(), e.getValue().cnt());
            }
        }
        return toReturn;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Map<String, Map<String, Long>> getAllCounters() {
        Map<String, Map<String, Long>> map = new ConcurrentHashMap<String, Map<String, Long>>();
        for (String e : enumCnts.keySet()) {
            try {
                map.put(e, getCounters((Class<? extends Enum<?>>) Class.forName(e)));
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException(e1);
            }
        }

        for (String e : strCnts.keySet()) {
            map.put(e, getCounters(e));
        }

        return map;
    }

    @Override
    public Map<String, Long> flattenAllCounters(String separator) {
        Map<String, Long> map = new HashMap<String, Long>();
        for (Map.Entry<String, Map<String, Long>> group : getAllCounters().entrySet()) {
            for (Map.Entry<String, Long> e : group.getValue().entrySet()) {
                map.put(group.getKey() + separator + e.getKey(), e.getValue());
            }
        }
        return map;
    }

    @Override
    public synchronized void addMetric(String name, CntsMetric cntsMetric) {
        if (metrics == null) {
            metrics = new ConcurrentHashMap<String, CntsMetric>();
        }
        metrics.put(name, cntsMetric);
    }

    @Override
    public synchronized void removeMetric(String name) {
        if (metrics == null) {
            metrics = new ConcurrentHashMap<String, CntsMetric>();
        }
        metrics.remove(name);
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder(1000);
        for (Map.Entry<String, Map<String, Long>> m : getAllCounters().entrySet()) {
            sb.append("\n************************************************************************************\n").
                    append("COUNTER: ").append(m.getKey()).append("\n************************************************************************************").
                    append("\n------------------------------------------------------------------------------------\n");
            int maxLength = 0;
            for (Map.Entry<String, Long> cs : m.getValue().entrySet()) {
                if (maxLength < cs.getKey().length()) {
                    maxLength = cs.getKey().length();
                }
            }


            for (Map.Entry<String, Long> cs : m.getValue().entrySet()) {
                sb.append("  ").append(cs.getKey()).append(": ").append(new String(new char[maxLength - cs.getKey().length()]).replace('\0', ' '))
                        .append(cs.getValue()).append("\n");
            }
            sb.append("====================================================================================\n");
        }

        if (metrics != null && !metrics.isEmpty()) {
            sb.append("\n++++++++++++++++++++++++++++++ METRICS +++++++++++++++++++++++++++++++++++++++++++++\n");
            for (Map.Entry<String, CntsMetric> e : metrics.entrySet()) {
                sb.append(e.getKey()).append(": ").append(e.getValue().getMetricString(this)).append("\n");
            }
        }
        sb.append("====================================================================================\n");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CntManagerImpl that = (CntManagerImpl) o;

        return !(enumCnts != null ? !enumCnts.equals(that.enumCnts) : that.enumCnts != null)
                && !(strCnts != null ? !strCnts.equals(that.strCnts) : that.strCnts != null);

    }

    @Override
    public int hashCode() {
        int result = enumCnts != null ? enumCnts.hashCode() : 0;
        result = 31 * result + (strCnts != null ? strCnts.hashCode() : 0);
        return result;
    }
}
