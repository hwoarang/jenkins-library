package com.suse.kubic;

class CaaspKvmTypeOptions implements Serializable {
	String image = null;
	String velumImage = null;
	String channel = 'devel';
	boolean vanilla = false;
	String extraRepo = null;
	boolean disableMeltdownSpectreFixes = false;

	int adminRam = 8192;
	int adminCpu = 4;
	int masterRam = 4096;
	int masterCpu = 4;
	int workerRam = 4096;
	int workerCpu = 4;
}
