package org.molgenis;

import org.molgenis.consensuslevel.ConsensusLevelVKGLHistoryAnalytics;

import java.io.File;

/**
 * Connects all the variants through time, it has 2 args:
 * Input: the merged variant releases resulting from 'MergeDataSets', e.g. /Users/joeri/VKGL/VKGL-releases/allReleasesCombined.tsv
 * Output: a file location for an R dataframe (used in alluvial-consensus.R), e.g. /Users/joeri/VKGL/VKGL-releases/dataframe.tsv
 */
public class Main {

    public static void main(String args[]) throws Exception {
        System.out.println("Starting...");
        long start = System.nanoTime();
        ConsensusLevelVKGLHistoryAnalytics c = new ConsensusLevelVKGLHistoryAnalytics(new File(args[0]), new File(args[1]));
        c.go();
        System.out.println("Done! Completed in " + ((System.nanoTime()-start)/1000000)+"ms.");
    }

}
