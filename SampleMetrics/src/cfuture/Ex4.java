package cfuture;

import java.util.concurrent.CompletableFuture;

public class Ex4 {

	public static void main(String[] args) {
		
		CompletableFuture<?> f = CompletableFuture.runAsync(() -> System.out.println("one"));
//		CompletableFuture<?> f2 = CompletableFuture.runAsync(() -> System.out.println("two"));
		CompletableFuture<Void> f2 = f.thenRunAsync(() -> {
			Utils.pause(2000);
			System.out.println("two");
			
		});
		
		f.join();
		
	}
}
