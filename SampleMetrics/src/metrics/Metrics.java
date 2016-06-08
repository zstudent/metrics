package metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Metrics {

	public static void main(final String[] args) {

		Metrics metrics = new Metrics();

		Instant start = Instant.now();

		metrics.doWork();

		Instant stop = Instant.now();

		Duration d = Duration.between(start, stop);

		System.out.println("Elapsed: " + d.toMillis());

	}

	private AtomicInteger count = new AtomicInteger();

	private void doWork() {

		Data data = new Data();
		ReferenceSequence sequence = new ReferenceSequence();

		long total = 0;

		Stream<Record> stream = StreamSupport.stream(data.spliterator(), true);
		
		total = stream.mapToInt(record -> 
			process(record, sequence.getRef(record))
		).sum();
		
		System.out.println(total);

	}

	private int process(final Record record, final Reference ref) {
		int sum = 0;
		try {
			Thread.sleep(0,100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(count.incrementAndGet());
		for (int i = 0; i < record.read.length; i++) {
			sum += record.read[i];
			// sum += ref.chromosome[i];
		}
		return sum;
	}

}
