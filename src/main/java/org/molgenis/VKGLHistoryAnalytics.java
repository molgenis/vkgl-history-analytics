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
    private Map<String, List<Variant>> vs;
    private Set<String> consBins;
    private Set<String> releases;
    private Set<String> uniqueIds;

    private Set<String> variantsThatAreOnceOpposite;


    public VKGLHistoryAnalytics(File vkglHistory, File outputDataFrame){
        this.vkglHistory = vkglHistory;
        this.outputDataFrame = outputDataFrame;
        this.consBins = new HashSet<>();
        this.releases = new LinkedHashSet<>();
        this.uniqueIds = new HashSet<>();
        this.vs = new HashMap<String, List<Variant>>();
        this.variantsThatAreOnceOpposite = new HashSet<>();
    }

    /**
     * columns:
     * 0	1	    2	        3	    4	    5	6	7	    8	    9	        10	    11	12	    13	    14	    15	            16	    17	18	    19	                        20	    21
     * id	export	chromosome	start	stop	ref	alt	gene	c_dna	transcript	protein	amc	umcg	lumc	vumc	radboud_mumc	umcu	nki	erasmus	consensus_classification	matches	comments
     */
    public void go() throws IOException {

        System.out.println("loading...");
        Scanner sc = new Scanner(vkglHistory);
        sc.nextLine(); //skip header
        int counter = 0;
        while(sc.hasNextLine())
        {
            String line = sc.nextLine();
            String[] s = line.split("\t", -1);

            Variant v = new Variant(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7].toUpperCase(), s[8].toUpperCase(), s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], s[18], s[19], s[20], s[21]);

            // assign uniqueID
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
                v.uniqueId = v.gene + "_" + v.c_dna;
            }
            else if(!v.gene.isEmpty() && !v.protein.isEmpty()){
                v.uniqueId = v.gene + "_" + v.protein;
            }
            else if(!v.chromosome.isEmpty() && !v.start.isEmpty() && !v.ref.isEmpty() && !v.alt.isEmpty())
            {
                v.uniqueId = v.chromosome + "_" + v.start + "_" + v.ref + "_" + v.alt;
            }else
            {
                System.out.println("DROPPING VARIANT, no unique ID possible: " + line);
                continue;
            }

            // add release
            releases.add(v.export);

            // add uID
            uniqueIds.add(v.uniqueId);

            // if one lab classification, add special values
            if(v.consensus_classification.equals("\"Classified by one lab\""))
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
                variantsThatAreOnceOpposite.add(v.uniqueId);
            }

            // add variant to its release
            if(!vs.containsKey(s[1]))
            {
                vs.put(v.export, new ArrayList<Variant>());
            }
            vs.get(v.export).add(v);


            counter++;
            if(counter % 10000 == 0)
            {
                System.out.println("line nr: " + counter);
            }
        }

        System.out.println("analyzing...");

        String previousRelease = null;

        FileWriter fw = new FileWriter(outputDataFrame);
        BufferedWriter bw = new BufferedWriter(fw);


        HashMap<String, AtomicInteger> droppedPerRelease = new HashMap<>();

        bw.write("Release\tConsensus\tId\n");
        // not needed actually since we dont compare releases side-by-side now
        for(String release: releases)
        {
            HashSet<String> uIdsInRelease = new HashSet<>();

            System.out.println("Working on release " + release);
            List<Variant> vr = vs.get(release);
            for(Variant v : vr)
            {

                if(!variantsThatAreOnceOpposite.contains(v.uniqueId))
                {
                    uIdsInRelease.add(v.uniqueId); //seen in release, do not add as missing
                    continue;
                }

                if(uIdsInRelease.contains(v.uniqueId))
                {
                 //   System.out.println("DUPLICATE ID IN RELEASE: " + v.uniqueId);
                    if(!droppedPerRelease.containsKey(release))
                    {
                        droppedPerRelease.put(release, new AtomicInteger());
                    }
                    droppedPerRelease.get(release).incrementAndGet();
                    continue;
                }
                bw.write(release.replace("\"", "") + "\t" + v.consensus_classification_withOneLab.replace("\"", "") + "\t" + v.uniqueId.replace("\"", "") + "\n");
                uIdsInRelease.add(v.uniqueId);
            }

            // fill up variant list with 'Missing', if not seen for this release
            HashSet<String> missingIds = new HashSet<String>(variantsThatAreOnceOpposite); //FIXME: use uniqueIds for full set
            missingIds.removeAll(uIdsInRelease);
            for(String mID : missingIds)
            {
                bw.write(release.replace("\"", "") + "\t" + "Absent from release" + "\t" + mID.replace("\"", "") + "\n");
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
