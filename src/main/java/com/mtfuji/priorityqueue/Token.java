package com.mtfuji.priorityqueue;

/**
 * A class representing a prioritized token
 */
public class Token
{
    private String tokenID;
    private int priority;

    public Token(String tokenID, int priority)
    {
        this.tokenID = tokenID;
        this.priority = priority;
    }

    /**
     *
     * Get the unique identifier for this token (guaranteed to be unique across all tokens).
     * @return the token ID
     */
    public String getTokenID()
    {
        return tokenID;
    }

    /**
     * Get the priority. A lower integer number indicates a higher priority.
     * @return the priority
     */
    public int getPriority()
    {
        return priority;
    }
}
