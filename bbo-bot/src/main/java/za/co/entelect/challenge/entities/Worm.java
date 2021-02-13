package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;
import java.lang.Comparable;

public class Worm implements Comparable<Worm>{
    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("position")
    public Position position;

    @SerializedName("diggingRange")
    public int diggingRange;

    @SerializedName("movementRange")
    public int movementRange;

    @SerializedName("profession")
    public String profession;

    @Override
    public int compareTo(Worm w) {
        if (w.profession.equals("Technologist") && !this.profession.equals("Technologist")) {
            // higher priority
            return -1;
        } else if (w.profession.equals("Agent") && this.profession.equals("Commando")) {
            // higher priority
            return -1;
        } else if (w.profession.equals("Agent") && this.profession.equals("Technologist")) {
            // lower priority
            return 1;
        } else if (w.profession.equals("Commando") && !this.profession.equals("Commando")) {
            // lower priority
            return 1;
        }

        // same profession, default
        return 1;
    }

}
