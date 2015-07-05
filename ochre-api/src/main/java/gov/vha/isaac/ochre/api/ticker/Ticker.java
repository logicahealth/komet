package gov.vha.isaac.ochre.api.ticker;

import gov.vha.isaac.ochre.api.memory.MemoryUtil;
import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Created by kec on 4/9/15.
 */
public class Ticker {

    private Subscription tickSubscription;

    public void start(int intervalInSeconds, Consumer consumer) {
        stop();
        tickSubscription = EventStreams.ticks(Duration.ofSeconds(intervalInSeconds))
                .subscribe(tick -> {
                    consumer.accept(tick);
                });
    }

    public void stop() {
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

}
