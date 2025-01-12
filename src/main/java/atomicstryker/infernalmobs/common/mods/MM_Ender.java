package atomicstryker.infernalmobs.common.mods;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.config.Configuration;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

public class MM_Ender extends MobModifier {

    private long nextAbilityUse = 0L;

    private static long coolDown;
    private static float reflectMultiplier;
    private static float maxReflectDamage;

    public MM_Ender(@Nullable MobModifier next) {
        super("Ender", next);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        long time = mob.ticksExisted;
        if (time > nextAbilityUse && source.getEntity() != null
            && source.getEntity() != mob
            && teleportToEntity(mob, source.getEntity())
            && !InfernalMobsCore.instance()
                .isInfiniteLoop(mob, source.getEntity())) {
            nextAbilityUse = time + coolDown;
            source.getEntity()
                .attackEntityFrom(
                    DamageSource.causeMobDamage(mob),
                    Math.min(maxReflectDamage, damage * reflectMultiplier));

            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(EntityLivingBase mob, Entity par1Entity) {
        Vec3 vector = Vec3.createVectorHelper(
            mob.posX - par1Entity.posX,
            mob.boundingBox.minY + (double) (mob.height / 2.0F) - par1Entity.posY + (double) par1Entity.getEyeHeight(),
            mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.xCoord * telDist;
        double destY = mob.posY + (double) (mob.worldObj.rand.nextInt(16) - 8) - vector.yCoord * telDist;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.zCoord * telDist;
        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(EntityLivingBase mob, double destX, double destY, double destZ) {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        boolean success = false;
        mob.posX = destX;
        mob.posY = destY;
        mob.posZ = destZ;
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        Block blockID;

        if (mob.worldObj.blockExists(x, y, z)) {
            boolean hitGround = false;
            while (!hitGround && y < 96 && y > 0) {
                blockID = mob.worldObj.getBlock(x, y - 1, z);
                if (blockID.getMaterial()
                    .blocksMovement()) {
                    hitGround = true;
                } else {
                    --mob.posY;
                    --y;
                }
            }

            if (hitGround) {
                mob.setPosition(mob.posX, mob.posY, mob.posZ);

                if (mob.worldObj.getCollidingBoundingBoxes(mob, mob.boundingBox)
                    .isEmpty() && !mob.worldObj.isAnyLiquid(mob.boundingBox)
                    && !mob.worldObj.checkBlockCollision(mob.boundingBox)) {
                    success = true;
                }
            } else {
                return false;
            }
        }

        if (!success) {
            mob.setPosition(oldX, oldY, oldZ);
            return false;
        } else {
            short range = 128;
            for (int i = 0; i < range; ++i) {
                double var19 = (double) i / ((double) range - 1.0D);
                float var21 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
                float var22 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
                float var23 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
                double var24 = oldX + (mob.posX - oldX) * var19
                    + (mob.worldObj.rand.nextDouble() - 0.5D) * (double) mob.width * 2.0D;
                double var26 = oldY + (mob.posY - oldY) * var19 + mob.worldObj.rand.nextDouble() * (double) mob.height;
                double var28 = oldZ + (mob.posZ - oldZ) * var19
                    + (mob.worldObj.rand.nextDouble() - 0.5D) * (double) mob.width * 2.0D;
                mob.worldObj
                    .spawnParticle("portal", var24, var26, var28, (double) var21, (double) var22, (double) var23);
            }

            mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "mob.endermen.portal", 1.0F, 1.0F);
            mob.worldObj.playSoundAtEntity(mob, "mob.endermen.portal", 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = { "theEnderborn", "theTrickster" };

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = { "enderborn", "tricky" };

    public static class Loader extends ModifierLoader<MM_Ender> {

        public Loader() {
            super(MM_Ender.class);
        }

        @Override
        public MM_Ender make(@Nullable MobModifier next) {
            return new MM_Ender(next);
        }

        @Override
        public void loadConfig(Configuration config) {
            coolDown = config.get(getModifierClassName(), "coolDownMillis", 15000L, "Time between ability uses")
                .getInt(15000) / 50;
            reflectMultiplier = (float) config.get(
                getModifierClassName(),
                "enderReflectMultiplier",
                0.75D,
                "When a mob with Ender modifier gets hurt it teleports and reflects some of the damage originally dealt. This sets the multiplier for the reflected damage")
                .getDouble(0.75D);
            maxReflectDamage = (float) config.get(
                getModifierClassName(),
                "enderReflectMaxDamage",
                10.0D,
                "When a mob with Ender modifier gets hurt it teleports and reflects some of the damage originally dealt. This sets the maximum amount that can be inflicted (0, or less than zero for unlimited reflect damage)")
                .getDouble(10.0D);
        }
    }
}
