package org.molgenis;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Intended to load all data, do complicated things, then write specific output
 *
 * problems:
 * genes not always properly capitalized
 * cDNA not always properly capitalized
 * variants not always merged properly (e.g. same gene/cDNA, but different positions)
 * bad cDNA notation i.e. "C.907G>C, NM_001083602.1"
 *
 */
public class VKGLHistoryAnalytics {

    private File vkglHistory;
    private File outputDataFrame;

    // sets to keep track of all unique bins and releases
    private Set<String> consBins;
    private Set<String> releases;

    // variants and helper lists with pointers to IDs
    private Map<String, List<Variant>> allVariants;
    private List<id> variantIds;
    private List<id> variantsThatAreOnceOpposite;

    // maps to help fill gaps
    private Map<String, String> cdnaToVCF;
    private Map<String, String> cdnaToProtein;
    private Map<String, String> vcfToCdna;
    private Map<String, String> vcfToProtein;
    private Map<String, String> proteinToVCF;
    private Map<String, String> proteinToCdna;


    public VKGLHistoryAnalytics(File vkglHistory, File outputDataFrame){
        this.vkglHistory = vkglHistory;
        this.outputDataFrame = outputDataFrame;
        this.consBins = new HashSet<>();
        this.releases = new LinkedHashSet<>();
        this.variantIds = new ArrayList<id>();
        this.allVariants = new HashMap<String, List<Variant>>();
        this.variantsThatAreOnceOpposite = new ArrayList<>();
        this.cdnaToVCF = new HashMap<String, String>();
        this.cdnaToProtein = new HashMap<String, String>();
        this.vcfToCdna = new HashMap<String, String>();
        this.vcfToProtein = new HashMap<String, String>();
        this.proteinToVCF = new HashMap<String, String>();
        this.proteinToCdna = new HashMap<String, String>();
    }

    /**
     * columns:
     * 0	1	    2	        3	    4	    5	6	7	    8	    9	        10	    11	12	    13	    14	    15	            16	    17	18	    19	                        20	    21
     * dbid	export	chromosome	start	stop	ref	alt	gene	c_dna	transcript	protein	amc	umcg	lumc	vumc	radboud_mumc	umcu	nki	erasmus	consensus_classification	matches	comments
     */
    public void go() throws IOException {

        System.out.println("loading...");
        Scanner sc = new Scanner(vkglHistory);
        sc.nextLine(); //skip header
        int counter = 0;
        while(sc.hasNextLine())
        {
            String line = sc.nextLine().replace("\"", "");;
            String[] s = line.split("\t", -1);

            Variant v = new Variant(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7].toUpperCase(), s[8].toUpperCase(), s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20], s[21]);

            // assign uniqueID
            String cDNAid = null;
            String Protid = null;
            String VCFid = null;

            if(!v.gene.isEmpty() && !v.c_dna.isEmpty()){
                // bad notation i.e. "C.907G>C, NM_001083602.1"
                if(v.c_dna.contains(", "))
                {
                    String[] splitFix = v.c_dna.split(", ");
                    if(splitFix.length == 2)
                    {
                        v.c_dna = splitFix[0] + "\""; //remove later, keep consistent with old notation for now
                    }
                }
                cDNAid = v.gene + "_" + v.c_dna;
            }

            if(!v.gene.isEmpty() && !v.protein.isEmpty()){
                Protid = v.gene + "_" + v.protein;
            }

            if(!v.chromosome.isEmpty() && !v.start.isEmpty() && !v.ref.isEmpty() && !v.alt.isEmpty())
            {
                VCFid = v.chromosome + "_" + v.start + "_" + v.ref + "_" + v.alt;
            }

            if(cDNAid != null || Protid != null || VCFid != null) {
                v.id = new id(cDNAid, Protid, VCFid);
            }
            else
            {
                System.out.println("DROPPING VARIANT, no unique ID possible: " + line);
                continue;
            }

            // add to maps to fill gaps later on
            if(cDNAid != null && VCFid != null)
            {
                cdnaToVCF.put(cDNAid, VCFid);
                vcfToCdna.put(VCFid, cDNAid);
            }
            if(cDNAid != null && Protid != null)
            {
                cdnaToProtein.put(cDNAid, Protid);
                proteinToCdna.put(Protid, cDNAid);
            }
            if(VCFid != null && Protid != null)
            {
                vcfToProtein.put(VCFid, Protid);
                proteinToVCF.put(Protid, VCFid);
            }

            // add release
            releases.add(v.export);

            // add to map with all IDs
            variantIds.add(v.id);

            // if one lab classification, add special values
            if(v.consensus_classification.equals("Classified by one lab"))
            {
                for(int i = 11; i <= 18; i++){
                    if(!s[i].isEmpty())
                    {
                        v.oneLabRawClassification = s[i];
                        v.oneLabLPPLBBmergedClassification = s[i].contains("athogenic") ? "(Likely) pathogenic" : s[i].contains("enign") ? "(Likely) benign" : s[i].contains("VUS") ? "VUS" : null;
                        v.consensus_classification_withOneLab = "One lab: "+v.oneLabLPPLBBmergedClassification;
                    }
                }
            }
            else{
                v.consensus_classification_withOneLab = "Multiple labs: " + v.consensus_classification;
            }
            consBins.add(v.consensus_classification_withOneLab);


            if(v.consensus_classification.contains("Opposite"))
            {
                variantsThatAreOnceOpposite.add(v.id);
            }

            // add variant to its release
            if(!allVariants.containsKey(s[1]))
            {
                allVariants.put(v.export, new ArrayList<Variant>());
            }
            allVariants.get(v.export).add(v);



            counter++;
            if(counter % 10000 == 0)
            {
                System.out.println("line nr: " + counter);
            }
        }

        System.out.println("lines: " + counter + " ID sanity check: " + variantIds.size());

//        for(String key : cdnaToProtein.keySet())
//        {
//            System.out.println(key + " -> " + cdnaToProtein.get(key));
//        }

        /**
         *
         * harmonize IDs, meaning that missing fields of things that map are 'filled in'
         * e.g. 1: A - B - C matches 2: A - B, then C from 1 is added to 2.
         * we also  'iron out' other discrepancies, e.g. when VCF notation is equal but cDNA is not
         * this happens by using the override equals() which is true when 1 of 3 'id fields' matches
         * all not-null fields are copied from var1 to var2 when a match is found
         *
         * to avoid multiple different values overwriting each other,
         *
         * dont worry about potential duplicates within releases, will be handled later
         *
         */
        System.out.println("harmonize IDs...");
        counter = 0;
      //  Set<id>
        for(id variantId : variantIds)
        {

            // gentle: only add missing values
//            if(variantId.Protid == null && variantId.cDNAid != null && cdnaToProtein.containsKey(variantId.cDNAid)){
//                variantId.Protid = cdnaToProtein.get(variantId.cDNAid);
//            }
//            if(variantId.Protid == null &&  variantId.VCFid != null && vcfToProtein.containsKey(variantId.VCFid)){
//                variantId.Protid = vcfToProtein.get(variantId.VCFid);
//            }
//            if(variantId.cDNAid == null && variantId.Protid != null && proteinToCdna.containsKey(variantId.Protid)){
//            variantId.cDNAid = proteinToCdna.get(variantId.Protid);
//        }
//            if(variantId.cDNAid == null &&  variantId.VCFid != null && vcfToCdna.containsKey(variantId.VCFid)){
//                variantId.cDNAid = vcfToCdna.get(variantId.VCFid);
//            }
//            if(variantId.VCFid == null && variantId.Protid != null && proteinToVCF.containsKey(variantId.Protid)){
//                variantId.VCFid = proteinToVCF.get(variantId.Protid);
//            }
//            if(variantId.VCFid == null &&  variantId.cDNAid != null && cdnaToVCF.containsKey(variantId.cDNAid)){
//                variantId.VCFid = cdnaToVCF.get(variantId.cDNAid);
//            }

            // aggressive: overwrite everything
            if(variantId.cDNAid != null && cdnaToProtein.containsKey(variantId.cDNAid)){
                variantId.Protid = cdnaToProtein.get(variantId.cDNAid);
            }
            if(variantId.VCFid != null && vcfToProtein.containsKey(variantId.VCFid)){
                variantId.Protid = vcfToProtein.get(variantId.VCFid);
            }
            if(variantId.Protid != null && proteinToCdna.containsKey(variantId.Protid)){
                variantId.cDNAid = proteinToCdna.get(variantId.Protid);
            }
            if(variantId.VCFid != null && vcfToCdna.containsKey(variantId.VCFid)){
                variantId.cDNAid = vcfToCdna.get(variantId.VCFid);
            }
            if(variantId.Protid != null && proteinToVCF.containsKey(variantId.Protid)){
                variantId.VCFid = proteinToVCF.get(variantId.Protid);
            }
            if(variantId.cDNAid != null && cdnaToVCF.containsKey(variantId.cDNAid)){
                variantId.VCFid = cdnaToVCF.get(variantId.cDNAid);
            }

            counter++;
            if(counter % 100000 == 0)
            {
                System.out.println("id nr: " + counter);
            }
        }


        /**
         *
         * now that IDs are harmonized, we can manipulate the data because HashMap requires hashCode()
         * and hashCode() can only work when all IDs that should match, all have the same values for each partial id
         *
         */


        System.out.println("analyzing...");

        String previousRelease = null;

        FileWriter fw = new FileWriter(outputDataFrame);
        BufferedWriter bw = new BufferedWriter(fw);


        HashMap<String, AtomicInteger> droppedPerRelease = new HashMap<>();

        bw.write("Release\tConsensus\tId\n");
        // not needed actually since we dont compare releases side-by-side now
        for(String release: releases)
        {
            HashSet<id> uIdsInRelease = new HashSet<>();

            System.out.println("Working on release " + release);
            List<Variant> vr = allVariants.get(release);

            for(Variant v : vr)
            {

                if(!variantsThatAreOnceOpposite.contains(v.id))
                {
                    uIdsInRelease.add(v.id); //seen in release, do not add as missing
                    continue;
                }

                if(uIdsInRelease.contains(v.id))
                {
                    System.out.println("DUPLICATE ID IN RELEASE: " + v.id);
                    if(!droppedPerRelease.containsKey(release))
                    {
                        droppedPerRelease.put(release, new AtomicInteger());
                    }
                    droppedPerRelease.get(release).incrementAndGet();
                    continue;
                }


                bw.write(release + "\t" + v.consensus_classification_withOneLab + "\t" + v.id.toString() + "\n"); //v.id.replace("\"", "")
                uIdsInRelease.add(v.id);
            }

            // fill up variant list with 'Missing', if not seen for this release
            HashSet<id> missingIds = new HashSet<id>(variantsThatAreOnceOpposite); //FIXME: use variantIds for full set
            missingIds.removeAll(uIdsInRelease);
            for(id mID : missingIds)
            {
                bw.write(release + "\t" + "Absent from release" + "\t" + mID.toString() + "\n"); //mID.replace("\"", "")
            }

        }

        for(String release : droppedPerRelease.keySet())
        {
            System.out.println("Release " + release + " dropped: " + droppedPerRelease.get(release));
        }

        bw.flush();
        bw.close();

        System.out.println(consBins.toString());



    }



}
