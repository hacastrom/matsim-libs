/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.taxi.optimizer.assignment;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.contrib.taxi.optimizer.assignment.TaxiToRequestAssignmentCostProvider.Mode;
import org.matsim.testcases.MatsimTestUtils;

import java.util.List;

import static org.matsim.contrib.taxi.optimizer.TaxiOptimizerTests.PreloadedBenchmark;
import static org.matsim.contrib.taxi.optimizer.TaxiOptimizerTests.TaxiConfigVariant;
import static org.matsim.contrib.taxi.optimizer.TaxiOptimizerTests.createDefaultTaxiConfigVariants;
import static org.matsim.contrib.taxi.optimizer.TaxiOptimizerTests.runBenchmark;

public class AssignmentTaxiOptimizerIT {
	@Rule
	public final MatsimTestUtils utils = new MatsimTestUtils();

	@Test @Ignore // temporarily ignore this test due to problems on the build server
	public void testAssignment_arrivalTime() {
		PreloadedBenchmark benchmark = new PreloadedBenchmark("3.0", "25");
		List<TaxiConfigVariant> variants = createDefaultTaxiConfigVariants(true);
		AssignmentTaxiOptimizerParams params = new AssignmentTaxiOptimizerParams();

		params.setMode(Mode.ARRIVAL_TIME);
		params.setVehPlanningHorizonOversupply(99999);
		params.setVehPlanningHorizonUndersupply(99999);
		params.setNearestRequestsLimit(99999);
		params.setNearestVehiclesLimit(99999);
		runBenchmark(variants, params, benchmark, utils.getOutputDirectory());
	}

	@Test @Ignore // temporarily ignore this test due to problems on the build server
	public void testAssignment_pickupTime() {
		PreloadedBenchmark benchmark = new PreloadedBenchmark("3.0", "25");
		List<TaxiConfigVariant> variants = createDefaultTaxiConfigVariants(true);
		AssignmentTaxiOptimizerParams params = new AssignmentTaxiOptimizerParams();

		params.setMode(Mode.PICKUP_TIME);
		params.setVehPlanningHorizonOversupply(120);
		params.setVehPlanningHorizonUndersupply(30);
		params.setNearestRequestsLimit(10);
		params.setNearestVehiclesLimit(10);
		params.setReoptimizationTimeStep(10);
		runBenchmark(variants, params, benchmark, utils.getOutputDirectory());
	}

	@Test @Ignore // temporarily ignore this test due to problems on the build server
	public void testAssignment_dse() {
		PreloadedBenchmark benchmark = new PreloadedBenchmark("3.0", "25");
		List<TaxiConfigVariant> variants = createDefaultTaxiConfigVariants(true);
		AssignmentTaxiOptimizerParams params = new AssignmentTaxiOptimizerParams();

		params.setVehPlanningHorizonOversupply(120);
		params.setVehPlanningHorizonUndersupply(30);
		params.setNearestRequestsLimit(10);
		params.setNearestVehiclesLimit(10);
		params.setReoptimizationTimeStep(10);

		params.setMode(Mode.DSE);
		runBenchmark(variants, params, benchmark, utils.getOutputDirectory());

	}

	@Test @Ignore // temporarily ignore this test due to problems on the build server
	public void testAssignment_totalWaitTime() {
		PreloadedBenchmark benchmark = new PreloadedBenchmark("3.0", "25");
		List<TaxiConfigVariant> variants = createDefaultTaxiConfigVariants(true);
		AssignmentTaxiOptimizerParams params = new AssignmentTaxiOptimizerParams();

		params.setVehPlanningHorizonOversupply(120);
		params.setVehPlanningHorizonUndersupply(30);
		params.setNearestRequestsLimit(10);
		params.setNearestVehiclesLimit(10);
		params.setReoptimizationTimeStep(10);

		params.setMode(Mode.TOTAL_WAIT_TIME);
		params.setNullPathCost(300);
		runBenchmark(variants, params, benchmark, utils.getOutputDirectory());
	}
}
