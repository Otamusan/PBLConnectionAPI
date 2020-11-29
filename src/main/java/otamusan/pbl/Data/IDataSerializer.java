package otamusan.pbl.Data;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface IDataType<T> {
	public void encode(T t, ByteBuffer buffer);

	public T decode(ByteBuffer buffer);

	//public String name();

	public int getCapacity();

	public Optional<T> cast(Object o);

	public boolean isCastable(Object o);
}