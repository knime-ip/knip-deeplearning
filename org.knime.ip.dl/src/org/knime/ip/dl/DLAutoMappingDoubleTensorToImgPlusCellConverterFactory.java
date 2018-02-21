package org.knime.ip.dl;

import org.knime.dl.core.data.DLReadableDoubleBuffer;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 * @author Adrian Nembach, KNIME, Konstanz, Germany
 */
public class DLAutoMappingDoubleTensorToImgPlusCellConverterFactory extends DLAbstractAutoMappingTensorToImgPlusCellConverterFactory<DLReadableDoubleBuffer> {

	@Override
	public Class<DLReadableDoubleBuffer> getBufferType() {
		return DLReadableDoubleBuffer.class;
	}
	
	protected <T extends RealType<T>> DLAbstractAutoMappingTensorToImgPlusCellConverter<DLReadableDoubleBuffer, T> createConverterInternal() {
		return new DLAbstractAutoMappingTensorToImgPlusCellConverter<DLReadableDoubleBuffer, T>() {

			@Override
			protected ImgPlus<T> createImg(DLReadableDoubleBuffer buffer, long[] shape, int exampleSize) {
				double[] exampleBuffer = new double[exampleSize];
				buffer.readToDoubleArray(exampleBuffer, 0, exampleSize);
				return (ImgPlus<T>) new ImgPlus<>(ArrayImgs.doubles(exampleBuffer, shape));
			}
		};
	}

}
