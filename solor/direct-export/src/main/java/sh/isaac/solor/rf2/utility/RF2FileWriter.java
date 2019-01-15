package sh.isaac.solor.rf2.utility;

import org.apache.commons.lang.ArrayUtils;
import sh.isaac.solor.rf2.config.RF2ConfigType;
import sh.isaac.solor.rf2.config.RF2Configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class RF2FileWriter {

    private static Map<RF2ConfigType, Semaphore> semaphoreMap = new HashMap<>();
    static{
        Arrays.stream(RF2ConfigType.values())
                .forEach(rf2ConfigType -> semaphoreMap.put(rf2ConfigType, new Semaphore(1)));
    }

    public void writeToFile(List<Byte[]> writeBytes, RF2Configuration rf2Configuration){

        semaphoreMap.get(rf2Configuration.getRf2ConfigType()).acquireUninterruptibly();

        try {

            if(!Files.exists(rf2Configuration.getFilePath().getParent()))
                Files.createDirectories(rf2Configuration.getFilePath().getParent());

            if(!Files.exists(rf2Configuration.getFilePath())) {
                Files.createFile(rf2Configuration.getFilePath());
                Files.write(rf2Configuration.getFilePath(),
                        rf2Configuration.getFileHeader().getBytes(Charset.forName("UTF-8")),
                        StandardOpenOption.APPEND );
            }

            for(Byte[] bytes : writeBytes){
                Files.write(rf2Configuration.getFilePath(), ArrayUtils.toPrimitive(bytes), StandardOpenOption.APPEND);
            }

        }catch (IOException ioE){
            ioE.printStackTrace();
        }

        semaphoreMap.get(rf2Configuration.getRf2ConfigType()).release();

    }
}
