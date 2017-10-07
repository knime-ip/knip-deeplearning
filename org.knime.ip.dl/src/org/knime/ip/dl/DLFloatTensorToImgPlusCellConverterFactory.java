package org.knime.ip.dl;

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
	public long getDestCount(final DLTensorSpec spec) {
		return 1;
	}

	@Override
	public DLTensorToDataCellConverter<DLReadableFloatBuffer, ImgPlusCell<?>> createConverter() {
		return (exec, input, out) -> out.accept(new ImgPlusCellFactory(exec)
				.createCell(new ImgPlus<>(ArrayImgs.floats(input.getBuffer().toFloatArray(),
						DLUtils.Shapes.getFixedShape(input.getSpec().getShape()).get()))));
	}
}
