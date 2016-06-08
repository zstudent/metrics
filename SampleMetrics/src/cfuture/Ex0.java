package cfuture;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ex0 {

	private static CompletableFuture<?> previous;
	private static CompletableFuture<Void> stopFuture;
	private static Lock lock = new ReentrantLock();

	public static void main(String[] args) {

		Reader reader = new Reader(1);
		Unpacker unpacker = new Unpacker(1);
		Preparator preparator = new Preparator(1);
		Processor processor = new Processor(1);

		AtomicBoolean finished = new AtomicBoolean(false);

		
		ExecutorService service = new ForkJoinPool(6);
		
//		service = Executors.newSingleThreadExecutor();

		previous = null;

		stopFuture = new CompletableFuture<Void>();

		Instant start = Instant.now();

		cycle(service, reader, unpacker, preparator, processor, finished);

		stopFuture.join();

		previous.join();

		Instant stop = Instant.now();

		System.out.println("Elapsed " + Duration.between(start, stop));

		try {
			System.out.println("LAST value: " + previous.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		System.out.println(processor.getDone());

	}

	private static void cycle(ExecutorService service, Reader reader, Unpacker unpacker,
			Preparator preparator, Processor processor, AtomicBoolean finished) {
		CompletableFuture<Void> cycleFuture = CompletableFuture.runAsync(
				() -> {

					CompletableFuture<String> current = CompletableFuture
							.supplyAsync(() -> {
								lock.lock();
								try {
									String s = reader.get();
									if (s == "") {
										throw new RuntimeException("EOD");
									}
									return s;
								} finally {
									lock.unlock();
								}
							}, service).exceptionally(t -> {
								finished.set(true);
								return "";
							}).thenApply(unpacker).thenApply(preparator)
							.thenApply(processor);

					if (previous != null) {
						previous = CompletableFuture.allOf(current, previous);
					} else {
						previous = current;
					}

				}, service);

		cycleFuture.join();

		if (!finished.get()) {
			CompletableFuture.runAsync(
					() -> cycle(service, reader, unpacker, preparator, processor,
							finished), service);
		} else {
			stopFuture.complete(null);
		}
	}

}
