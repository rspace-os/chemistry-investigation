package com.researchspace.chemistry_investigation;

import com.researchspace.chemistry_investigation.chemtools.Indigo;
import com.researchspace.chemistry_investigation.chemtools.OpenBabel;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application implements CommandLineRunner {
	public static final String OUTPUT_DIR = "src/main/resources/chemical_files/output/";
	private final Indigo indigo;
	private final OpenBabel openBabel;

	@Autowired
	public Application(Indigo indigo, OpenBabel openBabel) {
		this.indigo = indigo;
		this.openBabel = openBabel;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws IOException {
		clearOutputDir();
		indigo.runIndigo();
		openBabel.executeCommand();
	}

	private void clearOutputDir() {
		File[] outputFiles = new File(OUTPUT_DIR).listFiles();
		if(outputFiles != null) {
			for(File outputFile : outputFiles) {
				outputFile.delete();
			}
		}
	}

}
