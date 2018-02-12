package org.knime.ip.dl;

import java.util.Arrays;

import org.knime.dl.core.DLDefaultDimensionOrders;
import org.knime.dl.core.DLDimension;
import org.knime.dl.core.DLTensorSpec;
import org.knime.dl.util.DLUtils;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imagej.ImgPlusMetadata;
import net.imagej.axis.CalibratedAxis;

final class DLKnipUtil {

	private DLKnipUtil() {
		// utility class
	}
	
	public static long[] getShapeFromImg(final ImgPlusValue img, final DLTensorSpec tensorSpec) {
		if (tensorSpec.getDimensionOrder() == DLDefaultDimensionOrders.Unknown) {
			throw new IllegalArgumentException(
					"Can't infer shape from image if the dimension order of the input tensor is unknown");
		}
		DLDimension[] tensorDimensionOrder = tensorSpec.getDimensionOrder().getDimensions();
		DLDimension[] imgDimensionOrder = getDimensionOrder(getAxes(img.getMetadata()));
		int[] mapping = DLUtils.Dimensions.getMapping(imgDimensionOrder, tensorDimensionOrder);
		return mapShape(img.getDimensions(), mapping);
	}
	
	private static long[] mapShape(final long[] imgShape, final int[] mapping) {
		assert imgShape.length == mapping.length;
		long[] mappedShape = new long[imgShape.length];
		for (int i = 0; i < mappedShape.length; i++) {
			mappedShape[i] = imgShape[mapping[i]];
		}
		return mappedShape;
	}
	
	private static DLDimension[] getDimensionOrder(CalibratedAxis[] axes) {
		return Arrays.stream(axes).map(DLKnipUtil::axisToDimension).toArray(i -> new DLDimension[i]);
	}
	
	private static CalibratedAxis[] getAxes(final ImgPlusMetadata metaData) {
		CalibratedAxis[] axes = new CalibratedAxis[metaData.numDimensions()];
		metaData.axes(axes);
		return axes;
	}
	
	private static DLDimension axisToDimension(CalibratedAxis axis) {
		switch (axis.type().getLabel()) {
		case "X": return DLDimension.Width;
		case "Y": return DLDimension.Height;
		case "Z": return DLDimension.Depth;
		case "Channel": return DLDimension.Channel;
		case "Time": return DLDimension.Time;

		default:
			throw new IllegalArgumentException("Unknown axis '" + axis.type().getLabel() + "' encountered.");
		}
	}
}
