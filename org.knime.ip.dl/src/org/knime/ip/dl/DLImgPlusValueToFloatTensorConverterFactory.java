package org.knime.ip.dl;

import java.util.List;
import java.util.OptionalLong;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.ExtensibleUtilityFactory;
import org.knime.dl.core.data.DLWritableFloatBuffer;
import org.knime.dl.core.data.convert.DLDataValueToTensorConverter;
import org.knime.dl.core.data.convert.DLDataValueToTensorConverterFactory;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 */
public class DLImgPlusValueToFloatTensorConverterFactory<T extends RealType<T>>
		implements DLDataValueToTensorConverterFactory<ImgPlusValue, DLWritableFloatBuffer> {

	@Override
	public String getName() {
		return ((ExtensibleUtilityFactory) ImgPlusValue.UTILITY).getName();
	}

	@Override
	public Class<ImgPlusValue> getSourceType() {
		return ImgPlusValue.class;
	}

	@Override
	public Class<DLWritableFloatBuffer> getBufferType() {
		return DLWritableFloatBuffer.class;
	}

	@Override
	public OptionalLong getDestCount(final List<DataColumnSpec> spec) {
		return OptionalLong.empty();
	}

	@Override
	public DLDataValueToTensorConverter<ImgPlusValue, DLWritableFloatBuffer> createConverter() {
		return (inputs, output) -> {
			for (final ImgPlusValue input : inputs) {
				final Img<T> img = input.getImgPlus().getImg();
				final float[] out;
				if (input.getImgPlus().getImg() instanceof ArrayImg && img.firstElement() instanceof FloatType) {
					out = ((FloatArray) ((ArrayImg) img).update(null)).getCurrentStorageArray();
				} else {
					if (img.size() >= Integer.MAX_VALUE) {
						throw new IllegalArgumentException(
								"Can't process images with more than Integer.MAX_VALUE pixels, yet.");
					}
					// TODO can be parallelized
					// TODO consider iteration order
					out = new float[(int) img.size()];
					final Cursor<T> c = img.cursor();
					for (int i = 0; i < out.length; i++) {
						out[i] = c.next().getRealFloat();
					}
				}
				output.getBuffer().putAll(out);
			}
		};
	}
}
