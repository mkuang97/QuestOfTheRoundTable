package com.qotrt.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.qotrt.game.Game;
import com.qotrt.gameplayer.Player;
import com.qotrt.hub.Hub;
import com.qotrt.messages.quest.QuestBidClient;
import com.qotrt.messages.quest.QuestDiscardCardsClient;
import com.qotrt.messages.quest.QuestJoinClient;
import com.qotrt.messages.quest.QuestPickCardsClient;
import com.qotrt.messages.quest.QuestPickStagesClient;
import com.qotrt.messages.quest.QuestSponsorClient;


@Controller
public class QuestController {
	
	@Autowired
	private Hub hub;
	
	final static Logger logger = LogManager.getLogger(QuestController.class);

	@MessageMapping("/game.sponsorQuest")
	public void sponsorQuest(SimpMessageHeaderAccessor headerAccessor, 
			@Payload QuestSponsorClient chatMessage) {
		Game game = hub.getGameBySessionID(headerAccessor.getSessionId());
		Player player = game.getPlayerBySessionID(headerAccessor.getSessionId());
		
		logger.info("sponsor quest: " + chatMessage.player);
		if(chatMessage.sponser) {
			game.bmm.getQuestModel().acceptSponsor(player);
		} else {
			game.bmm.getQuestModel().declineSponsor(player);
		}
	}
	
	@MessageMapping("/game.bid")
	public void bid(SimpMessageHeaderAccessor headerAccessor, 
			@Payload QuestBidClient chatMessage) {
		Game game = hub.getGameBySessionID(headerAccessor.getSessionId());
		Player player = game.getPlayerBySessionID(headerAccessor.getSessionId());
		if(chatMessage.bid != -1) {
			game.bmm.getQuestModel().bid(player, chatMessage.bid);
		} else {
			game.bmm.getQuestModel().declineBid(player);
		}
	}
	
	@MessageMapping("/game.finishSelectingQuestCards")
	public void finishSelectingQuestCards(SimpMessageHeaderAccessor headerAccessor, 
			@Payload QuestPickCardsClient chatMessage) {
		Game game = hub.getGameBySessionID(headerAccessor.getSessionId());
		Player player = game.getPlayerBySessionID(headerAccessor.getSessionId());
		logger.info("finish selecting cards: " + chatMessage.player);
		game.bmm.getQuestModel().finishSelectingCards(player);
	}
	
	@MessageMapping("/game.finishSelectingQuestStages")
	public void finishSelectingQuestStages(SimpMessageHeaderAccessor headerAccessor, 
			@Payload QuestPickStagesClient chatMessage) {
		Game game = hub.getGameBySessionID(headerAccessor.getSessionId());
		Player player = game.getPlayerBySessionID(headerAccessor.getSessionId());
		logger.info("finish selecting cards: " + chatMessage.player);
		game.bmm.getQuestModel().finishSelectingStages(player);
	}
	
	@MessageMapping("/game.finishDiscard")
	public void finishDiscard(SimpMessageHeaderAccessor headerAccessor, 
			@Payload QuestDiscardCardsClient chatMessage) {
		Game game = hub.getGameBySessionID(headerAccessor.getSessionId());
		Player player = game.getPlayerBySessionID(headerAccessor.getSessionId());
		logger.info("finish discard cards: " + chatMessage.player);
		game.bmm.getQuestModel().finishDiscard(player);
	}
	
	@MessageMapping("/game.joinQuest")	
	public void finishSelectingTournament(SimpMessageHeaderAccessor headerAccessor, 
			@Payload QuestJoinClient chatMessage) {
		Game game = hub.getGameBySessionID(headerAccessor.getSessionId());
		Player player = game.getPlayerBySessionID(headerAccessor.getSessionId());
		if(chatMessage.joined) {
			game.bmm.getQuestModel().acceptQuest(player);
		} else {
			game.bmm.getQuestModel().declineQuest(player);
		}
	}

}