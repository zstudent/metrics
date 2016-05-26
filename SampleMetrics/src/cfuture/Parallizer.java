package cfuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Parallizer<T> {

	private CompletableFuture<Void> last;
	private CompletableFuture<Void> stopFuture;
	private Lock lock = new ReentrantLock();
	private AtomicBoolean finished = new AtomicBoolean(true);
	private ExecutorService service;
	private int parallelism;

	public Parallizer(int parallelism) {
		this.parallelism = parallelism;
	}

	private void init() {
		if (!finished.weakCompareAndSet(true, false)) {
			throw new IllegalStateException(
					"This parallizer is already processing some task");
		}
		System.out.println("init");
		last = CompletableFuture.completedFuture(null);
		stopFuture = new CompletableFuture<Void>();
	}

	public void process(Supplier<T> supplier, List<Function<T, T>> functions, Runnable finish,
			Function<Throwable, ? extends T> errorHandler) {

		init();

		service = Executors.newFixedThreadPool(parallelism);
		
		cycle(supplier, functions, (v) -> {
			System.out.println("stop");
			service.shutdown();
			service = null;
			finish.run();
		}, errorHandler);
	}
	
	private void cycle(Supplier<T> supplier, List<Function<T, T>> functions, Consumer<? super Void> finishingAction,
			Function<Throwable, ? extends T> errorHandler) {
		CompletableFuture<T> f1 = CompletableFuture.supplyAsync(() -> {
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

			last = CompletableFuture.allOf(f1, last);

		if (finished.get()) {
			last.thenAccept(finishingAction);
			stopFuture.complete(null);
		} else {
			CompletableFuture.runAsync(
					() -> cycle(supplier, functions, finishingAction, errorHandler), service);
		}
	}
}
