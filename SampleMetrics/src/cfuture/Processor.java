package cfuture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class Processor implements Function<String, String> {

	private int pause;
	private AtomicInteger done = new AtomicInteger();

	public Processor() {
		this(0);
	}

	public Processor(int pauseMillis) {
		pause = pauseMillis;
	}

	public int getDone() {
		return done.get();
	}

	@Override
	public String apply(String t) {
		if (t == null || t.isEmpty()) {
			return t;
		}
		// System.out.println("done");
//		int count = done.incrementAndGet();
//		if (count % 10_000 == 0) {
//			System.out.println(count);
//		}
//		Utils.pause(pause);
		return t;
	}

}