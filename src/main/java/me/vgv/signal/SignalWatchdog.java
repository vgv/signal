package me.vgv.signal;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class SignalWatchdog implements SignalListener {

	private static final Logger log = LoggerFactory.getLogger(SignalWatchdog.class);

	public static final int CHECK_PERIOD_IN_SECONDS = 5;

	public static final String SIGNAL_FILE_NAME = "signal";

	private final List<SignalListener> listeners;
	private final String signalFile;

	public SignalWatchdog(boolean useLastModified, SignalListener... listeners) {
		this.listeners = ImmutableList.copyOf(listeners != null ? Arrays.asList(listeners) : Collections.<SignalListener>emptyList());

		String currentDir = System.getProperty("user.dir");

		if (useLastModified) {
			// потестируем, поддерживает ли текущая операционная система
		}

		File file = new File(currentDir, SIGNAL_FILE_NAME);
		this.signalFile = file.getAbsolutePath();
		log.info("Use '" + this.signalFile + "' as signal file.");

		// удалим этот файл
		if (file.exists()) {
			if (file.delete()) {
				log.debug("Remove old signal file '" + signalFile + "'");
			} else {
				log.warn("Can't remove old signal file '" + signalFile + "'");
			}
		}

		// стартанем поток
		new WatchThread(CHECK_PERIOD_IN_SECONDS, signalFile, this).start();
	}

	@Override
	public void signal(Signal signal) {
		//
		for (SignalListener signalListener : listeners) {
			signalListener.signal(signal);
		}
	}
}
