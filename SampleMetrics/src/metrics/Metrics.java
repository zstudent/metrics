package metrics;

import java.time.Duration;
import java.time.Instant;

public class Metrics {
	
	
	public static void main(final String[] args) {
		
		Metrics metrics = new Metrics();
		
		metrics.doWork();
		
	}

	private void doWork() {
		
		Data data = new Data();
		ReferenceSequence sequence = new ReferenceSequence();
		
		Instant start = Instant.now();
		
		long total = 0;
		
		for (Record record : data) {
			Reference ref = sequence.getRef(record);
			
			int sum = process(record, ref);
			
			total += sum;
			
		}
		
		Instant stop = Instant.now();
		
		System.out.println(total);
		System.out.println(Duration.between(start, stop));
		
	}

	private int process(final Record record, final Reference ref) {
		int sum = 0;
		for (int i = 0; i < record.read.length; i++) {
			sum += record.read[i];
			sum += ref.read[i];
		}
		return sum;
	}
	

}
