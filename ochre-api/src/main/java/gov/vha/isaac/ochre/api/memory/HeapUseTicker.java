package gov.vha.isaac.ochre.api.memory;

import gov.vha.isaac.ochre.api.ticker.Ticker;
import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.function.Consumer;

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
