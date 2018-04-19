package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceManagerTest {
    @Test
    public void testRegisterDeviceActor() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef groupActor = testSystem.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = testKit.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = testKit.lastSender();
        assertNotEquals(deviceActor1, deviceActor2);

        deviceActor1.tell(new Device.RecordTemperature(0L, 1.0), testKit.testActor());
        assertEquals(0L, testKit.expectMsgClass(Device.TemperatureRecorded.class).requestId);
        deviceActor2.tell(new Device.RecordTemperature(1L, 2.0), testKit.testActor());
        assertEquals(1L, testKit.expectMsgClass(Device.TemperatureRecorded.class).requestId);
    }

    @Test
    public void testIgnoreRequestsForWrongGroupId() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef groupActor = testSystem.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device1"), testKit.testActor());
        testKit.expectNoMsg();
    }

    @Test
    public void testReturnSameActorForSameDeviceI() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef groupActor = testSystem.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = testKit.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2= testKit.lastSender();
        assertEquals(deviceActor1, deviceActor2);
    }
}