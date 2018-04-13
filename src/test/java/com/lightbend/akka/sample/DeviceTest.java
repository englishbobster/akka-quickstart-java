package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeviceTest {

    @Test
    public void testxxx() {
        ActorSystem testSystem = ActorSystem.create("test-system");
        TestKit testKit = new TestKit(testSystem);
        ActorRef actorRef = testSystem.actorOf(Device.props("group", "device"));
        actorRef.tell(new Device.ReadTemperature(1L), ActorRef.noSender());
        assertEquals(testKit.lastMessage().msg(), Device.ReadTemperature.class);
    }
}