package org.knime.ip.dl;

import java.util.HashMap;
import java.util.Map;

import org.knime.knip.core.ops.metadata.DimSwapper;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

class DLDimensionMapper {

	private final Map<AxisType, Integer> m_dimMapping;
	private final DLDimensionOrder m_dimOrder;
	
	public DLDimensionMapper(final DLDimensionOrder dimOrder) {
		m_dimOrder = dimOrder;
		m_dimMapping = createDimMapping(dimOrder);
	}
	
	/**
	 * This method must always be consistent with SUPPORTED_AXES
	 * 
	 * @param dimOrdering the dimension order the deep learning backend expects
	 * @return a map that maps the axes of a ImgPlus to the order the backend expects
	 */
	private static Map<AxisType, Integer> createDimMapping(DLDimensionOrder dimOrdering) {
		Map<AxisType, Integer> dimMapping = new HashMap<>();
		switch (dimOrdering) {
		case BCHW:
			dimMapping.put(Axes.X, 0);
			dimMapping.put(Axes.Y, 1);
			dimMapping.put(Axes.CHANNEL, 2);
			return dimMapping;
		case BHWC:
			dimMapping.put(Axes.X, 1);
			dimMapping.put(Axes.Y, 2);
			dimMapping.put(Axes.CHANNEL, 0);
			return dimMapping;
		default:
			throw new IllegalArgumentException("Unsupported dimension order '" + dimOrdering + "'.");
			
		}
	}
	
	public <T> RandomAccessibleInterval<T> mapDimensionsToDL(final ImgPlus<T> img) {
		int[] mapping = inferMapping(img);
		return DimSwapper.swap(img, mapping);
	}
	
	public <T extends RealType> ImgPlus<T> mapDimensionsFromDL(final ImgPlus img) {
		// TODO: meta data for the output
		switch (m_dimOrder) {
		case BCHW:
			return img;
		case BHWC:
			int[] mapping = new int[] {2, 0, 1};
			return new ImgPlus<T>(ImgView.wrap(DimSwapper.swap(img, mapping), img.factory()));
		default:
			throw new IllegalStateException("Unsupported dimension order '" + m_dimOrder + "'.");
		}
	}
	
	private int[] inferMapping(ImgPlus<?> img) {
		int[] mapping = new int[img.numDimensions()];
		for (int d = 0; d < img.numDimensions(); d++) {
			AxisType a = img.axis(d).type();
			Integer newIdx = m_dimMapping.get(a);
			if (newIdx == null) {
				throw new IllegalArgumentException("The axis type '" + a + "' is currently not supported.");
			}
			mapping[d] = newIdx;
		}
		return mapping;
	}
	
	public long[] getKNIPShape(final long[] dlShape) {
		switch (m_dimOrder) {
		case BCHW:
			return dlShape;
		case BHWC:
			if (dlShape.length == 2) {
				return dlShape;
			} else if (dlShape.length == 3) {
				long[] knipShape = new long[] {dlShape[2], dlShape[1], dlShape[0]};
				return knipShape;
			}
			throw new IllegalArgumentException("Shapes of rank " + dlShape.length + " are not supported, yet.");
		default:
			// not gonna happen because the constructor would already crash
			throw new IllegalStateException("Unsupported dimension order '" + m_dimOrder + "'.");
		
		}
	}
}
