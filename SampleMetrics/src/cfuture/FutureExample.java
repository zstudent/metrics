package cfuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureExample {
	
	public static void main(String[] args) {
		
		ExecutorService service = Executors.newCachedThreadPool();
		
		Future<String> future = service.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				Utils.pause(5000);
				return "hello";
			}
		});
		
		System.out.println("one");
		System.out.println("two");
		System.out.println("three");
		
		String result = null;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		System.out.println(result);
		
		service.shutdown();
		
	}

}
