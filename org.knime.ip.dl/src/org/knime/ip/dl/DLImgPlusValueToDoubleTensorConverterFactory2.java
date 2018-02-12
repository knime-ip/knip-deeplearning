package org.knime.ip.dl;

import org.knime.dl.core.data.DLWritableDoubleBuffer;

import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;

/**
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 *
 * @param <T>
 */
public final class DLImgPlusValueToDoubleTensorConverterFactory2 <T extends RealType<T>>
		extends DLAbstractImgPlusValueToTensorConverter<DLWritableDoubleBuffer, T> {

	@Override
	protected void fillWith(DLWritableDoubleBuffer buffer, Cursor<T> cursor) {
		while (cursor.hasNext()) {
			buffer.put(cursor.next().getRealDouble());
		}
	}

}
