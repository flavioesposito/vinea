/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * 
 *  * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */
package rina.routing;


import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Link State Routing
 */
public class Dijkstra {
	/**
	 * build Forwarding Table
	 * @param map
	 * @param source
	 * @return map
	 */
	public static LinkedHashMap buildForwardingTable(LinkedHashMap map, String source)
	{
		//using Dijkstra Method
		LinkedHashMap FT = new LinkedHashMap<String,String>();   // destnation and next hop
         

		String[] all_node= LinkedHashMapKeyToArray(map); 

		int numNode = all_node.length; 

		int inf=999999999;
		String [] neighbour = LinkedHashMapKeyToArray((LinkedHashMap)map.get(source));
		int [] neighbourDis = LinkedHashMapValueToArray((LinkedHashMap)map.get(source));

		int nunNeighbour= neighbour.length;

		LinkedHashMap Dis= new LinkedHashMap();//store the distance to other nodes in the map
		LinkedHashMap Previous_Node = new LinkedHashMap(); //store the previous to the source


		//// all nodes to the source is inf
		for(int i=0;i<numNode;i++)
		{
			if(!all_node[i].equals(source))
			{
				Dis.put(all_node[i], inf );
				Previous_Node.put(all_node[i], source);

			}
		}

		//// update the neighbour of the source
		for(int i=0;i<nunNeighbour;i++)
		{
			Dis.put(neighbour[i], neighbourDis[i]);
			Previous_Node.put(all_node[i], source);
		}

		//Initialisation finished


		//now start to build the Forwarding Table 



		while(FT.size()!=(numNode-1))// not all node are in the set FT
		{
			String min = MinDisKey(Dis); // find the node with min distance in NOT FT  
			int minDis= Integer.parseInt (Dis.get(min).toString() );

			String next_hop=source;
			String previous_hop = Previous_Node.get(min).toString();

			LinkedList this_path = new LinkedList() ;

			if( previous_hop.equals(source) )
			{
				next_hop = min;
				this_path.add(min);
			}
			else
			{
				while(!previous_hop.equals( source))
				{   
					this_path.add(previous_hop);
					next_hop=previous_hop;
					previous_hop= Previous_Node.get(previous_hop).toString();
				}


			}

			FT.put(min, next_hop); 

			//////////////
			// now stat to update the Dis for all node that not in the FT

			int minNeighbourNum= ( (LinkedHashMap)map.get(min) ).size();


			String [] minNeighbour = LinkedHashMapKeyToArray((LinkedHashMap)map.get(min));
			int [] minNeighbourDis = LinkedHashMapValueToArray((LinkedHashMap)map.get(min));

			int disViaMin;

			for(int i=0;i<minNeighbourNum;i++)
			{
				if(FT.containsKey(minNeighbour[i])==false && (!minNeighbour[i].equals(source)))
				{
					int  current_dis = Integer.parseInt(Dis.get(minNeighbour[i]).toString());

					disViaMin = minDis + minNeighbourDis[i];

					if(disViaMin< current_dis)
					{
						Dis.put(minNeighbour[i], disViaMin);
						Previous_Node.put(minNeighbour[i],min);
					}

				}

			}

			Dis.remove(min);//remove min from the NOT FT list;

		}


		return FT;
	}
	/**
	 * convert LinkedHashMap Key To Array
	 * @param map
	 * @return array
	 */
	public  static String[] LinkedHashMapKeyToArray( LinkedHashMap hp)
	{
		int num = hp.size();

		String[] keyArray= new String[num];

		Object [] array ;
		array = hp.keySet().toArray();


		for(int j =0;j< num;j++)
		{
			keyArray[j] =  array[j].toString();
		} 
		return keyArray;


	}

	/**
	 * LinkedHashMap Value To Array
	 * @param hash map
	 * @return array of int
	 */
	public static int[] LinkedHashMapValueToArray( LinkedHashMap hp)
	{
		String[] key = LinkedHashMapKeyToArray(hp);

		int num = hp.size();
		int[] valueArray= new int[num];

		for(int j =0;j< num;j++)
		{
			valueArray[j] = Integer.parseInt( hp.get(key[j]).toString());

		}

		return valueArray;


	}
	/**
	 * Minimum Distance Key
	 * @param map
	 * @return minimum distance
	 */
	public static String MinDisKey(LinkedHashMap hp)
	{
		String min;

		int n=hp.size();

		String[] key = LinkedHashMapKeyToArray(hp);

		int[] value = LinkedHashMapValueToArray(hp);


		//bubble sort


		String minKey=key[0];

		int minValue= value[0];

		for(int j=0;j<n;j++) 
		{


			if(value[j] < minValue)
			{
				minKey = key[j];
				minValue = value[j];
			}
		}
		min=minKey;
		return min;

	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}