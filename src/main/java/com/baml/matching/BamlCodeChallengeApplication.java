package com.baml.matching;

import com.baml.matching.client.ClientWorker;
import com.baml.matching.config.AppCfg;
import com.baml.matching.engine.EquityMatchingEngine;
import com.baml.matching.exception.SymbolNotSupportedException;
import com.baml.matching.symbols.EquitySymbol;
import com.baml.matching.symbols.EquitySymbolCache;
import com.baml.matching.types.OrderType;
import com.baml.matching.types.Side;
import com.baml.matching.util.MEDateUtils;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
@SpringBootApplication
public class BamlCodeChallengeApplication {

	@Autowired
	private AppCfg appCfg;


	public static void main(String[] args) {
		log.info("Application starting");
		SpringApplication.run(BamlCodeChallengeApplication.class, args);

		EquityMatchingEngine equityMatchingEngine = EquityMatchingEngine.getInstance();

		ClientWorker clientA = new ClientWorker();
		ClientWorker clientB = new ClientWorker();


		ExecutorService executorService = Executors.newFixedThreadPool(1);

		String symbol = "BAC";

		executorService.submit(()-> clientA.createAndSubmitOrder(symbol, Side.SELL, 20.30, 200, OrderType.LIMIT, 1));
		executorService.submit(()-> clientA.createAndSubmitOrder(symbol, Side.SELL, 20.25, 100, OrderType.LIMIT, 1));
		executorService.submit(()-> clientA.createAndSubmitOrder(symbol, Side.SELL, 20.30, 100, OrderType.LIMIT, 1));

		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.20, 200, OrderType.LIMIT, 1));
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.15, 100, OrderType.LIMIT, 1));
		executorService.submit(()-> clientB.createAndSubmitOrder(symbol, Side.BUY, 20.15, 200, OrderType.LIMIT, 1));

		try {

			for (int i = 0; i < 20; i++) {
				MEDateUtils.pause(5000);
				final EquitySymbol symbolBAC = EquitySymbolCache.get(symbol);
				log.info( "Order {}" ,  ()-> equityMatchingEngine.getOrderBook(symbolBAC));
				log.info("-----------------------------------------------------------------------");
				log.info( "Order History {} " , ()-> equityMatchingEngine.getOrderBook(symbolBAC).getOrderHistory());
			}

		} catch (SymbolNotSupportedException e) {
			log.error("Failed to create order for {}", symbol, e );
		}

		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

	}

}
