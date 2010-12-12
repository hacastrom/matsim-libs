/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package playground.jjoubert.roadpricing.senozon;

import org.matsim.api.core.v01.Id;

public abstract class SanralTollFactor {

	public static double getTollFactor(final Id vehicleId) {
		long id = Long.parseLong(vehicleId.toString());
		if (id < 1000000) { 
			/* It is a private vehicle */
			return 1.0;
		} else if (id < 1005000) {
			/* It is a commercial vehicle of Class B. From the counting station
			 * data I've inferred that the split between Class B and C is about
			 * 50/50, assuming that `Short' represents Class B, and `Medium' and
			 * `Long' combined represent Class C vehicles. Given the total number
			 * of 10,000 commercial vehicles for a 10% sample, the IDs are 
			 * associated accordingly. */
			return 3.0;
		} else if (id < 2000000) {
			/* It is a commercial vehicle of Class C. */
			return 6.0;
		} else if (id < 3000000) {
			/* It is a bus */
			double d = (3.0 * 0.7);
			return d;
		} else if (id < 4000000){
			/* It is a minibus taxi */
			return 0.7;
		} else {
			/* It is an external (presumably light) vehicle */
			return 1.0;
		}
	}
}
