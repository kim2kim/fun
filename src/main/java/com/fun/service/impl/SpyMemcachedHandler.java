package com.fun.service.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import org.springframework.stereotype.Component;

import com.fun.service.ISpyMemcachedHandler;

@Component
public class SpyMemcachedHandler implements ISpyMemcachedHandler {

	private MemcachedClient client;
	
	@PostConstruct
	public void init(){
		try {
			client = new MemcachedClient(new InetSocketAddress("localhost", 11211));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Object getObject(String key){
		// Try to get a value, for up to 5 seconds, and cancel if it doesn't return
		Future<Object> f = client.asyncGet(key);
		try {
			Object myObj= f.get(5, TimeUnit.SECONDS);
		    return myObj;
		} catch(TimeoutException e) {
		    // Since we don't need this, go ahead and cancel the operation.  This
		    // is not strictly necessary, but it'll save some work on the server.
		    f.cancel(false);
		    // Do other timeout related stuff
		} catch (InterruptedException e) {
			f.cancel(false);
			e.printStackTrace();
		} catch (ExecutionException e) {
			f.cancel(false);
			e.printStackTrace();
		}
		return null;
	}
	
	public void cacheObject(String key, Object obj){
		client.set(key, 3600, obj);
	}
	
	public void deleteCache(String key){
		client.delete(key);
	}
}
