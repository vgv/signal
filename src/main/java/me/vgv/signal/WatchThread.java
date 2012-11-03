package me.vgv.signal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
final class WatchThread extends Thread {

	private static final Logger log = LoggerFactory.getLogger(WatchThread.class);

	private final int checkIntervalInSeconds;
	private final String signalFile;
	private final SignalListener signalListener;

	public WatchThread(int checkIntervalInSeconds, String signalFile, SignalListener signalListener) {
		this.setName("SignalWatch");
		this.setDaemon(true);

		this.checkIntervalInSeconds = checkIntervalInSeconds;
		this.signalFile = signalFile;
		this.signalListener = signalListener;
	}

	@Override
	public void run() {
		while (true) {
			// пауза
			try {
				TimeUnit.SECONDS.sleep(checkIntervalInSeconds);
			} catch (InterruptedException e) {
				// NOP
			}

			ContentInfo contentInfo = readContent();
			if (contentInfo == null) {
				// если ничего не прочитали или какая-то ошибка при чтении ничего делать не будем
				continue;
			}

			// из файла что-то прочитали, отлично, сообщим об этом выше, пусть там разбираются
			Signal signal = new Signal(contentInfo.getContent(), contentInfo.getLastModified());
			signalListener.signal(signal);
		}
	}

	private ContentInfo readContent() {
		// мы читаем только первые MAX_BUFFER_SIZE, сигнал не может быть длиннее
		final int MAX_BUFFER_SIZE = 1024;

		File file = new File(signalFile);

		if (!file.exists()) {
			// файл не существует, не будем и читать
			log.debug("File '" + signalFile + "' doesn't exist");
			return null;
		}

		if (!file.canRead()) {
			// файл нечитаемый, не будем и читать
			log.debug("File '" + signalFile + "' not readable");
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			char[] buffer = new char[MAX_BUFFER_SIZE];
			int readChars = reader.read(buffer, 0, MAX_BUFFER_SIZE);

			String content = new String(buffer, 0, readChars).trim();
			long lastModified = file.lastModified();
			return new ContentInfo(content, lastModified);
		} catch (IOException e) {
			log.error("can't read signal file content", e);
			return null;
		}
	}
}

final class ContentInfo {
	private final String content;
	private final long lastModified;

	ContentInfo(@Nonnull String content, long lastModified) {
		this.content = content;
		this.lastModified = lastModified;
	}

	@Nonnull
	public String getContent() {
		return content;
	}

	public long getLastModified() {
		return lastModified;
	}
}
