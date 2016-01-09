package cfuture;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ex0 {

	private static CompletableFuture<?> last;

	public static void main(String[] args) {

		Reader reader = new Reader(0);
		Unpacker unpacker = new Unpacker(0);
		Preparator preparator = new Preparator(0);
		Processor processor = new Processor(0);

		AtomicBoolean finished = new AtomicBoolean(false);

		last = null;
		
		Instant start = Instant.now();

		while (!finished.get()) {

			CompletableFuture<Void> cycleFuture = CompletableFuture.runAsync(() -> {

				CompletableFuture<String> f1 = CompletableFuture
						.supplyAsync(() -> {
							String s = reader.get();
							if (s == "") {
								finished.set(true);
							}
							return s;
						});

				CompletableFuture<String> f2 = f1.thenApplyAsync(s -> {
					if (s == null) {
						return s;
					}
					String r = unpacker.apply(s);
					return r;
				});

				CompletableFuture<String> f3 = f2.thenApplyAsync(s -> {
					if (s == null) {
						return s;
					}
					String r = preparator.apply(s);
					return r;
				});

				CompletableFuture<String> finalFuture = f3.thenApplyAsync(s -> {
					if (s == null) {
						return null;
					}
					processor.accept(s);
					return s;
				});

				if (last != null) {
					last = CompletableFuture.allOf(finalFuture);
				} else {
					last = finalFuture;
				}

			});
			
			cycleFuture.join();

		}

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

}
