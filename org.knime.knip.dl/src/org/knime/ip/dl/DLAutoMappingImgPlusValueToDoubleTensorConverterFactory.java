package org.knime.ip.dl;

import org.knime.dl.core.data.DLWritableDoubleBuffer;
import org.knime.dl.core.data.convert.DLDataValueToTensorConverter;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;

/**
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 *
 * @param <T>
 */
public final class DLAutoMappingImgPlusValueToDoubleTensorConverterFactory <T extends RealType<T>>
		extends DLAbstractAutoMappingImgPlusValueToTensorConverterFactory<DLWritableDoubleBuffer> {
	

	@Override
	public Class<DLWritableDoubleBuffer> getBufferType() {
		return DLWritableDoubleBuffer.class;
	}

	@Override
	public DLDataValueToTensorConverter<ImgPlusValue, DLWritableDoubleBuffer> createConverter() {
		return new DLAbstractAutoMappingImgPlusValueToTensorConverter<DLWritableDoubleBuffer, T>() {

			@Override
			protected void fillWith(DLWritableDoubleBuffer buffer, Cursor<T> cursor) {
				while (cursor.hasNext()) {
					buffer.put(cursor.next().getRealDouble());
				}
			}

		};
	}

}
