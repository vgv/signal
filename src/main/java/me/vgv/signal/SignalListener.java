package me.vgv.signal;

import javax.annotation.Nonnull;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public interface SignalListener {

	public void signal(@Nonnull Signal signal);

}
