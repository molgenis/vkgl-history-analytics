package org.molgenis;

import org.molgenis.consensuslevel.ConsensusLevelVKGLHistoryAnalytics;

import java.io.File;

public class Main {

    public static void main(String args[]) throws Exception {
        System.out.println("Starting...");
        long start = System.nanoTime();

//        LabLevelVKGLHistoryAnalytics v = new LabLevelVKGLHistoryAnalytics(new File(args[0]), new File(args[1]));
//        v.go();

        ConsensusLevelVKGLHistoryAnalytics c = new ConsensusLevelVKGLHistoryAnalytics(new File(args[0]), new File(args[1]));
        c.go();

        System.out.println("Done! Completed in " + ((System.nanoTime()-start)/1000000)+"ms.");



    }

}
