package test;

import static org.junit.jupiter.api.Assertions.*;

import de.swa.gmaf.plugin.fachpraktikum.ImagePlugin;
import de.swa.gmaf.plugin.fachpraktikum.VideoPlugin;

/** 
 * Class for JUnit-testing
 * 
 * @author Melinda Betz
 */
class Test {
    /** 
     * Test-Instance of VideoPlugin
     */
    VideoPlugin vp = new VideoPlugin();
    /** 
     *  Test-Instance of ImagePlugin
     */
    ImagePlugin ip = new ImagePlugin();

    /** 
     * Test of Method VideoPlugin.canProcess.
     * Expected: failure
     */
    @org.junit.jupiter.api.Test
    void testVideo1() {
        String filename = "video.mp5";
        assertFalse(vp.canProcess(filename.substring(filename.length() - 4)),
                "Video " + filename + " can be processed");
    }

    /** 
     * Test of Method VideoPlugin.canProcess.
     * Expected: ok
     */
    @org.junit.jupiter.api.Test
    void testVideo2() {
        String filename = "video.mp4";
        assertTrue(vp.canProcess(filename.substring(filename.length() - 4)),
                "Video " + filename + " cannot be processed");
    }

    /** 
     * Test of Method ImagePlugin.canProcess.
     * Expected: failure
     */
    @org.junit.jupiter.api.Test
    void testImage1() {
        String filename = "image.xyz";
        assertFalse(ip.canProcess(filename.substring(filename.length() - 4)),
                "Image " + filename + " can be processed");
    }

    /** 
     * Test of Method ImagePlugin.canProcess.
     * Expected: ok
     */
    @org.junit.jupiter.api.Test
    void testImage2() {
        String filename = "image.png";
        assertTrue(ip.canProcess(filename.substring(filename.length() - 4)),
                "Image " + filename + " cannot be processed");
    }

    /** 
     * Test of Method ImagePlugin.canProcess.
     * Expected: ok
     */
    @org.junit.jupiter.api.Test
    void testImage3() {
        String filename = "image.jpg";
        assertTrue(ip.canProcess(filename.substring(filename.length() - 4)),
                "Image " + filename + " cannot be processed");
    }
}
