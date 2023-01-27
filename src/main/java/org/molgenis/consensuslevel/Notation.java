package org.molgenis.consensuslevel;
import org.apache.commons.lang3.StringUtils;


public class Notation {

    /**
     * AT ATT -> A AT
     * ATGTG ATG -> ATG A
     * ATGTG ATGTGTGTG -> A ATGTG
     * GATAT GAT -> GAT G
     *
     * Examples:
     * GATA GATAGATA -> G GATAG
     * TTCTT T -> TTCTT T (don't touch)
     */
    public static String[] backTrimRefAlt(String ref, String alt)
    {
        // GATA -> ATAG
        char[] refRev = StringUtils.reverse(ref).toCharArray();
        // GATAGATA -> ATAGATAG
        char[] altRev = StringUtils.reverse(alt).toCharArray();

        int nrToDelete = 0;
        //iterate over: A, T, A. Do not touch the last reference base (G).
        for(int i = 0; i < refRev.length-1; i++)
        {
            char refBase = refRev[i];
            char altBase = altRev[i];

            //altRev.length > i+1 prevents the last matching alt base from being/attempted deleted, e.g. TTCTT_T -> TTCT_
            //this may happen because we iterate over reference, which may be longer in case of a deletion
            if(refBase == altBase && altRev.length > i+1)
            {
                nrToDelete++;
            }
            else
            {
                break;
            }
        }
        String newRef = ref.substring(0, ref.length()-nrToDelete);
        String newAlt = alt.substring(0, alt.length()-nrToDelete);

        //result: GATA GATAGATA -> G GATAG
        return new String[]{newRef, newAlt};
    }

}
