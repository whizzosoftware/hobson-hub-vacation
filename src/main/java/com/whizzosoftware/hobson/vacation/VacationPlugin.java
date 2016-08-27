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
import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.property.PropertyConstraintType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A plugin that run a timed sequence of devices turning on/off to simulate occupancy.
 *
 * @author Dan Noguerol
 */
public class VacationPlugin extends AbstractHobsonPlugin {
    private static final Logger logger = LoggerFactory.getLogger(VacationPlugin.class);

    static final String PROP_ENABLED = "enabled";
    static final String PROP_ARRIVAL_TIME = "arrivalTime";
    static final String PROP_ARRIVAL_DEVICES = "arrivalDevices";
    static final String PROP_RETIRE_TIME = "retireTime";
    static final String PROP_RETIRE_DEVICES = "retireDevices";
    static final String PROP_BED_TIME = "bedTime";

    public VacationPlugin(String pluginId) {
        super(pluginId);
    }

    private boolean enabled;
    private Schedule schedule;
    private String arrivalTime;
    private List<DeviceContext> arrivalDevices;
    private String retireTime;
    private List<DeviceContext> retireDevices;
    private String bedTime;

    @Override
    public void onStartup(PropertyContainer config) {
        processConfig(config);

        // set the plugin to running status
        setStatus(new PluginStatus(PluginStatus.Code.RUNNING));
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public long getRefreshInterval() {
        return 60L;
    }

    Schedule getSchedule() {
        return schedule;
    }

    @Override
    public void onRefresh() {
        onRefresh(new LocalDateTime());
    }

    void onRefresh(LocalDateTime now) {
        if (enabled) {
            LocalDate currentDate = now.toLocalDate();
            LocalTime currentTime = now.toLocalTime();

            // if schedule is from the previous day, create a new one
            if (schedule == null || schedule.isPreviousDay(currentDate)) {
                schedule = new Schedule(
                    currentDate.toDate().getTime(),
                    arrivalTime,
                    retireTime,
                    bedTime
                );
                logger.info("Today's vacation schedule is (arrivalTime={}, retireTime={}, bedTime={})", schedule.getArrivalTime(), schedule.getRetireTime(), schedule.getBedTime());
            }

            // check if any device states should be changed
            if (schedule.checkArrivalTime(currentTime)) {
                for (DeviceContext dctx : arrivalDevices) {
                    getVariableManager().setVariable(VariableContext.create(dctx, VariableConstants.ON), true);
                }
            } else if (schedule.checkRetireTime(currentTime)) {
                for (DeviceContext dctx : arrivalDevices) {
                    getVariableManager().setVariable(VariableContext.create(dctx, VariableConstants.ON), false);
                }
                for (DeviceContext dctx : retireDevices) {
                    getVariableManager().setVariable(VariableContext.create(dctx, VariableConstants.ON), true);
                }
            } else if (schedule.checkBedTime(currentTime)) {
                for (DeviceContext dctx : retireDevices) {
                    getVariableManager().setVariable(VariableContext.create(dctx, VariableConstants.ON), false);
                }
            }
        } else {
            logger.trace("Vacation plugin is not enabled; skipping");
        }
    }

    @Override
    public String getName() {
        return "Vacation";
    }

    @Override
    public TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder(PROP_ENABLED, "Enabled", "Whether this plugin is active or not.", TypedProperty.Type.BOOLEAN).build(),
            new TypedProperty.Builder(PROP_ARRIVAL_TIME, "Arrival Time", "The time in the evening you would typically arrive home.", TypedProperty.Type.TIME).build(),
            new TypedProperty.Builder(PROP_ARRIVAL_DEVICES, "Arrival Devices", "The devices to turn on to simulate arrival.", TypedProperty.Type.DEVICES).
                constraint(PropertyConstraintType.deviceVariable, VariableConstants.ON).
                build(),
            new TypedProperty.Builder(PROP_RETIRE_TIME, "Retire Time", "The time you would typically get in bed.", TypedProperty.Type.TIME).build(),
            new TypedProperty.Builder(PROP_RETIRE_DEVICES, "Retire Devices", "The devices to turn on to simulate retiring.", TypedProperty.Type.DEVICES).
                constraint(PropertyConstraintType.deviceVariable, VariableConstants.ON).
                build(),
            new TypedProperty.Builder(PROP_BED_TIME, "Bed Time", "The time you would typically go to sleep.", TypedProperty.Type.TIME).build(),
        };
    }

    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
        processConfig(config);
    }

    private void processConfig(PropertyContainer config) {
        boolean e = config.getBooleanPropertyValue(PROP_ENABLED);
        if (e && !this.enabled) {
            logger.debug("Enabling vacation plugin");
        } else if (!e && this.enabled) {
            logger.debug("Disabling vacation plugin");
        }
        enabled = e;
        arrivalTime = config.getStringPropertyValue(PROP_ARRIVAL_TIME);
        retireTime = config.getStringPropertyValue(PROP_RETIRE_TIME);
        bedTime = config.getStringPropertyValue(PROP_BED_TIME);
        arrivalDevices = (List<DeviceContext>)config.getPropertyValue(PROP_ARRIVAL_DEVICES);
        retireDevices = (List<DeviceContext>)config.getPropertyValue(PROP_RETIRE_DEVICES);
        schedule = null;
    }
}
