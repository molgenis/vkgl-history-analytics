package org.molgenis.consensuslevel;

import org.molgenis.genepanels.SAID;
import org.molgenis.lablevel.id;
import org.molgenis.lablevel.LabLevelVariant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Uses the merged VKGL public release data
 * and outputs a dataframe for alluvial plots in R
 */
public class ConsensusLevelVKGLHistoryAnalytics {

    private static final String CURRENT_RELEASE = "july2023";

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

        // keep track of all unique genes while reading file
        HashSet<String> allGenes = new HashSet<>();

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

            /**
             * Variant filter the moment they are first encountered in source file
             */
            // "History of Y-chromosome variants that have appeared in any VKGL public consensus release"
//            if(!clv.chromosome.equals("Y")){
//                continue;
//            }
            // "History of BRCA1 insertion variants in the VKGL July 2023 public consensus release"
//            if(!clv.gene.equals("BRCA1")){
//                continue;
//            }
//            if(!clv.isInsertion){
//                continue;
//            }
            /****/

            allReleases.add(clv.release);
            allGenes.add(clv.gene);

            if(!quickMerge.containsKey(clv.chromosome))
            {
                quickMerge.put(clv.chromosome, new HashMap<String, ConsensusLevelVariant>());
            }
            HashMap<String, ConsensusLevelVariant> variantsOnChrom = quickMerge.get(clv.chromosome);

            if(variantsOnChrom.containsKey(clv.dbid)){
                if(variantsOnChrom.get(clv.dbid).releaseClassification.containsKey(clv.release))
                {
                    System.out.println("QUICK MERGE - WARNING: release already present in hashmap for " + clv);
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
         * Post process: add release of variant that were starting points for merging to their release-classification map
         */
        for(String chrom : quickMerge.keySet())
        {
            for(String variantId : quickMerge.get(chrom).keySet())
            {
                ConsensusLevelVariant clv = quickMerge.get(chrom).get(variantId);
                if(clv.releaseClassification.containsKey(clv.release))
                {
                    System.out.println("POST PROCESS - WARNING: release already present in hashmap for " + clv);
                }
                clv.releaseClassification.put(clv.release, clv.classification);
            }
        }


        /**
         * Deep merge based on other variant properties
         */
        HashMap<String, ArrayList<ConsensusLevelVariant>> deepMerge = new HashMap<>();
        for(String chrom : quickMerge.keySet())
        {
            System.out.println("deep merge variants on chrom " + chrom);
            HashMap<String, ConsensusLevelVariant> variantsOnChrom =  quickMerge.get(chrom);
            if(!deepMerge.containsKey(chrom))
            {
                deepMerge.put(chrom, new ArrayList<ConsensusLevelVariant>());
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
                    for(String releaseInNewCLV : clv.releaseClassification.keySet())
                    {
                        if(alreadyPresent.releaseClassification.containsKey(releaseInNewCLV))
                        {
                            String classificationInAlreadyPresent = alreadyPresent.releaseClassification.get(releaseInNewCLV);
                            String classificationInNewCLV = clv.releaseClassification.get(releaseInNewCLV);

                            if(classificationInAlreadyPresent.equals("CF"))
                            {
                                System.out.println("DEEP MERGE - WARNING - Classification already marked as conflicting for\n" + alreadyPresent + "\n" + clv+"\n");
                            }
                            else if(!classificationInAlreadyPresent.equals(classificationInNewCLV)){
                                System.out.println("DEEP MERGE - WARNING - Classification difference. Setting to CF for\n" + alreadyPresent + "\n" + clv+"\n");
                                alreadyPresent.releaseClassification.put(releaseInNewCLV, "CF");
                            }else{
                                //System.out.println("DEEP MERGE - WARNING - Classification is the same, ignoring.");
                            }
                        }
                        else{
                            alreadyPresent.releaseClassification.put(releaseInNewCLV, clv.releaseClassification.get(releaseInNewCLV));
                        }
                    }
                }
            }
            System.out.println("merged from " + variantsOnChrom.size() + " to "+deepMergeOnChrom.size()+" variants");
        }


        /**
         * Select interesting variants, e.g. those with differences in classifications over releases
         */
        ArrayList<ConsensusLevelVariant> interestingVariants = new ArrayList<>();
        Set<String> interestingVariantsGenes = new HashSet<>();
        for(String chrom : deepMerge.keySet()) {
            System.out.println("check interesting variants on chrom " + chrom);
            ArrayList<ConsensusLevelVariant> variantsOnChrom = deepMerge.get(chrom);
            for (ConsensusLevelVariant clv : variantsOnChrom) {
                HashSet<String> differentialClassifications = new HashSet<>();
                for(String release : clv.releaseClassification.keySet()){
                    differentialClassifications.add(clv.releaseClassification.get(release));
                }
                if(
                /**
                 * Filter by gene, gene panel, classifications, releases, etc.
                 */
                        true

                       // "Classification history of all variants in the VKGL July 2023 public consensus release"
                       // && clv.releaseClassification.containsKey(CURRENT_RELEASE)

                       // "History of variants in the VKGL July 2023 public consensus release with >1 different lifetime classifications"
                       // && clv.releaseClassification.containsKey(CURRENT_RELEASE)
                       // && differentialClassifications.size() > 1

                       // "History of variants in the VKGL April 2023 public consensus release with any lifetime LP-to-LB or LB-to-LP transition"
                       // && clv.releaseClassification.containsKey(CURRENT_RELEASE)
                       // && differentialClassifications.contains("LB")
                       // && differentialClassifications.contains("LP")

                       // "History of variants that have appeared in the VKGL public consensus that are not part of the July 2023 release"
                       // && !clv.releaseClassification.containsKey(CURRENT_RELEASE)

                       // "History of variants in the VKGL July 2023 public consensus release with conflicting classifications"
                       // && clv.releaseClassification.containsKey(CURRENT_RELEASE)
                       // && clv.releaseClassification.get(CURRENT_RELEASE).equals("CF")

                       // Other examples:
                       // && SAID.genes.contains(clv.gene)
                       // && differentialClassifications.size() > 1
                       // && differentialClassifications.contains("LB")
                       // && differentialClassifications.contains("VUS")
                       // && differentialClassifications.contains("LP")

                )
                {
                    interestingVariants.add(clv);
                    interestingVariantsGenes.add(clv.gene);
                }
            }
        }
        System.out.println("filtered variant list size = " + interestingVariants.size());

        /**
         * Output results
         */
        FileWriter fw = new FileWriter(outputDataFrame);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Release\tConsensus\tId\tLabel\tGene"+System.lineSeparator());
        for(String release : allReleases)
        {
            System.out.println(release);
            for(ConsensusLevelVariant clv : interestingVariants)
            {
                if(clv.releaseClassification.containsKey(release))
                {
                    bw.write(release + "\t" + clv.releaseClassification.get(release) + "\t" + clv.dbid + "\t" + makeLabel(clv) + "\t" + clv.gene + System.lineSeparator());
                }
                else{
                    bw.write(release + "\t" + "Absent" + "\t" + clv.dbid + "\t" + makeLabel(clv) + "\t" + clv.gene + System.lineSeparator());
                }
            }
            bw.flush();
        }
        bw.flush();
        bw.close();

    }

    private List<String> grabRandomGenes(HashSet<String> allGenes, int size) {
        // get random genes, but without repetitions
        List<String> allGenesArr = new ArrayList<>(allGenes);
        Collections.shuffle(allGenesArr);
        return allGenesArr.subList(0, size);
    }

    private String makeLabel(ConsensusLevelVariant clv) {
        String label = clv.gene;
        label = label + (clv.c_dna == null ? " "+clv.start+":"+clv.ref+">"+clv.alt : ":"+((clv.c_dna.contains(" ") ? clv.c_dna.substring(0, clv.c_dna.indexOf(" ")) : clv.c_dna)));
        return label;
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

    private void permute(HashSet allGenes, ArrayList interestingVariantsGenes, HashMap<String, ArrayList<ConsensusLevelVariant>> deepMerge, int nrOfPermutations)
    {
        /**
         * Permutation for panel analysis
         */
        for(int i=0; i<nrOfPermutations; i++)
        {
            ArrayList<ConsensusLevelVariant> permutations = new ArrayList<>();
            List<String> randomGenes = grabRandomGenes(allGenes, interestingVariantsGenes.size());
            for(String chrom : deepMerge.keySet()) {
                ArrayList<ConsensusLevelVariant> variantsOnChrom = deepMerge.get(chrom);
                for (ConsensusLevelVariant clv : variantsOnChrom) {
                    HashSet<String> differentialClassifications = new HashSet<>();
                    for (String release : clv.releaseClassification.keySet()) {
                        differentialClassifications.add(clv.releaseClassification.get(release));
                    }
                    if ( randomGenes.contains(clv.gene)
                        // && differentialClassifications.size() > 1
                    ) {
                        permutations.add(clv);
                    }
                }
            }
            System.out.println("perm["+i+"] = " + permutations.size());
        }
    }

}
