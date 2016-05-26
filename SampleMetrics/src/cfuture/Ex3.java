package cfuture;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class Ex3 {

	public static void main(String[] args) throws InterruptedException {

		Parallizer<String> p = new Parallizer<String>(5);

		Reader reader = new Reader(0);
		Unpacker unpacker = new Unpacker(0);
		Preparator preparator = new Preparator(0);
		Processor processor = new Processor(0);

		Instant start = Instant.now();

		p.process(
				reader,
				Arrays.asList(unpacker, preparator, processor),
				() -> {
					Instant stop = Instant.now();

					System.out.println("Elapsed "
							+ Duration.between(start, stop));

					System.out.println(processor.getDone());

				}, t -> {
					System.out.println(t);
					return "";
				});

	}

}
