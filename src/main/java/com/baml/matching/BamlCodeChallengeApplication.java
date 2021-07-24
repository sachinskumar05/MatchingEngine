package com.baml.matching;

import com.baml.matching.client.ClientBuyer;
import com.baml.matching.client.ClientSeller;
import com.baml.matching.config.AppCfg;
import com.baml.matching.engine.EquityMatchingEngine;
import com.baml.matching.exception.SymbolNotSupportedException;
import com.baml.matching.exchange.crossing.CrossingProcessor;
import com.baml.matching.symbols.EquitySymbol;
import com.baml.matching.symbols.EquitySymbolCache;
import com.baml.matching.types.OrderType;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SpringBootApplication
public class BamlCodeChallengeApplication {

	@Autowired
	private AppCfg appCfg;


	public static void main(String[] args) {
		log.info("Application starting");
		SpringApplication.run(BamlCodeChallengeApplication.class, args);

		EquityMatchingEngine equityMatchingEngine = EquityMatchingEngine.getInstance();

		ClientSeller clientSeller = new ClientSeller();
		ClientBuyer clientBuyer = new ClientBuyer();


		ExecutorService executorService = Executors.newFixedThreadPool(2);

		executorService.submit(()-> clientSeller.createAndSubmitOrder(1, 20000.0d, 10, OrderType.LIMIT));

		executorService.submit(()-> clientBuyer.createAndSubmitOrder(1, 20000.0d, 10, OrderType.LIMIT));

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			log.error("Interrupted while sleep ", e);
			Thread.currentThread().interrupt();
		}

		String symbolStr = "BAC";
		try {

			final EquitySymbol symbolBAC = EquitySymbolCache.get(symbolStr);
			log.info( "Order {}" ,  ()-> equityMatchingEngine.getOrderBook(symbolBAC));
			log.info( "Order History {} " , ()-> equityMatchingEngine.getOrderBook(symbolBAC).getOrderHistory());

		} catch (SymbolNotSupportedException e) {
			log.error("Failed to create order for {}", symbolStr, e );
		}

		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

	}


}
