package chbachman.armour.upgrade.upgradeList;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import chbachman.api.IModularItem;
import chbachman.api.Upgrade;
import chbachman.armour.util.EnergyUtil;
import chbachman.armour.util.UpgradeUtil;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class UpgradeJumpBoost extends Upgrade {

	public UpgradeJumpBoost() {
		super("jumpBoost");
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	@SubscribeEvent
	public void onPlayerJump(LivingJumpEvent event) {
		if(event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			
			List<ItemStack> list = UpgradeUtil.getPlayerUpgrades(player, this);
			
			for(ItemStack stack : list){
				if(stack != null && EnergyUtil.getEnergyStored(stack) > 1000){
					IModularItem modularItem = (IModularItem) stack.getItem();
					int level = modularItem.getLevel(stack);
					
					(modularItem).extractEnergy(stack, 1000 * level, false);
					player.motionY += .3 * level;
					
				}
			}
		}
	}

}
