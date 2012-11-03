package me.vgv.signal;

import me.vgv.signal.fs.FileInfo;
import me.vgv.signal.fs.FileSystemSupport;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class WatchThreadTest {

	@Test(groups = "unit")
	public void testNormalFile() throws Exception {
		//
		final List<FileInfo> fileInfoList = new ArrayList<FileInfo>() {{
			add(new FileInfo("1", 1));
			add(new FileInfo("2", 2));
			add(new FileInfo("3", 3));
		}};
		FileSystemSupport fileSystemSupport = Mockito.mock(FileSystemSupport.class);
		Mockito.when(fileSystemSupport.readFile(Mockito.anyString(), Mockito.anyInt())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				if (fileInfoList.isEmpty()) {
					return null;
				} else {
					return fileInfoList.remove(0);
				}
			}
		});

		//
		final List<Signal> signals = new ArrayList<>();
		SignalListener signalListener = new SignalListener() {
			@Override
			public void signal(@Nonnull Signal signal) {
				signals.add(signal);
			}
		};

		// начало теста
		final int CHECK_INTERVAL = 1;
		WatchThread watchThread = new WatchThread(fileSystemSupport, CHECK_INTERVAL, "abc", signalListener);
		watchThread.start();

		TimeUnit.MILLISECONDS.sleep(CHECK_INTERVAL * 500);
		// first read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		// second read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		// third read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		// null read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		// null read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);

		// тормозим поток, хватит
		watchThread.interrupt();

		// no read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);
		// no read
		TimeUnit.SECONDS.sleep(CHECK_INTERVAL);

		// конец теста

		// проверим, сколько раз дернули метод read
		Mockito.verify(fileSystemSupport, VerificationModeFactory.times(5)).readFile(Mockito.anyString(), Mockito.anyInt());

		// начнем проверки
		Assert.assertEquals(signals.size(), 3);

		Assert.assertEquals(signals.get(0).getSignal(), "1");
		Assert.assertEquals(signals.get(0).getLastModified(), 1);

		Assert.assertEquals(signals.get(1).getSignal(), "2");
		Assert.assertEquals(signals.get(1).getLastModified(), 2);

		Assert.assertEquals(signals.get(2).getSignal(), "3");
		Assert.assertEquals(signals.get(2).getLastModified(), 3);

		// проверка хода времени
		for (int i = 0; i < signals.size() - 1; i++) {
			Signal first = signals.get(i);
			Signal next = signals.get(i + 1);
			Assert.assertTrue(first.getStarted() < next.getStarted());
		}
	}


}
