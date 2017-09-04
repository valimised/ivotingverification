package ee.vvk.ivotingverification.model;

import ee.vvk.ivotingverification.util.C;

public class Candidate {
    public final static Candidate NO_CHOICE = new Candidate("", "", C.noChoice);

    public final String number;
    public final String party;
    public final String name;

    public Candidate(String packed) {
        String[] split = packed.split(Character.toString((char) 0x1F));
        if (split.length != 3) {
            throw new IllegalArgumentException("Bad candidate format: " + packed);
        }
        this.number = split[0];
        this.party = split[1];
        this.name = split[2];
    }

    public Candidate(String number, String party, String name) {
        this.number = number;
        this.party = party;
        this.name = name;
    }
}
