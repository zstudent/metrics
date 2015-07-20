package pill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PoisonPillExample {
	
	static class Worker implements Runnable {
		
		final static List<String> poisonPill = Collections.emptyList();

		BlockingQueue<List<String>> queue = new LinkedBlockingQueue<List<String>>();
		
		@Override
		public void run() {
			while (true) {
				try {
					List<String> data = queue.take();
					if (data.isEmpty()) {
						return;
					}
					process (data);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}

		private void process(final List<String> data) {
			for (String string : data) {
				System.out.println(string);
			}
		}

		public void submitData(final List<String> data) {
			try {
				queue.put(data);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void stop() {
			try {
				queue.put(poisonPill);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(final String[] args) {
		
		
		
		List<String> data = new ArrayList<String>() {
			{
				add("one");
				add("two");
				add("three");
			}
		};
		
		ExecutorService service = Executors.newCachedThreadPool();
		
		Worker worker = new Worker();
		
		service.execute(worker);
		
		worker.submitData(data);
		
		service.shutdown();
		
		worker.stop();
		
		
	}
	

}
