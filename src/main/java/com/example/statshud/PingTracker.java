package com.example.statshud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Measures real round-trip time by sending our own ping packets several times per second.
 * Only replies to pings WE sent are counted (the game sends its own pings too, with a
 * different clock, which would otherwise produce garbage numbers).
 */
public final class PingTracker {
	private static final int INTERVAL_TICKS = 5; // 4 pings per second
	private static final int SAMPLES = 4;        // average over ~1 second
	private static final long STALE_MS = 5000;
	private static final long MAX_SANE_RTT_MS = 10_000;
	private static final int MAX_PENDING = 32;

	private static final ArrayDeque<Long> RTT = new ArrayDeque<>();
	private static final LinkedHashSet<Long> PENDING = new LinkedHashSet<>();
	private static long lastPongMs = 0;
	private static int tickCounter = 0;

	private PingTracker() {}

	private static long nowMs() {
		return System.nanoTime() / 1_000_000L;
	}

	public static synchronized void reset() {
		RTT.clear();
		PENDING.clear();
		lastPongMs = 0;
		tickCounter = 0;
	}

	/** Called every client tick. */
	public static void tick(Minecraft mc) {
		ClientPacketListener conn = mc.getConnection();
		if (conn == null || mc.player == null) return;
		if (++tickCounter >= INTERVAL_TICKS) {
			tickCounter = 0;
			long sent = nowMs();
			synchronized (PingTracker.class) {
				PENDING.add(sent);
				while (PENDING.size() > MAX_PENDING) {
					Iterator<Long> it = PENDING.iterator();
					it.next();
					it.remove();
				}
			}
			conn.send(new ServerboundPingRequestPacket(sent));
		}
	}

	/** Called when any pong arrives; sentMs is the value that was put in the request. */
	public static synchronized void onPong(long sentMs) {
		// Ignore pongs for pings we did not send, and the duplicate handler call for ones we did.
		if (!PENDING.remove(sentMs)) return;
		long now = nowMs();
		long rtt = now - sentMs;
		if (rtt < 0 || rtt > MAX_SANE_RTT_MS) return;
		RTT.addLast(rtt);
		while (RTT.size() > SAMPLES) RTT.removeFirst();
		lastPongMs = now;
	}

	/** @return ping in ms, or -1 if there is no fresh measurement. */
	public static synchronized int getPing() {
		if (RTT.isEmpty() || nowMs() - lastPongMs > STALE_MS) return -1;
		long sum = 0;
		for (long v : RTT) sum += v;
		return (int) Math.round(sum / (double) RTT.size());
	}
}
