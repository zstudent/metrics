package cfuture;

import java.util.function.Function;

class Unpacker implements Function<String, String> {

	private int pause;
	
	public Unpacker() {
		this(0);
	}

	public Unpacker(int pause) {
		this.pause = pause;
	}

	@Override
	public String apply(String read) {
		if (read == null || read.isEmpty()) {
			return read;
		}
//		if (Math.random() >= 0.999) {
//			throw new RuntimeException("SUDDEN BUG");
//		}
		Utils.pause(pause);
		return read + read;
	}
	
}