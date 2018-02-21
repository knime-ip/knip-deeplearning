package org.knime.ip.dl;

import java.io.IOException;

import org.knime.core.node.ExecutionContext;
import org.knime.dl.core.DLTensor;
import org.knime.dl.core.data.DLReadableBuffer;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverter;
import org.knime.dl.util.DLUtils;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;

import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Base implementation that implements the conversion in a generic way.
 * 
 * Dimension auto-mapping is not yet implemented but will be in the future.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 *
 * @param <B> the {@link DLReadableBuffer} that backs the tensor
 * @param <T> needed for type-safety reasons
 */
abstract class DLAbstractAutoMappingTensorToImgPlusCellConverter <B extends DLReadableBuffer, T extends RealType<T>> implements DLTensorToDataCellConverter<B, ImgPlusCell<?>> {
	
	@Override
	public final void convert(DLTensor<B> input, ImgPlusCell<?>[] output, ExecutionContext exec) {
		final int batchSize = getSizeOfWholeBatch(input);
		final long[] shape = getKNIPShape(input);
		final int exampleSize = getSizeOfSingleExample(input);
		ImgPlusCellFactory imgCellFactory = new ImgPlusCellFactory(exec);
		for (int i = 0; i < batchSize / exampleSize; i++) {
			try {
				output[i] = imgCellFactory
						.createCell(createImg(input.getBuffer(), shape, exampleSize));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected abstract ImgPlus<T> createImg(B buffer, long[] shape, int exampleSize);
	
	private int getSizeOfWholeBatch(DLTensor<B> tensor) {
		final long batchSizeLong = tensor.getBuffer().size();
		if (batchSizeLong > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Invalid batch size. Converter only supports sizes up to 2^31-1.");
		}
		return (int) batchSizeLong;
	}
	
	private int getSizeOfSingleExample(DLTensor<B> tensor) {
		final long exampleSizeLong = DLUtils.Shapes.getFixedSize(tensor.getSpec().getShape()).getAsLong();
		if (exampleSizeLong > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Invalid example size. Converter only supports sizes up to 2^31-1.");
		}
		return (int) exampleSizeLong;
	}
	
	private long[] getKNIPShape(DLTensor<B> tensor) {
		final long[] shape = DLUtils.Shapes.getFixedShape(tensor.getSpec().getShape()).orElseThrow(
				() -> new IllegalArgumentException("Tensor spec does not provide a fully defined shape."));
		return DLKnipUtil.reverseShape(shape);
	}

}
