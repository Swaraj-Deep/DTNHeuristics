
package com.company;

public class DTNNode {
	int nodeNumber;
	int clusterId;
	double distanceFromMedoid;
	public void setNodeNumber(int i) {
		nodeNumber=i;
	}
	public void setClusterId(int i) {
		clusterId=i;
	}
	public void setDistanceFromMedoid(double i) {
		distanceFromMedoid=i;
	}
	public int getClusterId() {
		return clusterId;
	}
	public int getNodeNumber() {
		return nodeNumber;
	}
}
