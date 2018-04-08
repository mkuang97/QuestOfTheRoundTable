package com.qotrt.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.qotrt.PlayerTestCreator;
import com.qotrt.QotrtApplication;
import com.qotrt.messages.game.AIPlayer;
import com.qotrt.messages.game.GameCreateClient;
import com.qotrt.messages.game.PlayCardClient;
import com.qotrt.messages.game.PlayCardClient.ZONE;
import com.qotrt.messages.game.PlayCardServer;
import com.qotrt.messages.quest.FinishPickingStagesClient;
import com.qotrt.messages.quest.QuestBidClient;
import com.qotrt.messages.quest.QuestBidServer;
import com.qotrt.messages.quest.QuestDiscardCardsServer;
import com.qotrt.messages.quest.QuestJoinClient;
import com.qotrt.messages.quest.QuestJoinServer;
import com.qotrt.messages.quest.QuestPickCardsServer;
import com.qotrt.messages.quest.QuestPickStagesServer;
import com.qotrt.messages.quest.QuestSponsorClient;
import com.qotrt.messages.quest.QuestSponsorServer;
import com.qotrt.messages.quest.QuestUpServer;
import com.qotrt.messages.quest.QuestWinServer;
import com.qotrt.messages.quest.QuestWinServer.WINTYPES;
import com.qotrt.model.RiggedModel.RIGGED;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QotrtApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// used to restart spring application after every test
// might be able to remove later
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TestTimeout {

	WebSocketStompClient stompClient;
	final static Logger logger = LogManager.getLogger(TestQuest.class);
	@Value("${local.server.port}")
	private int port;
	static String WEBSOCKET_URI;

	@Before
	public void setup() {
		WEBSOCKET_URI = "ws://localhost:" + port + "/ws";
	}
	
	
	@Test
	public void testPlacingCardsTimeout() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

		Runnable thread2 = new Runnable() {
			@Override
			public void run() {
					PlayerTestCreator p = new PlayerTestCreator();
					p.connect(WEBSOCKET_URI);
					p.sendMessage("/app/game.createGame", 
							new GameCreateClient(2, "hello", RIGGED.ONESTAGETOURNAMENT, new AIPlayer[] {}, false));

					p.waitForThenSend(QuestJoinServer.class, 0, 
							"/app/game.joinQuest", new QuestJoinClient(0, true));
					
					p.waitForThenSend(QuestPickCardsServer.class, 0, 
							"/app/game.playForQuest", new PlayCardClient(0, 159, ZONE.HAND, ZONE.FACEDOWN));
					
					PlayCardServer pcs = p.take(PlayCardServer.class);
					assertEquals("", pcs.response);
					assertEquals(ZONE.FACEDOWN, pcs.zoneTo);
					
					p.sendMessage("/app/game.playForQuest", new PlayCardClient(0, 160, ZONE.HAND, ZONE.FACEDOWN));;
					pcs = p.take(PlayCardServer.class);
					assertNotEquals("", pcs.response);
					assertEquals(ZONE.HAND, pcs.zoneTo);
			}
		};
		
		new Thread(thread2).start();
		Thread.sleep(1000);
		PlayerTestCreator p = new PlayerTestCreator();
		p.connect(WEBSOCKET_URI);
		p.joinGame();
		p.waitForThenSend(QuestSponsorServer.class, 1,
				"/app/game.sponsorQuest", new QuestSponsorClient(1, true));
		p.waitForThenSend(QuestPickStagesServer.class, 1, "/app/game.playCardQuestSetup",
				new PlayCardClient(1, 153, ZONE.HAND, ZONE.STAGE1));
		PlayCardServer pcs = p.take(PlayCardServer.class);
		assertEquals("", pcs.response);
		assertEquals(ZONE.STAGE1, pcs.zoneTo);
		p.sendMessage("/app/game.playCardQuestSetup", new PlayCardClient(1, 155, ZONE.HAND, ZONE.STAGE1));
		pcs = p.take(PlayCardServer.class);
		assertNotEquals("", pcs.response);
		assertEquals(ZONE.HAND, pcs.zoneTo);
		
		p.sendMessage("/app/game.finishSelectingQuestStages", new FinishPickingStagesClient(1));
		
		Thread.sleep(60000);
		QuestWinServer qws = p.take(QuestWinServer.class);
		assertEquals(WINTYPES.PASSSTAGE, qws.type);
		assertEquals(1, qws.players.length);
		assertEquals(0, qws.players[0]);
		
		qws = p.take(QuestWinServer.class);
		assertEquals(WINTYPES.WON, qws.type);
		assertEquals(1, qws.players.length);
		assertEquals(0, qws.players[0]);
	}
	
	@Test
	public void testSponsorTimeout() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

		Runnable thread2 = new Runnable() {
			@Override
			public void run() {
					PlayerTestCreator p = new PlayerTestCreator();
					p.connect(WEBSOCKET_URI);
					p.sendMessage("/app/game.createGame", 
							new GameCreateClient(2, "hello", RIGGED.ONESTAGETOURNAMENT, new AIPlayer[] {}, false));
			}
		};
		
		new Thread(thread2).start();
		Thread.sleep(1000);
		PlayerTestCreator p = new PlayerTestCreator();
		p.connect(WEBSOCKET_URI);
		p.joinGame();
		Thread.sleep(60000);
		QuestWinServer qdcs = p.take(QuestWinServer.class);
		assertEquals(0, qdcs.players.length);
	}
	
	@Test
	public void testParticipateTimeout() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

		Runnable thread2 = new Runnable() {
			@Override
			public void run() {
					PlayerTestCreator p = new PlayerTestCreator();
					p.connect(WEBSOCKET_URI);
					p.sendMessage("/app/game.createGame", 
							new GameCreateClient(2, "hello", RIGGED.ONESTAGETOURNAMENT, new AIPlayer[] {}, false));
			}
		};
		
		new Thread(thread2).start();
		Thread.sleep(1000);
		PlayerTestCreator p = new PlayerTestCreator();
		p.connect(WEBSOCKET_URI);
		p.joinGame();
		p.waitForThenSend(QuestSponsorServer.class, 1,
				"/app/game.sponsorQuest", new QuestSponsorClient(1, true));
		p.waitForThenSend(QuestPickStagesServer.class, 1, "/app/game.playCardQuestSetup",
				new PlayCardClient(1, 152, ZONE.HAND, ZONE.STAGE1));
		PlayCardServer pcs = p.take(PlayCardServer.class);
		assertEquals("", pcs.response);
		assertEquals(ZONE.STAGE1, pcs.zoneTo);
		
		p.sendMessage("/app/game.finishSelectingQuestStages", new FinishPickingStagesClient(1));
		Thread.sleep(60000);
		QuestWinServer qdcs = p.take(QuestWinServer.class);
		assertEquals(0, qdcs.players.length);
	}
	
	@Test
	public void testBidTimeout() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

		Runnable thread2 = new Runnable() {
			@Override
			public void run() {
					PlayerTestCreator p = new PlayerTestCreator();
					p.connect(WEBSOCKET_URI);
					p.sendMessage("/app/game.createGame", 
							new GameCreateClient(2, "hello", RIGGED.ONESTAGETOURNAMENT, new AIPlayer[] {}, false));

					p.waitForThenSend(QuestJoinServer.class, 0, 
							"/app/game.joinQuest", new QuestJoinClient(0, true));
					
					QuestUpServer qus = p.take(QuestUpServer.class);
					assertEquals("Test of Valor", qus.cards[0]);
					assertEquals(1, qus.cards.length);
					
					QuestBidServer qbs = p.take(QuestBidServer.class, 0);
					assertEquals(3, qbs.minBidValue);
					
					p.sendMessage("/app/game.bid", new QuestBidClient(0, 3));
					qbs = p.take(QuestBidServer.class, 0);
					assertEquals(4, qbs.minBidValue);
					
					p.sendMessage("/app/game.bid", new QuestBidClient(0, 4));
					qbs = p.take(QuestBidServer.class, 0);
					assertEquals(5, qbs.minBidValue);
			}
		};
		
		new Thread(thread2).start();
		Thread.sleep(1000);
		PlayerTestCreator p = new PlayerTestCreator();
		p.connect(WEBSOCKET_URI);
		p.joinGame();
		p.waitForThenSend(QuestSponsorServer.class, 1,
				"/app/game.sponsorQuest", new QuestSponsorClient(1, true));
		p.waitForThenSend(QuestPickStagesServer.class, 1, "/app/game.playCardQuestSetup",
				new PlayCardClient(1, 152, ZONE.HAND, ZONE.STAGE1));
		PlayCardServer pcs = p.take(PlayCardServer.class);
		assertEquals("", pcs.response);
		assertEquals(ZONE.STAGE1, pcs.zoneTo);
		
		p.sendMessage("/app/game.finishSelectingQuestStages", new FinishPickingStagesClient(1));
		Thread.sleep(60000);
		QuestDiscardCardsServer qdcs = p.take(QuestDiscardCardsServer.class);
		assertEquals(0, qdcs.player);
		assertEquals(4, qdcs.cardsToDiscard);
	}
}
