package atomicstryker.infernalmobs.common.mods.api;

import javax.annotation.Nullable;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraftforge.common.config.Configuration;

public abstract class ModifierLoader<T extends MobModifier> {
	
	private final String modifierClassName;
	
	public ModifierLoader(Class<T> modifierClass) {
		this.modifierClassName = modifierClass.getSimpleName();
	}
	
	public abstract T make(@Nullable MobModifier next);
	
	public void loadConfig(Configuration config) {}
	
	public String getModifierClassName() {
		return modifierClassName;
	}

}
