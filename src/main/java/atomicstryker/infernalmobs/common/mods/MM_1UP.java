package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_1UP extends MobModifier
{
    private static double healAmount;

    private boolean healed;
    
    public MM_1UP(EntityLivingBase mob)
    {
        this.modName = "1UP";
        healed = false;
    }
    
    public MM_1UP(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "1UP";
        this.nextMod = prevMod;
        healed = false;
    }
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (!healed && mob.getHealth() < (getActualMaxHealth(mob)*0.25))
        {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, getActualMaxHealth(mob) * (float) healAmount);
            mob.worldObj.playSoundAtEntity(mob, "random.levelup", 1.0F, 1.0F);
            healed = true;
        }
        return super.onUpdate(mob);
    }
    
    public static void loadConfig(Configuration config)
    {
        healAmount = config.get(MM_1UP.class.getSimpleName(), "healAmountMultiplier", 1D, "Multiplies the mob maximum health when healing back up").getDouble(1D);
    }

    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class<?>[] disallowed = { EntityCreeper.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofRecurrence", "theUndying", "oftwinLives" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "recurring", "undying", "twinlived" };
}
