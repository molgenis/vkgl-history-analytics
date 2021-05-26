package org.molgenis;

import java.util.Objects;
import java.util.UUID;

public class id {
    public String cDNAid;
    public String Protid;
    public String VCFid;

    public id(String cDNAid, String protid, String VCFid) {
        this.cDNAid = cDNAid != null ? cDNAid.replace("\"", "") : null;
        this.Protid = protid != null ? protid.replace("\"", "") : null;
        this.VCFid = VCFid != null ? VCFid.replace("\"", "") : null;
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
        id ID = (id) o;
        // field comparison
        return ( this.cDNAid != null && this.cDNAid.equals(ID.cDNAid) )
                || ( this.Protid != null && this.Protid.equals(ID.Protid) )
                || ( this.VCFid != null && this.VCFid.equals(ID.VCFid));
    }

    /**
     * only 'works' for collapsed variants, i.e. those updated with each other's fields based on equals()
     * @return
     */
    @Override
    public int hashCode() {
//        if(this.cDNAid != null)
//        {
//            return this.cDNAid.hashCode();
//        }
        return Objects.hash(this.cDNAid, this.Protid, this.VCFid);
    }


    @Override
    public String toString() {
        return "dbid{" +
                "cDNAid='" + cDNAid + '\'' +
                ", Protid='" + Protid + '\'' +
                ", VCFid='" + VCFid + '\'' +
                '}';
    }
}
