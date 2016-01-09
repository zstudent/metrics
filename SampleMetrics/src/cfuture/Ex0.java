package cfuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ex0 {

	private static CompletableFuture<?> last;

	public static void main(String[] args) {

		Reader reader = new Reader(1);
		Unpacker unpacker = new Unpacker(0);
		Preparator preparator = new Preparator(0);
		Processor processor = new Processor(0);

		AtomicBoolean finished = new AtomicBoolean(false);

//		Semaphore sem = new Semaphore(100);

		last = null;

		while (!finished.get()) {

//			sem.acquireUninterruptibly();

			CompletableFuture<Void> cycleFuture = CompletableFuture.runAsync(() -> {

				CompletableFuture<String> f1 = CompletableFuture
						.supplyAsync(() -> {
							String s = reader.get();
//							System.out.println(">>" + s);
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
//					System.out.println(r);
					return r;
				});

				CompletableFuture<String> f3 = f2.thenApplyAsync(s -> {
					if (s == null) {
						return s;
					}
					String r = preparator.apply(s);
//					System.out.println(r);
					return r;
				});

				CompletableFuture<String> finalFuture = f3.thenApplyAsync(s -> {
					if (s == null) {
						return null;
					}
					processor.accept(s);
//					sem.release();
					return s;
				});

				if (last != null) {
					last = finalFuture.thenAcceptBoth(last, (s, u) -> {
//						Instant now = Instant.now();
//						try {
////							System.out.println("finished " + finalFuture.get() + " at " + now.toString());
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
					});
				} else {
					last = finalFuture;
				}

			});
			
			cycleFuture.join();

		}

		last.join();
		
		try {
			System.out.println("LAST value: " + last.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		System.out.println(processor.getDone());

	}

}
