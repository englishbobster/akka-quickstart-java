package com.lightbend.akka.sample.startstop;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class StartStopActor1 extends AbstractActor {

    @Override
    public void preStart() {
        System.out.println("first Started");
        getContext().actorOf(Props.create(StartStopActor2.class), "second");
    }

    @Override
    public void postStop() {
        System.out.println("first stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("stop", s -> {
            getContext().stop(getSelf());
        }).build();
    }
}
