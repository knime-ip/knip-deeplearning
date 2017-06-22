package org.knime.ip.dl;

import org.knime.core.data.ExtensibleUtilityFactory;
import org.knime.dl.core.DLLayerData;
import org.knime.dl.core.data.convert.input.DLDataValueToLayerDataConverter;
import org.knime.dl.core.data.convert.input.DLDataValueToLayerDataConverterFactory;
import org.knime.dl.core.data.writables.DLWritableFloatBuffer;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 */
public class DLImgPlusValueToFloatLayerDataConverterFactory<T extends RealType<T>>
		implements DLDataValueToLayerDataConverterFactory<ImgPlusValue, DLWritableFloatBuffer> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return ((ExtensibleUtilityFactory) ImgPlusValue.UTILITY).getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ImgPlusValue> getSourceType() {
		return ImgPlusValue.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<DLWritableFloatBuffer> getBufferType() {
		return DLWritableFloatBuffer.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DLDataValueToLayerDataConverter<ImgPlusValue, DLWritableFloatBuffer> createConverter() {
		return new DLDataValueToLayerDataConverter<ImgPlusValue, DLWritableFloatBuffer>() {

			@Override
			public void convert(final Iterable<? extends ImgPlusValue> inputs,
					final DLLayerData<DLWritableFloatBuffer> output) {
				for (final ImgPlusValue input : inputs) {
					final Img<T> img = input.getImgPlus().getImg();
					final float[] out;
					if (input.getImgPlus().getImg() instanceof ArrayImg && img.firstElement() instanceof DoubleType) {
						out = (float[]) ((ArrayImg) img).update(null);
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
			}
		};
	}
}
