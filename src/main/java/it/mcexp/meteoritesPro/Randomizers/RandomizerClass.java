package it.mcexp.meteoritesPro.Randomizers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import it.mcexp.meteoritesPro.ExceptionHandling.ConfigException;
import it.mcexp.meteoritesPro.Guardians.TreasureGuardian;
import it.mcexp.meteoritesPro.Meteorites.Meteorite;
import it.mcexp.meteoritesPro.MeteoritesPro;
import it.mcexp.meteoritesPro.Particles.MeteoriteParticle;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class RandomizerClass {
    private List<Chance> chances;
    private int sum;
    private Random random;

    public RandomizerClass(long seed) {
        this.random = new Random(seed);
        this.chances = new ArrayList<Chance>();
        this.sum = 0;
    }

    public boolean addMaterials(ConfigurationSection configurationSection, MeteoritesPro plugin) {
        try {
            for (String materialString : Objects.requireNonNull(configurationSection).getKeys(false)) {
                Material material = Material.getMaterial((String)materialString);
                if (material == null) {
                    throw new ConfigException("Invalid material type: '" + materialString + "'");
                }
                this.addMaterialChance(material, configurationSection.getInt(materialString));
            }
            return true;
        }
        catch (ConfigException e) {
            ConfigException.handleConfigException(plugin, e);
            return false;
        }
    }

    public void addGuardians(MeteoritesPro plugin, ConfigurationSection configurationSection) throws ConfigException {
        for (String guardianString : Objects.requireNonNull(configurationSection).getKeys(false)) {
            if (!configurationSection.getBoolean(guardianString + ".enabled")) continue;
            TreasureGuardian guardian = new TreasureGuardian(plugin, guardianString);
            this.addGuardianChance(guardian, configurationSection.getInt(guardianString + ".chance"));
            guardian.setGuardianMaterials();
            guardian.setGuardianAttributeValues();
            guardian.setGuardianSpawnSound();
        }
        if (this.chances.isEmpty()) {
            throw new ConfigException("All guardians are individually disabled but 'enable-treasure-guardian' was set to true, please set 'enable-treasure-guardian' to false to avoid errors!");
        }
    }

    public void addParticles(MeteoritesPro plugin, ConfigurationSection configurationSection) throws ConfigException {
        for (String particleString : configurationSection.getKeys(false)) {
            if (!configurationSection.getBoolean(particleString + ".enabled")) continue;
            MeteoriteParticle particle = new MeteoriteParticle(plugin, particleString);
            this.addParticleChance(particle, configurationSection.getInt(particleString + ".chance"));
        }
        if (this.chances.isEmpty()) {
            throw new ConfigException("All particles are individually disabled but 'enable-meteorite-particles' was set to true, please set 'enable-meteorite-particles' to false to avoid errors!");
        }
    }

    public Material getRandomMaterial() {
        int index = this.random.nextInt(this.sum);
        for (Chance chance : this.chances) {
            if (chance.getLowerLimit() > index || chance.getUpperLimit() <= index) continue;
            return chance.getMaterial();
        }
        return null;
    }

    public TreasureGuardian getRandomGuardian() {
        int index = this.random.nextInt(this.sum);
        for (Chance chance : this.chances) {
            if (chance.getLowerLimit() > index || chance.getUpperLimit() <= index) continue;
            return chance.getGuardian();
        }
        return null;
    }

    public MeteoriteParticle getRandomParticle() {
        int index = this.random.nextInt(this.sum);
        for (Chance chance : this.chances) {
            if (chance.getLowerLimit() > index || chance.getUpperLimit() <= index) continue;
            return chance.getParticle();
        }
        return null;
    }

    public Meteorite getRandomMeteorite() {
        int index = this.random.nextInt(this.sum);
        for (Chance chance : this.chances) {
            if (chance.getLowerLimit() > index || chance.getUpperLimit() <= index) continue;
            return chance.getMeteorite();
        }
        return null;
    }

    public void addMaterialChance(Material material, int chance) {
        if (!this.chances.contains(material)) {
            this.chances.add(new Chance(material, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }

    public void addGuardianChance(TreasureGuardian guardian, int chance) {
        if (!this.chances.contains(guardian)) {
            this.chances.add(new Chance(guardian, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }

    public void addParticleChance(MeteoriteParticle particle, int chance) {
        if (!this.chances.contains(particle)) {
            this.chances.add(new Chance(particle, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }

    public void addMeteoriteChance(Meteorite meteorite, int chance) {
        if (!this.chances.contains(meteorite)) {
            this.chances.add(new Chance(meteorite, this.sum, this.sum + chance));
            this.sum += chance;
        }
    }

    private class Chance {
        private int upperLimit;
        private int lowerLimit;
        private Material material;
        private TreasureGuardian guardian;
        private MeteoriteParticle particle;
        private Meteorite meteorite;

        public Chance(Material material, int lowerLimit, int upperLimit) {
            this.material = material;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }

        public Chance(TreasureGuardian guardian, int lowerLimit, int upperLimit) {
            this.guardian = guardian;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }

        public Chance(MeteoriteParticle particle, int lowerLimit, int upperLimit) {
            this.particle = particle;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }

        public Chance(Meteorite meteorite, int lowerLimit, int upperLimit) {
            this.meteorite = meteorite;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }

        public int getUpperLimit() {
            return this.upperLimit;
        }

        public int getLowerLimit() {
            return this.lowerLimit;
        }

        public Material getMaterial() {
            return this.material;
        }

        public TreasureGuardian getGuardian() {
            return this.guardian;
        }

        public MeteoriteParticle getParticle() {
            return this.particle;
        }

        public Meteorite getMeteorite() {
            return this.meteorite;
        }

        public String toString() {
            if (this.material != null) {
                return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.material.toString();
            }
            if (this.guardian != null) {
                return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.guardian.toString();
            }
            if (this.particle != null) {
                return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.particle.toString();
            }
            return "[" + this.lowerLimit + "|" + this.upperLimit + "]: " + this.meteorite.toString();
        }
    }
}
