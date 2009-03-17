/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.dgrether.analysis.gis;

import org.matsim.controler.ScenarioImpl;
import org.matsim.network.NetworkLayer;


/**
 * @author dgrether
 *
 */
public class ShapeFileNetworkWriter {
	
	String base = "/Volumes/data/work/cvsRep/vsp-cvs/runs/run";
	 
	static String network = "/Volumes/data/work/vspSvn/studies/schweiz-ivtch/baseCase/network/ivtch-osm.xml";

	static String outputNetwork = "/Volumes/data/work/vspSvn/studies/schweiz-ivtch/baseCase/network/ivtch-osm.shp";
	
	public ShapeFileNetworkWriter() {
		
	}
	
	
	public void writeNetwork(String network, String outfile) {
		ScenarioImpl scenario = new ScenarioImpl(null, network, null, null);	
		NetworkLayer net = scenario.getNetwork();
		writeNetwork(net, outfile);
	}
	
	
	public void writeNetwork(NetworkLayer network, String outfile) {
		//uncomment the following lines to let the tool work
//		MATSimNet2QGIS mn2q = new MATSimNet2QGIS();
//		MATSimNet2QGIS.setFlowCapFactor(0.1);
//		mn2q.setNetwork(network);
//		mn2q.setCrs(X2QGIS.ch1903);
//		mn2q.writeShapeFile(outfile);
//		System.out.println("Network written to " + outfile);
	}
	
	
	public static void main(String[] args) {
		
		new ShapeFileNetworkWriter().writeNetwork(network, outputNetwork);
	}
	

}
