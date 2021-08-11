package com.sk.matching;

import com.sk.matching.client.ClientWorker;
import com.sk.matching.engine.EquityMatchingEngine;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.symbols.EquitySymbol;
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
public class BamlCodeChallengeApplication {


	public static void main(String[] args) {
		log.info("Application starting");
		SpringApplication.run(BamlCodeChallengeApplication.class, args);

		EquityMatchingEngine equityMatchingEngine = EquityMatchingEngine.getInstance();

		ClientWorker clientA = new ClientWorker();
		ClientWorker clientB = new ClientWorker();


		ExecutorService executorService = Executors.newSingleThreadExecutor();

		String symbol = "BAC";

		executorService.submit(()-> clientA.createAndSubmitOrder(symbol, Side.SELL, 20.30, 100, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientA.createAndSubmitOrder(symbol, Side.SELL, 20.25, 100, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientA.createAndSubmitOrder(symbol, Side.SELL, 20.30, 200, OrderType.LIMIT));

		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.15, 100, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.20, 200, OrderType.LIMIT));
		METhreadPoolUtils.pause(1000);
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.15, 200, OrderType.LIMIT));

		EquitySymbol symbolBAC = null;

		try {
			symbolBAC = EquitySymbolCache.get(symbol);
		} catch (SymbolNotSupportedException e) {
			log.error("Failed to create order for {}", symbol, e );
		}

		METhreadPoolUtils.pause(1000);
		log.info( "Order {}" ,  equityMatchingEngine.getOrderBook(symbolBAC));

		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.25, 100, OrderType.LIMIT));
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.30, 100, OrderType.LIMIT));
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.30, 50, OrderType.LIMIT));

		METhreadPoolUtils.pause(1000);
		log.info( "Order {}" ,  equityMatchingEngine.getOrderBook(symbolBAC));

		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

	}

}
