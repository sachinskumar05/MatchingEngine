package com.sk.matching.symbols;


import com.sk.matching.config.AppCfg;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.util.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
@Log4j2
public class SymbolCache {

    @Autowired
    private AppCfg appCfg;

    public SymbolCache(){}
    public SymbolCache(AppCfg appCfg) {
        this.appCfg = appCfg;
    }

    private static final String SYMBOL_FILE_EXT = ".csv";
    private static final Map<String, Symbol> symbolMap = new ConcurrentHashMap<>();

    /**
     * KISS => Keeping it Super Simple [for demo]
     * Only for the demo purpose, in industrial product. Symbol class would be managed by static reference
     */
    @PostConstruct
    public void init() {
        //Read from file / stream / messaging system or flat file and create the EquitySymbol cache
        //for illustration I am hardcoding the array and keeping just one symbol here as asked in task
        // list is for illustration only, Will read it from data source
        Path dataPathDir = Paths.get(appCfg.getDataDir());
        String symbolFilename = appCfg.getSymbolFile();
        String separator = appCfg.getSymbolFileContentSeparator();
        Path symbolFilePath = dataPathDir.resolve(symbolFilename);
        try ( Stream<String> lines = Files.lines(symbolFilePath) ) {
            lines.forEach(ln -> {
                log.info("Symbol details {}", ln);
                String []symbolAttributes = ln.split(separator);
                if ( symbolAttributes.length > 2 ) {
                    String sy = symbolAttributes[1];
                    Path syPxFile = dataPathDir.resolve(String.format("%s%s",sy,SYMBOL_FILE_EXT) );
                    log.info("Loading last price from {}", syPxFile);
                    if ( Files.exists(syPxFile) ) {
                        String lastClosingLine = FileUtils.getLastLineOf(syPxFile);
                        if(null != lastClosingLine && !lastClosingLine.isBlank()) {

                            String[] lastClosingDetails = lastClosingLine.split(separator);
                            Double lastPx = Double.valueOf(lastClosingDetails[4]);

                            symbolMap.computeIfAbsent(sy, s->new Symbol(sy, lastPx));

                        }
                    } else {
                        log.error("There is no historical data file found in data dir {} for symbol {}",
                                dataPathDir, sy);
                    }
                } else {
                    log.warn("Empty or unexpected line format {}", ln);
                }
            });

        } catch (IOException e) {
            log.error("Failed to initialize Equity Symbols from file/s {}", dataPathDir, e );
        }


    }

    public static Symbol get(String symbolStr) throws SymbolNotSupportedException {
        Symbol symbol = null;
        if( isInitialized() ) {
            symbol = symbolMap.get(symbolStr);
            if (null == symbol) {
                throw new SymbolNotSupportedException(String.format("Symbol not supported %s ", symbolStr));
            }
        } else {
            throw new SymbolNotSupportedException(
                    String.format("Equity Symbol Cache is not initialized therefore, can't support symbol %s", symbolStr)
            );
        }
        return symbol;
    }


    public static List<Symbol> getAllSymbols() {
        List<Symbol> result = new ArrayList<>();
        if(isInitialized())
            result.addAll(symbolMap.values());//Returning an copy of list to keep cache not exposed for un expected mutations
        return result;
    }

    private static boolean isInitialized() {
        boolean isEmpty = symbolMap.isEmpty();
        if( isEmpty ) {
            log.error("Symbol cache is not initialized yet {}", symbolMap);
        }
        return !isEmpty;
    }

}
