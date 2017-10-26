package org.knime.ip.dl;

import org.knime.core.data.DataType;
import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.core.data.DLReadableDoubleBuffer;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverter;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverterFactory;
import org.knime.dl.util.DLUtils;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 */
public class DLDoubleTensorToImgPlusCellConverterFactory
		implements DLTensorToDataCellConverterFactory<DLReadableDoubleBuffer, ImgPlusCell<?>> {

	@Override
	public String getName() {
		return DataType.getType(ImgPlusCell.class).toPrettyString();
	}

	@Override
	public Class<DLReadableDoubleBuffer> getBufferType() {
		return DLReadableDoubleBuffer.class;
	}

	@Override
	public DataType getDestType() {
		return ImgPlusCell.TYPE;
	}

	@Override
	public long getDestCount(final DLTensorSpec spec) {
		return 1;
	}

	@Override
	public DLTensorToDataCellConverter<DLReadableDoubleBuffer, ImgPlusCell<?>> createConverter() {
		return (exec, input, out) -> {
			final long batchSizeLong = input.getBuffer().size();
			final long[] shape = DLUtils.Shapes.getFixedShape(input.getSpec().getShape()).orElseThrow(
					() -> new IllegalArgumentException("Tensor spec does not provide a fully defined shape."));
			final long exampleSizeLong = DLUtils.Shapes.getFixedSize(input.getSpec().getShape()).getAsLong();
			if (batchSizeLong > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"Invalid example size. Converter only supports sizes up to " + Integer.MAX_VALUE + ".");
			}
			final int batchSize = (int) batchSizeLong;
			final int exampleSize = (int) exampleSizeLong;
			final double[] batchBuffer = input.getBuffer().toDoubleArray();
			for (int i = 0; i < batchSize / exampleSize; i++) {
				// TODO: share buffer instead of copying (or introduce partial toDoubleArray method)
				final double[] exampleBuffer = new double[(int) exampleSizeLong];
				System.arraycopy(batchBuffer, i * exampleSize, exampleBuffer, 0, exampleSize);
				out.accept(new ImgPlusCellFactory(exec)
						.createCell(new ImgPlus<>(ArrayImgs.doubles(exampleBuffer, shape))));
			}
		};
	}
}
