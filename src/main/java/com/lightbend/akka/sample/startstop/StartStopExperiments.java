package com.lightbend.akka.sample.startstop;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

public class StartStopExperiments {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("system");
        ActorRef first = system.actorOf(Props.create(StartStopActor1.class), "first");
        first.tell("stop", ActorRef.noSender());

        System.out.println(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        }catch (IOException ioe) { }
        finally {
            system.terminate();
        }
    }
}
