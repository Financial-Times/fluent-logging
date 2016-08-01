package com.ft.membership.logging;

class TimeToComplete {

    private long initialTime;

    TimeToComplete(final long initialTime){
        this.initialTime = initialTime;
    }
    @Override
    public String toString() {
        return "" + (System.currentTimeMillis() - initialTime);
    }
}
