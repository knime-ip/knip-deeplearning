package org.knime.ip.dl;

import org.knime.dl.core.DLTensor;
import org.knime.dl.core.data.DLWritableBuffer;
import org.knime.dl.core.data.convert.DLAbstractTensorDataValueToTensorConverter;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Performs auto-mapping of dimensions.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 *
 * @param <VIA> the buffer type
 * @param <T>
 */
public abstract class DLAbstractImgPlusValueToTensorConverter <VIA extends DLWritableBuffer, T extends RealType<T>> 
extends DLAbstractTensorDataValueToTensorConverter<ImgPlusValue, VIA> {

	@Override
	protected final void convertInternal(ImgPlusValue element, DLTensor<VIA> output) {
		RandomAccessibleInterval<T> mapped = DLKnipUtil.mapImgToDL(element, output.getSpec());
		IterableInterval<T> iterableInterval = Views.flatIterable(mapped);
		if (iterableInterval.size() >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"Can't process images with more than Integer.MAX_VALUE pixels, yet.");
		}
		final Cursor<T> c = iterableInterval.cursor();
		VIA buffer = output.getBuffer();
		fillWith(buffer, c);
	}
	
	protected abstract void fillWith(VIA buffer, Cursor<T> cursor);
}
