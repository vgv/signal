package me.vgv.signal;

import com.google.common.io.Files;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class WatchThreadTest {

	@Test(groups = "unit")
	public void testFileNotExists() throws Exception {
		final int CHECK_INTERVAL = 1;

		SignalListener signalListener = Mockito.mock(SignalListener.class);

		// создадим временный файл и удалим его
		File file = File.createTempFile("signal_test_", null);
		file.delete();

		WatchThread watchThread = new WatchThread(CHECK_INTERVAL, file.getAbsolutePath(), signalListener);
		watchThread.start();

		TimeUnit.SECONDS.sleep(CHECK_INTERVAL * 3); // подождем аж в три раза дольше
		Mockito.verifyZeroInteractions(signalListener);
	}

	@Test(groups = "unit")
	public void testFileNotReadable() throws Exception {
		final int CHECK_INTERVAL = 1;

		SignalListener signalListener = Mockito.mock(SignalListener.class);

		// создадим временный файл и сделаем его нечитаемым
		File file = File.createTempFile("signal_test_", null);
		file.setReadable(false);
		Files.write("some content".getBytes("UTF-8"), file);

		WatchThread watchThread = new WatchThread(CHECK_INTERVAL, file.getAbsolutePath(), signalListener);
		watchThread.start();

		TimeUnit.SECONDS.sleep(CHECK_INTERVAL * 3); // подождем аж в три раза дольше
		Mockito.verifyZeroInteractions(signalListener);
	}

	@Test(groups = "unit")
	public void testNormalFile() throws Exception {
		final int CHECK_INTERVAL = 1;

		final List<Signal> signals = new ArrayList<>();
		SignalListener signalListener = new SignalListener() {
			@Override
			public void signal(Signal signal) {
				signals.add(signal);
			}
		};

		// создадим временный файл
		File file = File.createTempFile("signal_test_", null);

		WatchThread watchThread = new WatchThread(CHECK_INTERVAL, file.getAbsolutePath(), signalListener);
		watchThread.start();

		TimeUnit.MILLISECONDS.sleep(CHECK_INTERVAL * 500);
		Files.write("content1".getBytes("UTF-8"), file);
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		Files.write("content2".getBytes("UTF-8"), file);
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		Files.write("content3".getBytes("UTF-8"), file);
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);

		// начнем проверки
		Assert.assertEquals(signals.size(), 3);
		Assert.assertEquals(signals.get(0).getSignal(), "content1");
		Assert.assertEquals(signals.get(1).getSignal(), "content2");
		Assert.assertEquals(signals.get(2).getSignal(), "content3");

		// проверка времени
		for (int i = 0; i < signals.size() - 1; i++) {
			Signal first = signals.get(i);
			Signal next = signals.get(i + 1);
			Assert.assertTrue(first.getStarted() < next.getStarted());
		}
	}


}
