package me.vgv.signal;

import org.testng.annotations.Test;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class SignalWatchdogTest {

	@Test(groups = "unit")
	public void testSignal() throws Exception {
		/*
		final List<Signal> signals = Collections.synchronizedList(new ArrayList<Signal>());
		System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
		final File file = new File(System.getProperty("user.dir"), SignalWatchdog.SIGNAL_FILE_NAME);



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
		*/
	}

}
