package org.grobid.core.utilities.counters.impl;

import de.smtdp.paler.util.counters.CntManager;

public class CntManagerFactory {
    public static CntManager getCntManager() {
        return new CntManagerImpl();
    }

    public static CntManager getNoOpCntManager() {
        return new NoOpCntManagerImpl();
    }
}
