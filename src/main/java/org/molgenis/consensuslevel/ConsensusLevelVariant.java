package org.molgenis.consensuslevel;

import java.util.HashMap;
import java.util.Objects;

public class ConsensusLevelVariant {

    public String release;
    public String dbid;
    public String label;
    public String chromosome;
    public String start;
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
    public HashMap<String, String> releaseClassification;

    public ConsensusLevelVariant(String release, String dbid, String label, String chromosome, String start, String stop, String ref, String alt, String c_dna, String protein, String transcript, String hgvs, String gene, String classification, String support) throws Exception {
        this.release = release;
        this.dbid = dbid;
        this.label = label;
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
        this.ref = ref;
        this.alt = alt;
        this.c_dna = c_dna;
        this.protein = protein;
        this.transcript = transcript;
        this.hgvs = hgvs;
        this.gene = gene;
        this.classification = classification;
        this.support = support;
        this.releaseClassification = new HashMap<>();
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
        // field comparison
        return ( !this.gene.isEmpty() && this.gene.equals(clv.gene) && !this.c_dna.isEmpty() && this.c_dna.equals(clv.c_dna) )
                || ( !this.gene.isEmpty() && this.gene.equals(clv.gene) && !this.protein.isEmpty() && this.protein.equals(clv.protein) )
                || ( !this.start.isEmpty() && this.start.equals(clv.start) && !this.ref.isEmpty() && this.ref.equals(clv.ref) && !this.alt.isEmpty() && this.alt.equals(clv.alt));
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
                ", start='" + start + '\'' +
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
                '}';
    }

}
