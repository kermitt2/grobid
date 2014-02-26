package org.grobid.core.utilities.counters.impl;


import org.grobid.core.utilities.counters.CntManager;

public class CntManagerFactory {
    public static CntManager getCntManager() {
        return new CntManagerImpl();
    }

    public static CntManager getNoOpCntManager() {
        return new NoOpCntManagerImpl();
    }
}
