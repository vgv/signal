package me.vgv.signal.fs;

import com.google.common.io.Files;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;


/**
 * @author Vasily Vasilkov (vasily.vasilkov@gmail.com)
 */
public class FileSystemSupportImplTest {

	@Test(groups = "unit")
	public void testExists() throws Exception {
		// создадим временный файл
		File file = File.createTempFile("signal_test_", null);

		FileSystemSupport fileSystemSupport = new FileSystemSupportImpl();
		Assert.assertTrue(fileSystemSupport.exists(file.getAbsolutePath()));

		file.delete();
		Assert.assertFalse(fileSystemSupport.exists(file.getAbsolutePath()));
	}

	@Test(groups = "unit")
	public void testCanRead() throws Exception {
		// создадим временный файл
		File file = File.createTempFile("signal_test_", null);

		FileSystemSupport fileSystemSupport = new FileSystemSupportImpl();
		Assert.assertTrue(fileSystemSupport.canRead(file.getAbsolutePath()));

		file.setReadable(false);
		Assert.assertFalse(fileSystemSupport.canRead(file.getAbsolutePath()));
	}

	@Test(groups = "unit")
	public void testReadFile_NormalFile() throws Exception {
		// создадим временный файл
		File file = File.createTempFile("signal_test_", null);

		FileSystemSupport fileSystemSupport = new FileSystemSupportImpl();
		Assert.assertNull(fileSystemSupport.readFile(file.getAbsolutePath(), 1024));

		Files.write("test".getBytes("UTF-8"), file.getAbsoluteFile());
		Assert.assertEquals(fileSystemSupport.readFile(file.getAbsolutePath(), 1024).getContent(), "test");
	}

	@Test(groups = "unit")
	public void testReadFile_NotExists() throws Exception {
		FileSystemSupport fileSystemSupport = new FileSystemSupportImpl() {
			@Override
			public boolean exists(@Nonnull String path) {
				return false;
			}
		};

		Assert.assertNull(fileSystemSupport.readFile("any file name", 1024));
	}

	@Test(groups = "unit")
	public void testReadFile_CantRead() throws Exception {
		FileSystemSupport fileSystemSupport = new FileSystemSupportImpl() {
			@Override
			public boolean exists(@Nonnull String path) {
				return true;
			}

			@Override
			public boolean canRead(@Nonnull String path) {
				return false;
			}
		};

		Assert.assertNull(fileSystemSupport.readFile("any file name", 1024));
	}
}
