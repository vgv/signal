package me.vgv.signal.fs;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Vasily Vasilkov (vasily.vasilkov@gmail.com)
 */
public class FileSystemSupportImpl implements FileSystemSupport {

	private static final Logger log = LoggerFactory.getLogger(FileSystemSupportImpl.class);

	@Override
	public boolean lastModifiedSupported(int secondsToCheck) {
		try {
			File file = File.createTempFile("signal_test", null);
			long firstLastModified = file.lastModified();
			TimeUnit.SECONDS.sleep(secondsToCheck);

			Files.write("test content".getBytes("UTF-8"), file);
			long secondLastModified = file.lastModified();

			// если разница между значениями - хотя бы половина интервала - ну и отлично
			return (secondLastModified - firstLastModified) > (secondsToCheck * 1000 / 2);
		} catch (Exception e) {
			log.error("Can't test filesystem lastModified support");
			// раз мы не можем полагаться на lastModified - ну и не будем вообще включать эту фишку
			return false;
		}
	}

	@Override
	public boolean exists(@Nonnull String path) {
		return new File(path).exists();
	}

	@Override
	public boolean canRead(@Nonnull String path) {
		return new File(path).canRead();
	}

	@Nullable
	@Override
	public FileInfo readFile(@Nonnull String path, int charsCount) {
		if (!exists(path)) {
			// файл не существует, не будем и читать
			log.debug("File '" + path + "' doesn't exist");
			return null;
		}

		if (!canRead(path)) {
			// файл нечитаемый, не будем и читать
			log.debug("File '" + path + "' not readable");
			return null;
		}

		File file = new File(path);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			char[] buffer = new char[charsCount];
			int readChars = reader.read(buffer, 0, charsCount);

			if (readChars != -1) {
				String content = new String(buffer, 0, readChars).trim();
				long lastModified = file.lastModified();
				return new FileInfo(content, lastModified);
			} else {
				// ну файл просто пустой
				return null;
			}
		} catch (IOException e) {
			log.error("can't read signal file content", e);
			return null;
		}
	}
}
