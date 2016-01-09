package cfuture;

import java.util.function.Function;

class Preparator implements Function<String, String> {
	
	private int pause;
	
	public Preparator() {
		this(0);
	}

	public Preparator(int millis) {
		pause = millis;
	}

	@Override
	public String apply(String read) {
		if (read == null || read.isEmpty()) {
			return read;
		}
		Utils.pause(pause);
		return read.toUpperCase();
	}
	
}