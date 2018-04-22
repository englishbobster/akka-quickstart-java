package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class DeviceGroupQueryTest {

    @Test
    public void testReturnTemperatureValueForWorkingDevices() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit requester = new TestKit(testSystem);

        TestKit device1 = new TestKit(testSystem);
        TestKit device2 = new TestKit(testSystem);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.testActor(), "device1");
        actorToDeviceId.put(device2.testActor(), "device2");

        ActorRef queryActor = testSystem.actorOf(DeviceGroupQuery.props(actorToDeviceId,
                0L, requester.testActor(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(0L, device1.expectMsgClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMsgClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.testActor());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.testActor());

        DeviceGroup.RespondAllTemperatures respondAllTemperatures = requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertEquals(0L, respondAllTemperatures.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));

        for (Map.Entry<String, DeviceGroup.TemperatureReading> expectedEntry : expectedTemperatures.entrySet()) {
            String key = expectedEntry.getKey();
            assertThat((respondAllTemperatures.temperatures.get(key), is(equalTo(expectedEntry.getValue())));
        }
    }
}