package com.lightbend.akka.sample.supervising;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class SupervisingActor extends AbstractActor {
    ActorRef child = getContext().actorOf(Props.create(SupervisedActor.class), "supervised-actor");

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("failchild", f -> {
            child.tell("fail", getSelf());
        }).build();
    }
}
