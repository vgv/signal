package me.vgv.signal.fs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Vasily Vasilkov (vasily.vasilkov@gmail.com)
 */
public interface FileSystemSupport {

	boolean lastModifiedSupported(int secondsToCheck);

	boolean exists(@Nonnull String file);

	boolean canRead(@Nonnull String file);

	@Nullable
	FileInfo readFile(@Nonnull String file, int charsCount);

}
