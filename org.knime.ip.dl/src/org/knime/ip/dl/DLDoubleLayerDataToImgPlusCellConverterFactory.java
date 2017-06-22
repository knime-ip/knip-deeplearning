package org.knime.ip.dl;

import java.util.function.Consumer;

import org.knime.core.data.DataType;
import org.knime.core.node.ExecutionContext;
import org.knime.dl.core.DLLayerData;
import org.knime.dl.core.DLLayerDataSpec;
import org.knime.dl.core.data.DLReadableDoubleBuffer;
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
public class DLDoubleLayerDataToImgPlusCellConverterFactory
		implements DLLayerDataToDataCellConverterFactory<DLReadableDoubleBuffer, ImgPlusCell<?>> {

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
	public Class<DLReadableDoubleBuffer> getBufferType() {
		return DLReadableDoubleBuffer.class;
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
	public DLLayerDataToDataCellConverter<DLReadableDoubleBuffer, ImgPlusCell<?>> createConverter() {
		return new DLLayerDataToDataCellConverter<DLReadableDoubleBuffer, ImgPlusCell<?>>() {

			@Override
			public void convert(final ExecutionContext exec, final DLLayerData<DLReadableDoubleBuffer> input,
					final Consumer<ImgPlusCell<?>> out) throws Exception {
				out.accept(new ImgPlusCellFactory(exec)
						.createCell(new ImgPlus<>(ArrayImgs.doubles(input.getBuffer().toDoubleArray(),
								DLUtils.Shapes.getFixedShape(input.getSpec().getShape()).get()))));
			}
		};
	}
}
