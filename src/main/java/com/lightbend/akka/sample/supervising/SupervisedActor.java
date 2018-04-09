package com.lightbend.akka.sample.supervising;

import akka.actor.AbstractActor;

public class SupervisedActor extends AbstractActor {

    @Override
    public void preStart() {
        System.out.println("supervised actor started");
    }

    @Override
    public void postStop() {
        System.out.println("supervised actor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("fail", f -> {
            System.out.println("supervised actor fails now");
            throw new Exception("I failed!");
        }).build();
    }
}
