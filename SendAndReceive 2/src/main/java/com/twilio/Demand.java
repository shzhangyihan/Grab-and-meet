package com.twilio;

import java.util.*;

public class Demand {
    
    public String dest;
    public String number;
    public int items;
    public LinkedList<Supply> supListInDem;
    
    public Demand(String dest, String number, int items, LinkedList<Supply> ll) {
        this.dest = dest;
        this.number = number;
        this.items = items;
        this.supListInDem = ll;
    }
}
