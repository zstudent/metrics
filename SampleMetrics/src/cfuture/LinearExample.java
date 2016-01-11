package cfuture;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class LinearExample {
	
	public static void main(String[] args) {
		
		Reader reader = new Reader(1);
		Unpacker unpacker = new Unpacker(1);
		Preparator preparator = new Preparator(1);
		Processor processor = new Processor(1);

		Instant start = Instant.now();
		
		while (true) {
			String s = reader.get();
			if (s == null) {
				break;
			}
			s = unpacker.apply(s);
			s = preparator.apply(s);
			s = processor.apply(s);
		}
		
		
		Instant stop = Instant.now();
		
		System.out.println("Elapsed " + Duration.between(start, stop));
		
		System.out.println(processor.getDone());

		
	}
	

}
