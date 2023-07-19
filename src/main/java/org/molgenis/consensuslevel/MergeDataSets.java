package org.molgenis.consensuslevel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merges all VKGL variant classifications public releases into 1 file
 * Releases downloadable at https://vkgl.molgeniscloud.org/menu/main/background
 */
public class MergeDataSets {

    public static void main(String args[]) throws Exception {
        System.out.println("Starting...");
        long start = System.nanoTime();

        String ACCEPTABLE_HEADER_1 = "ID\tlabel\tchromosome\tstart\tstop\tref\talt\tc_notation\tp_notation\thgvs\tgene\tclassification\tsupport";
        String ACCEPTABLE_HEADER_2 = "ID\tlabel\tchromosome\tstart\tstop\tref\talt\tc_notation\tp_notation\ttranscript\thgvs\tgene\tclassification\tsupport";
        String ACCEPTABLE_HEADER_3 = "ID\tlabel\tchromosome\tstart\tstop\tref\talt\tc_notation\tp_notation\ttranscript\thgvs\tgene\tclassification\tsupport\tgene_id_entrez_gene";
        String OUTPUT_FILE_NAME = "allReleasesCombined.tsv";
        String OUTPUT_FILE_HEADER = "Release\tID\tlabel\tchromosome\tstart\tstop\tref\talt\tc_notation\tp_notation\ttranscript\thgvs\tgene\tclassification\tsupport";

        // directory with all (and only) VKGL releases, e.g. VKGL_public_consensus_sep2022.tsv
        File dir = new File("/Users/joeri/VKGL/VKGL-releases");

        //output
        FileWriter fw = new FileWriter(new File(dir, OUTPUT_FILE_NAME));
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(OUTPUT_FILE_HEADER + System.lineSeparator());

        for(File f : dir.listFiles()){
            if(f.getName().equals(OUTPUT_FILE_NAME)){
                continue;
            }

            //ignore
            if(f.isDirectory() || f.getName().equals(".DS_Store") || f.getName().equals(".Rhistory"))
            {
                continue;
            }
            boolean isCSV = false;
            //check
            if(f.getName().endsWith(".csv"))
            {
                isCSV = true;
            }
            else if(f.getName().endsWith(".tsv"))
            {
                isCSV = false;
            }
            else
            {
                throw new Exception("CSV or TSV expected, found " + f.getName());
            }
            System.out.println("filename: " +  f.getName());

            Scanner s = new Scanner(f);
            //get header, remove quotes, and check if acceptable
            String header = s.nextLine();
            header = header.replace("\"", "");
            header = header.replace(",","\t");
            int headerType;
            if(header.equals(ACCEPTABLE_HEADER_1))
            {
                headerType = 1;
            }else if(header.equals(ACCEPTABLE_HEADER_2))
            {
                headerType = 2;
            }else if(header.equals(ACCEPTABLE_HEADER_3))
            {
                headerType = 3;
            }else
            {
                throw new Exception("header not OK: " + header);
            }
            System.out.println("header OK, type = " + headerType + ". file isCSV? " + isCSV);

            // parse rest of release file
            int count = 0;
            while(s.hasNextLine()){

                //DEBUG
                count++;
                if(count == 100)
                {
                   // break;
                }

                String line = s.nextLine();

                //replace commas INSIDE quotes parts with whitespace
                if(isCSV)
                {
                    Pattern p = Pattern.compile("\"(.*?)\"");
                    Matcher matcher = p.matcher(line);
                    while (matcher.find()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(line, 0, matcher.start());
                        String fixMe = line.substring(matcher.start(), matcher.end());
                        fixMe = fixMe.replace(","," ");
                        sb.append(fixMe);
                        sb.append(line.substring(matcher.end()));
                        line = sb.toString();
                    }
                }

                // now remove all quotation and split on comma or tab
                line = line.replace("\"", "");
                String[] lineSplit = line.split((isCSV?",":"\t"), -1);

                String releaseName = f.getName().substring(0, f.getName().indexOf(".")).replace("VKGL_public_consensus_", "");
                StringBuilder sb = new StringBuilder();
                // check if split length is as expected
                if(headerType == 1 && lineSplit.length==13)
                {
                    sb.append(releaseName);
                    for(int i = 0; i < 13; i++)
                    {

                        //cleaning: replace 'MT' by 'M' and '08' by '8'
                        if(i==2)
                        {
                            String chrom =  lineSplit[i];
                            if(chrom.equals("MT"))
                            {
                                chrom = "M";
                            }else if(chrom.equals("08"))
                            {
                                chrom = "8";
                            }
                            sb.append("\t" + chrom);
                        }

                        // cleaning: replace some terms with LP / LB
                        else if(i == 11)
                        {
                            String classification =  lineSplit[i];
                            if(classification.equalsIgnoreCase("(Likely) Pathogenic")){
                                classification = "LP";
                            }
                            else if(classification.equalsIgnoreCase("(Likely) benign")){
                                classification = "LB";
                            }
                            if(!classification.equals("LP") && !classification.equals("LB") && !classification.equals("VUS"))
                            {
                                throw new Exception("classification unknown: " + classification);
                            }
                            sb.append("\t" + classification);

                        }else{
                            sb.append("\t" + lineSplit[i]);
                        }

                        // extra space to match common format
                        if(i == 8){
                            sb.append("\t");
                        }
                    }
                    bw.write(sb + System.lineSeparator());
                }
                else if((headerType == 2 && lineSplit.length==14) || headerType == 3 && lineSplit.length==15)
                {
                    sb.append(releaseName);
                    for(int i = 0; i < 14; i++)
                    {
                        //cleaning: replace 'MT' by 'M'
                        if(i==2)
                        {
                            String chrom =  lineSplit[i];
                            if(chrom.equals("MT"))
                            {
                                chrom = "M";
                            }
                            sb.append("\t" + chrom);
                        }
                        else{
                            sb.append("\t" + lineSplit[i]);
                        }
                    }
                    bw.write(sb + System.lineSeparator());
                }
                else{
                    throw new Exception("problem with the data: " + line + " has " + lineSplit.length + " values");
                }
            }
        }

        bw.flush();
        bw.close();

        System.out.println("Done! Completed in " + ((System.nanoTime()-start)/1000000)+"ms.");
    }

}
