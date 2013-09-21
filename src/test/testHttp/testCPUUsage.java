package test.testHttp;

public class testCPUUsage {

	public static void main(String args[])
	{
		double result = 1;
		while(true)
		{
			for(int i =1 ;  i<500 ; i++)
			{
				result*=i;
			}

			System.out.println("result is " + result);
		}
	}

}
