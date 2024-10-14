package com.researchspace.chemistry_investigation.chemtools;

import static com.researchspace.chemistry_investigation.Application.OUTPUT_DIR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenBabel {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenBabel.class);

  File nonIndexedChemicals = new File(OUTPUT_DIR + "non-indexed-new-chemicals.smi");

  File indexedChemicals = new File(OUTPUT_DIR + "indexed-chemicals.smi");

  File index = new File(OUTPUT_DIR + "index.fs");

  ExecutorService executorService = Executors.newSingleThreadExecutor();

  public void executeCommand(){
    try {
      saveChemicalToFile("c1ccccc1", "001");
      saveChemicalToFile("CC", "002");
      saveChemicalToFile("C=C", "003");
      searchNonIndexedFile("C=C");
      combineChemicalFiles();
      createIndexFile();
      searchIndexedFile("C=C");
    } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  /***
   * Add the chemical string to the non-indexed chemicals file
   * @param chemical smiles/smarts format chemical
   * @param chemicalId id of RSChemElement entity from rspace-web database
   */
  public void saveChemicalToFile(String chemical, String chemicalId) throws IOException {
    FileWriter fileWriter = new FileWriter(nonIndexedChemicals, true);
    try(PrintWriter printWriter = new PrintWriter(fileWriter);){
      printWriter.println(chemical + "    " + chemicalId);
      printWriter.flush();
      LOGGER.info("Wrote chemical {} to file.", chemical);
    } catch (Exception e){
      LOGGER.error("Error while saving chemical {}", chemical, e);
    }
  }

  /***
   * Search the non-indexed file for chemical matches
   * @param searchTerm smiles/smarts format chemical
   */
  public void searchNonIndexedFile(String searchTerm)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String outputFormat = "smi";
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("obabel", nonIndexedChemicals.getPath(), "-s"+searchTerm, "-o" +outputFormat);
    LOGGER.info("Searching without index for {} in file: {}", searchTerm, nonIndexedChemicals.getPath());
    LOGGER.info("FOUND:");
    executeCommand(builder);
  }

  /***
   * Find partial match of the given search terms
   * @param searchTerm smiles/smarts format chemical
   */
  public void searchIndexedFile(String searchTerm)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String outputFormat = "smi";
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("obabel", index.getPath(), "-s"+searchTerm, "-o" +outputFormat);
    LOGGER.info("Searching with index for {} in file: {}", searchTerm, indexedChemicals.getPath());
    LOGGER.info("FOUND:");
    executeCommand(builder);
  }

  private void executeCommand(ProcessBuilder builder)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    builder.directory(null); // uses current working directory
    Process process = builder.start();
    StreamGobbler streamGobbler =
        new StreamGobbler(process.getInputStream(), System.out::println);
    Future<?> future = executorService.submit(streamGobbler);
    process.waitFor();
    future.get(10, TimeUnit.SECONDS);
  }

  private void combineChemicalFiles() throws IOException {
    File out = new File(indexedChemicals.getPath());
    FileWriter fileWriter = new FileWriter(out, true);
    try(PrintWriter printWriter = new PrintWriter(fileWriter);){
      try(BufferedReader reader = new BufferedReader(new FileReader(nonIndexedChemicals))){
        String line;
        while((line = reader.readLine()) != null) {
          printWriter.println(line);
        }
      }
      // clear contents of non-indexed file, since they've been moved
      new FileWriter(nonIndexedChemicals).close();
      LOGGER.info("Combined chemical files");
    } catch (Exception e){
      LOGGER.error("Error while combining chemical files", e);
    }
  }

  private void createIndexFile()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("obabel", indexedChemicals.getPath(), "-O"+index.getPath());
    executeCommand(builder);
  }

  private record StreamGobbler(InputStream inputStream, Consumer<String> consumer) implements
      Runnable {

    @Override
      public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines()
            .forEach(consumer);
      }
    }
}
