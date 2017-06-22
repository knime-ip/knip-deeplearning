package org.knime.ip.dl;

import org.knime.core.data.ExtensibleUtilityFactory;
import org.knime.dl.core.DLLayerData;
import org.knime.dl.core.data.convert.input.DLDataValueToLayerDataConverter;
import org.knime.dl.core.data.convert.input.DLDataValueToLayerDataConverterFactory;
import org.knime.dl.core.data.writables.DLWritableDoubleBuffer;
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
public class DLImgPlusValueToDoubleLayerDataConverterFactory<T extends RealType<T>>
		implements DLDataValueToLayerDataConverterFactory<ImgPlusValue, DLWritableDoubleBuffer> {

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
	public Class<DLWritableDoubleBuffer> getBufferType() {
		return DLWritableDoubleBuffer.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DLDataValueToLayerDataConverter<ImgPlusValue, DLWritableDoubleBuffer> createConverter() {
		return new DLDataValueToLayerDataConverter<ImgPlusValue, DLWritableDoubleBuffer>() {

			@Override
			public void convert(final Iterable<? extends ImgPlusValue> inputs,
					final DLLayerData<DLWritableDoubleBuffer> output) {
				for (final ImgPlusValue input : inputs) {
					final Img<T> img = input.getImgPlus().getImg();
					final double[] out;
					if (input.getImgPlus().getImg() instanceof ArrayImg && img.firstElement() instanceof DoubleType) {
						out = (double[]) ((ArrayImg) img).update(null);
					} else {
						if (img.size() >= Integer.MAX_VALUE) {
							throw new IllegalArgumentException(
									"Can't process images with more than Integer.MAX_VALUE pixels, yet.");
						}
						// TODO can be parallelized
						// TODO consider iteration order
						out = new double[(int) img.size()];
						final Cursor<T> c = img.cursor();
						for (int i = 0; i < out.length; i++) {
							out[i] = c.next().getRealDouble();
						}
					}
					output.getBuffer().putAll(out);
				}
			}
		};
	}
}
