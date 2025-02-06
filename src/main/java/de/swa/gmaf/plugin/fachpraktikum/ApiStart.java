package de.swa.gmaf.plugin.fachpraktikum;

import java.io.*;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.builder.FeatureVectorBuilder;
import de.swa.mmfg.builder.XMLEncodeDecode;

/** 
 * Class for starting the GMAF API to create a MMFG and add the results 
 * of a video or an image object detection to this MMFG
 * 
 * @author Melinda Betz
 */
public class ApiStart {
    /**
     * Attribute to define the very special case of working with a folder of images 
     * instead of one image or video
     */
    public static Boolean folderProcessing = false;

    /**
     * This method starts the processing.
     * 
     * system exit code:  0   success 
     *                              10  to few arguments
     *                              11  wrong file extension
     *                              12  wrong type 
     *                              13  error creating xml 
     *                              13  error in plugin
     * 
     * @param args args[0] type of object detection: V for video - P for picture - F for folder
     *                       args[1] resultfile of the object detection
     *                       args[2] path of result file, also used as path for the output xml file of the MMFG
     */
    public static void main(String[] args) {     
        if (args.length < 3)
            System.exit(10);
        
        String type = args[0];
        String resultfile = args[1];
        String outputPath = args[2];
        File file = new File(outputPath+resultfile);
        MMFG mmfg = new MMFG();

        // video processing MMFG
        if (type.equals("V")) {
            VideoPlugin vp = new VideoPlugin();
            if (vp.canProcess(resultfile.substring(resultfile.length() - 4)))
                vp.process(null, file, null, mmfg);
            else
                System.exit(11);
        } else
        // image processing MMFG
            if (type.equals("P")) {
                ImagePlugin ip = new ImagePlugin();
                if (ip.canProcess(resultfile.substring(resultfile.length() - 4)))
                    ip.process(null, file, null, mmfg);
                else
                    System.exit(11);
            } else {
                // folder processing MMFG
                if (type.equals("F")) 
                    folderProcessing = true;
                if (type.equals("F") || type.equals("P")) {
                    ImagePlugin ipf = new ImagePlugin();
                    if (ipf.canProcess(resultfile.substring(resultfile.length() - 4)))
                        ipf.process(null, file, null, mmfg);
                    else
                        System.exit(11);
                } else
                    System.exit(12);
            }

        // build xml from created MMFG 
        String result = FeatureVectorBuilder.flatten(mmfg, new XMLEncodeDecode());      
        
        try {
         // save xml file to given output folder
            PrintWriter writer = new PrintWriter(outputPath+resultfile.substring(0, resultfile.length() - 10)+"mmfg.xml", "UTF-8");
            writer.println(result);
            writer.close();
          }
          catch(Exception e) {
              System.exit(13);
          }
    }
}