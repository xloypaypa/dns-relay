package online.xloypaypa.dns.relay.util;

import java.util.TimerTask;

public class TimerTaskWithConstructor extends TimerTask {

    private final Runnable runnable;

    public TimerTaskWithConstructor(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
