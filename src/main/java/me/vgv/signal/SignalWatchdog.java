package me.vgv.signal;

import com.google.common.collect.ImmutableList;
import me.vgv.signal.fs.FileSystemSupport;
import me.vgv.signal.fs.FileSystemSupportImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class SignalWatchdog implements SignalListener {

	private static final Logger log = LoggerFactory.getLogger(SignalWatchdog.class);

	public static final int CHECK_PERIOD_IN_SECONDS = 3;
	public static final String SIGNAL_FILE_NAME = "signal";

	private static final Signal DEFAULT_SIGNAL = new Signal("default-signal", Long.MIN_VALUE);

	private final FileSystemSupport fileSystemSupport;
	private final boolean triggerOnLastModified;
	private final List<SignalListener> listeners;
	private final String signalFile;

	private volatile Signal lastSignal = DEFAULT_SIGNAL;

	public SignalWatchdog(FileSystemSupport fileSystemSupport, boolean triggerOnLastModified, SignalListener... listeners) {
		this.fileSystemSupport = fileSystemSupport;
		this.listeners = ImmutableList.copyOf(listeners != null ? Arrays.asList(listeners) : Collections.<SignalListener>emptyList());

		// вычислим triggerOnLastModified
		if (triggerOnLastModified) {
			// клиент хочет включить поддержку lastModified
			// потестируем, поддерживает ли текущая операционная система, файловая система и
			// тип монтирования метку lastModified для файлов
			triggerOnLastModified = fileSystemSupport.lastModifiedSupported(CHECK_PERIOD_IN_SECONDS);
		}
		this.triggerOnLastModified = triggerOnLastModified;

		// сделаем ряд проверок и вычислим signalFile
		String currentDir = System.getProperty("user.dir");
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
	}

	public SignalWatchdog(boolean triggerOnLastModified, SignalListener... listeners) {
		this(new FileSystemSupportImpl(), triggerOnLastModified, listeners);
	}

	public SignalWatchdog(SignalListener... listeners) {
		this(new FileSystemSupportImpl(), false, listeners);
	}

	public void start() {
		// стартанем поток
		new WatchThread(fileSystemSupport, CHECK_PERIOD_IN_SECONDS, signalFile, this).start();
	}

	@Override
	public void signal(@Nonnull Signal signal) {
		// сравним новый сигнал с тем, что приходил в последний раз
		boolean byContent = !lastSignal.getSignal().equals(signal.getSignal());
		boolean byLastModified = triggerOnLastModified && (lastSignal.getLastModified() < signal.getLastModified());

		if (byContent || byLastModified) {
			lastSignal = signal;

			for (SignalListener signalListener : listeners) {
				signalListener.signal(signal);
			}
		}
	}
}
