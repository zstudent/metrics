package cfuture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class Reader implements Supplier<String> {

	private static final int DATA_SIZE = 10_000;
	int pause;
	volatile boolean eod = false;

	AtomicInteger count = new AtomicInteger();

	public Reader() {
		this(0);
	}

	public Reader(int pause) {
		this.pause = pause;
	}

	@Override
	public String get() {
		if (eod) {
			return null;
		}
		int value = count.incrementAndGet();
		String r = value <= DATA_SIZE ? new String(new byte[1000]) + value
				: null;
		if (r == null) {
			eod = true;
		} else {
			Utils.pause(pause);
		}
		return r;
	}

}