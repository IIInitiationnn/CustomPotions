package xyz.iiinitiationnn.custompotions;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Serializable;

/**
 * Interact with this class using the PotionEffectType, though it is internally stored as a String.
 */
public class PotionEffectSerializable implements Serializable {
    private String type;
    private int duration;
    private int amplifier;

    public PotionEffectSerializable(PotionEffectType type, int duration, int amplifier) {
        this.type = type.getName();
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public PotionEffectSerializable(PotionEffect potionEffect) {
        this.type = potionEffect.getType().getName();
        this.duration = potionEffect.getDuration();
        this.amplifier = potionEffect.getAmplifier();
    }

    /**
     * Returns the type of this potion effect as a PotionEffectType.
     * @return
     */
    public PotionEffectType getType() {
        return PotionEffectType.getByName(this.type);
    }

    public void setType(PotionEffectType type) {
        this.type = type.getName();
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public PotionEffect toPotionEffect() {
        return new PotionEffect(this.getType(), this.duration, this.amplifier);
    }

}
