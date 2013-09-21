package dap.cad.pnode.util;

import java.util.LinkedHashMap;

/**
 * 
 * @author Flavio Esposito
 * placeholder for pnode policies 
 */
public class PnodePolicies {

	private LinkedHashMap<String,String> nodeEmbeddingPoliciesMap= null;
	private LinkedHashMap<String,String> linkEmbeddingPoliciesMap= null;
	private LinkedHashMap<String,String> nodePoliciesMap= null;


	public PnodePolicies(){
		this.nodeEmbeddingPoliciesMap = new LinkedHashMap<String,String>();
		this.linkEmbeddingPoliciesMap = new LinkedHashMap<String, String>();

	}

	public PnodePolicies(LinkedHashMap<String,String> nodePolicies,LinkedHashMap<String,String> linkPolicies ){
		this.nodeEmbeddingPoliciesMap = nodePolicies;
		this.linkEmbeddingPoliciesMap = linkPolicies;
	}
	
	

	/**
	 * @return the nodeEmbeddingPoliciesMap
	 */
	public LinkedHashMap<String, String> getNodeEmbeddingPoliciesMap() {
		return nodeEmbeddingPoliciesMap;
	}

	/**
	 * @param nodeEmbeddingPoliciesMap the nodeEmbeddingPoliciesMap to set
	 */
	public void setNodeEmbeddingPoliciesMap(
			LinkedHashMap<String, String> nodeEmbeddingPoliciesMap) {
		this.nodeEmbeddingPoliciesMap = nodeEmbeddingPoliciesMap;
	}

	/**
	 * @return the linkEmbeddingPoliciesMap
	 */
	public LinkedHashMap<String, String> getLinkEmbeddingPoliciesMap() {
		return linkEmbeddingPoliciesMap;
	}

	/**
	 * @param linkEmbeddingPoliciesMap the linkEmbeddingPoliciesMap to set
	 */
	public void setLinkEmbeddingPoliciesMap(
			LinkedHashMap<String, String> linkEmbeddingPoliciesMap) {
		this.linkEmbeddingPoliciesMap = linkEmbeddingPoliciesMap;
	}

	/**
	 * @return the nodePoliciesMap
	 */
	public LinkedHashMap<String, String> getNodePoliciesMap() {
		return nodePoliciesMap;
	}

	/**
	 * @param nodePoliciesMap the nodePoliciesMap to set
	 */
	public void setNodePoliciesMap(LinkedHashMap<String, String> nodePoliciesMap) {
		this.nodePoliciesMap = nodePoliciesMap;
	}

	
	
	
}
