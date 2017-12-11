package org.knime.ip.dl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.knime.knip.core.ops.metadata.DimSwapper;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.type.numeric.RealType;

class DLDimensionMapper {
	
	private static final int NUM_SUPPORTED_AXIS = 5;

	private final Map<AxisType, Integer> m_dimMapping;
	private final DLDimensionOrder m_dimOrder;
	
	public DLDimensionMapper(final DLDimensionOrder dimOrder) {
		m_dimOrder = dimOrder;
		m_dimMapping = createDimMapping(dimOrder);
	}
	
	/**
	 * @param dimOrdering the dimension order the deep learning backend expects
	 * @return a map that maps the axes of a ImgPlus to the order the backend expects
	 */
	private static Map<AxisType, Integer> createDimMapping(DLDimensionOrder dimOrdering) {
		Map<AxisType, Integer> dimMapping = new HashMap<>();
		switch (dimOrdering) {
		case BTCDHW:
			dimMapping.put(Axes.X, 0);
			dimMapping.put(Axes.Y, 1);
			dimMapping.put(Axes.Z, 2);
			dimMapping.put(Axes.CHANNEL, 3);
			dimMapping.put(Axes.TIME, 4);
			return dimMapping;
		case BTDHWC:
			dimMapping.put(Axes.X, 1);
			dimMapping.put(Axes.Y, 2);
			dimMapping.put(Axes.Z, 3);
			dimMapping.put(Axes.TIME, 4);
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
		case BTCDHW:
			return img;
		case BTDHWC:
			int[] mapping = new int[] {2, 0, 1};
			return new ImgPlus<>(ImgView.wrap(DimSwapper.swap(img, mapping), img.factory()));
		default:
			throw new IllegalStateException("Unsupported dimension order '" + m_dimOrder + "'.");
		}
	}
	
	public long[] getDLShape(ImgPlus<?> img) {
		long[] shape = new long[img.numDimensions()];
		int[] mapping = inferMapping(img);
		assert shape.length == mapping.length : "Shape and mapping have different dimensionality.";
		for (int d = 0; d < mapping.length; d++) {
			// the mapping indicates how to alter the memory layout of the img
			// the layout in dl is interpreted from the back i.e. in BHWC the channel changes fastest
			// in KNIP the layout is interpreted from the front i.e. in XYC X changes fastest
			shape[mapping.length - mapping[d] - 1] = img.dimension(d);
		}
		return shape;
	}
	
	private int[] inferMapping(ImgPlus<?> img) {
		int[] mapping = new int[img.numDimensions()];
		// keeps track where we put the corresponding dimension
		int[] pos = new int[NUM_SUPPORTED_AXIS];
		// -1 indicates that we haven't seen the dimension yet
		Arrays.fill(pos, -1);
		// recognize img dimensions
		for (int d = 0; d < img.numDimensions(); d++) {
			AxisType a = img.axis(d).type();
			Integer newIdx = m_dimMapping.get(a);
			if (newIdx == null) {
				throw new IllegalArgumentException("The axis type '" + a + "' is currently not supported.");
			}
			pos[newIdx] = d;
		}
		int counter = 0;
		// fill mapping according to dimension priority
		for (int d = 0; d < pos.length; d++) {
			if (pos[d] == -1) {
				continue;
			}
			mapping[pos[d]] = counter++;
		}
		return mapping;
	}
	
	public long[] getKNIPShape(final long[] dlShape) {
		switch (m_dimOrder) {
		case BTCDHW:
			return dlShape;
		case BTDHWC:
			if (dlShape.length == 2) {
				return dlShape;
			} else if (dlShape.length == 3) {
				long[] knipShape = new long[] {dlShape[2], dlShape[1], dlShape[0]};
				return knipShape;
			}
			// TODO: Figure out how to handle higher dimensional output (Time vs. Depth)
			throw new IllegalArgumentException("Shapes of rank " + dlShape.length + " are not supported, yet.");
		default:
			// not gonna happen because the constructor would already crash
			throw new IllegalStateException("Unsupported dimension order '" + m_dimOrder + "'.");
		
		}
	}
}
