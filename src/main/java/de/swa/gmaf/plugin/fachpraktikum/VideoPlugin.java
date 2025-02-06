package de.swa.gmaf.plugin.fachpraktikum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.mmfg.GeneralMetadata;
import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.mmfg.Context;
import de.swa.mmfg.Weight;
import de.swa.mmfg.TechnicalAttribute;
import de.swa.mmfg.Location;

/** 
 * Class implementing the interface GMAF_Plugin to add the results 
 * of a image object detection to a MMFG
 * 
 * @author Melinda Betz
 */
public class VideoPlugin implements GMAF_Plugin {    
    /** 
     * This method is called, when a new asset for a video object detection has to be processed
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
            BufferedReader reader = new BufferedReader(new FileReader(f));
            
            String[] line;
            String[] line1 = null;
            String temp;
            int diff, i;
            MMFG fv;
            Node label;
            GeneralMetadata generalMetadata = new GeneralMetadata();
            Context confidence;
            Context stride;
            Context threshold;

            String linestr = reader.readLine();
            
            // set filename in general metadata
            if (linestr != null) 
                if (linestr.length() > 0)
                    line1 = linestr.split("\\s+");
                    filename = line1[0];
                    generalMetadata.setFileName(filename);

            mmfg.setGeneralMetadata(generalMetadata); 
            
            // create a root node for the object detection
            Node video = new Node("Root-Asset", mmfg);
            
            // set detectedby
            if (line1.length > 1) 
                video.setDetectedBy(line1[1]);
            
            // set stride as weight
            if (line1.length > 2) {
                stride = new Context();
                stride.setName("Stride");
                video.addWeight(new Weight(stride,  Integer.valueOf(line1[2])));                
            }
            
            // set threshold as weight
            if (line1.length > 3) {
                threshold = new Context();
                threshold.setName("Threshold");
                video.addWeight(new Weight(threshold,  Float.valueOf(line1[3])));                
            }
            
            linestr = reader.readLine();

            while (linestr != null) {
                if (linestr.length() > 0) {
                    // handle text queries containing blanks
                    temp = "";
                    line = linestr.split("\\s+");
                    diff = line.length - 8;
     
                    for (i=2; i <= 2+diff; i++) {
                        if (i < 2+diff)
                            temp = temp + line[i] + " ";
                        else
                            temp = temp + line[i];
                    }
                    
                    // create a node for the detection
                    label = new Node(temp, mmfg);
                    
                    // set confidence as weight
                    confidence = new Context();
                    confidence.setName("Confidence");
                    label.addWeight(new Weight(confidence,  Float.valueOf(line[7 + diff])));
                    
                    // set bounding box coordinates as technical attribute
                    label.addTechnicalAttribute(new TechnicalAttribute( 
                            Math.round(Float.valueOf(line[3 + diff])),
                            Math.round(Float.valueOf(line[4 + diff])),
                            Math.round(Float.valueOf(line[5 + diff])),
                            Math.round(Float.valueOf(line[6 + diff]))
                            , 0, 0));
                    
                    // set detectedby
                    if (line1.length > 1) 
                        label.setDetectedBy(line1[1]);
                    
                    // set filename in general metadata
                    generalMetadata = new GeneralMetadata();
                    generalMetadata.setFileName(line[0]);
                    fv = new MMFG();
                    fv.setGeneralMetadata(generalMetadata);
                    
                    // set classid and class (or text query) of the detection as type and name of a location
                    fv.addLocation(new Location(Integer.valueOf(line[1]), url, temp));
                    label.setFeatureVector(fv);                  
                    
                    video.addChildNode(label);
                }
                
                linestr = reader.readLine();
            }
                        
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(14);
        }
    }
    
    /**
     * This method returns true, if the plugin is able to process files with a given extension
     * 
     * @param extension  of file
     * @return true or false
     */    
    @Override
    public boolean canProcess(String extension) {
        if (extension.equalsIgnoreCase(".mp4")) return true;
        if (extension.equalsIgnoreCase(".avi")) return true;
        if (extension.equalsIgnoreCase(".mkv")) return true;        
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

