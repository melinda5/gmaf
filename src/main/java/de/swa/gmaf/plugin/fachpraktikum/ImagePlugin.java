package de.swa.gmaf.plugin.fachpraktikum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.mmfg.*;

/** 
 * Class implementing the interface GMAF_Plugin to add the results 
 * of a video object detection to a MMFG
 * 
 * @author Melinda Betz
 */
public class ImagePlugin implements GMAF_Plugin {    
    /** 
     * This method is called, when a new asset for an image object detection has to be processed
     * 
     * @param url represents the temporary URL of the Multimedia asset to be process (not used)
     * @param f represents the result file of the video object detection to be processed
     * @param bytes contains the file's bytes (not used)
     * @param mmfg represents the current MMFG, where the results of this plugin should be fused into
     */
    @Override
    public void process(URL url, File f, byte[] bytes, MMFG mmfg) {
        try {
            String filename;
            String detectedBy;
            BufferedReader reader = new BufferedReader(new FileReader(f));

            String[] line;
            String[] line1 = null;
            String temp;
            int diff, i;
            int min = 1;
            MMFG fv;
            Node label;
            GeneralMetadata generalMetadata = new GeneralMetadata();
            Context threshold;
            Context confidence;
            filename = "";

            String linestr = reader.readLine();            

            if (linestr != null) 
                if (linestr.length() > 0)
                    line1 = linestr.split("\\s+");
            
            if (ApiStart.folderProcessing)
                min = 0;
            else {
            // set filename in general metadata (not for the folder case)
                filename = line1[0];
                generalMetadata.setFileName(filename);
                mmfg.setGeneralMetadata(generalMetadata);
            }

            if (ApiStart.folderProcessing || canProcess(filename.substring(filename.length() - 4))) {
                // create a root node for the object detection
                Node image = new Node("Root-Asset", mmfg);

                // set detectedby
                if (line1.length > min) {
                    detectedBy = line1[min];
                    image.setDetectedBy(detectedBy);
                }

                // set threshold as weight
                if (line1.length > min + 1) {
                    threshold = new Context();
                    threshold.setName("Threshold");
                    image.addWeight(new Weight(threshold, Float.valueOf(line1[min + 1])));
                }

                linestr = reader.readLine();

                while (linestr != null) {
                    if (linestr.length() > 0) {
                        // handle classes or text queries containing blanks
                        temp = "";
                        line = linestr.split("\\s+");
                        diff = line.length - 8;

                        for (i = 2; i <= 2 + diff; i++) {
                            if (i < 2 + diff)
                                temp = temp + line[i] + " ";
                            else
                                temp = temp + line[i];
                        }

                        // create a node for the detection
                        label = new Node(temp, mmfg);

                        // set confidence as weight
                        confidence = new Context();
                        confidence.setName("Confidence");
                        label.addWeight(new Weight(confidence, Float.valueOf(line[7 + diff])));

                        // set bounding box coordinates as technical attribute
                        label.addTechnicalAttribute(new TechnicalAttribute(Math.round(Float.valueOf(line[3 + diff])),
                                Math.round(Float.valueOf(line[4 + diff])), Math.round(Float.valueOf(line[5 + diff])),
                                Math.round(Float.valueOf(line[6 + diff])), 0, 0));

                        // set detectedby
                        if (line1.length > min) label.setDetectedBy(line1[min]);

                        // set filename in general metadata
                        generalMetadata = new GeneralMetadata();
                        generalMetadata.setFileName(line[0]);
                        fv = new MMFG();
                        fv.setGeneralMetadata(generalMetadata);

                        // set classid and class (or text query) of the detection as type and name of a
                        // location
                        fv.addLocation(new Location(Integer.valueOf(line[1]), url, temp));
                        label.setFeatureVector(fv);

                        image.addChildNode(label);
                    }

                    linestr = reader.readLine();
                }

                reader.close();
            } else
                System.exit(11);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(14);
        }
    }

    /**
     * This method returns true, if the plugin is able to process files with a given extension
     * 
     * @param extension of file
     * @return true or false
     */  
    @Override
    public boolean canProcess(String extension) {
        if (extension.equalsIgnoreCase(".jpg")) return true;
        if (extension.equalsIgnoreCase(".jpeg")) return true;
        if (extension.equalsIgnoreCase(".png")) return true;
        if (extension.equalsIgnoreCase(".tiff")) return true;
        if (extension.equalsIgnoreCase(".gif")) return true;        
        return false;
    }
    
    /**
     * The following attributes and methods of the interface GMAF_Plugin are not  used by this class
     */
    private Vector<Node> detected = new Vector<Node>();
    
    @Override
    public Vector<Node> getDetectedNodes() {
        return detected;
    }
    
    @Override
    public boolean isGeneralPlugin() {
        return true;
    }

    @Override
    public boolean providesRecoursiveData() {
        return false;
    }
}
