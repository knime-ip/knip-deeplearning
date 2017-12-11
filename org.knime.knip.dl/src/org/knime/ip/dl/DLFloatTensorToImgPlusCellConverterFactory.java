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

import java.util.OptionalLong;

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
	
	private DLDimensionMapper m_dimensionMapper = new DLDimensionMapper(DLDimensionOrder.BTDHWC);

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
	public OptionalLong getDestCount(final DLTensorSpec spec) {
		return OptionalLong.of(1);
	}

	@Override
	public DLTensorToDataCellConverter<DLReadableFloatBuffer, ImgPlusCell<?>> createConverter() {
		return (exec, input, out) -> {
			final long batchSizeLong = input.getBuffer().size();
			final long[] shape = DLUtils.Shapes.getFixedShape(input.getSpec().getShape()).orElseThrow(
					() -> new IllegalArgumentException("Tensor spec does not provide a fully defined shape."));
			final long exampleSizeLong = DLUtils.Shapes.getFixedSize(input.getSpec().getShape()).getAsLong();
			if (batchSizeLong > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"Invalid example size. Converter only supports sizes up to " + Integer.MAX_VALUE + ".");
			}
			final int batchSize = (int) batchSizeLong;
			final int exampleSize = (int) exampleSizeLong;
			final float[] batchBuffer = input.getBuffer().toFloatArray();
			for (int i = 0; i < batchSize / exampleSize; i++) {
				// TODO: share buffer instead of copying (or introduce partial toFloatArray method)
				final float[] exampleBuffer = new float[(int) exampleSizeLong];
				System.arraycopy(batchBuffer, i * exampleSize, exampleBuffer, 0, exampleSize);
				out.accept(
						new ImgPlusCellFactory(exec).createCell(m_dimensionMapper.mapDimensionsFromDL(
								new ImgPlus<>(
										ArrayImgs.floats(exampleBuffer, m_dimensionMapper.getKNIPShape(shape))))));
			}
		};
	}
}
