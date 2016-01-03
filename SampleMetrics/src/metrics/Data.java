package metrics;

import java.util.Iterator;
import java.util.Random;

public class Data implements Iterable<Record> {
	
	public static final int MAX = 50;
	public static final int READ_SIZE = 1000;
	private static final Random random = new Random();
	
	
	
	private static final char[] LETTERS = {'A','C','G','T'};

	@Override
	public Iterator iterator() {
		return new Iterator<Record>() {
			private int count;

			@Override
			public boolean hasNext() {
				return count < MAX;
			}

			@Override
			public Record next() {
				count++;
				char[] data = create();
				prepare(data);
				return new Record(data);
			}


			@Override
			public void remove() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	private void pause(int i) {
		try {
			Thread.sleep(i + random.nextInt(i));
		} catch (InterruptedException e) {
		}
	}

	public char[] create() {
		pause(100);
		return new char[READ_SIZE];
	}

	public void prepare(char[] data) {
		pause(100);
		for (int i = 0; i < data.length; i++) {
			data[i] = LETTERS[random.nextInt(LETTERS.length)]; 
		}
	}
}
