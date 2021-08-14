package com.sk.matching;

import com.sk.matching.client.ClientWorker;
import com.sk.matching.engine.BasicMatchingEngine;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.EquitySymbolCache;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import com.sk.matching.util.METhreadPoolUtils;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SpringBootApplication
public class MatchingEngineApplication {


	public static void main(String[] args) {
		log.info("Application starting");
		SpringApplication.run(MatchingEngineApplication.class, args);

		BasicMatchingEngine basicMatchingEngine = BasicMatchingEngine.getInstance();

		ClientWorker clientA = new ClientWorker();
		ClientWorker clientB = new ClientWorker();


		ExecutorService executorService = Executors.newSingleThreadExecutor();

		final String BAC = "BAC";
		log.info("Trading simulation will start on {}", BAC);
		executorService.submit(()-> clientA.createAndSubmitOrder(BAC, Side.SELL, 20.30, 100, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientA.createAndSubmitOrder(BAC, Side.SELL, 20.25, 100, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientA.createAndSubmitOrder(BAC, Side.SELL, 20.30, 200, OrderType.LIMIT));

		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.15, 100, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.20, 200, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.15, 200, OrderType.LIMIT));

		Symbol symbolBAC = null;

		try {
			symbolBAC = EquitySymbolCache.get(BAC);
		} catch (SymbolNotSupportedException e) {
			log.error("Failed to create order for {}", BAC, e );
		}

		METhreadPoolUtils.pause(1000);
		log.info( "Order {}" ,  basicMatchingEngine.getOrderBook(symbolBAC));

		executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.25, 100, OrderType.LIMIT));
		executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.30, 100, OrderType.LIMIT));
		executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.30, 50, OrderType.LIMIT));

		METhreadPoolUtils.pause(1000);
		log.info( "Order {}" ,  basicMatchingEngine.getOrderBook(symbolBAC));

		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

	}

}
