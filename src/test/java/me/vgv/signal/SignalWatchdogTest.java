package me.vgv.signal;

import com.google.common.io.Files;
import me.vgv.signal.fs.FileSystemSupport;
import me.vgv.signal.fs.FileSystemSupportImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class SignalWatchdogTest {

	@Test(groups = "unit")
	public void testSignalWithNormalWrite() throws Exception {
		final List<Signal> signals = Collections.synchronizedList(new ArrayList<Signal>());
		System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
		final File file = new File(System.getProperty("user.dir"), SignalWatchdog.SIGNAL_FILE_NAME);

		SignalWatchdog signalWatchdog = new SignalWatchdog(false, new SignalListener() {
			@Override
			public void signal(@Nonnull Signal signal) {
				signals.add(signal);
			}
		});
		signalWatchdog.start();

		// ждем специально в два с лишним раза дольше, чтобы было несколько чтений файла c одинаковым сигналом
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.write("signal1", file, Charset.forName("UTF-8"));
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.write("signal2", file, Charset.forName("UTF-8"));
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.write("signal3", file, Charset.forName("UTF-8"));
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.write("signal4", file, Charset.forName("UTF-8"));
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);

		Assert.assertEquals(signals.size(), 4);
		Assert.assertEquals(signals.get(0).getSignal(), "signal1");
		Assert.assertEquals(signals.get(1).getSignal(), "signal2");
		Assert.assertEquals(signals.get(2).getSignal(), "signal3");
		Assert.assertEquals(signals.get(3).getSignal(), "signal4");
	}

	@Test(groups = "unit")
	public void testSignalWithOnlyLastModifiedChange() throws Exception {
		final List<Signal> signals = Collections.synchronizedList(new ArrayList<Signal>());
		System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
		final File file = new File(System.getProperty("user.dir"), SignalWatchdog.SIGNAL_FILE_NAME);

		FileSystemSupport fileSystemSupport = new FileSystemSupportImpl();
		if (!fileSystemSupport.lastModifiedSupported(SignalWatchdog.CHECK_PERIOD_IN_SECONDS)) {
			// этот тест не имеет смысла если lastModified не поддерживается
			System.out.println("Last modified not supported, skip test");
			return;
		}

		SignalWatchdog signalWatchdog = new SignalWatchdog(fileSystemSupport, true, new SignalListener() {
			@Override
			public void signal(@Nonnull Signal signal) {
				signals.add(signal);
			}
		});
		signalWatchdog.start();

		// ждем специально в два с лишним раза дольше, чтобы было несколько чтений файла c одинаковым сигналом
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.write("signal1", file, Charset.forName("UTF-8"));
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.touch(file);
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.touch(file);
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.touch(file);
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);

		Assert.assertEquals(signals.size(), 4);
		Assert.assertEquals(signals.get(0).getSignal(), "signal1");
		Assert.assertEquals(signals.get(1).getSignal(), "signal1");
		Assert.assertEquals(signals.get(2).getSignal(), "signal1");
		Assert.assertEquals(signals.get(3).getSignal(), "signal1");
	}

	@Test(groups = "unit")
	public void testSignalWithOnlyLastModifiedChangeButWithoutLastModifiedTrigger() throws Exception {
		final List<Signal> signals = Collections.synchronizedList(new ArrayList<Signal>());
		System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
		final File file = new File(System.getProperty("user.dir"), SignalWatchdog.SIGNAL_FILE_NAME);

		SignalWatchdog signalWatchdog = new SignalWatchdog(false, new SignalListener() {
			@Override
			public void signal(@Nonnull Signal signal) {
				signals.add(signal);
			}
		});
		signalWatchdog.start();

		// ждем специально в два с лишним раза дольше, чтобы было несколько чтений файла c одинаковым сигналом
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.write("signal1", file, Charset.forName("UTF-8"));
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.touch(file);
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.touch(file);
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);
		Files.touch(file);
		TimeUnit.SECONDS.sleep(SignalWatchdog.CHECK_PERIOD_IN_SECONDS + 1);

		Assert.assertEquals(signals.size(), 1);
		Assert.assertEquals(signals.get(0).getSignal(), "signal1");
	}

}
