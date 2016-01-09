package cfuture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

class Processor implements Consumer<String> {
	
	private int pause;
	private AtomicInteger done = new AtomicInteger();

	public Processor() {
		this(0);
	}

	public Processor(int pauseMillis) {
		pause = pauseMillis;
	}

	@Override
	public void accept(String t) {
		Utils.pause(pause);
//		System.out.println("done");
		if (t != null && !t.isEmpty()) {
			int count = done.incrementAndGet();
			if (count % 1_000_000 == 0) {
				System.out.println(count);
			}
		}
	}

	public int getDone() {
		return done.get();
	}
	
}