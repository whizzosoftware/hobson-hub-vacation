/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.vacation;

import org.joda.time.*;

/**
 * Class that manages the schedule of device manipulation.
 *
 * @author Dan Noguerol
 */
public class Schedule {
    private LocalDate date;
    private LocalTime arrivalTime;
    private boolean arrivalTimeFired;
    private LocalTime retireTime;
    private boolean retireTimeFired;
    private LocalTime bedTime;
    private boolean bedTimeFired;

    public Schedule(long now, String arrivalTime, String retireTime, String bedTime) {
        this(now, (int)(Math.random() * 60 - 30), arrivalTime, (int)(Math.random() * 60 - 30), retireTime, (int)(Math.random() * 60 - 30), bedTime);
    }

    public Schedule(long now, int arrivalTimeDeltaInMinutes, String arrivalTime, int retireTimeDeltaInMinutes, String retireTime, int bedTimeDeltaInMinutes, String bedTime) {
        int offsetInMillis = DateTimeZone.getDefault().getOffsetFromLocal(now);
        this.date = new LocalDate(now);
        this.arrivalTime = LocalTime.parse(arrivalTime.substring(0, arrivalTime.length() - 1)).plusMillis(offsetInMillis).plusMinutes(arrivalTimeDeltaInMinutes);
        this.retireTime = LocalTime.parse(retireTime.substring(0, retireTime.length() - 1)).plusMillis(offsetInMillis).plusMinutes(retireTimeDeltaInMinutes);
        this.bedTime = LocalTime.parse(bedTime.substring(0, bedTime.length() - 1)).plusMillis(offsetInMillis).plusMinutes(bedTimeDeltaInMinutes);
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalTime getRetireTime() {
        return retireTime;
    }

    public LocalTime getBedTime() {
        return bedTime;
    }

    public boolean checkArrivalTime(LocalTime now) {
        if (!arrivalTimeFired && now.compareTo(arrivalTime) >= 0) {
            arrivalTimeFired = true;
            return true;
        }
        return false;
    }

    public boolean checkRetireTime(LocalTime now) {
        if (!retireTimeFired && now.compareTo(retireTime) >= 0) {
            retireTimeFired = true;
            return true;
        }
        return false;
    }

    public boolean checkBedTime(LocalTime now) {
        if (!bedTimeFired && now.compareTo(bedTime) >= 0) {
            bedTimeFired = true;
            return true;
        }
        return false;
    }

    public boolean isComplete() {
        return (arrivalTimeFired && retireTimeFired && bedTimeFired);
    }

    public boolean isPreviousDay(LocalDate today) {
        return (DateTimeComparator.getDateOnlyInstance().compare(today.toDateTimeAtCurrentTime(), date.toDateTimeAtCurrentTime()) > 0);
    }
}
