/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * -------------------------------------------------------------------
 *
 */

package org.knime.ip.dl;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;

import org.eclipse.draw2d.Cursors;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.ExtensibleUtilityFactory;
import org.knime.dl.core.DLTensor;
import org.knime.dl.core.data.DLWritableFloatBuffer;
import org.knime.dl.core.data.convert.DLAbstractTensorDataValueToTensorConverter;
import org.knime.dl.core.data.convert.DLDataValueToTensorConverter;
import org.knime.dl.core.data.convert.DLDataValueToTensorConverterFactory;
import org.knime.knip.base.data.img.ImgPlusValue;

import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * @author Christian Dietz, KNIME, Konstanz, Germany
 * @author Marcel Wiedenmann, KNIME, Konstanz, Germany
 */
public class DLImgPlusValueToFloatTensorConverterFactory<T extends RealType<T>>
		implements DLDataValueToTensorConverterFactory<ImgPlusValue, DLWritableFloatBuffer> {
	
	// TODO: pull into abstract super class or at least set via constructor
	private final DLDimensionMapper m_dimMapper = new DLDimensionMapper(DLDimensionOrder.BTDHWC);
	
	@Override
	public String getName() {
		return ((ExtensibleUtilityFactory) ImgPlusValue.UTILITY).getName();
	}

	@Override
	public Class<ImgPlusValue> getSourceType() {
		return ImgPlusValue.class;
	}

	@Override
	public Class<DLWritableFloatBuffer> getBufferType() {
		return DLWritableFloatBuffer.class;
	}

	@Override
	public OptionalLong getDestCount(final List<DataColumnSpec> spec) {
		return OptionalLong.empty();
	}

	@Override
	public DLDataValueToTensorConverter<ImgPlusValue, DLWritableFloatBuffer> createConverter() {
		return new DLAbstractTensorDataValueToTensorConverter<ImgPlusValue, DLWritableFloatBuffer>() {

			@Override
			public void convertInternal(ImgPlusValue input, DLTensor<DLWritableFloatBuffer> output) {
				final float[] out;
				ImgPlus imgPlus = input.getImgPlus();
				RandomAccessibleInterval<T> permuted = m_dimMapper.mapDimensionsToDL(imgPlus);
				IterableInterval<T> iterableInterval = Views.flatIterable(permuted);
				if (imgPlus.size() >= Integer.MAX_VALUE) {
					throw new IllegalArgumentException(
							"Can't process images with more than Integer.MAX_VALUE pixels, yet.");
				}
				// TODO can be parallelized
				// TODO consider iteration order
				out = new float[(int) imgPlus.size()];
				final Cursor<T> c = iterableInterval.cursor();
				for (int i = 0; i < out.length; i++) {
					out[i] = c.next().getRealFloat();
				}
				output.getBuffer().putAll(out);

			}

			@Override
			protected long[] getShapeInternal(ImgPlusValue element) {
				return m_dimMapper.getDLShape(element.getImgPlus());
			}
		};
	}
				
}
