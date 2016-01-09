package cfuture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ex1 {

	static final int BLOCK_SIZE = 100;

	static class DataReader implements Iterator<String> {

		private static final int MAX_DATA = 10000;
		int count;
		Reader reader = new Reader();

		@Override
		public boolean hasNext() {
			return count < MAX_DATA;
		}

		@Override
		public String next() {
			count++;
			return reader.get();
		}

		List<String> fetchBlock() {
			List<String> block = new ArrayList<>(BLOCK_SIZE);
			for (int i = 0; i < BLOCK_SIZE; i++) {
				if (hasNext()) {
					block.add(next());
				}
			}
			return block;
		}

	}

	public static void main(String[] args) {

		DataReader dataReader = new DataReader();

		ExecutorService service = Executors.newFixedThreadPool(4);

		CompletableFuture<List<String>> f = offerBlock(dataReader, service);

		f.join();

		System.out.println("and there");

	}
	

	private static CompletableFuture<List<String>> offerBlock(
			DataReader dataReader, ExecutorService service) {
		return CompletableFuture.supplyAsync(
				() -> dataReader.fetchBlock(), service);
	}

}
