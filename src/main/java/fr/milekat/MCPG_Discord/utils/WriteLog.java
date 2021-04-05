package fr.milekat.MCPG_Discord.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class WriteLog {

    public WriteLog() throws IOException {
        File file = new File("logs.txt");
        if (!file.createNewFile()) {
            Files.move(Paths.get("logs.txt"), Paths.get("logs/logs_" + DateMilekat.setDateSysNow() + ".txt"));
            file.createNewFile();
        }
    }

    public void logger(String log) {
        try {
            Files.write(Paths.get("logs.txt"), (log + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
