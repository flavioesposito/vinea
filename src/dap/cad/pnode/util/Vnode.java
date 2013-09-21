/**
 * 
 */
package dap.cad.pnode.util;

/**
 * @author flavio
 *
 */
public class Vnode {
	/**
	 * virtual node id
	 */
	private int _vid = -1;

	/**
	 * node owner 
	 */
	private int _pNodeOwner = -1;
	
	/**
	 * 
	 */
	private int _vcpu = 0;
	
	public Vnode(int id){
		this._vid = id;
	}

	/**
	 * @return the _vid
	 */
	public int get_vid() {
		return _vid;
	}

	/**
	 * @param _vid the _vid to set
	 */
	public void set_vid(int _vid) {
		this._vid = _vid;
	}

	/**
	 * @return the _pNodeOwner
	 */
	public int get_pNodeOwner() {
		return _pNodeOwner;
	}

	/**
	 * @param _pNodeOwner the _pNodeOwner to set
	 */
	public void set_pNodeOwner(int _pNodeOwner) {
		this._pNodeOwner = _pNodeOwner;
	}

	/**
	 * @return the _vcpu
	 */
	public int get_vcpu() {
		return _vcpu;
	}

	/**
	 * @param _vcpu the _vcpu to set
	 */
	public void set_vcpu(int _vcpu) {
		this._vcpu = _vcpu;
	}
	
	
}
