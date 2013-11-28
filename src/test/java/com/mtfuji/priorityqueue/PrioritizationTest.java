package com.mtfuji.priorityqueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrioritizationTest
{
    static TokenPrioritizer prioritizer = TokenPrioritizerFactory.getInstance();

    @Before
    public void init() {
        // Clear down the queue prior to each test run
        ((TokenPrioritizerImpl)prioritizer).clear();
    }

    @Test
    public void returnFirstHighestPriorityToken() {
        Token highestPriorityToken = processTokenList(generateRandomizedTokenList(), 1, true);

        // Confirm that the first highest priority token is retrieved (note that it may be different each run).
        assertThat(highestPriorityToken.getTokenID(), equalTo(prioritizer.nextToken().getTokenID()));
    }

    @Test
    public void returnLastHighestPriorityToken() {
        Token expectedToken = processTokenList(generateRandomizedTokenList(), 1, false);
        Token selectedToken = null;
        Token token;

        // Iterate the priority queue until the last inserted token is found for the highest priority
        while ((token = prioritizer.nextToken()) != null) {
            // Confirm that we are working through the highest priority tokens
            if (token.getPriority() == 1) {
                selectedToken = token;
            } else {
                break;
            }
        }

        // Confirm that the correct token has been retrieved
        assertThat(expectedToken.getTokenID(), equalTo(selectedToken.getTokenID()));
    }

    @Test
    public void returnsInPriorityOrder() {
        // Add tokens to the queue
        int[] expectedResult = createTestData();

        // Confirm the retrieval of the tokens
        assertQueuePollingOrder(expectedResult);
    }

    @Test
    public void returnsInPriorityOrderMultiThread() {

        final List<Token> threadSafeTokenList = Collections.synchronizedList(new ArrayList<Token>());

        // Add tokens to the queue
        int[] expectedResult = createTestData();

        // Create a thread pool of 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 15; i++) {
            // Create a new thread to pull a token from the priority queue
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    // Log out to the console to show the threads that are executing in the pool
                    System.out.println(Thread.currentThread().getName() + " - Start");
                    Token token = prioritizer.nextToken();
                    threadSafeTokenList.add(token);
                    System.out.println(Thread.currentThread().getName() + " - Retrieved Token: " + token.getTokenID());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                    System.out.println(Thread.currentThread().getName() + " - End");
                }
            };
            executor.execute(worker);
        }

        // Execute an orderly shutdown of the threads and wait until all threads have terminated.
        executor.shutdown();
        while (!executor.isTerminated()) {}

        int i = 0;
        for (Token token : threadSafeTokenList) {
            assertThat("dummy-token-ID-" + expectedResult[i++], equalTo(token.getTokenID()));
        }

        assertThat(prioritizer.nextToken(), nullValue());
    }

    /**
     * Create a small batch of reusable test data
     */
    private int[] createTestData() {

        int[] expectedResult = {2, 4, 7, 15, 8, 11, 3, 6, 13, 12, 1, 10, 5, 9, 14};

        prioritizer.addToken(new Token("dummy-token-ID-1", 5));
        prioritizer.addToken(new Token("dummy-token-ID-2", 1));
        prioritizer.addToken(new Token("dummy-token-ID-3", 3));
        prioritizer.addToken(new Token("dummy-token-ID-4", 1));
        prioritizer.addToken(new Token("dummy-token-ID-5", 6));
        prioritizer.addToken(new Token("dummy-token-ID-6", 3));
        prioritizer.addToken(new Token("dummy-token-ID-7", 1));
        prioritizer.addToken(new Token("dummy-token-ID-8", 2));
        prioritizer.addToken(new Token("dummy-token-ID-9", 6));
        prioritizer.addToken(new Token("dummy-token-ID-10", 5));
        prioritizer.addToken(new Token("dummy-token-ID-11", 2));
        prioritizer.addToken(new Token("dummy-token-ID-12", 4));
        prioritizer.addToken(new Token("dummy-token-ID-13", 3));
        prioritizer.addToken(new Token("dummy-token-ID-14", 6));
        prioritizer.addToken(new Token("dummy-token-ID-15", 1));

        return expectedResult;
    }

    /**
     * Helper method to test assertions
     * @param args array of integer ids for the tokens in expected order of retrieval
     */
    private void assertQueuePollingOrder(int... args) {
        for (int i : args) {
            assertThat("dummy-token-ID-" + i, equalTo(prioritizer.nextToken().getTokenID()));
        }
        assertThat(prioritizer.nextToken(), nullValue());
    }

    /**
     * Generate a list of 50 tokens with groups of like priorities
     * @return list of tokens shuffled into a random order
     */
    private List<Token> generateRandomizedTokenList() {

        // Create an array of 50 tokens - the ID will be incremented from 1 thru 50
        // The priority will be 1 thru 10 for each block of 5 tokens
        List<Token> tokenList = new ArrayList<Token>();
        int id = 1;
        for (int i = 0; i < 10;) {
            for (int j = 0; j < 5; j++) {
                tokenList.add(new Token("dummy-token-ID-" + id++, ++i));
            }
        }

        // Shuffle the collection so that the list is populated differently each run
        Collections.shuffle(tokenList);

        return tokenList;
    }

    /**
     * Process the token list and add the content to the TokenPrioritizer Return a token for
     * comparison from the token list matching the indicated priority and position
     * @param tokenList list of tokens in random order
     * @param priority the priority of the token to return
     * @param first if true, the first token of that priority otherwise the last
     * @return the selected token
     */
    private Token processTokenList(List<Token> tokenList, int priority, boolean first) {
        Token selectedToken = null;

        for (Token token : tokenList) {
            // Keep track of the first token in this run matching the indicated priority
            if (token.getPriority() == priority) {
                if (first && selectedToken == null) {
                    selectedToken = token;
                } else {
                    // Store the current token - eventually, this will be the last token entered for the given priority
                    selectedToken = token;
                }
            }
            prioritizer.addToken(token);
        }
        return selectedToken;
    }

}
