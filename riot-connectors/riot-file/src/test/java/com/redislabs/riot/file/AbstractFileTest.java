package com.redislabs.riot.file;

import com.redislabs.riot.test.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AbstractFileTest extends BaseTest {

    protected final static int COUNT = 2410;

    private static Path tempDir;

    @BeforeAll
    public static void setupAll() throws IOException {
        tempDir = Files.createTempDirectory(AbstractFileTest.class.getName());
    }

    protected Path tempFile(String filename) throws IOException {
        Path path = tempDir.resolve(filename);
        if (Files.exists(path)) {
            Files.delete(path);
        }
        return path;
    }

    @Override
    protected String process(String command) {
        return super.process(command).replace("/tmp", tempDir.toString());
    }

    @Override
    protected int execute(String[] args) {
        return new RiotFile().execute(args);
    }

    @Override
    protected String applicationName() {
        return "riot-file";
    }


    protected <T> List<T> readAll(AbstractItemCountingItemStreamItemReader<T> reader) throws Exception {
        reader.open(new ExecutionContext());
        List<T> records = new ArrayList<>();
        T record;
        while ((record = reader.read()) != null) {
            records.add(record);
        }
        reader.close();
        return records;
    }


}
