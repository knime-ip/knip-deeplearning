package org.knime.ip.dl;

import java.io.IOException;
import java.util.OptionalLong;

import org.knime.core.data.DataType;
import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.core.data.DLReadableFloatBuffer;
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
public class DLFloatTensorToImgPlusCellConverterFactory
		implements DLTensorToDataCellConverterFactory<DLReadableFloatBuffer, ImgPlusCell<?>> {

	private static final OptionalLong DEST_COUNT = OptionalLong.of(1);

	@Override
	public String getName() {
		return DataType.getType(ImgPlusCell.class).toPrettyString();
	}

	@Override
	public Class<DLReadableFloatBuffer> getBufferType() {
		return DLReadableFloatBuffer.class;
	}

	@Override
	public DataType getDestType() {
		return ImgPlusCell.TYPE;
	}

	@Override
	public OptionalLong getDestCount(final DLTensorSpec spec) {
		return DEST_COUNT;
	}

	@Override
	public DLTensorToDataCellConverter<DLReadableFloatBuffer, ImgPlusCell<?>> createConverter() {
		return (input, out, exec) -> {
			final long batchSizeLong = input.getBuffer().size();
			if (batchSizeLong > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Invalid batch size. Converter only supports sizes up to 2^31-1.");
			}
			final int batchSize = (int) batchSizeLong;
			final long[] shape = DLUtils.Shapes.getFixedShape(input.getSpec().getShape()).orElseThrow(
					() -> new IllegalArgumentException("Tensor spec does not provide a fully defined shape."));
			final long exampleSizeLong = DLUtils.Shapes.getFixedSize(input.getSpec().getShape()).getAsLong();
			if (exampleSizeLong > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Invalid example size. Converter only supports sizes up to 2^31-1.");
			}
			final int exampleSize = (int) exampleSizeLong;
			final float[] batchBuffer = input.getBuffer().toFloatArray();
			for (int i = 0; i < batchSize / exampleSize; i++) {
				// TODO: share buffer instead of copying (- or introduce partial toFloatArray method)
				final float[] exampleBuffer = new float[(int) exampleSizeLong];
				System.arraycopy(batchBuffer, i * exampleSize, exampleBuffer, 0, exampleSize);
				try {
					out[i] = new ImgPlusCellFactory(exec)
							.createCell(new ImgPlus<>(ArrayImgs.floats(exampleBuffer, shape)));
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
