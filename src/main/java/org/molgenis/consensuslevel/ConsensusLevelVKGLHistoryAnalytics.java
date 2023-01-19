package org.molgenis.consensuslevel;

import org.molgenis.lablevel.id;
import org.molgenis.lablevel.LabLevelVariant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * new
 */
public class ConsensusLevelVKGLHistoryAnalytics {

    private File vkglHistory;
    private File outputDataFrame;


    public ConsensusLevelVKGLHistoryAnalytics(File vkglHistory, File outputDataFrame){
        this.vkglHistory = vkglHistory;
        this.outputDataFrame = outputDataFrame;
    }

    /**
     * columns:
     *
     * 0  = Release
     * 1  = ID
     * 2  = label
     * 3  = chromosome
     * 4  = start
     * 5  = stop
     * 6  = ref
     * 7  = alt
     * 8  = c_notation
     * 9  = p_notation
     * 10 = transcript
     * 11 = hgvs
     * 12 = gene
     * 13 = classification
     * 14 = support
     */
    public void go() throws Exception {

        // keep track of all unique releases while reading file
        HashSet<String> allReleases = new HashSet<>();

        /**
         * Read file and perform quick merge based on variant database ID (dbid)
         */
        System.out.println("loading...");
        Scanner sc = new Scanner(vkglHistory);
        sc.nextLine(); //skip header
        int counter = 0;
        HashMap<String, HashMap<String, ConsensusLevelVariant>> quickMerge = new HashMap<>();
        while(sc.hasNextLine())
        {
            String line = sc.nextLine().replace("\"", "");
            String[] s = line.split("\t", -1);

            ConsensusLevelVariant clv = new ConsensusLevelVariant(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14]);
            allReleases.add(clv.release);

            if(!quickMerge.containsKey(clv.chromosome))
            {
                quickMerge.put(clv.chromosome, new HashMap<>());
            }
            HashMap<String, ConsensusLevelVariant> variantsOnChrom = quickMerge.get(clv.chromosome);

            if(variantsOnChrom.containsKey(clv.dbid)){
                if(variantsOnChrom.get(clv.dbid).releaseClassification.containsKey(clv.release))
                {
                    System.out.println("WARNING: release already present in hashmap for " + clv);
                }
                variantsOnChrom.get(clv.dbid).releaseClassification.put(clv.release, clv.classification);
            }
            else{
                variantsOnChrom.put(clv.dbid, clv);
            }
            counter++;
            if(counter % 100000 == 0)
            {
                System.out.println("line nr: " + counter);
            }
        }
        System.out.println("total lines: " + counter);
        System.out.println("quick merge: " + quickMerge.size() + " chromosomes");
        for(String key : quickMerge.keySet())
        {
            System.out.println("chrom "+key+" variants: " + quickMerge.get(key).size());
        }


        /**
         * Deep merge based on other variant properties
         */
        HashMap<String, ArrayList<ConsensusLevelVariant>> deepMerge = new HashMap<>();
        for(String chrom : quickMerge.keySet())
        {
            //DEBUG
//            if(!chrom.equals("22") && !chrom.equals("21") && !chrom.equals("20") && !chrom.equals("19") && !chrom.equals("18"))
//            {
//                continue;
//            }
            System.out.println("deep merge variants on chrom " + chrom);
            HashMap<String, ConsensusLevelVariant> variantsOnChrom =  quickMerge.get(chrom);
            if(!deepMerge.containsKey(chrom))
            {
                deepMerge.put(chrom, new ArrayList<>());
            }
            ArrayList<ConsensusLevelVariant> deepMergeOnChrom = deepMerge.get(chrom);
            for(String key : variantsOnChrom.keySet()){
                ConsensusLevelVariant clv = variantsOnChrom.get(key);
                ConsensusLevelVariant alreadyPresent = containsConsensusLevelVariant(deepMergeOnChrom, clv);
                if(alreadyPresent == null)
                {
                    deepMergeOnChrom.add(clv);
                }
                else
                {
                    if(alreadyPresent.releaseClassification.containsKey(clv.release))
                    {
                        //System.out.println("WARNING: release already present for\n" + alreadyPresent + "\n" + clv+"\n");
                    }
                    alreadyPresent.releaseClassification.put(clv.release, clv.classification);
                }


            }
            System.out.println("merged from " + variantsOnChrom.size() + " to "+deepMergeOnChrom.size()+" variants");
        }


        /**
         * Select interesting variants, e.g. those with differences in classifications over releases
         */
        ArrayList<ConsensusLevelVariant> interestingVariants = new ArrayList<>();
        for(String chrom : quickMerge.keySet()) {
            System.out.println("check interesting variants on chrom " + chrom);
            HashMap<String, ConsensusLevelVariant> variantsOnChrom = quickMerge.get(chrom);
            for (String key : variantsOnChrom.keySet()) {
                ConsensusLevelVariant clv = variantsOnChrom.get(key);
                HashSet<String> differentialClassifications = new HashSet<>();
                for(String release : clv.releaseClassification.keySet()){
                    differentialClassifications.add(clv.releaseClassification.get(release));
                }
                if(differentialClassifications.contains("LP") && differentialClassifications.contains("LB"))
                {
                    interestingVariants.add(clv);
                }
            }
        }
        System.out.println("interestingVariants = " + interestingVariants.size());

        /**
         * Post process: add release of variant that were starting points for merging to the release-classification map
         */
        for(ConsensusLevelVariant clv : interestingVariants)
        {
            clv.releaseClassification.put(clv.release, clv.classification);
        }

        /**
         * Output results
         */
        FileWriter fw = new FileWriter(outputDataFrame);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Release\tConsensus\tId"+System.lineSeparator());
        for(String release : allReleases)
        {
            System.out.println(release);
            for(ConsensusLevelVariant clv : interestingVariants)
            {
                if(clv.releaseClassification.containsKey(release))
                {
                    bw.write(release + "\t" + clv.releaseClassification.get(release) + "\t" + clv.dbid + System.lineSeparator());
                }
                else{
                    bw.write(release + "\t" + "Not present" + "\t" + clv.dbid + System.lineSeparator());
                }
            }
            bw.flush();
        }
        bw.flush();
        bw.close();

    }

    private ConsensusLevelVariant containsConsensusLevelVariant(ArrayList<ConsensusLevelVariant> consensusLevelVariants, ConsensusLevelVariant clvToFind)
    {
        for(ConsensusLevelVariant clv : consensusLevelVariants)
        {
            if(clv.equals(clvToFind))
            {
                return clv;
            }
        }
        return null;
    }

}
