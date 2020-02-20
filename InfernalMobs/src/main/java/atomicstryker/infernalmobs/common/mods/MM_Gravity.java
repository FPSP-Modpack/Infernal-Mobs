package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Gravity extends MobModifier
{
    private final boolean offensive;
    private long nextAbilityUse = 0L;
    private static long coolDown;

    public MM_Gravity(EntityLivingBase mob)
    {
        this.modName = "Gravity";
        offensive = mob.getClass().isAssignableFrom(IMob.class);
    }
    
    public MM_Gravity(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Gravity";
        this.nextMod = prevMod;
        offensive = mob.getClass().isAssignableFrom(IMob.class);
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (offensive
        && getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob, getMobTarget());
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (!offensive
        && source.getEntity() != null
        && source.getEntity() instanceof EntityLivingBase)
        {
            tryAbility(mob, (EntityLivingBase) source.getEntity());
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            
            EntityLivingBase source = offensive ? mob : target;
            EntityLivingBase destination = offensive ? target : mob;
            double diffX = destination.posX - source.posX;
            double diffZ;
            for (diffZ = destination.posZ - source.posZ; diffX * diffX + diffZ * diffZ < 1.0E-4D; diffZ = (Math.random() - Math.random()) * 0.01D)
            {
                diffX = (Math.random() - Math.random()) * 0.01D;
            }
            
            mob.worldObj.playSoundAtEntity(mob, "mob.irongolem.throw", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            
            if (mob.worldObj.isRemote || !(target instanceof EntityPlayerMP))
            {
                knockBack(target, diffX, diffZ);
            }
            else
            {
                InfernalMobsCore.instance().sendKnockBackPacket((EntityPlayerMP) target, (float)diffX, (float)diffZ);
            }
        }
    }
    
    public static void knockBack(EntityLivingBase target, double x, double z)
    {
        target.isAirBorne = true;
        float normalizedPower = MathHelper.sqrt_double(x * x + z * z);
        float knockPower = 0.8F;
        target.motionX /= 2.0D;
        target.motionY /= 2.0D;
        target.motionZ /= 2.0D;
        target.motionX -= x / (double)normalizedPower * (double)knockPower;
        target.motionY += (double)knockPower;
        target.motionZ -= z / (double)normalizedPower * (double)knockPower;

        if (target.motionY > 0.4000000059604645D)
        {
            target.motionY = 0.4000000059604645D;
        }
    }

    public static void loadConfig(Configuration config)
    {
        coolDown = config.get(MM_Gravity.class.getSimpleName(), "coolDownMillis", 5000L, "Time between ability uses").getInt(5000);
    }

    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Webber.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofRepulsion", "theFlipper" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "repulsing", "sproing" };
    
}
