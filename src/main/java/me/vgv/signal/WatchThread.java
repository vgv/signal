package me.vgv.signal;

import me.vgv.signal.fs.FileInfo;
import me.vgv.signal.fs.FileSystemSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
final class WatchThread extends Thread {

	private static final Logger log = LoggerFactory.getLogger(WatchThread.class);

	private final FileSystemSupport fileSystemSupport;
	private final int checkIntervalInSeconds;
	private final String signalFile;
	private final SignalListener signalListener;

	public WatchThread(FileSystemSupport fileSystemSupport, int checkIntervalInSeconds, String signalFile, SignalListener signalListener) {
		this.setName("SignalWatch");
		this.setDaemon(true);

		this.fileSystemSupport = fileSystemSupport;
		this.checkIntervalInSeconds = checkIntervalInSeconds;
		this.signalFile = signalFile;
		this.signalListener = signalListener;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			// пауза
			try {
				TimeUnit.SECONDS.sleep(checkIntervalInSeconds);
			} catch (InterruptedException e) {
				// нас хотят остановить? Ну ладно
				break;
			}

			FileInfo fileInfo = fileSystemSupport.readFile(signalFile, 1024);
			if (fileInfo == null) {
				// если ничего не прочитали или какая-то ошибка при чтении ничего делать не будем
				continue;
			}

			// из файла что-то прочитали, отлично, сообщим об этом выше, пусть там разбираются
			Signal signal = new Signal(fileInfo.getContent(), fileInfo.getLastModified());
			signalListener.signal(signal);
		}
	}

}