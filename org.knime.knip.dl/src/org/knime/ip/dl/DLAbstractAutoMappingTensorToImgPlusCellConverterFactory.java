package org.knime.ip.dl;

import java.util.OptionalLong;

import org.knime.core.data.DataType;
import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.core.data.DLReadableBuffer;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverter;
import org.knime.dl.core.data.convert.DLTensorToDataCellConverterFactory;
import org.knime.knip.base.data.img.ImgPlusCell;

import net.imglib2.type.numeric.RealType;

abstract class DLAbstractAutoMappingTensorToImgPlusCellConverterFactory<B extends DLReadableBuffer> implements
DLTensorToDataCellConverterFactory<B, ImgPlusCell<?>> {
	private static final OptionalLong DEST_COUNT = OptionalLong.of(1);

	@Override
	public final String getName() {
		return DataType.getType(ImgPlusCell.class).toPrettyString() + " (Auto-mapping)";
	}
	
	@Override
	public final DataType getDestType() {
		return ImgPlusCell.TYPE;
	}

	@Override
	public final OptionalLong getDestCount(final DLTensorSpec spec) {
		return DEST_COUNT;
	}
	
	@Override
	public final DLTensorToDataCellConverter<B, ImgPlusCell<?>> createConverter() {
		return createConverterInternal();
	}
	
	protected abstract <T extends RealType<T>> DLTensorToDataCellConverter<B, ImgPlusCell<?>> createConverterInternal();
	
}
