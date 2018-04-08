package com.lightbend.akka.sample.hierarchy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

public class ActorHierarchyExperiments {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef firstRef = system.actorOf(Props.create(PrintMyActorRefActor.class), "first-actor");
        System.out.println("First:" + firstRef);
        firstRef.tell("printit", ActorRef.noSender());

        System.out.println(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        }catch (IOException ioe) { }
        finally {
            system.terminate();
        }
    }
}
