package com.fun;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

import junit.framework.TestCase;

public class StockTest extends TestCase {

	public void testDequeue(){
		Deque<BigInteger> hello = new ArrayDeque<BigInteger>();
		
		hello.add(new BigInteger("20141226"));
		hello.add(new BigInteger("20060209"));
		hello.add(new BigInteger("20060208"));
		

		while(!hello.isEmpty()){
			BigInteger i = hello.removeLast();
			System.out.println(i.toString());
		}
	}
	
	
}
