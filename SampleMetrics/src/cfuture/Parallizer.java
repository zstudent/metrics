package cfuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class Parallizer<T> {

	private CompletableFuture<?> last = null;
	private CompletableFuture<Void> stopFuture = new CompletableFuture<Void>();
	private Lock lock = new ReentrantLock();
	private AtomicBoolean finished = new AtomicBoolean(false);
	private ExecutorService service;
	private int parallelism;

	public Parallizer(int parallelism) {
		this.parallelism = parallelism;
	}

	public void process(Supplier<T> supplier, List<Function<T, T>> functions,
			Function<Throwable, ? extends T> errorHandler) {

		service = new ForkJoinPool(parallelism);

		cycle(supplier, functions, errorHandler);

		stopFuture.join();

		last.join();

		service.shutdown();

	}

	private void cycle(Supplier<T> supplier, List<Function<T, T>> functions,
			Function<Throwable, ? extends T> errorHandler) {
		CompletableFuture<Void> cycleFuture = CompletableFuture.runAsync(
				() -> {
					CompletableFuture<T> f1 = CompletableFuture.supplyAsync(
							() -> {
								lock.lock();
								try {
									T s = supplier.get();
									if (s == null) {
										throw new RuntimeException("EOD");
									}
									return s;
								} finally {
									lock.unlock();
								}
							}, service).exceptionally(t -> {
						finished.set(true);
						return null;
					});

					for (Function<T, T> function : functions) {
						f1 = f1.thenApply(function).exceptionally(t -> {
							System.err.println(t);
							return null;
						});
					}

					if (last != null) {
						last = CompletableFuture.allOf(f1, last);
					} else {
						last = f1;
					}
				}, service);

		cycleFuture.join();

		if (!finished.get()) {
			CompletableFuture.runAsync(
					() -> cycle(supplier, functions, errorHandler), service);
		} else {
			stopFuture.complete(null);
		}
	}
}
