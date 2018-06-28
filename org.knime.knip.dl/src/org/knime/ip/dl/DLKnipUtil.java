package org.knime.ip.dl;

import org.knime.dl.core.DLDefaultDimensionOrder;
import org.knime.dl.core.DLDimension;
import org.knime.dl.core.DLDimensionOrder;
import org.knime.dl.core.DLTensorSpec;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.core.ops.metadata.DimSwapper;

import net.imagej.ImgPlusMetadata;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

final class DLKnipUtil {

	private DLKnipUtil() {
		// utility class
	}
	
	public static <T extends RealType<T>> RandomAccessibleInterval<T> mapImgToDL(ImgPlusValue<T> img, DLTensorSpec tensorSpec) {
		int[] mapping = calculateMapping(img, tensorSpec);
		return DimSwapper.swap(img.getImgPlus(), mapping);
	}
	
	private static DLDimensionOrder extractDimensionOrder(DLTensorSpec tensorSpec) {
		if (tensorSpec.getDimensionOrder() == DLDefaultDimensionOrder.Unknown) {
			throw new IllegalArgumentException(
					"Can't infer shape from image if the dimension order of the input tensor is unknown");
		}
		return tensorSpec.getDimensionOrder();
	}
	
	public static <T extends RealType<T>> long[] getShapeFromImg(final ImgPlusValue<T> img, final DLTensorSpec tensorSpec) {
		int[] mapping = calculateMapping(img, tensorSpec);
		return mapShape(img.getDimensions(), mapping);
	}
	
	private static <T extends RealType<T>> int[] calculateMapping(final ImgPlusValue<T> img, DLTensorSpec tensorSpec) {
		DLDimensionOrder tensorDimensionOrder = extractDimensionOrder(tensorSpec);
		DLDimension[] imgDimensionOrder = getDimensionOrder(getAxes(img.getMetadata()));
		return tensorDimensionOrder.inferMappingFor(imgDimensionOrder);
	}
	
	private static long[] mapShape(final long[] imgShape, final int[] mapping) {
		assert imgShape.length == mapping.length;
		long[] mappedShape = new long[imgShape.length];
		for (int i = 0; i < mappedShape.length; i++) {
			// in KNIP the last dimension changes the slowest (e.g. C in XYC) while
			// in deep learning (especially TensorFlow) the last dimension changes the fastest.
			mappedShape[i] = imgShape[imgShape.length - mapping[i] - 1];
		}
		return mappedShape;
	}
	
	private static DLDimension[] getDimensionOrder(CalibratedAxis[] axes) {
		DLDimension[] dimOrder = new DLDimension[axes.length];
		for (int i = 0; i < axes.length; i++) {
			// in KNIP the last dimension changes the slowest (e.g. C in XYC) while
			// in deep learning (especially TensorFlow) the last dimension changes the fastest.
			dimOrder[i] = axisToDimension(axes[axes.length - i - 1]);
		}
		return dimOrder;
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
	
	static long[] reverseShape(long[] shape) {
		long[] reversedShape = new long[shape.length];
		for (int i = 0; i < shape.length; i++) {
			reversedShape[i] = shape[shape.length - i - 1];
		}
		return reversedShape;
	}
}
