/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package playground.dziemke.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacilitiesFactoryImpl;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;

/**
 * @author dziemke
 */
public class CreateFacilitiiesFileFromCSVFile {
	private final static Logger LOG = Logger.getLogger(CreateFacilitiiesFileFromCSVFile.class);

	public static void main(String[] args) {
		String csvFile = "../../../shared-svn/projects/maxess/data/nairobi/kodi/schools/primary_public/Public_Primary_School_listed_by_2007.csv";
		String facilitiesFile = "../../../shared-svn/projects/maxess/data/nairobi/kodi/schools/primary_public/facilities.xml";
		
		String facilitiesFileDescription = "Public Primary Schools in Kenya";
		String inputCRS = "EPSG:4326";
		String outputCRS = "EPSG:21037";
		String headOfCoordColumn = "Geolocation";
		String separator = ",";
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(inputCRS, outputCRS);

		ActivityFacilities activityFacilities = createActivityFaciltiesFromFile(csvFile, facilitiesFileDescription,
				headOfCoordColumn, ct, separator);
		writeFacilitiesFile(activityFacilities, facilitiesFile);
	}


	public static ActivityFacilities createActivityFaciltiesFromFile(String csvFileName, String facilitiesFileDescription,
			String headOfCoordColumn, CoordinateTransformation ct, String separator) {
		ActivityFacilities activityFacilities = FacilitiesUtils.createActivityFacilities(facilitiesFileDescription);
		int lineCount = 0;
		try {
			BufferedReader bufferedReader = IOUtils.getBufferedReader(csvFileName);

			// Header
			String currentLine = bufferedReader.readLine();
			Map<String,Integer> columnHeads = createColumnHeadsMap(currentLine, separator);
			lineCount++;

			// Lines with data
			while ((currentLine = bufferedReader.readLine()) != null) {
				// This quite complicated expression does the following:
				// Split on the comma only if that comma has zero, or an even number of quotes ahead of it.
				// See: http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
				String[] lineEntries = currentLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				lineCount++;

				ActivityFacility activityFacility = createFacility(lineCount, lineEntries, columnHeads, headOfCoordColumn, ct, separator);
				activityFacilities.addActivityFacility(activityFacility);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		LOG.info("All activity facilities created.");
		return activityFacilities;
	}


	private static Map<String,Integer> createColumnHeadsMap(String headLine, String separator) {
		String[] header = headLine.split(separator);
		Map<String,Integer> columnHeads = new LinkedHashMap<>(header.length);
		for (int i=0; i<header.length; i++) {
			columnHeads.put(header[i],i);
		}
		LOG.info("Map that relates all column head to their position created.");
		return columnHeads;
	}


	private static ActivityFacility createFacility(int lineCount, String[] lineEntries, Map<String,Integer> columnHeads,
			String headOfCoordColumn, CoordinateTransformation ct, String separator) {
		Id<ActivityFacility> id = Id.create(lineCount , ActivityFacility.class);

		String location = lineEntries[columnHeads.get(headOfCoordColumn)].replaceAll("[()\"]","");
		String[] coordinates = location.split(separator, -1);
		Coord coord = CoordUtils.createCoord(Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[0]));
		Coord transformedCoord = ct.transform(coord);

		ActivityFacilitiesFactory activityFacilitiesFactory = new ActivityFacilitiesFactoryImpl();
		ActivityFacility activityFacility = activityFacilitiesFactory.createActivityFacility(id, transformedCoord);
		ActivityOption activityOption = activityFacilitiesFactory.createActivityOption("Educational");
		activityFacility.addActivityOption(activityOption);
		return activityFacility;
	}


	private static void writeFacilitiesFile(ActivityFacilities activityFacilities, String facilitiesOutputFile) {
		FacilitiesWriter facilitiesWriter = new FacilitiesWriter(activityFacilities);
		facilitiesWriter.write(facilitiesOutputFile);
		LOG.info("Facility file written.");
	}
}