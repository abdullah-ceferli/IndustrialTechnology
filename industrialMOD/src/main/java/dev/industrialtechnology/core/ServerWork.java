package dev.industrialtechnology.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** 1.7.10 SimpleImpl runs on Netty; all packet mutations are queued to the server tick. */
public final class ServerWork {
    private static final ConcurrentLinkedQueue<Runnable> QUEUE = new ConcurrentLinkedQueue<Runnable>();
    private static final AtomicInteger SIZE = new AtomicInteger();
    public static void enqueue(Runnable task) {
        if (SIZE.incrementAndGet() > 2048) { SIZE.decrementAndGet(); return; }
        QUEUE.add(task);
    }
    public static void clear() { QUEUE.clear(); SIZE.set(0); }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        for (int i = 0; i < 256; i++) {
            Runnable task = QUEUE.poll();
            if (task == null) break;
            SIZE.decrementAndGet();
            task.run();
        }
    }
}
