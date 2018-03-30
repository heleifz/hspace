package com.helei.hspace.util;

public class Pair<L, R> {

    private L first;
    private R second;

    public Pair(L first, R second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(L val) {
        this.first = val;
    }

    public void setSecond(R val) {
        this.second = val; 
    }

    public L getFirst() {
        return this.first; 
    }

    public R getSecond() {
        return this.second; 
    }

}