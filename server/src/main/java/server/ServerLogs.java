package server;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class ServerLogs {
    public static final Logger logger = Logger.getLogger("");
    static {
        logger.removeHandler(logger.getHandlers()[0]);
        logger.setUseParentHandlers(false);
    }
    public static final Handler consoleHandler = new ConsoleHandler();
    public static final Formatter formatter = new Formatter() {
        Date date = new Date();
        @Override
        public String format(LogRecord record) {
            date.setTime(record.getMillis());
            return ">" + date + '\n' + record.getLevel() + ": " + record.getMessage() + '\n';
        }
    };
    static  {
        consoleHandler.setFormatter(formatter);
        logger.addHandler(consoleHandler);

    }
    public static Handler fileHandler;

    static {
        try {
            fileHandler = new FileHandler("server/src/main/resources/logs/log_%g.txt",100*1024,5,true);
        } catch (IOException e) {
            logger.log(Level.WARNING,e.toString());
            e.printStackTrace();
        }
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    }
}
