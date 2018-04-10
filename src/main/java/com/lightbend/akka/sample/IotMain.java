package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class IotMain {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("iot-system");
        try {
            ActorRef supervisor = system.actorOf(IotSupervisor.props(), "iot-supervisor");
            System.out.println("Press ENTER to exit system");
            System.in.read();
        } catch (Exception e) {
        } finally {
            system.terminate();
        }
    }
}
