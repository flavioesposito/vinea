/**
 * 
 */
package dap.cad.pnode.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import dap.cad.config.CADConfig;

/**
 * @author flavio
 *
 */
public class BiddingData {

	private boolean _rebroadcast = false;
	/**
	 * pNodeName
	 */
	private String _pNodeName = null;

	/**
	 * allocation policy default MAD (SAD or MAD or write your own)
	 */
	private String _allocationPolicy= null;
	/**
	 * allocation vector for each slice hosted or in hosting attempt <sliceID, <vnode, pnodeNameOrAddress>>
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer,String>> _allocationVectorMap = null;

	/**
	 * bid vector for each slice hosted or in hosting attempt
	 */
	private LinkedHashMap<Integer, LinkedList<Double>> _bidVectorMap = null;

	/**
	 * bundle vector for each slice hosted or in hosting attempt
	 */
	private LinkedHashMap<Integer, LinkedList<Integer>> _mMap = null;

	/**
	 * <sliceID, <vNodeID, biddingTime>>
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer,Long>> _biddingTimeMap = null;
	
	/**
	 * service providers can send requests for embedding
	 */
	private String _mySP = null;

	/**
	 * to reset to this value when the bundle is reset
	 */
	private double _nodeStressBeforeThisSliceRequest = 0.01;

	/**
	 * global node capacity
	 */
	private double _nodeStress = 0.01;

	/**
	 * initial stress on physical node
	 */
	private double _stress = 0.01;

	/**
	 * target node capacity
	 */
	private double _targetNodeCapacity = 100.0;


	/**
	 * target adjacent link capacity
	 */
	private double _targetLinkCapacity = 500.0;

	/**
	 * target stress
	 */
	private double _targetStress = 1.0;


	/**
	 * <sliceID, iteration_t >
	 */
	private LinkedHashMap<Integer, Integer> _iterationMap  =null;
	/**
	 * <sliceID,<pnode id, vnode id>> known so far
	 */
	private LinkedHashMap<Integer,LinkedHashMap<Integer, Integer>> _nodeMappingMap  = null;


	/**
	 * node bidding utility function
	 */
	private String nodeUtility = null;//cad.utility = utility1

	/**
	 * assignment Vector: least or most informative (x or a)
	 */
	private String assignmentVectorPolicy = null; //# least or most cad.assignmentVector = least
	/**
	 * bidVectorLength
	 */
	private int bidVectorLengthPolicy = 0;//	cad.bidVectorLength = 1 

	/**
	 * pnode utility function
	 */
	private PnodeUtil _NodeUtil= null;

	/**
	 * receiver allocation vector
	 */
	private LinkedHashMap<Integer,String> a_i = null;
	/**
	 * sender allocation vector
	 */
	private LinkedHashMap<Integer,String> a_k = null;
	
	/**
	 * constructor 
	 */
	public BiddingData() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the _allocationPolicy
	 */
	public String get_allocationPolicy() {
		return _allocationPolicy;
	}


	/**
	 * @param _allocationPolicy the _allocationPolicy to set
	 */
	public void set_allocationPolicy(String _allocationPolicy) {
		this._allocationPolicy = _allocationPolicy;
	}


	/**
	 * @return the _allocationVectorMap
	 */
	public LinkedHashMap<Integer, LinkedHashMap<Integer, String>> get_allocationVectorMap() {
		return _allocationVectorMap;
	}



	/**
	 * @param _allocationVectorMap the _allocationVectorMap to set
	 */
	public void set_allocationVectorMap(
			LinkedHashMap<Integer, LinkedHashMap<Integer, String>> _allocationVectorMap) {
		this._allocationVectorMap = _allocationVectorMap;
	}


	/**
	 * @return the _allocationVectorMap
	 */
	public LinkedHashMap<Integer, String> get_allocationVector(int sliceID) {
		return _allocationVectorMap.get(sliceID);
	}

	/**
	 * set _allocationVectorMap
	 * @param sliceID
	 * @param a
	 */
	public void set_allocationVector(int sliceID, LinkedHashMap<Integer, String> a) {
		_allocationVectorMap.put(sliceID, a);
	}


	/**
	 * @return the _bidVectorMap
	 */
	public LinkedHashMap<Integer, LinkedList<Double>> get_bidVectorMap() {
		return _bidVectorMap;
	}


	/**
	 * @param _bidVectorMap the _bidVectorMap to set
	 */
	public void set_bidVectorMap(
			LinkedHashMap<Integer, LinkedList<Double>> _bidVectorMap) {
		this._bidVectorMap = _bidVectorMap;
	}

	/**
	 * @return the _allocationVectorMap
	 */
	public LinkedList<Double> get_bidVector(int sliceID) {
		return _bidVectorMap.get(sliceID);
	}

	/**
	 * set _bidVectorMap
	 * @param sliceID
	 * @param bid vector
	 */
	public void set_bidVector(int sliceID, LinkedList<Double> b) {
		_bidVectorMap.put(sliceID, b);
	}

	/**
	 * @return the _mMap
	 */
	public LinkedHashMap<Integer, LinkedList<Integer>> get_mMap() {
		return _mMap;
	}


	/**
	 * @param _mMap the _mMap to set
	 */
	public void set_mMap(LinkedHashMap<Integer, LinkedList<Integer>> _mMap) {
		this._mMap = _mMap;
	}

	
	/**
	 * @return the _mMap
	 */
	public LinkedList<Integer> get_m(int sliceID) {
		return _mMap.get(sliceID);
	}

	/**
	 * the bundle vector to set
	 * @param sliceID
	 * @param m
	 */
	public void set_m(int sliceID, LinkedList<Integer> m) {
		this._mMap.put(sliceID, m);
	}
	
	

	/**
	 * @return the _mySP
	 */
	public String get_mySP() {
		return _mySP;
	}


	/**
	 * @param _mySP the _mySP to set
	 */
	public void set_mySP(String _mySP) {
		this._mySP = _mySP;
	}


	/**
	 * @return the _nodeStressBeforeThisSliceRequest
	 */
	public double get_nodeStressBeforeThisSliceRequest() {
		return _nodeStressBeforeThisSliceRequest;
	}


	/**
	 * @param _nodeStressBeforeThisSliceRequest the _nodeStressBeforeThisSliceRequest to set
	 */
	public void set_nodeStressBeforeThisSliceRequest(
			double _nodeStressBeforeThisSliceRequest) {
		this._nodeStressBeforeThisSliceRequest = _nodeStressBeforeThisSliceRequest;
	}


	/**
	 * @return the _nodeStress
	 */
	public double get_nodeStress() {
		return _nodeStress;
	}


	/**
	 * @param _nodeStress the _nodeStress to set
	 */
	public void set_nodeStress(double _nodeStress) {
		this._nodeStress = _nodeStress;
	}


	/**
	 * @return the _stress
	 */
	public double get_stress() {
		return _stress;
	}


	/**
	 * @param _stress the _stress to set
	 */
	public void set_stress(double _stress) {
		this._stress = _stress;
	}


	/**
	 * @return the _targetNodeCapacity
	 */
	public double get_targetNodeCapacity() {
		return _targetNodeCapacity;
	}


	/**
	 * @param _targetNodeCapacity the _targetNodeCapacity to set
	 */
	public void set_targetNodeCapacity(double _targetNodeCapacity) {
		this._targetNodeCapacity = _targetNodeCapacity;
	}


	/**
	 * @return the _targetLinkCapacity
	 */
	public double get_targetLinkCapacity() {
		return _targetLinkCapacity;
	}


	/**
	 * @param _targetLinkCapacity the _targetLinkCapacity to set
	 */
	public void set_targetLinkCapacity(double _targetLinkCapacity) {
		this._targetLinkCapacity = _targetLinkCapacity;
	}


	/**
	 * @return the _targetStress
	 */
	public double get_targetStress() {
		return _targetStress;
	}


	/**
	 * @param _targetStress the _targetStress to set
	 */
	public void set_targetStress(double _targetStress) {
		this._targetStress = _targetStress;
	}


	/**
	 * @return the _iterationMap
	 */
	public LinkedHashMap<Integer, Integer> get_iterationMap() {
		return _iterationMap;
	}


	/**
	 * @param _iterationMap the _iterationMap to set
	 */
	public void set_iterationMap(LinkedHashMap<Integer, Integer> _iterationMap) {
		this._iterationMap = _iterationMap;
	}


	/**
	 * @return the _nodeMappingMap
	 */
	public LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> get_nodeMappingMap() {
		return _nodeMappingMap;
	}


	/**
	 * @param _nodeMappingMap the _nodeMappingMap to set
	 */
	public void set_nodeMappingMap(
			LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> _nodeMappingMap) {
		this._nodeMappingMap = _nodeMappingMap;
	}


	/**
	 * @return the nodeUtility
	 */
	public String getNodeUtility() {
		return nodeUtility;
	}


	/**
	 * @param nodeUtility the nodeUtility to set
	 */
	public void setNodeUtility(String nodeUtility) {
		this.nodeUtility = nodeUtility;
	}


	/**
	 * @return the assignmentVectorPolicy
	 */
	public String getAssignmentVectorPolicy() {
		return assignmentVectorPolicy;
	}


	/**
	 * @param assignmentVectorPolicy the assignmentVectorPolicy to set
	 */
	public void setAssignmentVectorPolicy(String assignmentVectorPolicy) {
		this.assignmentVectorPolicy = assignmentVectorPolicy;
	}


	/**
	 * @return the bidVectorLengthPolicy
	 */
	public int getBidVectorLengthPolicy() {
		return bidVectorLengthPolicy;
	}


	/**
	 * @param bidVectorLengthPolicy the bidVectorLengthPolicy to set
	 */
	public void setBidVectorLengthPolicy(int bidVectorLengthPolicy) {
		this.bidVectorLengthPolicy = bidVectorLengthPolicy;
	}


	/**
	 * @return the _NodeUtil
	 */
	public PnodeUtil get_NodeUtil() {
		return _NodeUtil;
	}


	/**
	 * @param _NodeUtil the _NodeUtil to set
	 */
	public void set_NodeUtil(PnodeUtil _NodeUtil) {
		this._NodeUtil = _NodeUtil;
	}


	/**
	 * @return the pNodeName
	 */
	public String get_pNodeName() {
		return _pNodeName;
	}


	/**
	 * @param pNodeName the pNodeName to set
	 */
	public void set_pNodeName(String _pNodeName) {
		this._pNodeName = _pNodeName;
	}


	/**
	 * @return the _biddingTime
	 */
	public LinkedHashMap<Integer, LinkedHashMap<Integer, Long>> get_biddingTimeMap() {
		return _biddingTimeMap;
	}


	/**
	 * @param _biddingTime the _biddingTime to set
	 */
	public void set_biddingTimeMap(
			LinkedHashMap<Integer, LinkedHashMap<Integer, Long>> _biddingTimeMap) {
		this._biddingTimeMap = _biddingTimeMap;
	}



	/**
	 * @return the _biddingTimeMap (the _biddingTime vector for each sliceID)
	 */
	public LinkedHashMap<Integer, Long> get_biddingTimeVector(int sliceID) {
		return _biddingTimeMap.get(sliceID);
	}
	
	/**
	 *  the _biddingTime vector to set
	 * @param _biddingTimeVector
	 * @param sliceID
	 */
	public void set_biddingTimeVector(LinkedHashMap<Integer, Long> _biddingTimeVector, int sliceID) {
		this._biddingTimeMap.put(sliceID, _biddingTimeVector);
	}


	/**
	 * @return the _rebroadcast
	 */
	public boolean get_rebroadcast() {
		return _rebroadcast;
	}


	/**
	 * @param _rebroadcast the _rebroadcast to set
	 */
	public void set_rebroadcast(boolean _rebroadcast) {
		this._rebroadcast = _rebroadcast;
	}


	/**
	 * @return the a_i
	 */
	public LinkedHashMap<Integer, String> getA_i() {
		return a_i;
	}


	/**
	 * @param a_i the a_i to set
	 */
	public void setA_i(LinkedHashMap<Integer, String> a_i) {
		this.a_i = a_i;
	}


	/**
	 * @return the a_k
	 */
	public LinkedHashMap<Integer, String> getA_k() {
		return a_k;
	}


	/**
	 * @param a_k the a_k to set
	 */
	public void setA_k(LinkedHashMap<Integer, String> a_k) {
		this.a_k = a_k;
	}


}
