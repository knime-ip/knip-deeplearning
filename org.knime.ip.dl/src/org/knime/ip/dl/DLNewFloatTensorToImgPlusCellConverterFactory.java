package org.knime.ip.dl;

import org.knime.dl.core.data.DLReadableFloatBuffer;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverter;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverterFactory;
import org.knime.knip.base.data.img.ImgPlusCell;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 */
public class DLNewFloatTensorToImgPlusCellConverterFactory extends DLAbstractTensorToImgPlusCellConverterFactory<DLReadableFloatBuffer>
		implements DLTensorToDataCellConverterFactory<DLReadableFloatBuffer, ImgPlusCell<?>> {

	@Override
	public Class<DLReadableFloatBuffer> getBufferType() {
		return DLReadableFloatBuffer.class;
	}

	@Override
	protected <T extends RealType<T>> DLTensorToDataCellConverter<DLReadableFloatBuffer, ImgPlusCell<?>> createConverterInternal() {
		return new DLAbstractTensorToImgPlusCellConverter<DLReadableFloatBuffer, float[], T>() {


			@Override
			protected ImgPlus<T> createImg(DLReadableFloatBuffer buffer, long[] shape, int exampleSize) {
				float[] exampleBuffer = new float[exampleSize];
				buffer.readToFloatArray(exampleBuffer, 0, exampleSize);
				return (ImgPlus<T>) new ImgPlus<>(ArrayImgs.floats(exampleBuffer, shape));
			}
		};
	}
}
