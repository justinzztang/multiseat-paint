package model.helpers;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryEmergency {
    private static final double MAX_USAGE = 0.85; //percent of heap used

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    public static boolean memoryEmergency() {
        Runtime.getRuntime().gc();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        long max = heap.getMax();
        return (double) heap.getUsed() / max >= MAX_USAGE;
    }

}
