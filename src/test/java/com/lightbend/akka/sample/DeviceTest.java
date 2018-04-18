package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class DeviceTest {

    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef deviceActor = testSystem.actorOf(Device.props("group", "device"));
        deviceActor.tell(new Device.ReadTemperature(42L), testKit.testActor());
        Device.RespondTemperature response = testKit.expectMsgClass(Device.RespondTemperature.class);
        assertEquals(42L, response.requestId);
        assertEquals(Optional.empty(), response.value);
    }

    @Test
    public void testReplyWithLatestTemperatureReading() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef deviceActor = testSystem.actorOf(Device.props("group", "device"));

        deviceActor.tell(new Device.RecordTemperature(1L, 30.0), testKit.testActor());
        assertEquals(1L, testKit.expectMsgClass(Device.TemperatureRecorded.class).requestId);

        deviceActor.tell(new Device.ReadTemperature(2L), testKit.testActor());
        Device.RespondTemperature response1 = testKit.expectMsgClass(Device.RespondTemperature.class);
        assertEquals(2L, response1.requestId);
        assertEquals(Optional.of(30.0), response1.value);

        deviceActor.tell(new Device.RecordTemperature(3L, 100.0), testKit.testActor());
        assertEquals(3L, testKit.expectMsgClass(Device.TemperatureRecorded.class).requestId);

        deviceActor.tell(new Device.ReadTemperature(4L), testKit.testActor());
        Device.RespondTemperature response2 = testKit.expectMsgClass(Device.RespondTemperature.class);
        assertEquals(4L, response2.requestId);
        assertEquals(Optional.of(100.0), response2.value);
    }

    @Test
    public void testReplyToRegistrationRequest() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef device = testSystem.actorOf(Device.props("group", "device"));

        device.tell(new Device.RequestTrackingDevice("group", "device"), testKit.testActor());
        testKit.expectMsgClass(Device.DeviceRegistered.class);
        assertEquals(device, testKit.lastSender());
    }

    @Test
    public void testIgnoreWrongRegistrationRequest() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef device = testSystem.actorOf(Device.props("group", "device"));

        device.tell(new Device.RequestTrackingDevice("wrongGroup", "device"), testKit.testActor());
        testKit.expectNoMsg();

        device.tell(new Device.RequestTrackingDevice("group", "wrongDevice"), testKit.testActor());
        testKit.expectNoMsg();
    }
}