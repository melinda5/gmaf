package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import de.swa.gmaf.plugin.fachpraktikum.ImagePlugin;
import de.swa.gmaf.plugin.fachpraktikum.VideoPlugin;
import de.swa.mmfg.MMFG;

class Test {
    VideoPlugin vp = new VideoPlugin();
    ImagePlugin ip = new ImagePlugin();

    @org.junit.jupiter.api.Test
    void testVideo1() {
        String filename = "video.mp5";
        assertTrue(vp.canProcess(filename.substring(filename.length() - 4)),
                "Video " + filename + " cannot be processed");
    }

    @org.junit.jupiter.api.Test
    void testVideo2() {
        String filename = "video.mp4";
        assertTrue(vp.canProcess(filename.substring(filename.length() - 4)),
                "Video " + filename + " cannot be processed");
    }

    @org.junit.jupiter.api.Test
    void testImage1() {
        String filename = "image.xyz";
        assertTrue(ip.canProcess(filename.substring(filename.length() - 4)),
                "Image " + filename + " cannot be processed");
    }

    @org.junit.jupiter.api.Test
    void testImage2() {
        String filename = "image.png";
        assertTrue(ip.canProcess(filename.substring(filename.length() - 4)),
                "Image " + filename + " cannot be processed");
    }

    @org.junit.jupiter.api.Test
    void testImage3() {
        String filename = "image.jpg";
        assertTrue(ip.canProcess(filename.substring(filename.length() - 4)),
                "Image " + filename + " cannot be processed");
    }
}
