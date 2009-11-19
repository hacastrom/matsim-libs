/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package playground.mzilske.bvg09;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.DijkstraFactory;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.utils.CreatePseudoNetwork;
import org.matsim.transitSchedule.TransitScheduleWriterV1;
import org.matsim.transitSchedule.api.TransitLine;
import org.matsim.transitSchedule.api.TransitScheduleWriter;
import org.matsim.vehicles.BasicVehicle;
import org.matsim.vehicles.BasicVehicleCapacity;
import org.matsim.vehicles.BasicVehicleCapacityImpl;
import org.matsim.vehicles.BasicVehicleType;
import org.matsim.vehicles.VehicleWriterV1;
import org.matsim.vehicles.VehiclesFactory;
import org.matsim.vis.otfvis.opengl.OnTheFlyQueueSimQuad;
import org.xml.sax.SAXException;

import playground.marcel.pt.config.TransitConfigGroup;
import playground.marcel.pt.router.PlansCalcTransitRoute;
import playground.marcel.pt.router.TransitRouter;
import playground.marcel.pt.utils.MergeNetworks;
import playground.mohit.converter.VisumNetwork;
import playground.mohit.converter.VisumNetworkReader;
import playground.mzilske.pt.queuesim.GreedyUmlaufBuilderImpl;
import playground.mzilske.pt.queuesim.Umlauf;
import playground.mzilske.pt.queuesim.UmlaufInterpolator;

public class DataPrepare {

	private static final Logger log = Logger.getLogger(DataPrepare.class);

	// INPUT FILES
	private static String InVisumNetFile = "/Users/michaelzilske/Desktop/visumnet_utf8woBOM.net";
	private static String InNetworkFile = "/Volumes/Data/VSP/svn/shared-svn/studies/schweiz-ivtch/baseCase/network/ivtch-osm.xml";
	private static String InInputPlansFileWithXY2Links = "/Volumes/Data/VSP/coding/eclipse35/thesis-data/application/plans.census2000ivtch1pct.dilZh30km.sample.xml.gz";

	// INTERMEDIARY FILES
	private static String IntermediateTransitNetworkFile = "../berlin-bvg09/pt/alles_mit_umlaeufen/test/transit_network.xml";
	private static String IntermediateTransitScheduleWithoutNetworkFile = "/Users/michaelzilske/Desktop/transit_schedule_without_network.xml";

	// OUTPUT FILES
	private static String OutTransitScheduleWithNetworkFile = "../berlin-bvg09/pt/alles_mit_umlaeufen/test/transitSchedule.networkOevModellBln.xml";
	private static String OutVehicleFile = "../berlin-bvg09/pt/alles_mit_umlaeufen/test/vehicles.oevModellBln.xml";
	private static String OutMultimodalNetworkFile = "../berlin-bvg09/pt/alles_mit_umlaeufen/test/network.multimodal.mini.xml";
	private static String OutRoutedPlanFile = "../berlin-bvg09/pt/alles_mit_umlaeufen/test/plan.routedOevModell.BVB344.moreLegPlan_Agent.xml";


	private final ScenarioImpl scenario;
	private final Config config;

	private NetworkLayer pseudoNetwork;

	public DataPrepare() {
		this.scenario = new ScenarioImpl();
		this.config = this.scenario.getConfig();
	}

	protected void prepareConfig() {
		this.config.scenario().setUseTransit(true);
		this.config.scenario().setUseVehicles(true);
	}

	protected void convertSchedule() {
		final VisumNetwork vNetwork = new VisumNetwork();
		try {
			log.info("reading visum network.");
			new VisumNetworkReader(vNetwork).read(InVisumNetFile);
			log.info("converting visum data to TransitSchedule.");
			Visum2TransitSchedule converter = new Visum2TransitSchedule(vNetwork, this.scenario.getTransitSchedule(), this.scenario.getVehicles());

			// configure how transport modes must be converted
			// the ones for Berlin
			converter.registerTransportMode("B", TransportMode.bus);
			converter.registerTransportMode("F", TransportMode.walk);
			converter.registerTransportMode("K", TransportMode.bus);
			converter.registerTransportMode("L", TransportMode.other);
			converter.registerTransportMode("P", TransportMode.car);
			converter.registerTransportMode("R", TransportMode.bike);
			converter.registerTransportMode("S", TransportMode.train);
			converter.registerTransportMode("T", TransportMode.tram);
			converter.registerTransportMode("U", TransportMode.train);
			converter.registerTransportMode("V", TransportMode.other);
			converter.registerTransportMode("W", TransportMode.bus);
			converter.registerTransportMode("Z", TransportMode.train);
			converter.convert();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeScheduleAndVehicles() throws IOException,
			FileNotFoundException {
		log.info("writing TransitSchedule to file.");
		new TransitScheduleWriterV1(this.scenario.getTransitSchedule()).write(IntermediateTransitScheduleWithoutNetworkFile);
		log.info("writing vehicles to file.");
		new VehicleWriterV1(this.scenario.getVehicles()).writeFile(OutVehicleFile);
		new NetworkWriter(pseudoNetwork, IntermediateTransitNetworkFile).write();
		try {
			new TransitScheduleWriter(this.scenario.getTransitSchedule()).writeFile(OutTransitScheduleWithNetworkFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void createNetworkFromSchedule() {
		pseudoNetwork = new NetworkLayer();
		new CreatePseudoNetwork(this.scenario.getTransitSchedule(), pseudoNetwork, "tr_").createNetwork();
	}

	protected void mergeNetworks() {
		NetworkLayer transitNetwork = new NetworkLayer();
		NetworkLayer streetNetwork = new NetworkLayer();
		try {
			new MatsimNetworkReader(transitNetwork).parse(IntermediateTransitNetworkFile);
			new MatsimNetworkReader(streetNetwork).parse(InNetworkFile);
			MergeNetworks.merge(streetNetwork, "", transitNetwork, "", this.scenario.getNetwork());
			new NetworkWriter(this.scenario.getNetwork(), OutMultimodalNetworkFile).write();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void routePopulation() {
		PopulationImpl pop = this.scenario.getPopulation();
		try {
			new MatsimPopulationReader(this.scenario).parse(InInputPlansFileWithXY2Links);
			pop.printPlansCount();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		DijkstraFactory dijkstraFactory = new DijkstraFactory();
		FreespeedTravelTimeCost timeCostCalculator = new FreespeedTravelTimeCost(this.scenario.getConfig().charyparNagelScoring());
		TransitConfigGroup transitConfig = new TransitConfigGroup();
		PlansCalcTransitRoute router = new PlansCalcTransitRoute(this.scenario.getConfig().plansCalcRoute(),
				this.scenario.getNetwork(), timeCostCalculator, timeCostCalculator, dijkstraFactory,
				this.scenario.getTransitSchedule(), transitConfig);
		log.info("start pt-router");
		router.run(pop);
		log.info("write routed plans out.");
		new PopulationWriter(pop).write(OutRoutedPlanFile);
	}

	protected void visualizeRouterNetwork() {
		TransitRouter router = new TransitRouter(this.scenario.getTransitSchedule());
		Network routerNet = router.getTransitRouterNetwork();

		log.info("create vis network");
		ScenarioImpl visScenario = new ScenarioImpl();
		NetworkLayer visNet = visScenario.getNetwork();

		for (Node node : routerNet.getNodes().values()) {
			visNet.createAndAddNode(node.getId(), node.getCoord());
		}
		for (Link link : routerNet.getLinks().values()) {
			visNet.createAndAddLink(link.getId(), visNet.getNodes().get(link.getFromNode().getId()), visNet.getNodes().get(link.getToNode().getId()),
					link.getLength(), link.getFreespeed(Time.UNDEFINED_TIME), link.getCapacity(Time.UNDEFINED_TIME), link.getNumberOfLanes(Time.UNDEFINED_TIME));
		}

		log.info("write routerNet.xml");
		new NetworkWriter(visNet, "visNet.xml").write();

		log.info("start visualizer");
		EventsManagerImpl events = new EventsManagerImpl();
		OnTheFlyQueueSimQuad client = new OnTheFlyQueueSimQuad(visScenario, events);
		client.run();
	}
	
	private void buildUmlaeufe() {
		Collection<TransitLine> transitLines = scenario.getTransitSchedule().getTransitLines().values();
		GreedyUmlaufBuilderImpl greedyUmlaufBuilder = new GreedyUmlaufBuilderImpl(new UmlaufInterpolator(scenario.getNetwork()), transitLines);
		Collection<Umlauf> umlaeufe = greedyUmlaufBuilder.build();
		
		VehiclesFactory vb = this.scenario.getVehicles().getFactory();
		BasicVehicleType vehicleType = vb.createVehicleType(new IdImpl(
				"defaultTransitVehicleType"));
		BasicVehicleCapacity capacity = new BasicVehicleCapacityImpl();
		capacity.setSeats(Integer.valueOf(101));
		capacity.setStandingRoom(Integer.valueOf(0));
		vehicleType.setCapacity(capacity);
		this.scenario.getVehicles().getVehicleTypes().put(vehicleType.getId(),
				vehicleType);

		long vehId = 0;
		for (Umlauf umlauf : umlaeufe) {
			BasicVehicle veh = vb.createVehicle(new IdImpl("veh_"+ Long.toString(vehId++)), vehicleType);
			this.scenario.getVehicles().getVehicles().put(veh.getId(), veh);
			umlauf.setVehicleId(veh.getId());
		}
	}


	private void emptyVehicles() {
		this.scenario.getVehicles().getVehicles().clear();
	}
	

	public static void run(String inVisumFile, String inNetworkFile, String inInputPlansFile,
			String interTransitNetworkFile, String interTransitScheduleWithoutNetworkFile,
			String outTransitScheduleWithNetworkFile, String outVehicleFile, String outMultimodalNetworkFile, 
			String outRoutedPlansFile){
		
		DataPrepare.InVisumNetFile = inVisumFile;
		DataPrepare.InNetworkFile = inNetworkFile;
		DataPrepare.InInputPlansFileWithXY2Links = inInputPlansFile;
		
		DataPrepare.IntermediateTransitNetworkFile = interTransitNetworkFile;
		DataPrepare.IntermediateTransitScheduleWithoutNetworkFile = interTransitScheduleWithoutNetworkFile;
		
		DataPrepare.OutTransitScheduleWithNetworkFile = outTransitScheduleWithNetworkFile;
		DataPrepare.OutVehicleFile = outVehicleFile;
		DataPrepare.OutMultimodalNetworkFile = outMultimodalNetworkFile;
		DataPrepare.OutRoutedPlanFile = outRoutedPlansFile;
				
		DataPrepare app = new DataPrepare();
		app.prepareConfig();
		app.convertSchedule();
		app.createNetworkFromSchedule();
		app.emptyVehicles();
		app.buildUmlaeufe();
		
		try {
			app.writeScheduleAndVehicles();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		app.mergeNetworks();
		app.routePopulation();
//		app.visualizeRouterNetwork();

		log.info("done.");
	}

	public static void main(final String[] args) {
		DataPrepare app = new DataPrepare();
		app.prepareConfig();
		app.convertSchedule();
		app.createNetworkFromSchedule();
		app.emptyVehicles();
		app.buildUmlaeufe();
		
		try {
			app.writeScheduleAndVehicles();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		app.mergeNetworks();
//		app.routePopulation();
//		app.visualizeRouterNetwork();

		log.info("done.");
	}

}
