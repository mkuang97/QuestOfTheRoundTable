package com.qotrt.confirmation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.qotrt.gameplayer.Player;
import com.qotrt.util.PlayerUtil;
import com.qotrt.model.GenericPair;
import com.qotrt.model.Observable;

public abstract class Confirmation extends Observable {
	protected CountDownLatch cdl = new CountDownLatch(1);
	protected String eventName;
	protected String acceptEventName;
	protected String declineEventName;
	protected int backingInt;
	protected List<Player> toAsk;
	protected List<Player> accepted = new ArrayList<Player>();

	public Confirmation(String eventName, String acceptEventName, String declineEventName) {
		this.eventName = eventName;
		this.acceptEventName = acceptEventName;
		this.declineEventName = declineEventName;
	}

	public void start(List<Player> toAsk) {
		cdl = new CountDownLatch(1);
		accepted = new ArrayList<Player>();
		this.toAsk = toAsk;
		backingInt = this.toAsk.size();
		if(eventName != null) {
			fireEvent(eventName, null, PlayerUtil.playersToIDs(toAsk));
		}
	}

	public void start(List<Player> toAsk, Object obj) {
		cdl = new CountDownLatch(1);
		accepted = new ArrayList<Player>();
		this.toAsk = toAsk;
		backingInt = this.toAsk.size();
		if(eventName != null) {
			fireEvent(eventName, null, new GenericPair(PlayerUtil.playersToIDs(toAsk), obj));
		}
	}
	
	public abstract boolean accept(Player player, String attempt, String success, String failure);

	public void decline(Player player, String attempt, String success, String failure) {
		System.out.println(attempt);
		if(backingInt > 0) {
			toAsk.remove(player);
			backingInt--;
			System.out.println(success);
			if(declineEventName != null) {
				fireEvent(declineEventName, null, player);
			}
			checkIfCanOpenLatch(cdl, backingInt);
		} else {
			System.out.println(failure);
		}
	}
	
	public void forAllPlayers(Consumer<Player> c) {
		for(Player player: toAsk) {
			c.accept(player);
		}
	}

	public boolean can() {
		return backingInt > 0;
	}

	public List<Player> get(){
		backingInt = -1;
		return this.accepted;
	}

	public CountDownLatch getCountDownLatch() {
		return this.cdl;
	}
}
