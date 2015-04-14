package gov.vha.isaac.ochre.api.memory;

import gov.vha.isaac.ochre.api.ticker.Ticker;

/**
 * Created by kec on 4/9/15.
 */
public class HeapUseTicker {

    private static final Ticker ticker = new Ticker();

    public static void stop() {
        ticker.stop();
    }

    public static void start(int intervalInSeconds) {
        ticker.start(intervalInSeconds, (tick) -> System.out.println(MemoryUtil.getHeapPercentUse()));
    }

}
