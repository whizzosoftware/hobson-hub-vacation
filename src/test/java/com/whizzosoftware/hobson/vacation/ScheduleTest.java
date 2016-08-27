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

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ScheduleTest {
    @Test
    public void testFireTimes() {
        Schedule s = new Schedule(1472238195000L, 0, "00:00:00Z", 0, "04:00:00Z", 0, "05:30:00Z");

        assertFalse(s.checkArrivalTime(new LocalTime(17, 0)));
        assertFalse(s.checkArrivalTime(new LocalTime(17, 59)));
        assertTrue(s.checkArrivalTime(new LocalTime(18, 1)));
        assertFalse(s.checkArrivalTime(new LocalTime(18, 2)));

        assertFalse(s.isComplete());

        assertFalse(s.checkRetireTime(new LocalTime(19, 0)));
        assertFalse(s.checkRetireTime(new LocalTime(21, 59)));
        assertTrue(s.checkRetireTime(new LocalTime(22, 0)));
        assertFalse(s.checkRetireTime(new LocalTime(22, 1)));

        assertFalse(s.isComplete());

        assertFalse(s.checkBedTime(new LocalTime(23, 0)));
        assertFalse(s.checkBedTime(new LocalTime(23, 28)));
        assertTrue(s.checkBedTime(new LocalTime(23, 31)));
        assertFalse(s.checkBedTime(new LocalTime(23, 36)));

        assertTrue(s.isComplete());
    }

    @Test
    public void testSchedule() {
        Schedule s = new Schedule(1472256288000L, 0, "02:30:00Z", 0, "04:30:00Z", 0, "05:30:00Z");
        assertEquals("20:30:00.000", s.getArrivalTime().toString());
        assertEquals("22:30:00.000", s.getRetireTime().toString());
        assertEquals("23:30:00.000", s.getBedTime().toString());
    }

    @Test
    public void testIsPreviousDay() {
        Schedule s = new Schedule(1472238195000L, 0, "00:00:00Z", 0, "04:00:00Z", 0, "05:30:00Z");
        assertFalse(s.isPreviousDay(new LocalDate(1472238195000L)));
        assertTrue(s.isPreviousDay(new LocalDate(1472324595000L)));
    }
}
