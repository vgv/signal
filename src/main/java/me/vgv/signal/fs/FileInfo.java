package me.vgv.signal.fs;

import javax.annotation.Nonnull;

/**
 * @author Vasily Vasilkov (vasily.vasilkov@gmail.com)
 */
public final class FileInfo {

	private final String content;
	private final long lastModified;

	public FileInfo(@Nonnull String content, long lastModified) {
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
