package metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
	
	public static final int MAX_PAIRS = 1000; 

	public static void main(final String[] args) {

		System.out.println("Start");

		long start = System.nanoTime();

		Metrics metrics = new Metrics();

		metrics.doWork();

		long stop = System.nanoTime();
		System.out.println("Finish");
		System.out.println("Elapsed: " + (stop - start));
	}

	private void doWork() {

		Data data = new Data();
		ReferenceSequence sequence = new ReferenceSequence();

		final AtomicLong total = new AtomicLong(0);

		// Iterator<Record> it = data.iterator();
		//
		// while (it.hasNext()) {
		// Record record = it.next();
		//
		// ///
		// }

		ExecutorService service = Executors.newCachedThreadPool();
		
		List<Object[]> pairs = new ArrayList<>(MAX_PAIRS);

		for (final Record record : data) {
			final Reference ref = sequence.getRef(record);
			
			pairs.add(new Object[] {record, ref});
			
			if (pairs.size() < MAX_PAIRS) {
				continue;
			}
			
			final List<Object[]> tmpPairs = pairs;
			pairs = new ArrayList<>(MAX_PAIRS);

			service.submit(new Runnable() {

				@Override
				public void run() {
					
					int sum = 0;
					
					for (Object[] objects : tmpPairs) {
						Record rec = (Record) objects[0];
						Reference ref = (Reference) objects[1];
						sum += process(rec, ref);
					}
					
					total.addAndGet(sum);
				}
			});

			// total += sum;

		}

		service.shutdown();

		try {
			service.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(total.get());

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
