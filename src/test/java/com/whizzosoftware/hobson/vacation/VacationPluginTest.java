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

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class VacationPluginTest {
    @Test
    public void testOnRefresh() {
        MockVariableManager vm = new MockVariableManager();
        VacationPlugin p = new VacationPlugin("vacation");

        DeviceContext dctx1 = DeviceContext.create(PluginContext.createLocal("plugin1"), "device1");
        DeviceContext dctx2 = DeviceContext.create(PluginContext.createLocal("plugin2"), "device2");

        Map<String,Object> values = new HashMap<>();
        values.put(VacationPlugin.PROP_ENABLED, true);
        values.put(VacationPlugin.PROP_ARRIVAL_TIME, "00:00:00Z");
        values.put(VacationPlugin.PROP_ARRIVAL_DEVICES, Collections.singletonList(dctx1));
        values.put(VacationPlugin.PROP_RETIRE_TIME, "04:00:00Z");
        values.put(VacationPlugin.PROP_RETIRE_DEVICES, Collections.singletonList(dctx2));
        values.put(VacationPlugin.PROP_BED_TIME, "05:30:00Z");
        PropertyContainer config = new PropertyContainer(PropertyContainerClassContext.create(p.getContext(), "config"), values);

        p.onStartup(config);
        p.setVariableManager(vm);

        p.onRefresh(new LocalDateTime(2016, 8, 26, 0, 0, 0));
        assertEquals(0, vm.getVariableSets().size());

        LocalTime lt = p.getSchedule().getArrivalTime();
        p.onRefresh(new LocalDateTime(2016, 8, 26, lt.getHourOfDay(), lt.getMinuteOfHour()));
        assertEquals(1, vm.getVariableSets().size());
        assertEquals(true, vm.getVariableSets().get(VariableContext.create(dctx1, VariableConstants.ON)));
        vm.clearVariableSets();
        assertEquals(0, vm.getVariableSets().size());

        lt = p.getSchedule().getRetireTime().minusHours(1);
        p.onRefresh(new LocalDateTime(2016, 8, 26, lt.getHourOfDay(), lt.getMinuteOfHour()));
        assertEquals(0, vm.getVariableSets().size());

        lt = p.getSchedule().getRetireTime();
        p.onRefresh(new LocalDateTime(2016, 8, 26, lt.getHourOfDay(), lt.getMinuteOfHour()));
        assertEquals(2, vm.getVariableSets().size());
        assertEquals(false, vm.getVariableSets().get(VariableContext.create(dctx1, VariableConstants.ON)));
        assertEquals(true, vm.getVariableSets().get(VariableContext.create(dctx2, VariableConstants.ON)));
        vm.clearVariableSets();
        assertEquals(0, vm.getVariableSets().size());

        lt = p.getSchedule().getBedTime().minusMinutes(30);
        p.onRefresh(new LocalDateTime(2016, 8, 26, lt.getHourOfDay(), lt.getMinuteOfHour()));
        assertEquals(0, vm.getVariableSets().size());

        lt = p.getSchedule().getBedTime();
        p.onRefresh(new LocalDateTime(2016, 8, 26, lt.getHourOfDay(), lt.getMinuteOfHour()));
        assertEquals(1, vm.getVariableSets().size());
        assertEquals(false, vm.getVariableSets().get(VariableContext.create(dctx2, VariableConstants.ON)));
    }

    @Test
    public void testOnRefreshNewSchedule() {
        VacationPlugin p = new VacationPlugin("vacation");

        DeviceContext dctx1 = DeviceContext.create(PluginContext.createLocal("plugin1"), "device1");
        DeviceContext dctx2 = DeviceContext.create(PluginContext.createLocal("plugin2"), "device2");

        Map<String,Object> values = new HashMap<>();
        values.put(VacationPlugin.PROP_ENABLED, true);
        values.put(VacationPlugin.PROP_ARRIVAL_TIME, "18:00:00");
        values.put(VacationPlugin.PROP_ARRIVAL_DEVICES, Collections.singletonList(dctx1));
        values.put(VacationPlugin.PROP_RETIRE_TIME, "22:00:00");
        values.put(VacationPlugin.PROP_RETIRE_DEVICES, Collections.singletonList(dctx2));
        values.put(VacationPlugin.PROP_BED_TIME, "23:30:00");
        PropertyContainer config = new PropertyContainer(PropertyContainerClassContext.create(p.getContext(), "config"), values);

        p.onStartup(config);
        assertNull(p.getSchedule());
        p.onRefresh(new LocalDateTime(2016, 1, 1, 0, 0, 0));
        assertFalse(p.getSchedule().isPreviousDay(new LocalDate(2016, 1, 1)));
        assertTrue(p.getSchedule().isPreviousDay(new LocalDate(2016, 1, 2)));
        p.onRefresh(new LocalDateTime(2016, 1, 2, 0, 0, 0));
        assertFalse(p.getSchedule().isPreviousDay(new LocalDate(2016, 1, 2)));
        assertTrue(p.getSchedule().isPreviousDay(new LocalDate(2016, 1, 3)));
    }
}
