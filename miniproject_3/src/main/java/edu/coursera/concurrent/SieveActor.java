package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.*;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {

        final SieveActorActor actor = new SieveActorActor(2);
        finish(() -> {
            for(int i = 3; i <= limit; i += 2) {
                actor.send(i);
            }
            actor.send(0);
        });

        int numPrimes = 0;
        SieveActorActor loopActor = actor;
        while(loopActor != null) {
            numPrimes += loopActor.localPrimes.size();
            loopActor = loopActor.nextActor;
        }

        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */

        private static final int MAX_LOCAL_PRIMES = 1_000;
        private SieveActorActor nextActor;
        private final List<Integer> localPrimes = new ArrayList<>();

        SieveActorActor(int localPrime) {
            localPrimes.add(localPrime);
        }

        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if (candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(msg);
                }
            }
            else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if (locallyPrime) {
                    if (localPrimes.size() < MAX_LOCAL_PRIMES) {
                        localPrimes.add(candidate);
                    }
                    else if (nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    }
                    else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        public boolean isLocallyPrime(int candidate) {
            for(int i = 0; i < localPrimes.size(); i++) {
                if (candidate % localPrimes.get(i) == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
