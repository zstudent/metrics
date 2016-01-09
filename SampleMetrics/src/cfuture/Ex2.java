package cfuture;

import java.util.concurrent.CompletableFuture;

public class Ex2 {
	
	public static void main(String[] args) {
		
		CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "hello");
		
		CompletableFuture<String> f2 = f1.thenComposeAsync(s -> {
			return f1;
		});
		
	}

}
