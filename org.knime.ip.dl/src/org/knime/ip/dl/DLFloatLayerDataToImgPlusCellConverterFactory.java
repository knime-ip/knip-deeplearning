package org.knime.ip.dl;

import java.io.IOException;
import java.util.function.Consumer;

import org.knime.core.data.DataType;
import org.knime.core.node.ExecutionContext;
import org.knime.dl.core.DLLayerData;
import org.knime.dl.core.DLLayerDataSpec;
import org.knime.dl.core.data.DLReadableFloatBuffer;
import org.knime.dl.core.data.convert.output.DLLayerDataToDataCellConverter;
import org.knime.dl.core.data.convert.output.DLLayerDataToDataCellConverterFactory;
import org.knime.dl.util.DLUtils;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 */
public class DLFloatLayerDataToImgPlusCellConverterFactory
		implements DLLayerDataToDataCellConverterFactory<DLReadableFloatBuffer, ImgPlusCell<?>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return DataType.getType(ImgPlusCell.class).toPrettyString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<DLReadableFloatBuffer> getBufferType() {
		return DLReadableFloatBuffer.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType getDestType() {
		return ImgPlusCell.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getDestCount(final DLLayerDataSpec spec) {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DLLayerDataToDataCellConverter<DLReadableFloatBuffer, ImgPlusCell<?>> createConverter() {
		return new DLLayerDataToDataCellConverter<DLReadableFloatBuffer, ImgPlusCell<?>>() {

			@Override
			public void convert(final ExecutionContext exec, final DLLayerData<DLReadableFloatBuffer> input,
					final Consumer<ImgPlusCell<?>> out) throws IOException {
				out.accept(new ImgPlusCellFactory(exec)
						.createCell(new ImgPlus<>(ArrayImgs.floats(input.getBuffer().toFloatArray(),
								DLUtils.Shapes.getFixedShape(input.getSpec().getShape()).get()))));
			}
		};
	}
}
