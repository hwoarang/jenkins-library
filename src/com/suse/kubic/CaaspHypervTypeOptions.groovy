package com.suse.kubic;

class CaaspHypervTypeOptions implements Serializable {
	String image = null;
	String imageSourceUrl = null;
	String hvJumpHost = null;

	String adminRam = '8192mb';
	int adminCpu = 4;
	String masterRam = '4096mb';
	int masterCpu = 2;
	String workerRam = '2048mb';
	int workerCpu = 1;
}
