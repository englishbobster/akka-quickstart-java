package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.TestKit;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DeviceGroupTest {
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
        ActorRef deviceActor2 = testKit.lastSender();
        assertEquals(deviceActor1, deviceActor2);
    }

    @Test
    public void testListActiveDevices() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef groupActor = testSystem.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceGroup.RequestDeviceList(0L), testKit.testActor());
        DeviceGroup.ReplyDeviceList replyDeviceList = testKit.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L, replyDeviceList.requestId);
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), replyDeviceList.ids);
    }

    @Test
    public void testListActiveDevicesAfterOneShutsDown() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef deviceGroup = testSystem.actorOf(DeviceGroup.props("group"));

        deviceGroup.tell(new DeviceManager.RequestTrackDevice("group", "device1"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef toShutDown = testKit.lastSender();

        deviceGroup.tell(new DeviceManager.RequestTrackDevice("group", "device2"), testKit.testActor());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        deviceGroup.tell(new DeviceGroup.RequestDeviceList(0L), testKit.testActor());
        DeviceGroup.ReplyDeviceList reply = testKit.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);

        testKit.watch(toShutDown);
        toShutDown.tell(PoisonPill.getInstance(), ActorRef.noSender());
        testKit.expectTerminated(toShutDown, new FiniteDuration(100, TimeUnit.SECONDS));

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        testKit.awaitAssert(() -> {
            deviceGroup.tell(new DeviceGroup.RequestDeviceList(1L), testKit.testActor());
            DeviceGroup.ReplyDeviceList r = testKit.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
            assertEquals(1L, r.requestId);
            assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.ids);
            return null;
        }, new FiniteDuration(10, TimeUnit.SECONDS), new FiniteDuration(10, TimeUnit.SECONDS));
    }
}
