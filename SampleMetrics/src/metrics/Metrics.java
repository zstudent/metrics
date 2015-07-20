package metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Metrics {

	private static final int QUEUE_CAPACITY = 2;
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

		final ExecutorService service = Executors.newFixedThreadPool(4);

		final BlockingQueue<List<Object[]>> queue = new LinkedBlockingQueue<List<Object[]>>(
				QUEUE_CAPACITY);
		
		final Semaphore sem = new Semaphore(6);

		List<Object[]> pairs = new ArrayList<>(MAX_PAIRS);

		service.execute(new Runnable() {

			@Override
			public void run() {
				while (true) {

					try {
						final List<Object[]> tmpPairs = queue.take();
						
						sem.acquire();
						
						service.submit(new Runnable() {

							@Override
							public void run() {

								int sum = 0;

//								try {
//									Thread.sleep(8000);
//								} catch (InterruptedException e) {
//									e.printStackTrace();
//								}

								for (Object[] objects : tmpPairs) {
									Record rec = (Record) objects[0];
									Reference ref = (Reference) objects[1];
									sum += process(rec, ref);
								}

								total.addAndGet(sum); // CAS compare and swap
								
								sem.release();
							}
						});

					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		int pairsCount = 0;

		for (final Record record : data) {
			final Reference ref = sequence.getRef(record);

			pairs.add(new Object[] { record, ref });

			if (pairs.size() < MAX_PAIRS) {
				continue;
			}

			pairsCount++;

//			System.out.println("lists: " + pairsCount);

			try {
				queue.put(pairs);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			pairs = new ArrayList<>(MAX_PAIRS);

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
