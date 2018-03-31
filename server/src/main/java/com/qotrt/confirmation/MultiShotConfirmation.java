package com.qotrt.confirmation;

import com.qotrt.gameplayer.Player;

public class MultiShotConfirmation extends Confirmation {

	public MultiShotConfirmation(String eventName, String acceptEventName, String declineEventName, boolean racing) {
		super(eventName, acceptEventName, declineEventName, racing);
	}

	@Override
	public boolean accept(Player player, String attempt, String success, String failure) {
		logger.info(attempt);
		if(backingInt > 0) {
			backingInt--;
			logger.info(success);
			accepted.add(player);
			if(acceptEventName != null) {
				fireEvent(acceptEventName, null, player);
			}
			checkIfCanOpenLatch(cdl, backingInt);
			racer.nextPlayerToAsk();
			return true;
		} else {
			logger.info(failure);
			return false;
		}
	}
}
