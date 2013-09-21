/**
 * 
 */
package vinea.sp.util;

import vinea.slicespec.impl.googleprotobuf.SliceSpec;

/**
 * @author Flavio Esposito
 *
 */
public class SlicePartition {

	private SliceSpec.Slice.Builder toEmbed = null;
	private SliceSpec.Slice.Builder alreadyEmbedded = null;
	private SliceSpec.Slice.Builder residual = null;
	private int sliceID = -2;
	
	/**
	 * dummy constructor
	 */
	public SlicePartition() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param toEmbed
	 * @param alreadyEmbedded
	 */
	public SlicePartition(SliceSpec.Slice.Builder toEmbed,
			SliceSpec.Slice.Builder alreadyEmbedded) {
		
		this.toEmbed = toEmbed;
		this.alreadyEmbedded = alreadyEmbedded;
		
	}

	/**
	 * @return the toEmbed
	 */
	public synchronized SliceSpec.Slice.Builder getToEmbed() {
		return toEmbed;
	}

	/**
	 * @param toEmbed the toEmbed to set
	 */
	public synchronized void setToEmbed(SliceSpec.Slice.Builder toEmbed) {
		this.toEmbed = toEmbed;
	}

	/**
	 * @return the alreadyEmbedded
	 */
	public synchronized SliceSpec.Slice.Builder getAlreadyEmbedded() {
		return alreadyEmbedded;
	}

	/**
	 * @param alreadyEmbedded the alreadyEmbedded to set
	 */
	public synchronized void setAlreadyEmbedded(
			SliceSpec.Slice.Builder alreadyEmbedded) {
		this.alreadyEmbedded = alreadyEmbedded;
	}

	/**
	 * @return the residual
	 */
	public synchronized SliceSpec.Slice.Builder getResidual() {
		return residual;
	}

	/**
	 * @param residual the residual to set
	 */
	public synchronized void setResidual(SliceSpec.Slice.Builder residual) {
		this.residual = residual;
	}

	/**
	 * @return the sliceID
	 */
	public synchronized int getSliceID() {
		return sliceID;
	}

	/**
	 * @param sliceID the sliceID to set
	 */
	public synchronized void setSliceID(int sliceID) {
		this.sliceID = sliceID;
	}

}

