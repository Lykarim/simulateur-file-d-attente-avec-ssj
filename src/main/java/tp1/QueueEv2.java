package tp1;

import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.Tally;

import java.util.LinkedList;



public class QueueEv2 {
    int serveur ;
    RandomVariateGen genArr;
    RandomVariateGen genServ;
    LinkedList<QueueEv.Customer> waitList = new LinkedList<QueueEv.Customer>();
    LinkedList<QueueEv.Customer> servList = new LinkedList<QueueEv.Customer>();
    Tally custWaits = new Tally("Waiting times");
    Accumulate totWait = new Accumulate("Size of queue");

    static class Customer {
        double arrivTime, servTime;
    }

    public QueueEv2(double lambda, double mu, int s) {
        genArr = new ExponentialGen(new MRG32k3a(), lambda);
        genServ = new ExponentialGen(new MRG32k3a(), mu);
        this.serveur=s;
    }
    static class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }
    class Departure extends Event {
        public void actions() {
            servList.removeFirst();
            if (!waitList.isEmpty()) {
                // Starts service for next one in queue.
                QueueEv.Customer cust = waitList.removeFirst();
                totWait.update (waitList.size());
                custWaits.add (Sim.time() - cust.arrivTime);
                servList.addLast (cust);
                new QueueEv2.Departure().schedule (cust.servTime);
            }
        }
    }
    public void simulateOneRun(double timeHorizon) {
        Sim.init();
        new QueueEv2.EndOfSim().schedule(timeHorizon);
        new QueueEv2.Arrival().schedule(genArr.nextDouble());
        Sim.start();
    }

    class Arrival extends Event {
        public void actions() {
            new QueueEv2.Arrival().schedule(genArr.nextDouble()); // Next arrival.
            QueueEv.Customer cust = new QueueEv.Customer();  // Cust just arrived.
            cust.arrivTime = Sim.time();
            cust.servTime = genServ.nextDouble();
            if (servList.size()>serveur) {       // Must join the queue.
                waitList.addLast(cust);
                totWait.update(waitList.size());
            } else {                         // Starts service.
                custWaits.add(0.0);
                servList.addLast(cust);
                new QueueEv2.Departure().schedule(cust.servTime);
            }
        }
    }

    public static void main (String[] args) {
        double mu=2.0;
        double lambda=5.0;
        int serveur = 3 ;
        QueueEv2 queue = new QueueEv2 (1.0, 2.0,serveur);
        queue.simulateOneRun (100000.0);
        System.out.println (queue.custWaits.report());
        System.out.println (queue.totWait.report());

        //Compute the théorical value of average waiting time and average queue length

        double Wq=(lambda)/(mu*(mu-lambda));
        System.out.println ("W="+Wq);
        double Lq=(lambda*lambda)/(mu*(mu-lambda));
        System.out.println ("Lq="+Lq);
    }
}