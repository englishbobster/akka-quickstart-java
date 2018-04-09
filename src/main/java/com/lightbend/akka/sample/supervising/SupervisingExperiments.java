package com.lightbend.akka.sample.supervising;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

public class SupervisingExperiments {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("system");
        ActorRef supervisingActor = system.actorOf(Props.create(SupervisingActor.class), "supervising-actor");
        supervisingActor.tell("failchild", ActorRef.noSender());

        System.out.println(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        }catch (IOException ioe) { }
        finally {
            system.terminate();
        }


    }

}
