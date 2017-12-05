package org.knime.ip.dl;

/**
 * Describes the dimension ordering that is used in the deep learning backend.</br>
 * Note that there exist two interpretations for the dimension ordering:</br>
 * Keras and TensorFlow use the notation Batch-Height-Width-Channel (also known as "channels-last") and this means
 * that the channel changes fastest when iterating over the image.</br>
 * In KNIP and many other tools for image analysis the notation X-Y-Channel is used
 * (the batch dimension is deep learning specific). However, this is also considered to be "channels-last" but in this
 * case the channel changes slowest when iterating over the image.</br>
 * Therefore "channels-last" in deep learning corresponds to "channels-first" in KNIP.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 *
 */
public enum DLDimensionOrder {
	
	/**
	 * Corresponds to CXY in KNIP.
	 */
	BHWC,
	/**
	 * Corresponds to XYC in KNIP.
	 */
	BCHW;

}
