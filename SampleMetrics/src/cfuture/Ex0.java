package cfuture;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ex0 {

	private static CompletableFuture<?> last;
	private static CompletableFuture<Void> stopFuture;
	private static Lock lock = new ReentrantLock();
	private static ExecutorService service;

	public static void main(String[] args) {

		Reader reader = new Reader();
		Unpacker unpacker = new Unpacker();
		Preparator preparator = new Preparator();
		Processor processor = new Processor();

		AtomicBoolean finished = new AtomicBoolean(false);
		
		service = new ForkJoinPool(4);

		last = null;

		stopFuture = new CompletableFuture<Void>();

		Instant start = Instant.now();

		cycle(reader, unpacker, preparator, processor, finished);

		stopFuture.join();

		last.join();

		Instant stop = Instant.now();

		Duration elapsed = Duration.between(start, stop);

		System.out.println("Elapsed " + elapsed);

		try {
			System.out.println("LAST value: " + last.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		System.out.println(processor.getDone());

	}

	private static void cycle(Reader reader, Unpacker unpacker,
			Preparator preparator, Processor processor, AtomicBoolean finished) {
		CompletableFuture<Void> cycleFuture = CompletableFuture
				.runAsync(() -> {

					CompletableFuture<String> f1 = CompletableFuture
							.supplyAsync(() -> {
								lock.lock();
								try {
									return reader.get();
								} finally {
									lock.unlock();
								}
							}, service)
							.thenApply(unpacker)
							.thenApply(preparator)
							.thenApply(processor);

					if (last != null) {
						last = f1.thenAcceptBoth(last, (s, u) -> {
							if (s == "") {
								finished.set(true);
							}
						});
					} else {
						last = f1;
					}

				}, service);

		cycleFuture.join();

		if (!finished.get()) {
			CompletableFuture.runAsync(() -> cycle(reader, unpacker,
					preparator, processor, finished), service);
		} else {
			stopFuture.complete(null);
		}
	}

}
