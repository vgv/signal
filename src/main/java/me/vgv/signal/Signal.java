package me.vgv.signal;

import com.google.common.base.Preconditions;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class Signal {

	private final long started = System.currentTimeMillis();
	private final String signal;
	private final long lastModified;

	public Signal(String signal, long lastModified) {
		Preconditions.checkNotNull(signal, "signal is null");
		this.signal = signal;
		this.lastModified = lastModified;
	}

	public long getStarted() {
		return started;
	}

	public String getSignal() {
		return signal;
	}

	public long getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Signal{" +
				"started=" + started +
				", signal='" + signal + '\'' +
				", lastModified=" + lastModified +
				'}';
	}
}
