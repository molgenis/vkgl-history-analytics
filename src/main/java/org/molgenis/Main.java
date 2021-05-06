package org.molgenis;

import java.io.File;

public class Main {

    public static void main(String args[]) throws Exception {
        System.out.println("Starting...");
        long start = System.nanoTime();
        VKGLHistoryAnalytics v = new VKGLHistoryAnalytics(new File(args[0]), new File(args[1]));
        v.go();
        System.out.println("Done! Completed in " + ((System.nanoTime()-start)/1000000)+"ms.");
    }

}
