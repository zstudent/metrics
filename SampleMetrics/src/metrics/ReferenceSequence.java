package metrics;

public class ReferenceSequence {

	public Reference getRef(final Record record) {
		return new Reference(record.read);
	}

}
