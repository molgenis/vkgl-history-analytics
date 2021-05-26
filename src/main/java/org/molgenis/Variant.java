package org.molgenis;

public class Variant {

    public String dbid;
    public String export;
    public String chromosome;
    public String start;
    public String stop;
    public String ref;
    public String alt;
    public String gene;
    public String c_dna;
    public String transcript;
    public String protein;
    public String amc;
    public String umcg;
    public String lumc;
    public String vumc;
    public String radboud_mumc;
    public String umcu;
    public String nki;
    public String erasmus;
    public String consensus_classification;
    public String matches;
    public String comments;
    public String oneLabRawClassification;
    public String oneLabLPPLBBmergedClassification;
    public String consensus_classification_withOneLab;
    public id id;

    public Variant(String id, String export, String chromosome, String start, String stop, String ref, String alt, String gene, String c_dna, String transcript, String protein, String amc, String umcg, String lumc, String vumc, String radboud_mumc, String umcu, String nki, String erasmus, String consensus_classification, String matches, String comments) {
        this.dbid = id;
        this.export = export;
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
        this.ref = ref;
        this.alt = alt;
        this.gene = gene;
        this.c_dna = c_dna;
        this.transcript = transcript;
        this.protein = protein;
        this.amc = amc;
        this.umcg = umcg;
        this.lumc = lumc;
        this.vumc = vumc;
        this.radboud_mumc = radboud_mumc;
        this.umcu = umcu;
        this.nki = nki;
        this.erasmus = erasmus;
        this.consensus_classification = consensus_classification;
        this.matches = matches;
        this.comments = comments;
    }
}
