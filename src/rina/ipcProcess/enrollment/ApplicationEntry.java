package rina.ipcProcess.enrollment;

public class ApplicationEntry {
	
	public String ipcName;
	public int wellKnownPort;
	
	public ApplicationEntry(String ipcName, int wellKnownPort)
	{
		this.ipcName = ipcName;
		this.wellKnownPort = wellKnownPort;
	}

}
