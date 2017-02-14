 /*
 * Copyright (C) 2011 Nipuna Gunathilake.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.usf.cutr.gtfsrtvalidator.validation.entity;

import com.google.transit.realtime.GtfsRealtime;
import edu.usf.cutr.gtfsrtvalidator.api.model.MessageLogModel;
import edu.usf.cutr.gtfsrtvalidator.api.model.OccurrenceModel;
import edu.usf.cutr.gtfsrtvalidator.helper.ErrorListHelperModel;
import edu.usf.cutr.gtfsrtvalidator.validation.interfaces.FeedEntityValidator;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckPOSIXTime implements FeedEntityValidator{

    public static final String START_DATE = "2012/01/01";

    @Override
    public ErrorListHelperModel validate(GtfsDaoImpl gtfsData, GtfsRealtime.FeedMessage feedMessage) {
        MessageLogModel messageLogModel = new MessageLogModel("e001");
        OccurrenceModel errorOccurrence;
        List<OccurrenceModel> errorOccurrenceList = new ArrayList<>();
        
        long headerTimestamp = feedMessage.getHeader().getTimestamp();
        if (headerTimestamp != 0 && !isPosix(headerTimestamp)) {
            System.out.println("Timestamp not in POSIX time in FeedHeader");
            errorOccurrence = new OccurrenceModel("$.header.timestamp not in POSIX time", String.valueOf(headerTimestamp));
            errorOccurrenceList.add(errorOccurrence);
        }
        for(GtfsRealtime.FeedEntity entity: feedMessage.getEntityList()) {
            long tripupdateTimestamp = entity.getTripUpdate().getTimestamp();
            long vehicleTimestamp = entity.getVehicle().getTimestamp();
            
            if (tripupdateTimestamp != 0 && !isPosix(tripupdateTimestamp)) {
                System.out.println("Timestamp not in POSIX time in TripUpdate");
                errorOccurrence = new OccurrenceModel("$.entity.*.trip_update.timestamp not in POSIX time", String.valueOf(tripupdateTimestamp));
                errorOccurrenceList.add(errorOccurrence);
            }
            if (vehicleTimestamp != 0 && !isPosix(vehicleTimestamp)) {
                System.out.println("Timestamp not in POSIX time in VehiclePosition");
                errorOccurrence = new OccurrenceModel("$.entity.*.vehicle_position.timestamp not in POSIX time", String.valueOf(vehicleTimestamp));
                errorOccurrenceList.add(errorOccurrence);
            }
        }
        return new ErrorListHelperModel(messageLogModel, errorOccurrenceList);
    }

    //Checks if the value is a valid Unix date object
    public static boolean isPosix(long timestamp){
        long min_time;
        long max_time;
        try {
            min_time = new SimpleDateFormat("yyyy/MM/dd").parse(START_DATE).getTime()/1000;
            max_time = (long)Math.ceil((double)new Date().getTime()/1000);

        } catch (ParseException e) {
            return false;
        }
        return min_time < timestamp && timestamp <= max_time;
    }
}
