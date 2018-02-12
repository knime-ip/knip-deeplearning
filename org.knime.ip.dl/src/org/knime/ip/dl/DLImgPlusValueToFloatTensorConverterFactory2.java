package org.knime.ip.dl;

import org.knime.dl.core.data.DLWritableFloatBuffer;
import org.knime.dl.core.data.convert.DLDataValueToTensorConverter;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;

/**
 * Performs auto-mapping for image dimensions.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @param <T> 
 *
 */
public class DLImgPlusValueToFloatTensorConverterFactory2 <T extends RealType<T>>
		extends DLAbstractImgPlusValueToTensorConverterFactory<DLWritableFloatBuffer> {

	@Override
	public Class<DLWritableFloatBuffer> getBufferType() {
		return DLWritableFloatBuffer.class;
	}

	@Override
	public DLDataValueToTensorConverter<ImgPlusValue, DLWritableFloatBuffer> createConverter() {
		
		return new DLAbstractImgPlusValueToTensorConverter<DLWritableFloatBuffer, T>() {

			@Override
			protected void fillWith(DLWritableFloatBuffer buffer, Cursor<T> cursor) {
				while (cursor.hasNext()) {
					buffer.put(cursor.next().getRealFloat());
				}
			}

		};
	}

}
