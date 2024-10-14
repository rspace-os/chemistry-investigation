package com.researchspace.chemistry_investigation.chemtools;

import static com.researchspace.chemistry_investigation.Application.OUTPUT_DIR;

import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class Indigo {
  private static final Logger LOGGER = LoggerFactory.getLogger(Indigo.class);

  @Value("classpath:chemical_files/input/molgen.mol")
  private Resource molFile;

  @Value("classpath:chemical_files/input/suzuki coupling.cdxml")
  private Resource rxnFile;

  public void runIndigo() throws IOException {
    renderImage();
    readMolInformation();
    readReactionInformation();
    convertToCDXML();
  }

  public void renderImage() throws IOException {
    com.epam.indigo.Indigo indigo = new com.epam.indigo.Indigo();
    IndigoRenderer renderer = new IndigoRenderer(indigo);

    IndigoObject mol = indigo.loadMoleculeFromFile(molFile.getFile().getPath());
    indigo.setOption("render-output-format", "png");
    indigo.setOption("render-margins", 10, 10);
    mol.layout();
    String filename = OUTPUT_DIR + "mol-pic.png";
    renderer.renderToFile(mol, filename);
    LOGGER.info("Rendered image: {}", filename);
  }

  public void readMolInformation() throws IOException {
    com.epam.indigo.Indigo indigo = new com.epam.indigo.Indigo();
    IndigoObject mol = indigo.loadMoleculeFromFile(molFile.getFile().getPath());

    LOGGER.info("MOL file {} converts to smiles: {}", molFile.getFile().getPath(), mol.smiles());
  }

  public void readReactionInformation() throws IOException {
    com.epam.indigo.Indigo indigo = new com.epam.indigo.Indigo();
    IndigoObject reaction = indigo.loadReactionFromFile(rxnFile.getFile().getPath());
    LOGGER.info("Getting mol info for rxn file: {}", rxnFile.getFile().getPath());
    for (IndigoObject reactant : reaction.iterateReactants()) {
      LOGGER.info("reactant: {}", reactant.smiles());
    }

    for (IndigoObject product : reaction.iterateProducts()) {
      LOGGER.info("product: {}", product.smiles());
    }
  }

  public void convertToCDXML() throws IOException {
    com.epam.indigo.Indigo indigo = new com.epam.indigo.Indigo();
    IndigoRenderer renderer = new IndigoRenderer(indigo);

    IndigoObject mol = indigo.loadMoleculeFromFile(molFile.getFile().getPath());
    indigo.setOption("render-output-format", "cdxml");
    indigo.setOption("render-margins", 10, 10);
    mol.layout();
    String filename = OUTPUT_DIR + "mol.cdxml";
    renderer.renderToFile(mol, filename);
    LOGGER.info("Created cdxml file: {} from mol file: {}", filename, molFile.getFile().getPath());

  }
}
