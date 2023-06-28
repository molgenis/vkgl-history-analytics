package org.molgenis.consensuslevel;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.molgenis.consensuslevel.Notation.backTrimRefAlt;

public class ConsensusLevelVariant {

    public String release;
    public String dbid;
    public String label;
    public String chromosome;
    public long start;
    public String stop;
    public String ref;
    public String alt;
    public String c_dna;
    public String protein;
    public String transcript;
    public String hgvs;
    public String gene;
    public String classification;
    public String support;
    public boolean isInDel;
    public boolean isInsertion;
    public boolean isDeletion;

    // filled in later
    public HashMap<String, String> releaseClassification;
    public Boolean hasDotNotation;
    public String refDotNotationForIndel;
    public String altDotNotationForIndel;
    public long startDotNotationForIndel;
    public long startCoordFromHGVS;
    Set<Long> allStartCoords;


    public ConsensusLevelVariant(String release, String dbid, String label, String chromosome, String start, String stop, String ref, String alt, String c_dna, String protein, String transcript, String hgvs, String gene, String classification, String support) throws Exception {
        this.release = release;
        this.dbid = dbid;
        this.label = label;
        this.chromosome = chromosome;
        if(start == null || start.isEmpty())
        {
            throw new Exception("start cannot be null or empty");
        }
        this.start = Long.parseLong(start);
        this.stop = stop;
        if(ref == null || ref.isEmpty())
        {
            throw new Exception("ref cannot be null or empty");
        }
        this.ref = ref;
        if(alt == null || alt.isEmpty())
        {
            throw new Exception("alt cannot be null or empty");
        }
        this.alt = alt;

        // set insertion/deletion/indel flag
        this.isInsertion = false;
        if((ref.equals(".") && !alt.equals(".")) || alt.length() > ref.length())
        {
            this.isInsertion = true;
        }
        this.isDeletion = false;
        if((alt.equals(".") && !ref.equals(".")) || alt.length() < ref.length())
        {
            this.isDeletion = true;
        }
        this.isInDel = false;
        if(this.isInsertion || this.isDeletion)
        {
            this.isInDel = true;
        }

        // must start with c. and length at least (e.g. "c.1A>G" is okay, but "c.*73" is not)
        if(c_dna.startsWith("c.") && c_dna.length() > 5)
        {
            this.c_dna = c_dna;
        }
        // must start with p. and length at least 4 (e.g. "p.R9Q" or "p.C9=" is okay, but "p.=" is not)
        // extra filter for protein notations like "p.(=)", "p.(=)  NM_001143773.1", "xx:p.?"
        if(protein.startsWith("p.") && protein.length() > 4 && !protein.contains("(=)") && !protein.contains("p.?"))
        {
            this.protein = protein;
        }
        this.transcript = transcript;
        this.hgvs = hgvs;
        if(gene == null || gene.isEmpty())
        {
            throw new Exception("gene cannot be null or empty");
        }
        this.gene = gene;
        this.classification = classification;
        this.support = support;
        this.releaseClassification = new HashMap<>();
        this.hasDotNotation = false;

        // extract start coord from HGVS for fixing matching, e.g.
        // june2019	17_78157802_AGG_._CARD14	17:78157802 CARD14 AGG>.	17	78157802	78157804	AGG	.	c.440_442del	p.Glu147del			CARD14	VUS	1 lab
        // to
        // oct2019	ba7a0e69a6	17:78157798 CARD14 AAGG>A	17	78157798	78157801	AAGG	A			NM_024110.4	NC_000017.10:g.78157802_78157804delAGG	CARD14	LP	1 lab
        // where start=78157802 matches coordinate in NC_000017.10:g.78157802
        if(!this.hgvs.isEmpty())
        {
            Pattern p = Pattern.compile(":g.(.+?)\\D"); //start from g. until a non-digit
            Matcher matcher = p.matcher(this.hgvs);
            this.startCoordFromHGVS = matcher.find() ? Long.parseLong(matcher.group(1)) : 0;
        }

        // prepare "dot notations" for deep ref/alt matching
        if(ref.length() > 1 || alt.length() > 1)
        {
            // if different lentght and does NOT contain a dot, turn into dot notation
            // todo

            /*
            case: match
            C	CA  (start 18077386)
            to
            .	A   (start 18077386)
             */
            if(ref.length() == 1 && alt.length() > 1 && !ref.equals("."))
            {
                this.refDotNotationForIndel = ".";
                this.altDotNotationForIndel = this.alt.substring(1);
                this.startDotNotationForIndel = this.start;
                this.hasDotNotation = true;
            }
            /*
            other case:
            TCA	T   (start 38508217)
            to
            CA	.   (start 38508218)

            or e.g.
            AC	A   (start 114903782)
            to
            C	.   (start 114903783)
             */
            else if(ref.length() > 1 && alt.length() == 1 && !alt.equals(".")){
                this.refDotNotationForIndel = this.ref.substring(1);
                this.altDotNotationForIndel = ".";
                this.startDotNotationForIndel = this.start+1;
                this.hasDotNotation = true;
            }
            else
            {
                // indels where multiple bases are replaced with multiple bases, e.g.  ref='CTT', alt='AATAAGG'
                // must also deal with this: CA_CAA can be C_CA etc. so, must shorten notation.
                String[] trim = backTrimRefAlt(this.ref, this.alt);
                if(!trim[0].equals(this.ref) || !trim[1].equals(this.alt)){
             //       System.out.println("updated multibase notation!");
                    this.refDotNotationForIndel = trim[0];
                    this.altDotNotationForIndel = trim[1];
                    this.startDotNotationForIndel = this.start;
                    this.hasDotNotation = true;
                }
            }
        }

        // gather all start coords in array for easy comparison
        allStartCoords = new HashSet<>();
        allStartCoords.add(this.start);
        if(hasDotNotation)
        {
            allStartCoords.add(startDotNotationForIndel);
        }
        if(startCoordFromHGVS != 0L)
        {
            allStartCoords.add(startCoordFromHGVS);
        }
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        ConsensusLevelVariant clv = (ConsensusLevelVariant) o;
        boolean startNotationMatch = StartNotationMatch(this, clv);

        // field comparison
        return     ( this.gene.equals(clv.gene) && this.c_dna != null && this.c_dna.equals(clv.c_dna) )
                || ( this.gene.equals(clv.gene) && this.protein != null && this.protein.equals(clv.protein) )
                || ( startNotationMatch && this.ref.equals(clv.ref) && this.alt.equals(clv.alt))
                || ( this.hasDotNotation && startNotationMatch && this.refDotNotationForIndel.equals(clv.ref) && this.altDotNotationForIndel.equals(clv.alt))
                || ( clv.hasDotNotation && startNotationMatch && clv.refDotNotationForIndel.equals(this.ref) && clv.altDotNotationForIndel.equals(this.alt))
                ;
    }

    public boolean StartNotationMatch(ConsensusLevelVariant thisVariant, ConsensusLevelVariant clv)
    {
        for(long thisStart : thisVariant.allStartCoords)
        {
            for(long clvStart : clv.allStartCoords)
            {
                if(thisStart == clvStart)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dbid);
    }

    @Override
    public String toString() {
        return "ConsensusLevelVariant{" +
                "release='" + release + '\'' +
                ", dbid='" + dbid + '\'' +
                ", label='" + label + '\'' +
                ", chromosome='" + chromosome + '\'' +
                ", start=" + start +
                ", stop='" + stop + '\'' +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", c_dna='" + c_dna + '\'' +
                ", protein='" + protein + '\'' +
                ", transcript='" + transcript + '\'' +
                ", hgvs='" + hgvs + '\'' +
                ", gene='" + gene + '\'' +
                ", classification='" + classification + '\'' +
                ", support='" + support + '\'' +
                ", releaseClassification=" + releaseClassification +
                ", hasDotNotation=" + hasDotNotation +
                ", refDotNotationForIndel='" + refDotNotationForIndel + '\'' +
                ", altDotNotationForIndel='" + altDotNotationForIndel + '\'' +
                ", startDotNotationForIndel=" + startDotNotationForIndel +
                ", startCoordFromHGVS=" + startCoordFromHGVS +
                ", allStartCoords=" + allStartCoords +
                '}';
    }
}
