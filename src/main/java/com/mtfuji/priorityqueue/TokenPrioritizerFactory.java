package com.mtfuji.priorityqueue;

public class TokenPrioritizerFactory
{
    private static TokenPrioritizer singletonInstance = new TokenPrioritizerImpl();

    static TokenPrioritizer getInstance()
    {
        return singletonInstance;
    }
}
