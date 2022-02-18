package com.sk.matching;

import com.sk.matching.client.ClientWorker;
import com.sk.matching.engine.BasicMatchingEngine;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.SymbolCache;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import com.sk.matching.util.ThreadUtils;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SpringBootApplication
public class MatchingEngineApplication {

	private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public static void main(String[] args) {
		log.info("Application starting");
		SpringApplication.run(MatchingEngineApplication.class, args);

		String testInputFile = "./input-test-data/test2.txt";
		List<String[]> lineListArr  = new ArrayList<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(testInputFile));
			log.info("Reading Trade Input file {}", testInputFile);
			for(String line: lines) {
				log.info("Loaded Trade Line {} " , line);
				lineListArr.add(line.split(","));
			}
		} catch (IOException e) {
			log.error("Failed Test Case " , e);
			return;
		}

		BasicMatchingEngine basicMatchingEngine = BasicMatchingEngine.getInstance();
		ClientWorker clientA = new ClientWorker();

		final String BAC = "BAC";
		log.info("Trading simulation will start on {} using input file {}", BAC, testInputFile);
		for (String[] attributes : lineListArr ) {
			log.info("Creating order using file input {}", Arrays.toString(attributes));
			String clOrdId = attributes[0];
			Side side = Side.valueOf(attributes[1].charAt(0));
			double px = Double.parseDouble(attributes[2]);
			double qty = Double.parseDouble(attributes[3]);
			double visibleQty = Double.NaN;
			if( attributes.length > 4 ) {
				visibleQty = Double.parseDouble(attributes[3]);
			}
			double finalVisibleQty = visibleQty;
			executorService.submit(()-> clientA.createAndSubmitOrder(BAC,
					side,
					px,
					qty,
					OrderType.LIMIT,
					clOrdId,
					finalVisibleQty));
		}

		Symbol symbol = null;

		try {
			symbol = SymbolCache.get(BAC);
		} catch (SymbolNotSupportedException e) {
			log.error("Failed to create order for {}", BAC, e );
		}


		log.info( "Order {}" ,  basicMatchingEngine.getOrderBook(symbol));

		Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

	}

}
