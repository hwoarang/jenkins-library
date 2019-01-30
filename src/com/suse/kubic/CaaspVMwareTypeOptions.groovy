package com.suse.kubic;

class CaaspVMwareTypeOptions implements Serializable {
	String image = null;
	String velumImage = null;
	String channel = 'devel_15';
	boolean vanilla = false;
	String extraRepo = null;
	boolean disableMeltdownSpectreFixes = false;
	int timeout = 45;

	int adminRam = 8192;
	int adminCpu = 4;
	int masterRam = 4096;
	int masterCpu = 4;
	int workerRam = 4096;
	int workerCpu = 4;
}
