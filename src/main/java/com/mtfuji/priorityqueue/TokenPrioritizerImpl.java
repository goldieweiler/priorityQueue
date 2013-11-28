package com.mtfuji.priorityqueue;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TokenPrioritizerImpl implements TokenPrioritizer
{
    private BlockingQueue<TokenWrapper> priorityQueue;
    private final AtomicLong counter;

    private static final int QUEUE_SIZE = 100;
    private static final long POLL_TIMEOUT = 500;

    // Private class to wrap the token along with an insertion index for ensuring order of extraction
    private class TokenWrapper {
        private final long index;
        private final Token token;

        public TokenWrapper(long index, Token token) {
            this.index = index;
            this.token = token;
        }
    }

    /**
     * Limited scope constructor as this class is constructed using the TokenPrioritizerFactory
     */
    protected TokenPrioritizerImpl() {
        // Initialize a counter - this will help ensure that items of the same priority are returned in order of
        // insertion.
        counter = new AtomicLong(0);

        // Define a comparator for the queue that first compares on priority and then on index where priority matches.
        Comparator<TokenWrapper> comparator = new Comparator<TokenWrapper>() {
            @Override
            public int compare(TokenWrapper o1, TokenWrapper o2) {
                int p1 = o1.token.getPriority();
                int p2 = o2.token.getPriority();
                long i1 = o1.index;
                long i2 = o2.index;

                // First compare by priority, then compare by index
                return p1 > p2 ? 1 : (p1 < p2 ? -1 :
                        (i1 > i2 ? 1 : (i1 < i2 ? -1 : 0)));
            }
        };
        priorityQueue = new PriorityBlockingQueue<TokenWrapper>(QUEUE_SIZE, comparator);
    }

	public Token nextToken() {
        try {
            TokenWrapper wrapper = priorityQueue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS);
            if (wrapper != null) {
                return wrapper.token;
            }
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
        return null;
	}

	public void addToken( Token theToken )
	{
        // Offer the token to the queue. There is no need to call the method with timeouts as this method
        // never blocks and therefore timeouts are ignored.
        priorityQueue.offer(new TokenWrapper(counter.incrementAndGet(), theToken));
	}

    /**
     * Helper method for the unit tests to allow the queue to be cleared between test runs
     */
    protected void clear() {
        priorityQueue.clear();
    }
}
